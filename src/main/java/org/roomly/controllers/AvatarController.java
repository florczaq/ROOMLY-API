package org.roomly.controllers;

import lombok.RequiredArgsConstructor;
import org.roomly.services.AvatarService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/open/api/avatars")
@RequiredArgsConstructor
public class AvatarController {
    private final AvatarService avatarService;

    @GetMapping(value = "/{name}", produces = {"image/png", "image/jpeg"})
    public ResponseEntity<byte[]> getAvatar (@PathVariable String name,
      @RequestParam(name = "color", required = false) String color,
      @RequestParam(name = "hex", required = false) String hex
    ) throws IOException {
        if (color != null) {
            return ResponseEntity.ok(avatarService.loadAvatarFromStorage(name, color));
        }
        if (hex != null) {
            return ResponseEntity.ok(avatarService.loadAvatarFromStorage(name, "#" + hex));
        }
        return ResponseEntity.badRequest().build();
    }
    
}
