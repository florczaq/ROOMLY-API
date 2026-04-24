package org.roomly.resolvers;

import lombok.RequiredArgsConstructor;
import org.roomly.dto.HouseholdDTO;
import org.roomly.services.HouseholdOrchestrationService;
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
    private final HouseholdOrchestrationService householdOrchestrationService;
    
    //TODO temporary for testing, delete in production
    @QueryMapping
    public String householdInfo (@Argument(name = "householdId") String id) {
        return householdService.getHouseholdInfoTest(id);
    }
    
    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public HouseholdDTO household (@Argument(name = "householdId") String id) {
        return householdService.getHousehold(id).toDTO();
    }
    
    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public HouseholdDTO createHousehold (@Argument String name,
      @Argument int membersLimit,
      @Argument String nickname,
      @Argument String avatarName,
      @Argument String avatarColorName
    ) {
        return householdOrchestrationService.createHouseholdWithResources(
          name, membersLimit, nickname, avatarName, avatarColorName
        );
    }
    
    public String updateHousehold () {
        return "HouseholdResolver";
    }
    
    public String deleteHousehold () {
        return "HouseholdResolver";
    }
    
}
