package org.roomly.security.authentication.services;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.roomly.security.authentication.entities.Account;
import org.roomly.security.authentication.enums.AuthProvider;
import org.roomly.security.authentication.jwt.dto.TokenResponse;
import org.roomly.security.authentication.jwt.repositories.RefreshTokenRepository;
import org.roomly.security.authentication.jwt.services.JwtService;
import org.roomly.security.authentication.jwt.services.TokenType;
import org.roomly.security.authentication.repositories.AccountRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService implements UserDetailsService {
    private final AccountRepository accountRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    
    @Transactional
    public String registerDevice () {
        String deviceId = UUID.randomUUID().toString();
        
        Account deviceAccount = new Account()
          .setAuthProvider(AuthProvider.DEVICE_ONLY)
          .setDevices(List.of(deviceId));
        
        log.info("Registering new device with id: {}", deviceId);
        
        accountRepository.save(deviceAccount);
        accountRepository.flush(); // Ensure it's persisted
        
        
        return deviceId;
    }
    
    @Transactional
    public TokenResponse register (String email, String password, Optional<String> deviceId) {
        if (accountRepository.findFirstByEmail(email).isPresent()) {
            throw new IllegalArgumentException("User with email " + email + " already exists");
        }
        
        // If deviceId is present, try to find a DEVICE_ONLY account with that device.
        // If found, upgrade it to EMAIL_PASSWORD.
        // If not found, create a new account with the email, password.
        // Device can be shared across multiple accounts.
        Optional<Account> deviceOnlyAccount = deviceId.flatMap(accountRepository::findByDevicesContaining)
          .filter(acc -> acc.getAuthProvider() == AuthProvider.DEVICE_ONLY);
        
        Account account = deviceOnlyAccount.orElseGet(Account::new);
        
        account.setEmail(email);
        account.setPasswordHash(passwordEncoder.encode(password));
        account.setAuthProvider(AuthProvider.EMAIL_PASSWORD);
        
        // Add deviceId to account's devices list if provided
        deviceId.ifPresent(id -> {
            List<String> devices = account.getDevices();
            if (devices == null) {
                account.setDevices(List.of(id));
            } else if (!devices.contains(id)) {
                List<String> updatedDevices = new java.util.ArrayList<>(devices);
                updatedDevices.add(id);
                account.setDevices(updatedDevices);
            }
        });
        
        // TODO send activation email with activation link (e.g. /auth/activate?token=...)
        
        Account newAccount = accountRepository.save(account);
        accountRepository.flush();
        return this.generateTokensForUser(newAccount.getId());
    }
    
    @Transactional
    public TokenResponse login (String email, String password) {
        Account account = accountRepository.findByEmail(email)
          .orElseThrow(() -> new IllegalArgumentException("User with email %s not found".formatted(email)));
        
        if (account.getAuthProvider() != AuthProvider.EMAIL_PASSWORD) {
            throw new IllegalArgumentException(
              "User with email %s is not an email-password user".formatted(email));
        }
        
        if (!passwordEncoder.matches(password, account.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid password");
        }
        
        return this.generateTokensForUser(account.getId());
    }
    
    @Transactional
    public TokenResponse loginWithDevice (String deviceId) {
        log.info("Attempting device login with device id: {}", deviceId);
        
        Account account = accountRepository.findByDevicesContaining(deviceId)
          .orElseThrow(() -> new IllegalArgumentException("Device not registered " + deviceId));
        
        if (account.getAuthProvider() != AuthProvider.DEVICE_ONLY) {
            throw new IllegalArgumentException("Device is associated with a non-device-only account");
        }
        
        return this.generateTokensForUser(account.getId());
    }
    
    @Transactional
    public TokenResponse refreshAccessToken (String refreshToken) {
        if (!jwtService.validateToken(refreshToken, TokenType.REFRESH)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }
        
        String uuid = jwtService.getAllClaimsFromToken(refreshToken).getSubject();
        
        deactivateRefreshToken(refreshToken);
        
        if (uuid == null || uuid.isBlank()) {
            throw new IllegalArgumentException("Invalid refresh token");
        }
        
        if (accountRepository.findById(uuid).isEmpty()) {
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
    
    @Override
    @NonNull
    public UserDetails loadUserByUsername (@NonNull String uuid) throws UsernameNotFoundException {
        var account = accountRepository.findById(uuid)
          .orElseThrow(() -> new UsernameNotFoundException("User with id " + uuid + " not found"));
        
        // For device-only users, return a dummy password since it won't be used for authentication.
        String password =
          account.getAuthProvider() == AuthProvider.EMAIL_PASSWORD
          ? account.getPasswordHash()
          : "{noop}DEVICE_ONLY";
        
        return new org.springframework.security.core.userdetails.User(
          account.getId(), password, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }
    
    public Account loadAccountById (String uuid) {
        return accountRepository.findById(uuid)
          .orElseThrow(() -> new IllegalArgumentException("User with id " + uuid + " not found"));
    }
    
    public Account getCurrentlyAuthenticatedAccount () {
        String accountId = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
          .map(Principal::getName)
          .orElseThrow(() -> new IllegalStateException("No authenticated user found"));
        
        return this.loadAccountById(accountId);
    }
}
