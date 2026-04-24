package org.roomly.security.authentication.jwt.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.roomly.security.authentication.jwt.entities.RefreshToken;
import org.roomly.security.authentication.jwt.repositories.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class JwtService {
    
    private final long refreshTokenExpirationTime;
    private final long accessTokenExpirationTime;
    private final String secretKey;
    private final RefreshTokenRepository refreshTokenRepository;
    
    public JwtService (
      @Value("${security.jwt.refresh.token.expiration}") long refreshTokenExpirationTime,
      @Value("${security.jwt.access.token.expiration}") long accessTokenExpirationTime,
      @Value("${security.jwt.secret}") String secretKey,
      RefreshTokenRepository refreshTokenRepository
    ) {
        this.refreshTokenExpirationTime = refreshTokenExpirationTime;
        this.accessTokenExpirationTime = accessTokenExpirationTime;
        this.secretKey = secretKey;
        this.refreshTokenRepository = refreshTokenRepository;
    }
    
    public String generateToken (String uuid, TokenType tokenType) {
        return createToken(uuid, tokenType);
    }
    
    @SuppressWarnings("all")
    public boolean validateToken (String token, TokenType expectedType) {
        try {
            //If token is invalid, extracting claims will throw an exception
            Claims claims = getAllClaimsFromToken(token);
            
            String subject = claims.getSubject();
            String type = claims.get("type", String.class);
            Date expiration = claims.getExpiration();
            
            if (expectedType == TokenType.REFRESH) {
                RefreshToken storedToken = refreshTokenRepository.findByToken(token).orElse(null);
                if (storedToken == null || !storedToken.isActive() || storedToken.getExpiryDate() < System.currentTimeMillis()) {
                    log.warn("Refresh token not found or invalid in database");
                    return false;
                }
            }
            
            return subject != null
              && !subject.isBlank()
              && expiration != null
              && expiration.after(new Date())
              && expectedType.name().equals(type);
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }
    
    public Claims getAllClaimsFromToken (String token) {
        return Jwts.parser()
          .verifyWith(this.getSigningKey())
          .build()
          .parseSignedClaims(token)
          .getPayload();
    }
    
    public String getSubjectFromToken (String token) {
        return this.getAllClaimsFromToken(token).getSubject();
    }
    
    private SecretKey getSigningKey () {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    private String createToken (String subject, TokenType type) {
        return createToken(subject, type, Map.of());
    }
    
    private String createToken (String subject, TokenType type, Map<String, Object> extraClaims) {
        long ttl = (type == TokenType.REFRESH) ? refreshTokenExpirationTime : accessTokenExpirationTime;
        String token = Jwts
          .builder()
          .claims(extraClaims)
          .subject(subject)
          .id(UUID.randomUUID().toString())
          .issuedAt(new Date())
          .expiration(Date.from(Instant.now().plusMillis(ttl)))
          .signWith(this.getSigningKey(), Jwts.SIG.HS256)
          .claim("type", type.name())
          .compact();
        
        if (type == TokenType.REFRESH) {
            try {
                RefreshToken savedToken = refreshTokenRepository.save(
                  new RefreshToken(
                    null,
                    token,
                    subject,
                    Date.from(Instant.now().plusMillis(ttl)).getTime(),
                    true
                  )
                );
                
                if (savedToken.getId() == null) {
                    throw new IllegalArgumentException("Failed to save refresh token");
                }
            } catch (DataIntegrityViolationException e) {
                //Generate if same token was generated before
                return createToken(subject, type, extraClaims);
            }
        }
        
        return token;
    }
}

