package org.roomly.controllers;

import lombok.RequiredArgsConstructor;
import org.roomly.services.AvatarService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
// TODO 'open' is temporary for testing, delete in production
@RequestMapping("/open/api/avatars")
@RequiredArgsConstructor
public class AvatarController {
    private final AvatarService avatarService;
    
    @GetMapping(value = "/{name}", produces = {"image/png", "image/jpeg"})
    public ResponseEntity<byte[]> getAvatar (@PathVariable String name,
      @RequestParam(name = "colorName") String colorName
    ) {
        return ResponseEntity.ok(avatarService.loadAvatarFromStorage(name, colorName));
    }
    
}
