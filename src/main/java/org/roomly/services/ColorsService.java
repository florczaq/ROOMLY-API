package org.roomly.services;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.json.JSONParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@SuppressWarnings("unused")
public class ColorsService {
    
    private static Map<String, String> hexToColor = new HashMap<>();
    private static Map<String, String> colorToHex = new HashMap<>();
    
    @Value("${colors.storage.path}")
    private String colorsCatalogPath;
    
    @Value("${colors.storage.filename}")
    private String colorsCatalogFilename;
    
    @PostConstruct
    public void init() {
        Map<String, String> parsedHexToColor = new HashMap<>();
        Map<String, String> parsedColorToHex = new HashMap<>();
        
        try {
            Path filePath = Path.of(colorsCatalogPath, colorsCatalogFilename);
            if (filePath.toFile().exists()) {
                log.info("Loading colors catalog from: {}", filePath);
                String jsonContent = Files.readString(filePath);
                JSONParser parser = new JSONParser(jsonContent);
                Object parsed = parser.parse();
                if (parsed instanceof List) {
                    List<Object> colorsList = (List<Object>) parsed;
                    for (Object object : colorsList) {
                        if (!(object instanceof Map<?, ?> map)) {
                            continue;
                        }
                        String hex = normalizeHex((String) map.get("hex"));
                        String colorName = normalizeColorName((String) map.get("name"));
                        if (hex != null && colorName != null) {
                            parsedHexToColor.put(hex, colorName);
                            parsedColorToHex.put(colorName, hex);
                        }
                    }
                    log.info("Loaded {} colors into catalog", parsedHexToColor.size());
                } else {
                    log.error("Colors catalog JSON is not an array: {}", colorsCatalogFilename);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to construct path for colors catalog", e);
        }
        
        hexToColor = Collections.unmodifiableMap(parsedHexToColor);
        colorToHex = Collections.unmodifiableMap(parsedColorToHex);
    }
    
    public static Map<String, String> getAllColors () {
        return colorToHex.entrySet().stream()
          .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    }
    
    public static String getColorByHex (String hex) {
        String normalizedHex = normalizeHex(hex);
        if (normalizedHex == null) {
            return null;
        }
        return hexToColor.get(normalizedHex);
    }
    
    public static String getHexByColor (String colorName) {
        String normalizedColorName = normalizeColorName(colorName);
        if (normalizedColorName == null) {
            return null;
        }
        return colorToHex.get(normalizedColorName);
    }
    
    public static boolean isValidColor (String colorName) {
        return getHexByColor(colorName) != null;
    }
    
    public static boolean isValidHex (String hex) {
        return getColorByHex(hex) != null;
    }
    
    private static String normalizeHex (String hex) {
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
    
    private static String normalizeColorName (String colorName) {
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

