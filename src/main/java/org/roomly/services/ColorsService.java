package org.roomly.services;

import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.json.JSONParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Slf4j
@Service
public class ColorsService {
    
    private final Map<String, String> hexToColor;
    private final Map<String, String> colorToHex;
    
    public ColorsService (
      @Value("${colors.storage.path}") String colorsCatalogPath,
      @Value("${colors.storage.filename}") String colorsCatalogFilename
    ) {
        
        Map<String, String> parsedHexToColor = new HashMap<>();
        Map<String, String> parsedColorToHex = new HashMap<>();
        
        try {
            Path filePath = Path.of(colorsCatalogPath, colorsCatalogFilename);
            if (filePath.toFile().exists()) {
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
                } else {
                    log.error("Colors catalog JSON is not an array: {}", colorsCatalogFilename);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to construct path for colors catalog", e);
        }
        
        this.hexToColor = Collections.unmodifiableMap(parsedHexToColor);
        this.colorToHex = Collections.unmodifiableMap(parsedColorToHex);
    }
    
    public String getColorByHex (String hex) {
        String normalizedHex = normalizeHex(hex);
        if (normalizedHex == null) {
            return null;
        }
        return hexToColor.get(normalizedHex);
    }
    
    public String getHexByColor (String colorName) {
        String normalizedColorName = normalizeColorName(colorName);
        if (normalizedColorName == null) {
            return null;
        }
        return colorToHex.get(normalizedColorName);
    }
    
    
    private String normalizeHex (String hex) {
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
    
    private String normalizeColorName (String colorName) {
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

