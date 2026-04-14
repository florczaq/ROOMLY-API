package org.roomly.security.authentication.jwt.dto;

import jakarta.validation.constraints.NotBlank;

public record DeviceLoginBody(
  @NotBlank String deviceId
) {
}

