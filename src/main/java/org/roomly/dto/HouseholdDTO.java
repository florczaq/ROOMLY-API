package org.roomly.dto;

import java.util.List;

public record HouseholdDTO(
  String id,
  String name,
  String joinCode,
  int membersLimit,
  ProfileDTO owner,
  List<ProfileDTO> members,
  InventoryDTO sharedInventory,
  ShoppingListDTO sharedShoppingList,
  int membersCount,
  ProfileDTO currentUserProfile
) {

}
