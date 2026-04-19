package org.roomly.dto;

import java.util.List;

public record AvailableAvatarsAndColorsDTO(List<String> avatars, List<ColorDTO> colors) {
}
