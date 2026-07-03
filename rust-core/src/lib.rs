/*
 * Copyright (c) 2026 DvHume
 *
 * Licensed under the MIT License.
 * See the LICENSE file in the project root for license information
 */

use std::{ffi::{CStr, CString, c_char}, ptr};

use aes_gcm::{
    aead::{Aead, KeyInit},
    Aes256Gcm, Nonce,
};
use argon2::{
    password_hash::SaltString,
    Argon2, Params, Version
};
use base64::{engine::general_purpose::STANDARD, Engine as _};
use rand::{rngs::OsRng, RngCore};

/// Uses algorithm Argon2id, which protects against password guessing (brute force)
/// both on video cards (GPUs) and application-specific integrated circuits (ASICs)
/// To do this, the function strictly allocates a significant amount of RAM for calculations.
/// 
/// params:
/// - m_cost: 65536 (allocates exactly 64MB of RAM for one iteration).
/// - t-cost: 3 (performs 3 passes through memory to complicate calculations).
/// - p_cost: 1 (uses 1 thread for determinism on weak devices).
fn derive_key(password: &[u8], salt: &SaltString) -> Result<[u8; 32], String> {
    // We configure Argon2id parameters to meet the requirements of cryptographic strength
    let params = Params::new(65536, 3, 1, Some(32))
        .map_err(|e| format!("Parameter error Argon2: {}", e))?;
    
    let argon2 = Argon2::new(argon2::Algorithm::Argon2id, Version::V0x13, params);

    // Buffer for the 256-bit key required by the AES-256 algorithm
    let mut key = [0u8; 32];

    // We start calculating the hash by adding salt to the password
    // What is “salt” anyway?
    // Salt is a random string of data added to the password before hashing it. As Google says...
    // But simply put, salt makes each hash unique, preventing rainbow table attacks.
    // So, if two users have the same password, their hashes will still be different.
    argon2
        .hash_password_into(password, salt.as_str().as_bytes(), &mut key)
        .map_err(|e| format!("Key generation error: {}", e))?;

    Ok(key)
}

/// The main function of data encryption.
/// Implements the AES-256-GCM encryption scheme. The algorithm is authenticated (AEAD),
/// which means simultaneously ensuring data confidentiality and monitoring its integrity.
/// Any attempt to change the encrypted bit will result in decryption failure.
/// 
/// Returns a Base64 packed string containing salt, nonce and ciphertext.
pub fn encrypt(secret_data: &[u8], master_password: &[u8]) -> Result<String, String> {
    // Generating a random cryptographic salt for Argon2id using the OsRng generator
    let salt = SaltString::generate(&mut OsRng);
    let key_bytes = derive_key(master_password, &salt)?;

    // We initialize the AES-256 cipher with the received 32-byte key
    let cipher = Aes256Gcm::new_from_slice(&key_bytes)
        .map_err(|e| format!("AES Initialization error: {}", e))?;

    // We generate the required 12-byte one-time initialization vector.
    // Reusing the key-nonce pair in GCM mode completely destroys security.
    let mut nonce_bytes = [0u8; 12];
    OsRng.fill_bytes(&mut nonce_bytes);
    let nonce = Nonce::from_slice(&nonce_bytes);

    // Encrypt the data
    let ciphertext = cipher
        .encrypt(nonce, secret_data)
        .map_err(|e| format!("Encryption error: {}", e))?;

    // We form a binary package for transmission to the Java side.
    // Packaging diagram (packed_data):
    // [1 byte: salt length] + [N byte: the salt itself] + [12 bytes: nonce] + [remaining bytes: ciphertext]
    let salt_bytes = salt.as_str().as_bytes();
    let mut packed_data = Vec::new();
    packed_data.push(salt_bytes.len() as u8);
    packed_data.extend_from_slice(salt_bytes);
    packed_data.extend_from_slice(&nonce_bytes);
    packed_data.extend_from_slice(&ciphertext);

    // We encode the finished byte array into a safe ASCII string in Base64 format
    Ok(STANDARD.encode(packed_data))
}

