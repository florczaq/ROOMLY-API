package org.roomly.repositories;

import org.roomly.entities.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventsRepository extends JpaRepository<Event, Integer> {
    List<Event> findAllByHouseholdIdAndStartTimeBetween (String householdId,
      LocalDateTime startTimeAfter,
      LocalDateTime startTimeBefore
    );
    
    @Query("SELECT e FROM Event e JOIN e.attendees a WHERE a.id = :attendeeId AND e.startTime BETWEEN :startTimeAfter AND :startTimeBefore")
    List<Event> findAllByAttendees_IdAndStartTimeBetween(
      @Param("attendeeId") String attendeeId,
      @Param("startTimeAfter") LocalDateTime startTimeAfter,
      @Param("startTimeBefore") LocalDateTime startTimeBefore
    );
    
}

