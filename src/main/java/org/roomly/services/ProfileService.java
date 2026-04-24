package org.roomly.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.roomly.entities.Household;
import org.roomly.entities.Profile;
import org.roomly.repositories.ProfileRepository;
import org.roomly.security.authentication.entities.Account;
import org.roomly.security.authentication.services.AuthenticationService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {
    private final ProfileRepository profileRepository;
    private final AuthenticationService authenticationService;
    
    /**
     * Validates that a user can join a household with the given parameters.
     * Throws exceptions if validation fails.
     */
    public void validateJoinHousehold (Account account,
      Household household,
      String nickname,
      String avatarName,
      String avatarColorName
    ) {
        validateNotAlreadyMember(account, household);
        validateNicknameAvailability(household, nickname);
        validateHouseholdCapacity(household);
        validateAvatarCombinationAvailability(household, avatarName, avatarColorName);
        validateAvatarColor(avatarColorName);
        validateAvatarName(avatarName);
    }
    
    /**
     * Creates and saves a new profile.
     */
    public Profile createProfile (String nickname,
      String avatarName,
      String avatarColorName,
      Account account,
      Household household
    ) {
        return profileRepository.save(
          new Profile()
            .setNickname(nickname)
            .setAccount(account)
            .setHousehold(household)
            .setAvatarName(avatarName)
            .setAvatarColorName(avatarColorName));
    }
    
    public Profile getCurrentlyAuthenticatedUserProfile (Household household) {
        Account account = authenticationService.getCurrentlyAuthenticatedAccount();
        return profileRepository.findByHouseholdAndAccount(household, account)
          .orElseThrow(() -> new RuntimeException("Profile not found for account: " + account.getId()));
    }
    
    private void validateNotAlreadyMember (Account account, Household household) {
        if (profileRepository.existsByAccountAndHousehold(account, household)) {
            throw new IllegalArgumentException("User is already a member of this household");
        }
    }
    
    private void validateNicknameAvailability (Household household, String nickname) {
        if (profileRepository.existsByHouseholdAndNickname(household, nickname)) {
            throw new IllegalArgumentException("Nickname is already taken in this household");
        }
    }
    
    private void validateHouseholdCapacity (Household household) {
        if (profileRepository.countByHousehold(household) >= household.getMembersLimit()) {
            throw new IllegalStateException("Household has reached its members limit");
        }
    }
    
    private void validateAvatarCombinationAvailability (Household household,
      String avatarName,
      String avatarColorName
    ) {
        if (profileRepository.existsByHouseholdAndAvatarNameAndAvatarColorName(
          household, avatarName, avatarColorName)) {
            throw new IllegalArgumentException("Avatar name or color is already taken in this household");
        }
    }
    
    private void validateAvatarColor (String avatarColorName) {
        if (!ColorsService.isValidColor(avatarColorName)) {
            throw new IllegalArgumentException("Invalid avatar color: " + avatarColorName);
        }
    }
    
    private void validateAvatarName (String avatarName) {
        //TODO validate by checking if the avatar name exists in the catalog of available avatars. For now, just check if it's not empty.
        if (avatarName == null || avatarName.trim().isEmpty()) {
            throw new IllegalArgumentException("Avatar name cannot be empty");
        }
    }
    
    
}
