package org.roomly.notifications.enums;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Notification
{
    @Id
    int id;
    String title;
    String message;
    String timestamp;
    boolean read;
}
