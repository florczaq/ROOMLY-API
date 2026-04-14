package org.roomly.security.authentication.jwt.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.Optional;

public record RegisterBody(
  @NotBlank @Email String email,
  @NotBlank String password,
  Optional<String> deviceId) {
}
