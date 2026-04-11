package org.roomly.security.authentication.services;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.roomly.security.authentication.entities.Role;
import org.roomly.security.authentication.entities.User;
import org.roomly.security.authentication.jwt.dto.TokenResponse;
import org.roomly.security.authentication.jwt.repositories.RefreshTokenRepository;
import org.roomly.security.authentication.jwt.services.JwtService;
import org.roomly.security.authentication.jwt.services.TokenType;
import org.roomly.security.authentication.repositories.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class AuthenticationService implements UserDetailsService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    
    public TokenResponse register (String email, String password, String name) {
        if (userRepository.findFirstById(email).isPresent()) {
            throw new IllegalArgumentException("User with email " + email + " already exists");
        }
        
        var user = userRepository.save(
          new User(
            null,
            name,
            email,
            passwordEncoder.encode(password),
            Role.USER
          )
        );
        
        return this.generateTokensForUser(user.getId());
    }
    
    public TokenResponse login (String email, String password) {
        User user = userRepository.findByEmail(email)
          .orElseThrow(() -> new IllegalArgumentException("User with email " + email + " not found"));
        
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }
        
        
        
        return this.generateTokensForUser(user.getId());
    }
    
    public TokenResponse refreshAccessToken (String refreshToken) {
        if (!jwtService.validateToken(refreshToken, TokenType.REFRESH)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }
        
        String uuid = jwtService.getAllClaimsFromToken(refreshToken).getSubject();
        
        deactivateRefreshToken(refreshToken);
        
        if (uuid == null || uuid.isBlank()) {
            throw new IllegalArgumentException("Invalid refresh token");
        }
        
        if (userRepository.findFirstById(uuid).isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        
        return this.generateTokensForUser(uuid);
    }
    
    private TokenResponse generateTokensForUser (String uuid) {
        return new TokenResponse(
          jwtService.generateToken(uuid, TokenType.ACCESS),
          jwtService.generateToken(uuid, TokenType.REFRESH)
        );
    }
    
    private void deactivateRefreshToken (String refreshToken) {
        refreshTokenRepository.findFirstByToken(refreshToken).ifPresent(token -> {
            token.setActive(false);
            refreshTokenRepository.save(token);
        });
    }
    
    /**
     * Spring Security method to load user details by uuid (used as username)
     */
    @Override
    @NonNull
    public UserDetails loadUserByUsername (@NonNull String uuid) throws UsernameNotFoundException {
        var user = userRepository.findFirstById(uuid)
          .orElseThrow(() -> new IllegalArgumentException("User with id " + uuid + " not found"));
        
        
        return new org.springframework.security.core.userdetails.User(
          user.getId(), user.getPassword(), List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
    }
    
    public User getUserByUuid (String uuid) {
        return userRepository.findFirstById(uuid)
          .orElseThrow(() -> new IllegalArgumentException("User with id " + uuid + " not found"));
    }
    
}
