package org.roomly.dto;

import java.util.List;

public record InventoryDTO(
  int id,
  List<InventoryItemDTO> items
) {
}

