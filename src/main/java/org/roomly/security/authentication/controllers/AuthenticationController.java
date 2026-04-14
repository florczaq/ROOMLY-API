package org.roomly.security.authentication.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.roomly.security.authentication.jwt.dto.LoginBody;
import org.roomly.security.authentication.jwt.dto.RegisterBody;
import org.roomly.security.authentication.jwt.dto.TokenResponse;
import org.roomly.security.authentication.services.AuthenticationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    
    @PostMapping("/register")
    public TokenResponse register (@RequestBody @Valid RegisterBody registerBody) {
        return authenticationService.register(
          registerBody.email(),
          registerBody.password(),
          registerBody.deviceId()
        );
    }
    
    @PostMapping("/login")
    public TokenResponse login (@RequestBody @Valid LoginBody loginBody) {
        return authenticationService.login(
          loginBody.email(),
          loginBody.password()
        );
    }
    
    @GetMapping("/refresh")
    public TokenResponse refreshToken (@RequestParam(name = "t") String refreshToken) {
        return authenticationService.refreshAccessToken(refreshToken);
    }
    
}
