package org.roomly.services;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.roomly.annotations.Notifiable;
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
 * Orchestrates complex multiservice workflows that span multiple domains.
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
    private final EntityManager entityManager;
    
    /**
     * Creates a new household with an owner profile and all associated shared/personal resources.
     * <p>
     * Steps performed atomically: create owner profile → create household → link profile to
     * household → create shared shopping list and inventory → create owner's personal shopping
     * list and inventory. Returns a DTO of the fully populated household.
     * </p>
     *
     * @param name            display name for the household
     * @param membersLimit    maximum number of members allowed
     * @param nickname        owner's in-household nickname
     * @param avatarName      owner's avatar name
     * @param avatarColorName owner's avatar color name
     * @return DTO of the created household
     * @throws IllegalStateException if the user is not authenticated
     */
    @Transactional
    public HouseholdDTO createHouseholdWithResources (
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
        
        // Create owner profile (without saving yet)
        Profile ownerProfile = new Profile()
          .setAccount(account)
          .setNickname(nickname)
          .setAvatarName(avatarName)
          .setAvatarColorName(avatarColorName);
        
        // Save profile first to get an ID
        ownerProfile = profileRepository.save(ownerProfile);
        
        // Create household (without saving yet)
        Household household = new Household()
      .setId(householdService.generateNewHouseholdId())
          .setName(name)
          .setMembersLimit(membersLimit)
          .setJoinCode(householdService.generateNewJoinCode())
          .setOwner(ownerProfile);
        
        // Save household to get it persisted (needed before creating resources)
        household = householdRepository.save(household);
        
        // Update owner profile with household reference and save to persist the relationship
        ownerProfile.setHousehold(household);
        ownerProfile = profileRepository.save(ownerProfile);
        
        // Create shared resources
        var sharedShoppingList = shoppingListService.createShoppingList(null, household);
        var sharedInventory = inventoryService.createInventory(null);
        
        // Assign shared resources to household and save once with all updates
        household.setSharedShoppingList(sharedShoppingList);
        household.setSharedInventory(sharedInventory);
        household = householdRepository.save(household);
        
        // Create owner's personal resources and assign to profile (saves profile with all updates)
        createAndAssignPersonalResources(ownerProfile, household);
        
        // Flush to ensure all changes are persisted before returning DTO
        entityManager.flush();
        
        log.info("Created household {} with owner {}", household.getId(), ownerProfile.getId());
        return household.toDTO();
    }
    
    /**
     * Joins an existing household using a join code, creating a profile and personal resources
     * for the authenticated user. Validates household capacity and nickname/avatar uniqueness
     * before persisting. Sends a push notification to the household owner on success.
     *
     * @param nickname        the new member's in-household nickname
     * @param avatarName      the new member's avatar name
     * @param avatarColorName the new member's avatar color name
     * @param joinCode        the household join code
     * @return the newly created {@link Profile}
     * @throws IllegalArgumentException if validation fails (already a member, nickname taken, etc.)
     * @throws IllegalStateException    if the household is at capacity
     */
    @Notifiable(
      title = "New Household Member",
      description = "#{#result.nickname} has joined your household ",
      recipientProfileId = "#{#result.household.owner.id}"
    )
    @Transactional
    public Profile addMemberToHousehold (
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
        Profile savedProfile = profileService.createProfile(
          nickname, avatarName, avatarColorName, account, household);
        
        // Create resources for new member and assign them
        createAndAssignPersonalResources(savedProfile, household);
        
        // Flush to ensure all changes are persisted before returning DTO
        entityManager.flush();
        
        log.info("Added member {} to household {}", savedProfile.getId(), household.getId());
        return savedProfile;
    }
    
    /**
     * Creates a personal shopping list and inventory for the given profile and persists
     * the updated profile. Used for both household creation and member join flows.
     *
     * @param profile   the profile to assign resources to
     * @param household the household the profile belongs to
     */
    @Transactional
    protected void createAndAssignPersonalResources (Profile profile, Household household) {
        var shoppingList = shoppingListService.createShoppingList(profile, household);
        var inventory = inventoryService.createInventory(profile);
        
        profile.setShoppingList(shoppingList);
        profile.setInventory(inventory);
        profileRepository.save(profile);
    }
    
    /**
     * Removes the profile with the given ID from its household.
     * <p>
     * The authenticated user must own the profile being removed. Sends a push notification
     * to the household owner on success.
     * </p>
     *
     * @param profileId ID of the profile to remove
     * @return DTO of the removed profile (used by {@code @Notifiable} for notification data)
     * @throws IllegalArgumentException if the profile does not belong to the authenticated user
     */
    @Transactional
    @Notifiable(
      title = "Household Member Left",
      description = "#{#result.nickname} has left your household ",
      recipientProfileId = "#{#result.household.owner.id}"
    )
    @SuppressWarnings("UnusedReturnValue")
    public ProfileDTO removeMemberFromHousehold (String profileId) {
        Account account = authenticationService.getCurrentlyAuthenticatedAccount();
        log.info("User {} is attempting to leave household with profile ID {}", account.getId(), profileId);
        
        Profile profile = profileService.getProfileById(profileId);
        
        if (!profile.getAccount().getId().equals(account.getId())) {
            throw new IllegalArgumentException("Profile does not belong to the currently authenticated user");
        }
        
        householdService.removeMemberFromHousehold(profile);
        log.info("Removed member {} from household {}", profile.getId(), profile.getHousehold().getId());
        return profile.toDTO();
    }
}

