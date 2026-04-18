package org.roomly.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.roomly.dto.HouseholdDTO;

import java.util.Optional;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Accessors(chain = true)
public class Household {
    @Id
    String id;
    String name;
    
    @Column(unique = true, nullable = false, length = 6)
    String joinCode;
    
    @Min(1)
    @Max(30)
    @Column(nullable = false)
    int membersLimit;
    @Column(nullable = false)
    String ownerId;
    
    @Override
    public String toString () {
        return """
               \nHousehold {
                    id: %s,
                    name: %s,
                    joinCode: %s,
                    membersLimit: %d,
                    ownerId: %s
               """.formatted(id, name, joinCode, membersLimit, ownerId);
    }
    
    public HouseholdDTO toDTO () {
        return new HouseholdDTO(Optional.ofNullable(id), name, joinCode, membersLimit);
    }
    
}
