package org.roomly.dto;

import java.util.Optional;

public record HouseholdDTO(Optional<String> id, String name, String joinCode, int membersLimit) {
    
}
