package org.roomly.resolvers;

import lombok.RequiredArgsConstructor;
import org.roomly.dto.HouseholdDTO;
import org.roomly.entities.Household;
import org.roomly.entities.Profile;
import org.roomly.services.HouseholdOrchestrationService;
import org.roomly.services.HouseholdService;
import org.roomly.services.ProfileService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class HouseholdResolver {
    private final HouseholdService householdService;
    private final HouseholdOrchestrationService householdOrchestrationService;
    private final ProfileService profileService;

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public HouseholdDTO household(@Argument(name = "householdId") String id, Authentication authentication) {
        Household household = householdService.getHousehold(id);
        Profile currentUserProfile = profileService.getCurrentlyAuthenticatedUserProfile(household, authentication);
        return household.toDTO(currentUserProfile);
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<HouseholdDTO> households(Authentication authentication) {
        List<Household> allHouseholds = householdService.getAllHouseholds(authentication.getName());
        List<String> householdIds = allHouseholds.
            stream()
            .map(Household::getId).toList();
        Map<String, Profile> profileByHousehold = profileService.getProfilesByHouseholdIds(householdIds, authentication.getName());
        return allHouseholds.stream()
            .map(h -> h.toDTO(profileByHousehold.get(h.getId())))
            .toList();
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public HouseholdDTO createHousehold(@Argument String name,
                                        @Argument int membersLimit,
                                        @Argument String nickname,
                                        @Argument String avatarName,
                                        @Argument String avatarColorName,
                                        Authentication authentication
    ) {
        return householdOrchestrationService.createHouseholdWithResources(
            name, membersLimit, nickname, avatarName, avatarColorName, authentication
        );
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public HouseholdDTO householdByJoinCode(@Argument String joinCode) {
        return householdService.getHouseHoldByJoinCode(joinCode).toDTO(null);
    }

    public String updateHousehold() {
        return "HouseholdResolver";
    }

    public String deleteHousehold() {
        return "HouseholdResolver";
    }

}
