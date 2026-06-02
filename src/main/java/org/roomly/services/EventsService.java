package org.roomly.services;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.roomly.annotations.Notifiable;
import org.roomly.entities.Event;
import org.roomly.entities.Household;
import org.roomly.entities.Profile;
import org.roomly.repositories.EventsRepository;
import org.roomly.repositories.HouseholdRepository;
import org.roomly.repositories.ProfileRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing household calendar events.
 * <p>
 * Events belong to a household and are created by a profile (the "creator").
 * Only the creator may update, delete, or manage the attendee list for an event.
 * Push notifications are sent to affected attendees when they are added or removed.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class EventsService {
    private final EventsRepository eventsRepository;
    private final HouseholdRepository householdRepository;
    private final ProfileRepository profileRepository;

    /**
     * Returns all events for a household whose start time falls within the given range.
     *
     * @param householdId ID of the household
     * @param from        range start (inclusive)
     * @param to          range end (inclusive)
     * @return list of matching events
     * @throws EntityNotFoundException if no household exists with the given ID
     */
    public List<Event> getAllEvents (String householdId, LocalDateTime from, LocalDateTime to) {
        if (!householdRepository.existsById(householdId)) {
            throw new EntityNotFoundException("Household with id " + householdId + " not found");
        }
        return eventsRepository.findAllByHouseholdIdAndStartTimeBetween(householdId, from, to);
    }
    
    /**
     * Returns a single event by ID.
     *
     * @param eventId ID of the event
     * @return the matching {@link Event}
     * @throws EntityNotFoundException if no event exists with the given ID
     */
    public Event getEventById (int eventId) {
        return eventsRepository.findById(eventId)
          .orElseThrow(() -> new EntityNotFoundException("Event with id " + eventId + " not found"));
    }
    
    /**
     * Returns all events a profile is attending whose start time falls within the given range.
     *
     * @param profileId ID of the profile (attendee)
     * @param from      range start (inclusive)
     * @param to        range end (inclusive)
     * @return list of events the profile attends in the given window
     */
    public List<Event> getAllEventsForProfile (String profileId, LocalDateTime from, LocalDateTime to) {
        return eventsRepository.findAllByAttendees_IdAndStartTimeBetween(profileId, from, to);
    }
    
    /**
     * Creates and persists a new event.
     *
     * @param name        display name of the event
     * @param description optional description
     * @param startTime   when the event starts
     * @param endTime     when the event ends
     * @return the persisted {@link Event}
     */
    @Notifiable(
      title = "New event: #{#result.name}",
      description = "#{#result.creator.nickname} invited you to '#{#result.name}'",
      recipientProfileIds = "#{#result.attendees.![id]}"
    )
    public Event addEvent (String householdId, String name, String description,
                           LocalDateTime startTime, LocalDateTime endTime,
                           List<String> attendeeIds, String accountId) {
        Household household = householdRepository.findById(householdId)
          .orElseThrow(() -> new EntityNotFoundException("Household with id " + householdId + " not found"));

        Profile creator = profileRepository.findByHouseholdIdAndAccountId(householdId, accountId)
          .orElseThrow(() -> new EntityNotFoundException("Profile not found for account in household " + householdId));

        List<Profile> attendees = attendeeIds != null && !attendeeIds.isEmpty()
          ? profileRepository.findAllById(attendeeIds)
          : new ArrayList<>();

        Event event = new Event()
          .setName(name)
          .setDescription(description)
          .setStartTime(startTime)
          .setEndTime(endTime)
          .setHousehold(household)
          .setCreator(creator)
          .setAttendees(attendees);
        return eventsRepository.save(event);
    }
    
    /**
     * Partially updates an existing event. Only non-null fields that differ from the
     * current values are applied; unchanged fields are left as-is.
     *
     * @param eventId     ID of the event to update
     * @param name        new name, or {@code null} to keep current
     * @param description new description, or {@code null} to keep current
     * @param startTime   new start time, or {@code null} to keep current
     * @param endTime     new end time, or {@code null} to keep current
     * @return the updated and persisted {@link Event}
     * @throws EntityNotFoundException if no event exists with the given ID
     */
    @Notifiable(
      title = "Event updated: #{#result.name}",
      description = "The event '#{#result.name}' has been updated",
      recipientProfileIds = "#{#result.attendees.![id]}"
    )
    public Event updateEvent (
      int eventId,
      String name,
      String description,
      LocalDateTime startTime,
      LocalDateTime endTime,
      List<String> attendeeIds
    ) {
        Event event = this.getEventById(eventId);

        if (name != null && !name.equals(event.getName())) event.setName(name);
        if (description != null && !description.equals(event.getDescription())) event.setDescription(description);
        if (startTime != null && !startTime.equals(event.getStartTime())) event.setStartTime(startTime);
        if (endTime != null && !endTime.equals(event.getEndTime())) event.setEndTime(endTime);
        if (attendeeIds != null) event.setAttendees(profileRepository.findAllById(attendeeIds));

        return eventsRepository.save(event);
    }
    
    /**
     * Deletes an event. Only the event's creator (matched by authenticated account ID) may delete it.
     *
     * @param eventId ID of the event to delete
     * @return {@code true} on success
     * @throws EntityNotFoundException if no event exists with the given ID
     * @throws SecurityException       if the authenticated user is not the event creator
     */
    public Boolean deleteEvent (int eventId, String accountId) {
        Event event = this.getEventById(eventId);

        if (event.getCreator().getAccount().getId().equals(accountId)) {
            eventsRepository.deleteById(eventId);
        } else {
            throw new SecurityException("User is not the creator of the event");
        }
        return true;
    }
    
    /**
     * Adds an attendee to an event. Only the event creator may perform this action.
     * Sends a push notification to the added profile.
     *
     * @param eventId   ID of the event
     * @param profileId ID of the profile to add
     * @param accountId account ID of the authenticated user (must be creator)
     * @return the updated {@link Event}
     * @throws EntityNotFoundException  if the event does not exist
     * @throws IllegalArgumentException if the profile is already an attendee
     * @throws SecurityException        if the authenticated user is not the event creator
     */
    @Notifiable(
      title = "You have been added to an event",
      description = "You have been added as an attendee to the event: #{#result.name}",
      recipientProfileId = "#{#profileId}"
    )
    public Event addAttendee (int eventId, String profileId, String accountId) {
        Event event = this.getEventById(eventId);

        if (!event.getCreator().getAccount().getId().equals(accountId)) {
            throw new SecurityException("User is not the creator of the event");
        }
        if (event.getAttendees().stream().anyMatch(p -> p.getId().equals(profileId))) {
            throw new IllegalArgumentException(
              "Profile with id %s is already an attendee of the event".formatted(profileId));
        }
        event.getAttendees().add(new Profile().setId(profileId));
        return eventsRepository.save(event);
    }

    /**
     * Removes an attendee from an event. Only the event creator may perform this action.
     * Sends a push notification to the removed profile.
     *
     * @param eventId   ID of the event
     * @param profileId ID of the profile to remove
     * @param accountId account ID of the authenticated user (must be creator)
     * @return the updated {@link Event}
     * @throws EntityNotFoundException  if the event does not exist
     * @throws IllegalArgumentException if the profile is not currently an attendee
     * @throws SecurityException        if the authenticated user is not the event creator
     */
    @Notifiable(
      title = "You have been removed from an event",
      description = "You have been removed as an attendee from the event: #{#result.name}",
      recipientProfileId = "#{#profileId}"
    )
    public Event removeAttendee (int eventId, String profileId, String accountId) {
        Event event = this.getEventById(eventId);

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
    
}

