package org.roomly.security.authentication.jwt.dto;

public record TokenResponse(String accessToken, String refreshToken) {
}
