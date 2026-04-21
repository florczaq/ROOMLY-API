package org.roomly.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.roomly.dto.AvatarDTO;
import org.roomly.dto.UserDTO;
import org.roomly.entities.Household;
import org.roomly.entities.Profile;
import org.roomly.repositories.ProfileRepository;
import org.roomly.security.authentication.entities.Account;
import org.roomly.security.authentication.services.AuthenticationService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {
    private final ProfileRepository profileRepository;
    private final ColorsService colorsService;
    private final HouseholdService householdService;
    private final AuthenticationService authenticationService;
    private final ShoppingListService shoppingListService;
    private final InventoryService inventoryService;
    
    public UserDTO joinHousehold (String nickname, String avatarName, String avatarColorName, String joinCode) {
        Account account = getCurrentlyAuthenticatedAccount();
        log.info("User {} is attempting to join household with join code {}", account.getId(), joinCode);
        
        Household household = householdService.getHouseHoldByJoinCode(joinCode);
        log.info(
          "Account {} is trying to join household {} with nickname '{}', avatar name '{}' and avatar color '{}'",
          account.getId(), household.getId(), nickname, avatarName, avatarColorName
        );
        
        validateJoinHousehold(account, household, nickname, avatarName, avatarColorName);
        
        Profile savedProfile = createAndSaveUser(nickname, avatarName, avatarColorName, account, household);
        
        //Shopping list for new user
        shoppingListService.createShoppingList(savedProfile, household);
        //Inventory for new user
        inventoryService.createInventory(savedProfile, household);
        
        return buildUserDTO(savedProfile);
    }
    
    private Account getCurrentlyAuthenticatedAccount () {
        String accountId = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
          .map(Principal::getName)
          .orElseThrow(() -> new IllegalStateException("No authenticated user found"));
        
        return authenticationService.loadAccountById(accountId);
    }
    
    private void validateJoinHousehold (Account account,
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
        if (!colorsService.isValidColor(avatarColorName)) {
            throw new IllegalArgumentException("Invalid avatar color: " + avatarColorName);
        }
    }
    
    private void validateAvatarName (String avatarName) {
        //TODO validate by checking if the avatar name exists in the catalog of available avatars. For now, just check if it's not empty.
        if (avatarName == null || avatarName.trim().isEmpty()) {
            throw new IllegalArgumentException("Avatar name cannot be empty");
        }
    }
    
    private Profile createAndSaveUser (String nickname,
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
    
    private UserDTO buildUserDTO (Profile profile) {
        return new UserDTO(
          profile.getNickname(),
          new AvatarDTO(
            profile.getAvatarName(),
            profile.getAvatarColorName(),
            colorsService.getHexByColor(profile.getAvatarColorName())
          )
        );
    }
    
    
}
