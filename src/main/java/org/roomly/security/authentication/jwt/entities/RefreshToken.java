package org.roomly.security.authentication.jwt.entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.roomly.security.authentication.entities.Account;

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
    
    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;
    
    @Column(name = "expiry_date", nullable = false)
    private long expiryDate;

    @Column(name = "active", nullable = false)
    @Setter
    private boolean active;
}
