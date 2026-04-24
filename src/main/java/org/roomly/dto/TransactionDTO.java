package org.roomly.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;

public record TransactionDTO(
  int id,
  String title,
  LocalDateTime sendAt,
  @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
  double amount,
  ProfileDTO sender,
  ProfileDTO recipient,
  @Pattern(regexp = "^(INCOME|EXPENSE)$", message = "Type must be either INCOME or EXPENSE")
  String type
) {
}

