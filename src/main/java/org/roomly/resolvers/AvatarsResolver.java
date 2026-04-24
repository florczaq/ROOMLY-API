package org.roomly.resolvers;

import lombok.RequiredArgsConstructor;
import org.roomly.dto.AvailableAvatarsAndColorsDTO;
import org.roomly.dto.ColorDTO;
import org.roomly.services.AvatarService;
import org.roomly.utils.ColorsUtil;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;


@Controller
@RequiredArgsConstructor
public class AvatarsResolver {
    private final AvatarService avatarService;
    
    @QueryMapping
    public AvailableAvatarsAndColorsDTO availableAvatarsAndColors () throws IOException {
        List<String> avatars = avatarService.getAvailableAvatarsFromCatalog();
        Map<String, String> colorsMap = ColorsUtil.getAllColors();
        
        // Convert Map to List of ColorDTO
        List<ColorDTO> colors = colorsMap.entrySet().stream()
            .map(entry -> new ColorDTO(entry.getKey(), entry.getValue()))
            .toList();
        
        return new AvailableAvatarsAndColorsDTO(avatars, colors);
    }
    
    
}
