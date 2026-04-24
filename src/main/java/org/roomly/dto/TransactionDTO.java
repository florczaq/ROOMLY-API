package org.roomly.dto;

import java.time.LocalDateTime;

public record TransactionDTO(
  int id,
  String title,
  LocalDateTime sendAt,
  double amount,
  ProfileDTO sender,
  ProfileDTO recipient,
  String type
) {
}

