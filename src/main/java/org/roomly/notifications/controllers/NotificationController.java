package org.roomly.notifications.controllers;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    @GetMapping("/{userId}")
    public String getNotificationsForUser (@PathVariable String userId) {
        //id, title, message, timestamp, read,
        // Placeholder implementation
        return "Notifications for user: " + userId;
    }
    
    @PostMapping("/markAsRead")
    public String markNotificationAsRead (@RequestBody List<Integer> notificationId) {
        // Placeholder implementation
        return "Marked notification " + notificationId + " as read.";
    }
}
