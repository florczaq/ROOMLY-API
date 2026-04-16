package org.roomly.enums;

import lombok.Getter;

@Getter
public enum QuantityUnits {
    PIECE("piece"),
    GRAM("gram"),
    KILOGRAM("kilogram"),
    LITER("liter"),
    MILLILITER("milliliter"),
    CUP("cup"),
    TABLESPOON("tablespoon"),
    TEASPOON("teaspoon");

    private final String unit;

    QuantityUnits(String unit) {
        this.unit = unit;
    }
    
}
