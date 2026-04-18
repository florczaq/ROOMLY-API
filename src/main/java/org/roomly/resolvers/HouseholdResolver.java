package org.roomly.resolvers;

import lombok.RequiredArgsConstructor;
import org.roomly.dto.HouseholdDTO;
import org.roomly.services.HouseholdService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class HouseholdResolver {
    private final HouseholdService householdService;
    
    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public HouseholdDTO household (@Argument(name = "householdId") String id) {
        return householdService.getHousehold(id);
    }
    
    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public HouseholdDTO createHousehold (@Argument String name, @Argument int membersLimit) {
        return householdService.createHousehold(name, membersLimit);
    }
    
    public String updateHousehold () {
        return "HouseholdResolver";
    }
    
    public String deleteHousehold () {
        return "HouseholdResolver";
    }
    
}
