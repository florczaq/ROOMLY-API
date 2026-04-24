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
@SuppressWarnings("JpaDataSourceORMInspection")
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Setter
    @Column(name = "token", nullable = false, unique = true)
    private String token;
    @Column(name = "userId", nullable = false)
    private String userId;
    
    @Column(name = "expiry_date", nullable = false)
    private long expiryDate;

    @Column(name = "active", nullable = false)
    @Setter
    private boolean active;
}
