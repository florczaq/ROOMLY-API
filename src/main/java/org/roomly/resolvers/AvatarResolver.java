package org.roomly.resolvers;

import lombok.RequiredArgsConstructor;
import org.roomly.entities.Avatar;
import org.roomly.services.AvatarService;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class AvatarResolver {
    private final AvatarService avatarService;
    
    public Avatar avatar (String userId) {
        return avatarService.getAvatarByUserId(userId);
    }
    
    public Avatar avatarById (String id) {
        return avatarService.getAvatarById(id);
    }
    
    public Avatar createAvatar (String name, String colorHex) {
        return avatarService.createAvatar(name, colorHex);
    }
    
    public Avatar updateAvatar (String id, String name, String colorHex) {
        return avatarService.updateAvatar(id, name, colorHex);
    }
    
}
