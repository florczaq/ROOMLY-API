package org.roomly.resolvers;

import lombok.RequiredArgsConstructor;
import org.roomly.services.HouseholdService;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class HouseholdResolver {
    private final HouseholdService householdService;
    
    @QueryMapping
    public String household () {
        return "HouseholdResolver";
    }
    
    public String addHousehold () {
        return "HouseholdResolver";
    }
    
    public String updateHousehold () {
        return "HouseholdResolver";
    }
    
    public String deleteHousehold () {
        return "HouseholdResolver";
    }
    
}
