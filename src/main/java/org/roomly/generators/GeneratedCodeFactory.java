package org.roomly.generators;

import org.roomly.enums.CodeCharacters;

import java.security.SecureRandom;

public final class GeneratedCodeFactory {
    
    private static final SecureRandom RANDOM = new SecureRandom();
    
    private GeneratedCodeFactory() {
        /* This utility class should not be instantiated */
    }

    public static String generate(int size, CodeCharacters set) {
        var allowedChars = set.getCharacters();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < size; i++) {
            int idx = RANDOM.nextInt(allowedChars.length());
            builder.append(allowedChars.charAt(idx));
        }
        return builder.toString();
    }
}