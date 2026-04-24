package org.roomly.utils;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility class for color name and hex code conversions.
 * Initialized by ColorsService at application startup.
 */
public final class ColorsUtil {
    
    private static Map<String, String> hexToColor = Collections.emptyMap();
    private static Map<String, String> colorToHex = Collections.emptyMap();
    
    private ColorsUtil() {
        // Prevent instantiation
    }
    
    /**
     * Initialize the color mappings. Called by ColorsService at startup.
     * Should only be called once during application initialization.
     */
    public static void initialize(Map<String, String> hexToColorMap, Map<String, String> colorToHexMap) {
        hexToColor = Collections.unmodifiableMap(hexToColorMap);
        colorToHex = Collections.unmodifiableMap(colorToHexMap);
    }
    
    /**
     * Get all colors as a map of hex codes to color names.
     */
    public static Map<String, String> getAllColors() {
        return colorToHex.entrySet().stream()
          .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    }
    
    /**
     * Get the color name for a given hex code.
     * @param hex The hex code (with or without #)
     * @return The color name, or null if not found
     */
    public static String getColorByHex(String hex) {
        String normalizedHex = normalizeHex(hex);
        return normalizedHex != null ? hexToColor.get(normalizedHex) : null;
    }
    
    /**
     * Get the hex code for a given color name.
     * @param colorName The color name
     * @return The hex code, or null if not found
     */
    public static String getHexByColor(String colorName) {
        String normalizedColorName = normalizeColorName(colorName);
        return normalizedColorName != null ? colorToHex.get(normalizedColorName) : null;
    }
    
    /**
     * Check if a color name exists in the catalog.
     */
    public static boolean isValidColor(String colorName) {
        return getHexByColor(colorName) != null;
    }
    
    /**
     * Check if a hex code exists in the catalog.
     */
    public static boolean isValidHex(String hex) {
        return getColorByHex(hex) != null;
    }
    
    /**
     * Normalize a hex code to uppercase format with # prefix.
     */
    public static String normalizeHex(String hex) {
        if (hex == null) {
            return null;
        }
        
        String trimmed = hex.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        
        return trimmed.startsWith("#")
               ? trimmed.toUpperCase(Locale.ROOT)
               : ("#" + trimmed).toUpperCase(Locale.ROOT);
    }
    
    /**
     * Normalize a color name to uppercase format with underscores.
     */
    public static String normalizeColorName(String colorName) {
        if (colorName == null) {
            return null;
        }
        
        String trimmed = colorName.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        
        return trimmed.replace(' ', '_').toUpperCase(Locale.ROOT);
    }
}


