package org.roomly.notifications.dto;

import java.time.LocalDateTime;

public record NotificationDTO(
  String id,
  String title,
  String message,
  LocalDateTime timestamp,
  String profileId
) {
}
