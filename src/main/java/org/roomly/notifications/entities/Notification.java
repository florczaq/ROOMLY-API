package org.roomly.notifications.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.roomly.entities.Profile;
import org.roomly.notifications.dto.NotificationDTO;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Setter
@Getter
@Table(name = "notifications")
public class Notification {
    
    @Id
    String id;
    @Column(length = 250)
    String title;
    @Column(length = 350)
    String message;
    LocalDateTime timestamp;
    boolean read = false;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    Profile profile;
    
    
    public NotificationDTO toDto () {
        return new NotificationDTO(
          id,
          title,
          message,
          timestamp,
          profile.getId()
        );
    }
    
}
