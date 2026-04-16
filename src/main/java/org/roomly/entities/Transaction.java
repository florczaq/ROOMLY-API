package org.roomly.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.roomly.enums.TransactionType;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Accessors(chain = true)
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    @Column(nullable = false)
    String title;
    
    @Column(nullable = false)
    long timestamp;
    
    @Column(nullable = false)
    double amount;
    
    @Column(nullable = false)
    String senderId;
    
    @Column(nullable = false)
    String recipientId;
    
    @Enumerated(EnumType.STRING)
    TransactionType type;
}
