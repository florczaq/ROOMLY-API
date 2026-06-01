package org.roomly.resolvers;

import lombok.RequiredArgsConstructor;
import org.roomly.dto.EventDTO;
import org.roomly.entities.Event;
import org.roomly.services.EventsService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class EventsResolver {
    private final EventsService eventsService;
    
    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<EventDTO> events (@Argument String householdId,
      @Argument LocalDateTime from,
      @Argument LocalDateTime to
    ) {
        return eventsService
          .getAllEvents(householdId, from, to)
          .stream()
          .map(Event::toDTO)
          .toList();
    }
    
    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public EventDTO event (@Argument int eventId) {
        return eventsService.getEventById(eventId).toDTO();
    }
    
    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<EventDTO> eventsForProfile (@Argument String profileId,
      @Argument LocalDateTime from,
      @Argument LocalDateTime to
    ) {
        return eventsService.getAllEventsForProfile(profileId, from, to).stream().map(Event::toDTO).toList();
    }
    
    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public EventDTO addEvent (@Argument String name,
      @Argument String description,
      @Argument LocalDateTime startTime,
      @Argument LocalDateTime endTime
    ) {
        return eventsService.addEvent(name, description, startTime, endTime).toDTO();
    }
    
    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public EventDTO updateEvent (@Argument int eventId,
      @Argument String name,
      @Argument String description,
      @Argument LocalDateTime startTime,
      @Argument LocalDateTime endTime
    ) {
        return eventsService.updateEvent(eventId, name, description, startTime, endTime).toDTO();
    }
    
    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public Boolean deleteEvent (@Argument int eventId) {
        return eventsService.deleteEvent(eventId);
    }
    
}

