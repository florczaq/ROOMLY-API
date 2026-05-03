package org.roomly.utils;


import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public final class AvatarsUtil {
    private static List<String> availableAvatars;
    
    
    private AvatarsUtil () {
        // Prevent instantiation
    }
    
    public static void initialize (List<String> avatars) {
        log.info("Initializing available avatars with: {}", avatars);
        if (avatars == null || avatars.isEmpty()) {
            throw new IllegalArgumentException("Available avatars list cannot be null or empty");
        }
        availableAvatars = List.copyOf(avatars);
    }
    
    
    public static boolean isValidAvatarName (String avatarName) {
        return availableAvatars != null && availableAvatars.contains(avatarName);
    }
    
    public static List<String> getAvailableAvatars () {
        if (availableAvatars == null) {
            throw new IllegalStateException("Available avatars have not been initialized");
        }
        return availableAvatars;
    }
    
}
