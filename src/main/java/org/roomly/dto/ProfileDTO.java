package org.roomly.dto;

public record ProfileDTO(
  String id,
  String nickname,
  AvatarDTO avatar
  ) {
}
