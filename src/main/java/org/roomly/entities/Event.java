package org.roomly.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.roomly.dto.EventDTO;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Accessors(chain = true)
@SuppressWarnings("JpaDataSourceORMInspection")
public class Event {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    int id;
    String name;
    String description;
    LocalDateTime startTime;
    LocalDateTime endTime;
    
    @ManyToOne
    @JoinColumn(name = "household_id", nullable = false)
    Household household;
    
    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    Profile creator;
    
    @ManyToMany
    @JoinTable(
        name = "event_attendees",
        joinColumns = @JoinColumn(name = "event_id"),
        inverseJoinColumns = @JoinColumn(name = "profile_id")
    )
    List<Profile> attendees;
    
    public EventDTO toDTO () {
        return new EventDTO(
          id,
          name,
          description,
          startTime != null ? startTime.atOffset(ZoneOffset.UTC) : null,
          endTime != null ? endTime.atOffset(ZoneOffset.UTC) : null,
          household.getId(),
          creator.toDTO(),
          attendees != null ? attendees.stream().map(Profile::toDTO).toList() : List.of()
        );
    }
    
}
