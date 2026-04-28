package org.roomly.notifications.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.NonUniqueResultException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.roomly.enums.CodeCharacters;
import org.roomly.generators.GeneratedCodeFactory;
import org.roomly.notifications.entities.Notification;
import org.roomly.notifications.repositories.NotificationRepository;
import org.roomly.repositories.ProfileRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final ProfileRepository profileRepository;
    
    @Transactional
    public void createAndSaveNotification (@NotNull @NotBlank String title,
      @NotNull @NotBlank String message,
      String profileId
    ) {
        var profile = profileRepository
          .findProfileById(profileId)
          .orElseThrow(() -> new EntityNotFoundException("Profile not found with id: %s".formatted(profileId)));
        
        // Generate a unique ID for the notification.
        // Set max attempts to prevent infinite loop in case of an unlikely collision
        int maxAttempts = 100;
        int attempts = 0;
        StringBuilder id = new StringBuilder();
        do {
            if (attempts > maxAttempts) {
                throw new NonUniqueResultException(
                  "Failed to generate unique notification ID after %d attempts".formatted(maxAttempts));
            }
            id.setLength(0);
            id.append(GeneratedCodeFactory.generate(4, CodeCharacters.LOWERCASE_LETTERS_AND_DIGITS));
            attempts++;
        } while (notificationRepository.existsById(id.toString()));
        
        notificationRepository.save(new Notification()
          .setId(id.toString())
          .setTitle(title)
          .setMessage(message)
          .setProfile(profile)
          .setTimestamp(java.time.LocalDateTime.now())
          .setRead(false));
    }
    
    public List<Notification> getNotificationsForAccount () {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("User is not authenticated");
        }
        return notificationRepository.findAllByAccountIdUnreadAndOrderFromOldest(authentication.getName());
    }
    
    @Transactional
    public void markNotificationsAsRead (List<String> notificationId) {
        notificationRepository.saveAll(
          notificationRepository
            .findAllById(notificationId)
            .stream()
            .map(n -> n.setRead(true))
            .toList()
        );
    }
}
