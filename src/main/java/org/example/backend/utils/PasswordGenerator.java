package org.example.backend.utils;

import java.security.SecureRandom;

public class PasswordGenerator {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqurstuvwxyz1234567890!@#$%^&*()-=_+";
    private static final Integer PASSWORD_LENGTH = 13;
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generatePassword() {
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            password.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return password.toString();
    }
}
