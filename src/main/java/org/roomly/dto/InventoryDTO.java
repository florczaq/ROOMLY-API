package org.roomly.dto;

public record InventoryDTO(
  int id,
  String name,
  String householdId,
  ProfileDTO owner
) {
}

