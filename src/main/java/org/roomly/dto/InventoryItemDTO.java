package org.roomly.dto;

import java.time.LocalDateTime;

public record InventoryItemDTO(
  int id,
  ProductDTO product,
  int count,
  LocalDateTime addedAt,
  String notes
) {
}