/// Data decryption function
/// 
/// Accepts a Base64 packed string and Master Password. Unpacks metadata
/// (salt and nonce), restores the encryption key and checks the validity of the data
pub fn decrypt(base64_str: &str, master_password: &[u8]) -> Result<Vec<u8>, String> {
    // Decode the incoming Base64 string back into raw bytes of the packet
    let packed_data = STANDARD
        .decode(base64_str)
        .map_err(|e| format!("Base64 Decoding error: {}", e))?;

    if packed_data.is_empty() {
        return Err("The data for decryption is empty".to_string());
    }

    // Extracting the length of the salt from the very first service byte of the packet
    let salt_len = packed_data[0] as usize;
    // Checking the minimum acceptable packet size to protect against array overruns
    if packed_data.len() < 1 + salt_len + 12 {
        return Err("Incorrect encrypted data format".to_string());
    }

    // Cutting out the salt and restoring its string representation
    let salt_end = 1 + salt_len;
    let salt_str = std::str::from_utf8(&packed_data[1..salt_end])
        .map_err(|e| format!("Salt reading error: {}", e))?;

    let salt = SaltString::from_b64(salt_str).map_err(|e| format!("Invalid salt format: {}", e))?;

    // Cut 12 bytes of the initialization vector (nonce)
    let nonce_end = salt_end + 12;
    let nonce_bytes = &packed_data[salt_end..nonce_end];
    let nonce = Nonce::from_slice(nonce_bytes);

    // All remaining bytes of the packet are ciphertext
    let ciphertext = &packed_data[nonce_end..];
    // We generate exactly the same key using the salt saved during encryption
    let key_bytes = derive_key(master_password, &salt)?;

    let cipher = Aes256Gcm::new_from_slice(&key_bytes)
        .map_err(|e| format!("AES Initialization error: {}", e))?;

    // Decrypt the data. If the password is incorrect or the data has been changed, the GCM authentication tag
    // will not match, and the method will return an error. We specifically return a general error, without details
    // so as not to give any hints to a potential attacker.
    let decrypted_data = cipher
        .decrypt(nonce, ciphertext)
        .map_err(|_| "The password is incorrect or data has been corrupted.".to_string())?;

    Ok(decrypted_data)
}


// ===================================================================
// FFI (Foreign Function Interface) for interfacing with Java (JNA)
// ===================================================================

/// C-compatible wrapper for data encryption
/// 
/// Accepts pointers to strings from the external environment and returns a pointer
/// to the Base64 string allocated in the Rust heap

#[unsafe(no_mangle)]
pub extern "C" fn rust_encrypt(secret: *const c_char, password: *const c_char) -> *mut c_char {
    // Protection against null pointers from the external environment
    if secret.is_null() || password.is_null() { return ptr::null_mut(); }

    // Safely convert raw pointers to regular Rust byte slices.
    // The reference is only valid inside this function, since we do not own this memory.
    let c_secret = unsafe { CStr::from_ptr(secret) }.to_bytes();
    let c_password = unsafe { CStr::from_ptr(password) }.to_bytes();

    match encrypt(c_secret, c_password) {
        // Convert Rust string to CString
        // and call into_raw(), which transfers ownership of the memory to the caller.
        // Rust no longer controls this line. The "rust_string_free" method must clear it
        Ok(base64_str) => CString::new(base64_str).unwrap().into_raw(),
        Err(_) => std::ptr::null_mut(),
    }
}

/// C-compatible wrapper for data decryption.
/// 
/// Accepts pointers to an incoming Base64 string and password. Returns a pointer
/// to a valid UTF-8 string with the original secret if successful.
/// 
/// The function uses unsafe to work with raw C pointers.
#[unsafe(no_mangle)]
pub extern "C" fn rust_decrypt(base64_str: *const c_char, password: *const c_char) -> *mut c_char {
    if base64_str.is_null() || password.is_null() { return ptr::null_mut(); }

    // Recover string types from pointers. If Base64 is corrupted at the text level,
    // substitute an empty string for a safe fall inside decrypt()
    let c_base64 = unsafe {
        CStr::from_ptr(base64_str)
    }.to_str().unwrap_or("");

    let c_password = unsafe { CStr::from_ptr(password) }.to_bytes();

    match decrypt(c_base64, c_password) {
        Ok(decrypted_bytes) => {
            // Check that the decrypted bytes are valid UTF-8 text
            if let Ok(decrypted_str) = String::from_utf8(decrypted_bytes) {
                // Passing a raw pointer to the heap in Java. Freeing up memory also through "rust_string_free"
                CString::new(decrypted_str).unwrap().into_raw()
            } else {
                std::ptr::null_mut()
            }
        }
        Err(_) => std::ptr::null_mut(),
    }
}

/// Freeing memory allocated for C-strings
///
/// Critical feature for preventing memory leaks.
/// Since Java only receives a raw `*mut c_char` pointer, the java garbage collector
/// doesn't know how to clear memory inside the heap.
///
#[unsafe(no_mangle)]
pub extern "C" fn rust_string_free(ptr: *mut c_char) {
    if !ptr.is_null() {
        //The from_raw method takes the pointer back under the control of the Rust allocator
        unsafe {let _ = CString::from_raw(ptr); }
    }
}