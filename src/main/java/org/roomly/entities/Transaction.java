package org.roomly.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.roomly.enums.TransactionType;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Accessors(chain = true)
@SuppressWarnings("JpaDataSourceORMInspection")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    @Column(nullable = false)
    String title;
    
    @Column(name = "send_at", nullable = false)
    LocalDateTime sendAt = LocalDateTime.now();
    
    @Column(nullable = false)
    @DecimalMin("0.01")
    @Max(100_000)
    double amount;
    
    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    Profile sender;
    
    @ManyToOne
    @JoinColumn(name = "recipient_id", nullable = false)
    Profile recipient;
    
    @Enumerated(EnumType.STRING)
    TransactionType type;
    
    @PrePersist
    public void prePersist () {
        if (sendAt == null) {
            sendAt = LocalDateTime.now();
        }
    }
}
