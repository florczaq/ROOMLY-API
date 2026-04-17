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
}
