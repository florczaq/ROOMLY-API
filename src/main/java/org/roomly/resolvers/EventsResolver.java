package org.roomly.resolvers;

import lombok.RequiredArgsConstructor;
import org.roomly.services.EventsService;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class EventsResolver {
    private final EventsService eventsService;
    
    public String events () {
        return "EventsResolver";
    }
    
    public String addEvents () {
        return "EventsResolver";
    }
    
    public String updateEvents () {
        return "EventsResolver";
    }
    
    public String deleteEvents () {
        return "EventsResolver";
    }
    
}

