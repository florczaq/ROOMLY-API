package org.roomly.security.authentication.jwt.dto;

public record RegisterBody(String name, String email, String password) {
}
