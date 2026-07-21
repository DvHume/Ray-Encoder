package org.quark.ray.core;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import org.quark.ray.I18n;

/*
 * Copyright (c) 2026 DvHume
 *
 * Licensed under the MIT License.
 * See the LICENSE file in the project root for license information
 */
public class CryptCore {
    private CryptCore() {
    }

    private interface Ray extends Library {
        Ray INSTANCE = Native.load("quark_password_encryptor", Ray.class);

        Pointer rust_encrypt(String secret, String password);

        Pointer rust_decrypt(String b64, String password);

        void rust_string_free(Pointer ptr);
    }

    /**
     * Encrypts plaintext using the specified password
     * @param secret Encryption source text
     * @param password Access password
     * @return A {@link Result} object with a Base64-encoded string or error information
     */
    public static Result encrypt(String secret, String password) {
        if (secret == null || secret.isEmpty()) {
            return Result.fail(Status.INVALID_DATA, I18n.get("error.secret.empty"));
        }
        if (password == null || password.isEmpty()) {
            return Result.fail(Status.INVALID_PASSWORD, I18n.get("error.password.empty"));
        }

        try {
            Pointer ptr = Ray.INSTANCE.rust_encrypt(secret, password);
            if (ptr == null) {
                return Result.fail(Status.LIB_ERROR, I18n.get("error.lib"));
            }

            try {
                String encrypted = ptr.getString(0);
                return Result.ok(encrypted, I18n.get("success.encrypt"));
            } finally {
                Ray.INSTANCE.rust_string_free(ptr);
            }
        } catch (Exception e) {
            return Result.fail(Status.INTERNAL_ERROR, I18n.get("error.internal"));
        }
    }

    public static Result decrypt(String b64, String pass) {
        if (b64 == null || b64.trim().isEmpty()) {
            return Result.fail(Status.INVALID_DATA, I18n.get("error.data.empty"));
        }

        if (pass == null || pass.isEmpty()) {
            return Result.fail(Status.INVALID_PASSWORD, I18n.get("error.password.empty"));
        }

        try {
            Pointer ptr = Ray.INSTANCE.rust_decrypt(b64.trim(), pass);

            if (ptr == null) {
                return Result.fail(Status.INVALID_PASSWORD, I18n.get("error.invalid.password"));
            }
            try {
                String decrypted = ptr.getString(0);
                return Result.ok(decrypted, I18n.get("success.decrypt"));
            } finally {
                Ray.INSTANCE.rust_string_free(ptr);
            }
        } catch (Exception e) {
            return Result.fail(Status.INTERNAL_ERROR, I18n.get("error.internal", e.getMessage()));
        }
    }
}