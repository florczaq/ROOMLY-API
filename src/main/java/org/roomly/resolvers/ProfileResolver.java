package org.roomly.resolvers;


import lombok.RequiredArgsConstructor;
import org.roomly.dto.ProfileDTO;
import org.roomly.services.HouseholdOrchestrationService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ProfileResolver {
    private final HouseholdOrchestrationService householdOrchestrationService;
    
    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public ProfileDTO joinHousehold (
      @Argument String nickname,
      @Argument String avatarName,
      @Argument String avatarColorName,
      @Argument String joinCode
    ) {
        return householdOrchestrationService.addMemberToHousehold(
          nickname, avatarName, avatarColorName, joinCode
        );
    }
}
