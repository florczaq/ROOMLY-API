package org.roomly.security.authentication.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.roomly.security.authentication.enums.AuthProvider;

import java.util.List;

@Entity
//@Table(name = "users", schema = "articles_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@SuppressWarnings("JpaDataSourceORMInspection")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(unique = true)
    private String email;
    
    private String passwordHash;
    
    @ElementCollection
    @CollectionTable(name = "account_devices", joinColumns = @JoinColumn(name = "account_id"))
    @Column(name = "device_id")
    private List<String> devices;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AuthProvider authProvider = AuthProvider.DEVICE_ONLY;
    
    private boolean emailVerified = false;
    
        @Override
        public String toString () {
            String devicesStr;
            try {
                devicesStr = devices != null ? devices.toString() : "null";
            } catch (Exception e) {
                devicesStr = "<not loaded>";
            }
            return """
                    \nAccount {
                        id: %s,
                        email: %s,
                        authProvider: %s,
                        emailVerified: %b,
                        devices: %s
                    }
                    """.formatted(id, email, authProvider, emailVerified, devicesStr);
        }
}
