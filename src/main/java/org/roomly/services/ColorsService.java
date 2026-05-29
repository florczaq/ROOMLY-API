package org.roomly.services;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.json.JSONParser;
import org.roomly.utils.ColorsUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Service that loads the color catalog from a JSON file and initializes {@link ColorsUtil}.
 * <p>
 * The catalog maps color names to their hex codes and vice versa. It is read once
 * at application startup via {@link #init()}. If the file is missing or the JSON is
 * malformed, {@link ColorsUtil} is initialized with an empty catalog so the application
 * can still start.
 * </p>
 */
@Slf4j
@Service
public class ColorsService {
    
    /** The directory path where the colors catalog file is located (e.g. "/data/colors"). */
    @Value("${colors.storage.path}")
    private String colorsCatalogPath;
    
    /** The name of the colors catalog file (e.g. "colors.json"). */
    @Value("${colors.storage.filename}")
    private String colorsCatalogFilename;
    
    /**
     * Loads the color catalog from the configured JSON file and initializes {@link ColorsUtil}.
     * <p>
     * If the file is missing, {@link ColorsUtil} is initialized with empty maps.
     * If the JSON is present but invalid, an {@link IOException} is thrown after
     * still initializing {@link ColorsUtil} with whatever entries were successfully parsed.
     * </p>
     *
     * @throws IOException if the catalog file exists but cannot be read or its JSON is invalid
     */
    @PostConstruct
    public void init () throws IOException {
        Map<String, String> parsedHexToColor = new HashMap<>();
        Map<String, String> parsedColorToHex = new HashMap<>();
        
        Path filePath = Path.of(colorsCatalogPath, colorsCatalogFilename);
        if (!filePath.toFile().exists()) {
            ColorsUtil.initialize(parsedHexToColor, parsedColorToHex);
            return;
        }
        
        try {
            log.info("Loading colors catalog from: {}", filePath);
            String jsonContent = Files.readString(filePath);
            Object parsed = new JSONParser(jsonContent).parse();
            
            if (!(parsed instanceof List)) {
                log.error("Colors catalog JSON is not an array: {}", colorsCatalogFilename);
                ColorsUtil.initialize(parsedHexToColor, parsedColorToHex);
                return;
            }
            
            List<Object> colorsList = (List<Object>) parsed;
            for (Object object : colorsList) {
                if (!(object instanceof Map<?, ?> map)) {
                    continue;
                }
                String hex = ColorsUtil.normalizeHex((String) map.get("hex"));
                String colorName = ColorsUtil.normalizeColorName((String) map.get("name"));
                if (hex != null && colorName != null) {
                    parsedHexToColor.put(hex, colorName);
                    parsedColorToHex.put(colorName, hex);
                }
            }
            log.info("Loaded {} colors into catalog", parsedHexToColor.size());
        } catch (Exception e) {
            throw new IOException("Failed to load colors catalog", e);
        } finally {
            ColorsUtil.initialize(parsedHexToColor, parsedColorToHex);
        }
    }
}