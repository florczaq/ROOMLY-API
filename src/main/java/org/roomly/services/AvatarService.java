package org.roomly.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Service
public class AvatarService {
    private final ColorsService colorsService;
    private final String storagePath;
    
    
    public AvatarService (
      ColorsService colorsService,
      @Value("${avatar.storage.path}") String storagePath
    ) {
        this.storagePath = storagePath;
        this.colorsService = colorsService;
    }
    
    public byte[] loadAvatarFromStorage (String name, String color) {
        if (color.contains("#")) {
            color = colorsService.getColorByHex(color);
            if (color == null) {
                throw new RuntimeException("Color not found for hex: " + color);
            }
        }
        String normalizedColor = color.toUpperCase();
        String baseName = (name + "_" + normalizedColor).toUpperCase();
        
        for (String ext : new String[]{"png", "jpg"}) {
            Path filePath = Path.of(storagePath, baseName + "." + ext);
            log.info("Loading avatar from file: {}, {}", filePath, filePath.toFile().exists());
            if (filePath.toFile().exists()) {
                try {
                    return Files.readAllBytes(filePath);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to load avatar file: " + filePath, e);
                }
            }
        }
        throw new RuntimeException("Avatar file not found for: " + baseName);
    }
    
}
