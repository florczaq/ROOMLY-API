package org.roomly.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.roomly.entities.Household;
import org.roomly.entities.Profile;
import org.roomly.repositories.ProfileRepository;
import org.roomly.security.authentication.entities.Account;
import org.roomly.security.authentication.services.AuthenticationService;
import org.roomly.utils.AvatarsUtil;
import org.roomly.utils.ColorsUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for profile lifecycle management within a household.
 * <p>
 * Profiles are per-household identities for an {@link Account}. This service handles
 * creation, retrieval, update, and validation of profile attributes (nickname, avatar name,
 * avatar color) to maintain uniqueness constraints within a household.
 * </p>
 */
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
    @Transactional
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
    
    /**
     * Retrieves the profile of the currently authenticated user in the given household.
     * Throws an exception if no profile is found.
     */
    public Profile getCurrentlyAuthenticatedUserProfile (Household household) {
        Account account = authenticationService.getCurrentlyAuthenticatedAccount();
        return profileRepository.findByHouseholdAndAccount(household, account)
          .orElseThrow(() -> new RuntimeException("Profile not found for account: " + account.getId()));
    }
    
    /**
     * Validates that the user is not already a member of the household.
     */
    private void validateNotAlreadyMember (Account account, Household household) {
        if (profileRepository.existsByAccountAndHousehold(account, household)) {
            throw new IllegalArgumentException("User is already a member of this household");
        }
    }
    
    /**
     * Validates that the nickname is not already taken in the household.
     */
    private void validateNicknameAvailability (Household household, String nickname) {
        if (profileRepository.existsByHouseholdAndNickname(household, nickname)) {
            throw new IllegalArgumentException("Nickname is already taken in this household");
        }
    }
    
    /**
     * Validates that the household has not reached its members limit.
     */
    private void validateHouseholdCapacity (Household household) {
        if (profileRepository.countByHousehold(household) >= household.getMembersLimit()) {
            throw new IllegalStateException("Household has reached its members limit");
        }
    }
    
    /**
     * Validates that the combination of avatar name and color is available in the household.
     * This ensures that no two profiles in the same household can have the same avatar name or color.
     */
    private void validateAvatarCombinationAvailability (Household household,
      String avatarName,
      String avatarColorName
    ) {
        if (profileRepository.existsByHouseholdAndAvatarNameOrAvatarColorName(
          household, avatarName, avatarColorName)) {
            throw new IllegalArgumentException("Avatar name or color is already taken in this household");
        }
    }
    
    /**
     * Validates that the avatar color is valid.
     */
    private void validateAvatarColor (String avatarColorName) {
        if (!ColorsUtil.isValidColor(avatarColorName)) {
            throw new IllegalArgumentException("Invalid avatar color: " + avatarColorName);
        }
    }
    
    /**
     * Validates that the avatar name is valid.
     */
    private void validateAvatarName (String avatarName) {
        if (avatarName == null || avatarName.trim().isEmpty()) {
            throw new IllegalArgumentException("Avatar name cannot be empty");
        }
        if (!AvatarsUtil.isValidAvatarName(avatarName)) {
            throw new IllegalArgumentException("Invalid avatar name: " + avatarName);
        }
    }
    
    /**
     * Retrieves a profile by its ID.
     * Throws an exception if no profile is found.
     */
    public Profile getProfileById (String profileId) {
        return profileRepository.findById(profileId)
          .orElseThrow(() -> new IllegalArgumentException("Profile with id %s not found".formatted(profileId)));
    }
    
    /**
     * Partially updates a profile's nickname, avatar name, and/or avatar color.
     * Only non-null fields that differ from current values are applied and re-validated
     * for uniqueness within the household.
     *
     * @param profileId       ID of the profile to update
     * @param nickname        new nickname, or {@code null} to keep current
     * @param avatarName      new avatar name, or {@code null} to keep current
     * @param avatarColorName new avatar color name, or {@code null} to keep current
     * @return the updated and persisted {@link Profile}
     * @throws IllegalArgumentException if the profile is not found or a uniqueness constraint is violated
     */
    @Transactional
    public Profile updateProfile (String profileId, String nickname, String avatarName, String avatarColorName) {
        Profile profile = getProfileById(profileId);
        Household household = profile.getHousehold();
        
        boolean nicknameChanged = nickname != null && !nickname.equals(profile.getNickname());
        boolean avatarNameChanged = avatarName != null && !avatarName.equals(profile.getAvatarName());
        boolean avatarColorChanged = avatarColorName != null && !avatarColorName.equals(
          profile.getAvatarColorName());
        
        if (nicknameChanged) {
            validateNicknameAvailability(household, nickname);
        }
        
        if (avatarNameChanged) {
            validateAvatarName(avatarName);
        }
        
        if (avatarColorChanged) {
            validateAvatarColor(avatarColorName);
        }
        
        if (avatarNameChanged || avatarColorChanged) {
            String newAvatarName = avatarName != null ? avatarName : profile.getAvatarName();
            String newAvatarColor = avatarColorName != null ? avatarColorName : profile.getAvatarColorName();
            validateAvatarCombinationAvailability(household, newAvatarName, newAvatarColor);
        }
        
        if (nickname != null) {
            profile.setNickname(nickname);
        }
        if (avatarName != null) {
            profile.setAvatarName(avatarName);
        }
        if (avatarColorName != null) {
            profile.setAvatarColorName(avatarColorName);
        }
        
        return profileRepository.save(profile);
    }
}
