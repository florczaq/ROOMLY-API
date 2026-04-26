package org.roomly.resolvers;

import lombok.RequiredArgsConstructor;
import org.roomly.dto.AvailableAvatarsAndColorsDTO;
import org.roomly.dto.ColorDTO;
import org.roomly.utils.AvatarsUtil;
import org.roomly.utils.ColorsUtil;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Map;


@Controller
@RequiredArgsConstructor
public class AvatarsResolver {
    
    @QueryMapping
    public AvailableAvatarsAndColorsDTO availableAvatarsAndColors () {
        Map<String, String> colorsMap = ColorsUtil.getAllColors();
        
        return new AvailableAvatarsAndColorsDTO(
          AvatarsUtil.getAvailableAvatars(),
          colorsMap
            .entrySet()
            .stream()
            .map(entry -> new ColorDTO(entry.getKey(), entry.getValue()))
            .toList()
        );
    }
    
    
}
