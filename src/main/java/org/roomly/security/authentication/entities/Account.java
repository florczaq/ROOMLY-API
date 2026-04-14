package org.roomly.security.authentication.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.roomly.security.authentication.enums.AuthProvider;

import java.util.List;

@Entity
//@Table(name = "users", schema = "articles_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(unique = true)
    private String email;
    
    private String passwordHash;
    
    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> devices;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AuthProvider authProvider = AuthProvider.DEVICE_ONLY;
    
    private boolean emailVerified = false;
    
}
