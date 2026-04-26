package org.roomly.utils;


import java.util.List;

public final class AvatarsUtil {
    private static List<String> availableAvatars;
    
    
    private AvatarsUtil () {
        // Prevent instantiation
    }
    
    public static void initialize (List<String> avatars) {
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
