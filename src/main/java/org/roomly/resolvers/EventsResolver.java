package org.roomly.resolvers;

import lombok.RequiredArgsConstructor;
import org.roomly.services.EventsService;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class EventsResolver {
    private final EventsService eventsService;
    

}

