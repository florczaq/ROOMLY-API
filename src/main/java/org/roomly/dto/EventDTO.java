package org.roomly.dto;

import java.time.LocalDateTime;
import java.util.List;

public record EventDTO(
  int id,
  String name,
  String description,
  LocalDateTime startTime,
  LocalDateTime endTime,
  String householdId,
  ProfileDTO creator,
  List<ProfileDTO> attendees
) {
}

