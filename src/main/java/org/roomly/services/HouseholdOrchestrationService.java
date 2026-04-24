package org.roomly.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.roomly.dto.HouseholdDTO;
import org.roomly.dto.ProfileDTO;
import org.roomly.entities.Household;
import org.roomly.entities.Profile;
import org.roomly.repositories.HouseholdRepository;
import org.roomly.repositories.ProfileRepository;
import org.roomly.security.authentication.entities.Account;
import org.roomly.security.authentication.services.AuthenticationService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Orchestrates complex multi-service workflows that span multiple domains.
 * Keeps individual services focused on single responsibilities while
 * managing coordination and transactional boundaries.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HouseholdOrchestrationService {
    private final HouseholdService householdService;
    private final ProfileService profileService;
    private final ShoppingListService shoppingListService;
    private final InventoryService inventoryService;
    private final AuthenticationService authenticationService;
    private final HouseholdRepository householdRepository;
    private final ProfileRepository profileRepository;
    
    /**
     * Creates a new household with owner profile and all necessary resources.
     * This is an atomic operation - all steps succeed or all fail together.
     */
    @Transactional
    public HouseholdDTO createHouseholdWithResources(
      String name,
      int membersLimit,
      String nickname,
      String avatarName,
      String avatarColorName
    ) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User must be authenticated to create a household");
        }
        
        var account = authenticationService.loadAccountById(authentication.getName());
        
        Profile ownerProfile = profileRepository.save(
          new Profile()
            .setAccount(account)
            .setNickname(nickname)
            .setAvatarName(avatarName)
            .setAvatarColorName(avatarColorName)
        );
        
        Household household = householdRepository.save(
          new Household()
            .setId(householdService.generateNewHouseholdId())
            .setName(name)
            .setMembersLimit(membersLimit)
            .setJoinCode(householdService.generateNewJoinCode())
            .setOwner(ownerProfile)
        );
        
        ownerProfile.setHousehold(household);
        profileRepository.save(ownerProfile);
        
        shoppingListService.createShoppingList(null, household);
        inventoryService.createInventory(null, household);
        
        shoppingListService.createShoppingList(ownerProfile, household);
        inventoryService.createInventory(ownerProfile, household);
        
        log.info("Created household {} with owner {}", household.getId(), ownerProfile.getId());
        return household.toDTO();
    }
    
    /**
     * Adds a new member to an existing household with validation and resource creation.
     * This is an atomic operation.
     */
    @Transactional
    public ProfileDTO addMemberToHousehold(
      String nickname,
      String avatarName,
      String avatarColorName,
      String joinCode
    ) {
        Account account = authenticationService.getCurrentlyAuthenticatedAccount();
        log.info("User {} is attempting to join household with join code {}", account.getId(), joinCode);
        
        Household household = householdService.getHouseHoldByJoinCode(joinCode);
        log.info(
          "Account {} is trying to join household {} with nickname '{}', avatar name '{}' and avatar color '{}'",
          account.getId(), household.getId(), nickname, avatarName, avatarColorName
        );
        
        // Validate
        profileService.validateJoinHousehold(account, household, nickname, avatarName, avatarColorName);
        
        // Create profile
        Profile savedProfile = profileService.createProfile(nickname, avatarName, avatarColorName, account, household);
        
        // Create resources for new member
        shoppingListService.createShoppingList(savedProfile, household);
        inventoryService.createInventory(savedProfile, household);
        
        log.info("Added member {} to household {}", savedProfile.getId(), household.getId());
        return savedProfile.toDTO();
    }
}

