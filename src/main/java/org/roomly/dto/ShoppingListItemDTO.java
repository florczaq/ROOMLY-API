package org.roomly.dto;

import java.time.LocalDateTime;

public record ShoppingListItemDTO(
  int id,
  ProductDTO product,
  int count,
  LocalDateTime addedAt,
  String notes
) {
}
