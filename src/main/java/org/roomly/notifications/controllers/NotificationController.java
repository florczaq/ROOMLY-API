package org.roomly.notifications.controllers;

import lombok.RequiredArgsConstructor;
import org.roomly.notifications.dto.NotificationDTO;
import org.roomly.notifications.entities.Notification;
import org.roomly.notifications.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;
    
    @GetMapping
    public List<NotificationDTO> getNotificationsForAccount () {
        return notificationService
          .getNotificationsForAccount()
          .stream()
          .map(Notification::toDto)
          .toList();
    }
    
    @PostMapping("/markAsRead")
    public void markNotificationAsRead (@RequestBody List<String> notificationId) {
        notificationService.markNotificationsAsRead(notificationId);
    }
}
