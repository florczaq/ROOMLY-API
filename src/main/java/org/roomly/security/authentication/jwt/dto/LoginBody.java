package org.roomly.security.authentication.jwt.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginBody(
  @NotBlank @Email String email,
  @NotBlank String password
) {
}
