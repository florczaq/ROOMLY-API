package org.roomly.services;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.roomly.annotations.Notifiable;
import org.roomly.entities.Event;
import org.roomly.entities.Profile;
import org.roomly.repositories.EventsRepository;
import org.roomly.repositories.HouseholdRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventsService {
    private final EventsRepository eventsRepository;
    private final HouseholdRepository householdRepository;
    
    public List<Event> getAllEvents (String householdId, LocalDateTime from, LocalDateTime to) {
        if (!householdRepository.existsById(householdId)) {
            throw new EntityNotFoundException("Household with id " + householdId + " not found");
        }
        return eventsRepository.findAllByHouseholdIdAndStartTimeBetween(householdId, from, to);
    }
    
    public Event getEventById (int eventId) {
        return eventsRepository.findById(eventId)
          .orElseThrow(() -> new EntityNotFoundException("Event with id " + eventId + " not found"));
    }
    
    public List<Event> getAllEventsForProfile (String profileId, LocalDateTime from, LocalDateTime to) {
        return eventsRepository.findAllByAttendees_IdAndStartTimeBetween(profileId, from, to);
    }
    
    public Event addEvent (String name, String description, LocalDateTime startTime, LocalDateTime endTime) {
        Event event = new Event()
          .setName(name)
          .setDescription(description)
          .setStartTime(startTime)
          .setEndTime(endTime);
        return eventsRepository.save(event);
    }
    
    public Event updateEvent (
      int eventId,
      String name,
      String description,
      LocalDateTime startTime,
      LocalDateTime endTime
    ) {
        Event event = this.getEventById(eventId);
        
        boolean nameChanged = name != null && !name.equals(event.getName());
        boolean descriptionChanged = description != null && !description.equals(event.getDescription());
        boolean startTimeChanged = startTime != null && !startTime.equals(event.getStartTime());
        boolean endTimeChanged = endTime != null && !endTime.equals(event.getEndTime());
        
        if (nameChanged) {
            event.setName(name);
        }
        if (descriptionChanged) {
            event.setDescription(description);
        }
        if (startTimeChanged) {
            event.setStartTime(startTime);
        }
        if (endTimeChanged) {
            event.setEndTime(endTime);
        }
        return eventsRepository.save(event);
    }
    
    public Boolean deleteEvent (int eventId) {
        Event event = this.getEventById(eventId);
        String accountId = this.getAuthenticatedUserId();
        
        if (event.getCreator().getAccount().getId().equals(accountId)) {
            eventsRepository.deleteById(eventId);
        } else {
            throw new SecurityException("User is not the creator of the event");
        }
        return true;
    }
    
    //TODO test
    @Notifiable(
      title = "You have been added to an event",
      description = "You have been added as an attendee to the event: #{#result.name}",
      recipientProfileId = "#{#profileId}"
    )
    public Event addAttendee (int eventId, String profileId) {
        Event event = this.getEventById(eventId);
        
        String accountId = this.getAuthenticatedUserId();
        
        if (event.getCreator().getAccount().getId().equals(accountId)) {
            if (event.getAttendees().stream().anyMatch(profile -> profile.getId().equals(profileId))) {
                throw new IllegalArgumentException(
                  "Profile with id %s is already an attendee of the event".formatted(profileId));
            }
            event.getAttendees().add(new Profile().setId(profileId));
            return eventsRepository.save(event);
        } else {
            throw new SecurityException("User is not the creator of the event");
        }
    }
    
    @Notifiable(
      title = "You have been removed from an event",
      description = "You have been removed as an attendee from the event: #{#result.name}",
      recipientProfileId = "#{#profileId}"
    )
    public Event removeAttendee (int eventId, String profileId) {
        Event event = this.getEventById(eventId);
        String accountId = this.getAuthenticatedUserId();
        
        
        if (event.getCreator().getAccount().getId().equals(accountId)) {
            if (event.getAttendees().stream().noneMatch(profile -> profile.getId().equals(profileId))) {
                throw new IllegalArgumentException(
                  "Profile with id %s is not an attendee of the event".formatted(profileId));
            }
            event.getAttendees().removeIf(profile -> profile.getId().equals(profileId));
            return eventsRepository.save(event);
        } else {
            throw new SecurityException("User is not the creator of the event");
        }
    }
    
    private String getAuthenticatedUserId () {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("User is not authenticated");
        }
        return authentication.getName();
    }
}

