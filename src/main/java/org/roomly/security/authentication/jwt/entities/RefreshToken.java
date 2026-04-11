package org.roomly.security.authentication.jwt.entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
//@Table(name = "refresh_tokens", schema = "security")
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Setter
    @Column(name = "token", nullable = false, unique = true)
    private String token;
    @Column(name = "uuid", nullable = false)
    private String uuid;
    
    @Column(name = "expiry_date", nullable = false)
    private long expiryDate;

    @Column(name = "active", nullable = false)
    @Setter
    private boolean active;
}
