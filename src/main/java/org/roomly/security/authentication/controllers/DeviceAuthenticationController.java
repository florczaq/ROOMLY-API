package org.roomly.security.authentication.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.roomly.security.authentication.jwt.dto.DeviceLoginBody;
import org.roomly.security.authentication.jwt.dto.TokenResponse;
import org.roomly.security.authentication.services.AuthenticationService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/device")
@RequiredArgsConstructor
public class DeviceAuthenticationController {
    private final AuthenticationService authenticationService;
    
    @PostMapping("/register")
    public String registerDevice() {
        return authenticationService.registerDevice();
    }

    @PostMapping("/login")
    public TokenResponse loginWithDevice(@RequestBody @Valid DeviceLoginBody body) {
        return authenticationService.loginWithDevice(body.deviceId());
    }
    
}
