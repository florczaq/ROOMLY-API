package org.roomly.services;

import lombok.extern.slf4j.Slf4j;
import org.roomly.entities.Avatar;
import org.roomly.repositories.AvatarsRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Service
public class AvatarService {
    private final AvatarsRepository avatarsRepository;
    private final ColorsService colorsService;
    private final String storagePath;
    
    
    public AvatarService (
      AvatarsRepository avatarsRepository,
      ColorsService colorsService,
      @Value("${avatar.storage.path}") String storagePath
    ) {
        this.avatarsRepository = avatarsRepository;
        this.storagePath = storagePath;
        this.colorsService = colorsService;
    }
    
    
    public Avatar createAvatar (String name, String colorHex) {
        Avatar avatar = new Avatar();
        avatar.setName(name);
        avatar.setColorName(colorHex);
        return avatar;
    }
    
    public Avatar getAvatarByUserId (String userId) {
        // In a real application, this would query the database
        // For this example, we'll return a dummy avatar
        return new Avatar("User Avatar", "#000000");
    }
    
    public Avatar getAvatarById (String id) {
        // In a real application, this would query the database
        // For this example, we'll return a dummy avatar
        return new Avatar("Dummy Avatar", "#FFFFFF");
    }
    
    public Avatar updateAvatar (String id, String name, String colorHex) {
        // In a real application, this would update the avatar in the database
        // For this example, we'll return an updated dummy avatar
        return new Avatar(name, colorHex);
    }
    
    private boolean isAvatarAvailable (String name, String householdId) {
        // In a real application, this would check the database for existing avatars
        // For this example, we'll assume all names are available
        return true;
    }
    
    private boolean isColorAvailable (String colorHex, String householdId) {
        // In a real application, this would check the database for existing colors
        // For this example, we'll assume all colors are available
        return true;
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
