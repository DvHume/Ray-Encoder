package org.quark.ray.core;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

/*
 * Copyright (c) 2026 DvHume
 *
 * Licensed under the MIT License.
 * See the LICENSE file in the project root for license information
 */
public class CryptCore {
    private CryptCore() {}

    private interface Ray extends Library {
        Ray INSTANCE = Native.load("quark_password_encryptor", Ray.class);

        Pointer rust_encrypt(String secret, String password);
        Pointer rust_decrypt(String b64, String password);
        void rust_string_free(Pointer ptr);
    }

    public static Result encrypt(String secret, String password) {
        Pointer ptr = Ray.INSTANCE.rust_encrypt(secret, password);
        if (ptr == null) {
            return new Result(
                    Status.LIB_ERROR, null, "The native library returned a null pointer."
            );
        }

        try {
            String encrypted = ptr.getString(0);
            return new Result(Status.SUCCESS, encrypted, "Encryption completed successfully");
        } finally {
            Ray.INSTANCE.rust_string_free(ptr);
        }
    } catch (Exception e) {
        return new Result(Status.INTERNAL_ERROR, null, e.getMessage());
    }

    public static String decrypt(String b64, String pass) {
        Pointer ptr = Ray.INSTANCE.rust_decrypt(b64, pass);

        if (ptr == null)
            return null;

        try {
            return ptr.getString(0);
        } finally {
            Ray.INSTANCE.rust_string_free(ptr);
        }
    }
}
