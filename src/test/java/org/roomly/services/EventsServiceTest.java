package org.roomly.services;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.roomly.entities.Event;
import org.roomly.entities.Profile;
import org.roomly.repositories.EventsRepository;
import org.roomly.repositories.HouseholdRepository;
import org.roomly.security.authentication.entities.Account;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventsServiceTest {

    @Mock
    private EventsRepository eventsRepository;

    @Mock
    private HouseholdRepository householdRepository;

    @InjectMocks
    private EventsService eventsService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAllEventsReturnsEventsForExistingHouseholdWithinProvidedRange() {
        LocalDateTime from = LocalDateTime.now().minusDays(1);
        LocalDateTime to = LocalDateTime.now().plusDays(1);
        List<Event> expected = List.of(new Event().setId(1).setName("Dinner"));

        when(householdRepository.existsById("household-1")).thenReturn(true);
        when(eventsRepository.findAllByHouseholdIdAndStartTimeBetween("household-1", from, to)).thenReturn(expected);

        List<Event> result = eventsService.getAllEvents("household-1", from, to);

        assertEquals(expected, result);
    }

    @Test
    void getAllEventsThrowsWhenHouseholdDoesNotExist() {
        LocalDateTime from = LocalDateTime.now().minusDays(1);
        LocalDateTime to = LocalDateTime.now().plusDays(1);

        when(householdRepository.existsById("missing-household")).thenReturn(false);

        assertThrows(
          EntityNotFoundException.class,
          () -> eventsService.getAllEvents("missing-household", from, to)
        );
    }

    @Test
    void getEventByIdReturnsEventWhenPresent() {
        Event event = new Event().setId(11).setName("Movie night");

        when(eventsRepository.findById(11)).thenReturn(Optional.of(event));

        Event result = eventsService.getEventById(11);

        assertSame(event, result);
    }

    @Test
    void getEventByIdThrowsWhenEventDoesNotExist() {
        when(eventsRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> eventsService.getEventById(999));
    }

    @Test
    void getAllEventsForProfileReturnsEventsForAttendeeInRange() {
        LocalDateTime from = LocalDateTime.now().minusDays(3);
        LocalDateTime to = LocalDateTime.now().plusDays(3);
        List<Event> expected = List.of(new Event().setId(1), new Event().setId(2));

        when(eventsRepository.findAllByAttendees_IdAndStartTimeBetween("profile-1", from, to)).thenReturn(expected);

        List<Event> result = eventsService.getAllEventsForProfile("profile-1", from, to);

        assertEquals(expected, result);
    }

    @Test
    void addEventCreatesAndSavesEventWithProvidedValues() {
        LocalDateTime startTime = LocalDateTime.now().plusHours(2);
        LocalDateTime endTime = startTime.plusHours(1);
        Event persisted = new Event()
          .setId(5)
          .setName("Clean up")
          .setDescription("Kitchen and bathroom")
          .setStartTime(startTime)
          .setEndTime(endTime);

        when(eventsRepository.save(any(Event.class))).thenReturn(persisted);

        Event result = eventsService.addEvent("Clean up", "Kitchen and bathroom", startTime, endTime);

        assertSame(persisted, result);
    }

    @Test
    void updateEventUpdatesOnlyFieldsThatChangedAndAreNotNull() {
        LocalDateTime originalStart = LocalDateTime.now().plusDays(1);
        LocalDateTime originalEnd = originalStart.plusHours(1);
        LocalDateTime updatedStart = originalStart.plusHours(2);

        Event existing = new Event()
          .setId(7)
          .setName("Old name")
          .setDescription("Old description")
          .setStartTime(originalStart)
          .setEndTime(originalEnd);

        when(eventsRepository.findById(7)).thenReturn(Optional.of(existing));
        when(eventsRepository.save(existing)).thenReturn(existing);

        Event result = eventsService.updateEvent(
          7,
          "New name",
          null,
          updatedStart,
          originalEnd
        );

        assertEquals("New name", result.getName());
        assertEquals("Old description", result.getDescription());
        assertEquals(updatedStart, result.getStartTime());
        assertEquals(originalEnd, result.getEndTime());
    }

    @Test
    void updateEventKeepsValuesWhenProvidedValuesAreNullOrEqual() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusHours(1);

        Event existing = new Event()
          .setId(8)
          .setName("Board games")
          .setDescription("Friday evening")
          .setStartTime(start)
          .setEndTime(end);

        when(eventsRepository.findById(8)).thenReturn(Optional.of(existing));
        when(eventsRepository.save(existing)).thenReturn(existing);

        Event result = eventsService.updateEvent(
          8,
          "Board games",
          null,
          start,
          null
        );

        assertEquals("Board games", result.getName());
        assertEquals("Friday evening", result.getDescription());
        assertEquals(start, result.getStartTime());
        assertEquals(end, result.getEndTime());
    }

    @Test
    void updateEventThrowsWhenEventDoesNotExist() {
        when(eventsRepository.findById(404)).thenReturn(Optional.empty());

        assertThrows(
          EntityNotFoundException.class,
          () -> eventsService.updateEvent(404, "Name", "Desc", LocalDateTime.now(), LocalDateTime.now().plusHours(1))
        );
    }

    @Test
    void deleteEventDeletesEventWhenAuthenticatedUserIsCreator() {
        setAuthenticatedUser("account-1");
        Event event = eventWithCreator("account-1");

        when(eventsRepository.findById(12)).thenReturn(Optional.of(event));

        Boolean deleted = eventsService.deleteEvent(12);

        assertEquals(true, deleted);
        verify(eventsRepository).deleteById(12);
    }

    @Test
    void deleteEventThrowsWhenAuthenticatedUserIsNotCreator() {
        setAuthenticatedUser("account-2");
        Event event = eventWithCreator("account-1");

        when(eventsRepository.findById(12)).thenReturn(Optional.of(event));

        assertThrows(SecurityException.class, () -> eventsService.deleteEvent(12));
    }

    @Test
    void deleteEventThrowsWhenNoAuthenticatedUserExists() {
        Event event = eventWithCreator("account-1");
        when(eventsRepository.findById(12)).thenReturn(Optional.of(event));

        assertThrows(SecurityException.class, () -> eventsService.deleteEvent(12));
    }

    @Test
    void addAttendeeAddsProfileWhenAuthenticatedUserIsCreatorAndProfileIsNotPresent() {
        setAuthenticatedUser("account-1");
        Event event = eventWithCreator("account-1");
        event.setAttendees(new ArrayList<>(List.of(new Profile().setId("profile-1"))));

        when(eventsRepository.findById(21)).thenReturn(Optional.of(event));
        when(eventsRepository.save(event)).thenReturn(event);

        Event result = eventsService.addAttendee(21, "profile-2");

        assertEquals(2, result.getAttendees().size());
        assertEquals("profile-2", result.getAttendees().get(1).getId());
    }

    @Test
    void addAttendeeThrowsWhenProfileIsAlreadyAttendee() {
        setAuthenticatedUser("account-1");
        Event event = eventWithCreator("account-1");
        event.setAttendees(new ArrayList<>(List.of(new Profile().setId("profile-1"))));

        when(eventsRepository.findById(21)).thenReturn(Optional.of(event));

        assertThrows(IllegalArgumentException.class, () -> eventsService.addAttendee(21, "profile-1"));
    }

    @Test
    void addAttendeeThrowsWhenAuthenticatedUserIsNotCreator() {
        setAuthenticatedUser("account-2");
        Event event = eventWithCreator("account-1");
        event.setAttendees(new ArrayList<>(emptyList()));

        when(eventsRepository.findById(21)).thenReturn(Optional.of(event));

        assertThrows(SecurityException.class, () -> eventsService.addAttendee(21, "profile-1"));
    }

    @Test
    void removeAttendeeRemovesProfileWhenAuthenticatedUserIsCreator() {
        setAuthenticatedUser("account-1");
        Event event = eventWithCreator("account-1");
        event.setAttendees(new ArrayList<>(List.of(
          new Profile().setId("profile-1"),
          new Profile().setId("profile-2")
        )));

        when(eventsRepository.findById(31)).thenReturn(Optional.of(event));
        when(eventsRepository.save(event)).thenReturn(event);

        Event result = eventsService.removeAttendee(31, "profile-1");

        assertEquals(1, result.getAttendees().size());
        assertEquals("profile-2", result.getAttendees().get(0).getId());
    }

    @Test
    void removeAttendeeThrowsWhenProfileIsNotAttendee() {
        setAuthenticatedUser("account-1");
        Event event = eventWithCreator("account-1");
        event.setAttendees(new ArrayList<>(List.of(new Profile().setId("profile-1"))));

        when(eventsRepository.findById(31)).thenReturn(Optional.of(event));

        assertThrows(IllegalArgumentException.class, () -> eventsService.removeAttendee(31, "profile-9"));
    }

    @Test
    void removeAttendeeThrowsWhenAuthenticatedUserIsNotCreator() {
        setAuthenticatedUser("account-2");
        Event event = eventWithCreator("account-1");
        event.setAttendees(new ArrayList<>(List.of(new Profile().setId("profile-1"))));

        when(eventsRepository.findById(31)).thenReturn(Optional.of(event));

        assertThrows(SecurityException.class, () -> eventsService.removeAttendee(31, "profile-1"));
    }

    @Test
    void addAttendeeThrowsWhenUserIsNotAuthenticated() {
        Event event = eventWithCreator("account-1");
        event.setAttendees(new ArrayList<>(emptyList()));

        when(eventsRepository.findById(21)).thenReturn(Optional.of(event));

        assertThrows(SecurityException.class, () -> eventsService.addAttendee(21, "profile-1"));
    }

    @Test
    void removeAttendeeThrowsWhenUserIsNotAuthenticated() {
        Event event = eventWithCreator("account-1");
        event.setAttendees(new ArrayList<>(List.of(new Profile().setId("profile-1"))));

        when(eventsRepository.findById(31)).thenReturn(Optional.of(event));

        assertThrows(SecurityException.class, () -> eventsService.removeAttendee(31, "profile-1"));
    }

    private static Event eventWithCreator(String accountId) {
        Account creatorAccount = new Account().setId(accountId);
        Profile creator = new Profile().setId("creator-profile").setAccount(creatorAccount);
        return new Event().setId(1).setCreator(creator);
    }

    private static void setAuthenticatedUser(String accountId) {
        UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(accountId, null, emptyList());
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }
}

