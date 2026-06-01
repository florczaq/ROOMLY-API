package org.roomly.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record EventDTO(
  int id,
  String name,
  String description,
  OffsetDateTime startTime,
  OffsetDateTime endTime,
  String householdId,
  ProfileDTO creator,
  List<ProfileDTO> attendees
) {
}

