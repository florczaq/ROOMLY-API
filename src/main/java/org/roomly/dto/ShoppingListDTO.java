package org.roomly.dto;


import java.util.List;


public record ShoppingListDTO(
  int id,
  List<ShoppingListItemDTO> items
) {
}
