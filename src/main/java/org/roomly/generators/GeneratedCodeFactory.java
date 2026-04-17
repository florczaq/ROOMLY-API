package org.roomly.generators;


import org.roomly.enums.CodeCharacters;

import java.security.SecureRandom;

public final class GeneratedCodeFactory {
    public static String generate (int size, CodeCharacters set) {
        var allowedChars = set.getCharacters();
        SecureRandom random = new SecureRandom();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < size; i++) {
            int idx = random.nextInt(allowedChars.length());
            builder.append(allowedChars.charAt(idx));
        }
        return builder.toString();
    }
}
