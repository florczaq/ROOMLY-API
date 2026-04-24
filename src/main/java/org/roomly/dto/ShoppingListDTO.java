package org.roomly.dto;


import java.util.List;


public record ShoppingListDTO(
  int id,
  String householdId,
  List<ShoppingListItemDTO> items
) {
}
