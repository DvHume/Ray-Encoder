package org.quark.ray;

/*
 * Copyright (c) 2026 DvHume
 *
 * Licensed under the MIT License.
 * See the LICENSE file in the project root for license information
 */

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;

import java.util.Scanner;

public class Main {

    public interface Ray extends Library {
        Ray INSTANCE = Native.load("quark_password_encryptor", Ray.class);

        Pointer rust_encrypt(String secret, String password);
        Pointer rust_decrypt(String base64Str, String password);
        void rust_string_free(Pointer ptr);
    }

    public static String RESET = "\u001B[0m";
    public static String CYAN = "\u001B[36m";
    public static String GREEN = "\u001B[32m";
    public static String RED = "\u001B[31m";
    public static String PURPLE = "\u001B[35m";
    public static String WHITE_BOLD = "\u001B[1;37m";

    public static void main(String[] args) {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            boolean ansiSupported = false;
            try {
                ansiSupported = Kernel32.INSTANCE.SetConsoleMode(
                        Kernel32.INSTANCE.GetStdHandle(-11),
                        0x0001 | 0x0004
                );
            } catch (Throwable ignored) {
            }

            if (!ansiSupported) {
                RESET = "";
                CYAN = "";
                GREEN = "";
                RED = "";
                PURPLE = "";
                WHITE_BOLD = "";
            }
        }

        try (Scanner sc = new Scanner(System.in)) {

            printHeader();

            boolean running = true;
            while (running) {
                System.out.println(WHITE_BOLD + " [1]" + RESET + " Encrypt data");
                System.out.println(WHITE_BOLD + " [2]" + RESET + " Decrypt data");
                System.out.println(WHITE_BOLD + " [3]" + RESET + " Exit");
                System.out.print(CYAN + "\ncrypto-vault@user:~# " + RESET);

                String choice = sc.nextLine().trim();

                switch (choice) {
                    case "1" -> handleEncryption(sc);
                    case "2" -> handleDecryption(sc);
                    case "3" -> {
                        System.out.println(PURPLE + "\n[!] The session has ended." + RESET);
                        running = false;
                    }
                    default -> System.out.println(RED + "[!] Unknown command. Try again.\n" + RESET);
                }
            }
        }
    }

    private static void handleEncryption(Scanner scanner) {
        System.out.print(WHITE_BOLD + "Enter... " + RESET);
        String secret = scanner.nextLine();
        System.out.println(WHITE_BOLD + "Create a Master Password" + RESET);
        String encPass = scanner.nextLine();

        System.out.println(CYAN + "[*] ..." + RESET);

        Pointer encPtr = Ray.INSTANCE.rust_encrypt(secret, encPass);
        if (encPtr != null) {
            String resultBase64 = encPtr.getString(0);
            Ray.INSTANCE.rust_string_free(encPtr);

            System.out.println(GREEN + "\n[✔] SUCCESSFULLY!" + RESET);
            System.out.print("LINE TO SAVE: ");
            System.out.println(PURPLE + resultBase64 + RESET + "\n");
        } else {
            System.out.println(RED + "\n[⨯] Kernel side encryption error\n" + RESET);
        }
    }

    private static void handleDecryption(Scanner scanner) {
        System.out.println(WHITE_BOLD + "Paste the Base64 encrypted string: " + RESET);
        String base64Str = scanner.nextLine();
        System.out.println(WHITE_BOLD + "Enter your Master Password: " + RESET);
        String decPass = scanner.nextLine();

        System.out.println(CYAN + "[*] Decryption and integrity checking..." + RESET);

        Pointer decPtr = Ray.INSTANCE.rust_decrypt(base64Str, decPass);
        if (decPtr != null) {
            String decryptedText = decPtr.getString(0);
            Ray.INSTANCE.rust_string_free(decPtr);

            System.out.println(GREEN + "\n[✔] SUCCESSFULLY!" + RESET);
            System.out.println("Your secret: " + WHITE_BOLD + decryptedText + RESET + "\n");
        } else {
            System.out.println("\n[⨯] ACCESS DENIED: Incorrect password or corrupted data\n" + RESET);
        }
    }

    private static void printHeader() {
        System.out.println(CYAN + "================================================" + RESET);
        System.out.println(WHITE_BOLD + "   Quark Ray Encryptor     " + RESET);
        System.out.println(GREEN + "   AES-256-GCM CRYPTO VAULT v1.0.0"     + RESET);
        System.out.println(CYAN + "================================================" + RESET);
        System.out.println(" Initializing the cryptographic core... " + GREEN + "[OK]" + RESET + "\n");
    }
}