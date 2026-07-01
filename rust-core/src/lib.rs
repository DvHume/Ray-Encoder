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

fn derive_key(password: &[u8], salt: &SaltString) -> Result<[u8; 32], String> {
    let params = Params::new(65536, 3, 1, Some(32))
        .map_err(|e| format!("Parameter error Argon2: {}", e))?;
    
    let argon2 = Argon2::new(argon2::Algorithm::Argon2id, Version::V0x13, params);

    let mut key = [0u8; 32];
    argon2
        .hash_password_into(password, salt.as_str().as_bytes(), &mut key)
        .map_err(|e| format!("Key generation error: {}", e))?;

    Ok(key)
}

pub fn encrypt(secret_data: &[u8], master_password: &[u8]) -> Result<String, String> {
    let salt = SaltString::generate(&mut OsRng);
    let key_bytes = derive_key(master_password, &salt)?;

    let cipher = Aes256Gcm::new_from_slice(&key_bytes)
        .map_err(|e| format!("AES Initialization error: {}", e))?;

    let mut nonce_bytes = [0u8; 12];
    OsRng.fill_bytes(&mut nonce_bytes);
    let nonce = Nonce::from_slice(&nonce_bytes);

    let ciphertext = cipher
        .encrypt(nonce, secret_data)
        .map_err(|e| format!("Encryption error: {}", e))?;

    let salt_bytes = salt.as_str().as_bytes();
    let mut packed_data = Vec::new();
    packed_data.push(salt_bytes.len() as u8);
    packed_data.extend_from_slice(salt_bytes);
    packed_data.extend_from_slice(&nonce_bytes);
    packed_data.extend_from_slice(&ciphertext);

    Ok(STANDARD.encode(packed_data))
}

pub fn decrypt(base64_str: &str, master_password: &[u8]) -> Result<Vec<u8>, String> {
    let packed_data = STANDARD
        .decode(base64_str)
        .map_err(|e| format!("Base64 Decoding error: {}", e))?;

    if packed_data.is_empty() {
        return Err("The data for decryption is empty".to_string());
    }

    let salt_len = packed_data[0] as usize;
    if packed_data.len() < 1 + salt_len + 12 {
        return Err("Incorrect encrypted data format".to_string());
    }

    let salt_end = 1 + salt_len;
    let salt_str = std::str::from_utf8(&packed_data[1..salt_end])
        .map_err(|e| format!("Salt reading error: {}", e))?;

    let salt = SaltString::new(salt_str).map_err(|e| format!("Invalid salt format: {}", e))?;

    let nonce_end = salt_end + 12;
    let nonce_bytes = &packed_data[salt_end..nonce_end];
    let nonce = Nonce::from_slice(nonce_bytes);

    let ciphertext = &packed_data[nonce_end..];
    let key_bytes = derive_key(master_password, &salt)?;

    let cipher = Aes256Gcm::new_from_slice(&key_bytes)
        .map_err(|e| format!("AES Initialization error: {}", e))?;

    let decrypted_data = cipher
        .decrypt(nonce, ciphertext)
        .map_err(|_| "The password is incorrect or data has been corrupted.".to_string())?;

    Ok(decrypted_data)
}

#[unsafe(no_mangle)]
pub extern "C" fn rust_encrypt(secret: *const c_char, password: *const c_char) -> *mut c_char {
    if secret.is_null() || password.is_null() { return std::ptr::null_mut(); }

    let c_secret = unsafe { CStr::from_ptr(secret) }.to_bytes();
    let c_password = unsafe { CStr::from_ptr(password) }.to_bytes();

    match encrypt(c_secret, c_password) {
        Ok(base64_str) => CString::new(base64_str).unwrap().into_raw(),
        Err(_) => std::ptr::null_mut(),
    }
}

#[unsafe(no_mangle)]
pub extern "C" fn rust_decrypt(base64_str: *const c_char, password: *const c_char) -> *mut c_char {
    if base64_str.is_null() || password.is_null() { return std::ptr::null_mut(); }

    let c_base64 = unsafe {
        CStr::from_ptr(base64_str)
    }.to_str().unwrap_or("");

    let c_password = unsafe { CStr::from_ptr(password) }.to_bytes();

    match decrypt(c_base64, c_password) {
        Ok(decrypted_bytes) => {
            if let Ok(decrypted_str) = String::from_utf8(decrypted_bytes) {
                CString::new(decrypted_str).unwrap().into_raw()
            } else {
                std::ptr::null_mut()
            }
        }
        Err(_) => std::ptr::null_mut(),
    }
}

// Freeing up memory
#[unsafe(no_mangle)]
pub extern "C" fn free_string_pls(ptr: *mut c_char) {
    if !ptr.is_null() {
        unsafe { CString::from_raw(ptr); }
    }
}