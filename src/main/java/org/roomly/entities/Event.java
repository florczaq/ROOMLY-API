package org.roomly.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Accessors(chain = true)
public class Event {
    @Id
    int id;
    String name;
    String description;
    LocalDateTime startTime;
    LocalDateTime endTime;
    String householdId;
    String createdBy;
    //TODO joining table for attendees
}
