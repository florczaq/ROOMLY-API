package org.roomly.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CodeCharacters {
    UPPERCASE_LETTERS("ABCDEFGHIJKLMNOPQRSTUVWXYZ"),
    LOWERCASE_LETTERS("abcdefghijklmnopqrstuvwxyz"),
    DIGITS("0123456789"),
    SPECIAL_CHARACTERS("!@#$%^&*()-_=+[]{}|;:,.<>?"),
    LOWERCASE_LETTERS_AND_DIGITS(LOWERCASE_LETTERS.characters + DIGITS.characters),
    UPPERCASE_LETTERS_AND_DIGITS(UPPERCASE_LETTERS.characters + DIGITS.characters);
    
    private final String characters;
}
