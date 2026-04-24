package org.roomly.dto;

import java.time.LocalDateTime;

public record ShoppingListItemDTO(
  int id,
  ProductDTO product,
  int count,
  boolean purchased,
  LocalDateTime addedAt,
  LocalDateTime purchasedAt,
  ProfileDTO addedBy,
  String notes
) {
}
