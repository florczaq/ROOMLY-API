package org.roomly.resolvers;


import lombok.RequiredArgsConstructor;
import org.roomly.dto.ProfileDTO;
import org.roomly.services.HouseholdOrchestrationService;
import org.roomly.services.ProfileService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ProfileResolver {
    private final HouseholdOrchestrationService householdOrchestrationService;
    private final ProfileService profileService;

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public ProfileDTO profile(@Argument String profileId) {
        return profileService.getProfileById(profileId).toDTO();
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public ProfileDTO currentUserProfile(Authentication authentication) {
        return profileService.getProfileById(authentication.getName()).toDTO();
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public ProfileDTO joinHousehold(
        @Argument String nickname,
        @Argument String avatarName,
        @Argument String avatarColorName,
        @Argument String joinCode
    ) {
        return householdOrchestrationService.addMemberToHousehold(
            nickname, avatarName, avatarColorName, joinCode
        ).toDTO();
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public ProfileDTO updateProfile(
        @Argument String profileId,
        @Argument String nickname,
        @Argument String avatarName,
        @Argument String avatarColorName
    ) {
        return profileService.updateProfile(profileId, nickname, avatarName, avatarColorName).toDTO();
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public boolean leaveHousehold(@Argument String profileId) {
        householdOrchestrationService.removeMemberFromHousehold(profileId);
        return true;
    }
}
