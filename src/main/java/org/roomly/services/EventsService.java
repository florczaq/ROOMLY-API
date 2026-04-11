package org.roomly.services;

import lombok.RequiredArgsConstructor;
import org.roomly.repositories.EventsRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventsService {
    private final EventsRepository eventsRepository;
    
    public String getEvents () {
        return "EventsService";
    }
    
    public String addEvents () {
        return "EventsService";
    }
    
    public String updateEvents () {
        return "EventsService";
    }
    
    public String deleteEvents () {
        return "EventsService";
    }
    
}

