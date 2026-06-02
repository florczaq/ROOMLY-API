package org.roomly.services;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.roomly.annotations.Notifiable;
import org.roomly.dto.HouseholdDTO;
import org.roomly.dto.ProfileDTO;
import org.roomly.entities.Household;
import org.roomly.entities.Inventory;
import org.roomly.entities.Profile;
import org.roomly.entities.ShoppingList;
import org.roomly.exception.ConflictException;
import org.roomly.notifications.repositories.NotificationRepository;
import org.roomly.repositories.EventsRepository;
import org.roomly.repositories.HouseholdRepository;
import org.roomly.repositories.InventoryRepository;
import org.roomly.repositories.ProfileRepository;
import org.roomly.repositories.ShoppingListRepository;
import org.roomly.repositories.TransactionsRepository;
import org.roomly.security.authentication.entities.Account;
import org.roomly.security.authentication.services.AuthenticationService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * Orchestrates complex multi-service workflows that span multiple domains.
 * <p>
 * Handles household creation, member join/leave, and household deletion — operations
 * that require coordinating profiles, inventories, shopping lists, events, transactions,
 * and notifications within a single transaction boundary.
 * </p>
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
    private final EventsRepository eventsRepository;
    private final TransactionsRepository transactionsRepository;
    private final NotificationRepository notificationRepository;
    private final InventoryRepository inventoryRepository;
    private final ShoppingListRepository shoppingListRepository;
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
     * @param authentication  the authenticated user creating the household
     * @return DTO of the created household
     */
    @Transactional
    public HouseholdDTO createHouseholdWithResources (
      String name,
      int membersLimit,
      String nickname,
      String avatarName,
      String avatarColorName,
      Authentication authentication
    ) {
        var account = authenticationService.loadAccountById(authentication.getName());

        Profile ownerProfile = new Profile()
          .setAccount(account)
          .setNickname(nickname)
          .setAvatarName(avatarName)
          .setAvatarColorName(avatarColorName);

        // Profile must be saved first so it has an ID before being set as household owner
        ownerProfile = profileRepository.save(ownerProfile);

        Household household = new Household()
          .setId(householdService.generateNewHouseholdId())
          .setName(name)
          .setMembersLimit(membersLimit)
          .setJoinCode(householdService.generateNewJoinCode())
          .setOwner(ownerProfile);

        household = householdRepository.save(household);

        ownerProfile.setHousehold(household);
        ownerProfile = profileRepository.save(ownerProfile);

        var sharedShoppingList = shoppingListService.createShoppingList(null, household);
        var sharedInventory = inventoryService.createInventory(null);

        household.setSharedShoppingList(sharedShoppingList);
        household.setSharedInventory(sharedInventory);
        household = householdRepository.save(household);

        createAndAssignPersonalResources(ownerProfile, household);

        entityManager.flush();
        
        log.info("Created household {} with owner {}", household.getId(), ownerProfile.getId());
        return household.toDTO(ownerProfile);
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
     * @param authentication  the authenticated user joining the household
     * @return the newly created {@link Profile}
     * @throws ConflictException if the user is already a member, the nickname is taken,
     *                           the avatar combination is unavailable, or the household is at capacity
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
      String joinCode,
      Authentication authentication
    ) {
        Account account = authenticationService.loadAccountById(authentication.getName());
        log.info("User {} is attempting to join household with join code {}", account.getId(), joinCode);
        
        Household household = householdService.getHouseHoldByJoinCode(joinCode);
        log.info(
          "Account {} is trying to join household {} with nickname '{}', avatar name '{}' and avatar color '{}'",
          account.getId(), household.getId(), nickname, avatarName, avatarColorName
        );
        
        profileService.validateJoinHousehold(account, household, nickname, avatarName, avatarColorName);

        Profile savedProfile = profileService.createProfile(
          nickname, avatarName, avatarColorName, account, household);

        createAndAssignPersonalResources(savedProfile, household);

        entityManager.flush();
        
        log.info("Added member {} to household {}", savedProfile.getId(), household.getId());
        return savedProfile;
    }
    
    /**
     * Deletes a household and all associated resources. Only the household owner may perform this.
     * Deletion order respects FK constraints: notifications → transactions → events →
     * household (cascades to all member profiles and their personal resources) → shared resources.
     *
     * @param householdId ID of the household to delete
     * @param accountId   ID of the authenticated account
     * @return {@code true} on success
     * @throws SecurityException if the authenticated user is not the household owner
     */
    @Transactional
    public boolean deleteHousehold (String householdId, String accountId) {
        Household household = householdService.getHousehold(householdId);

        if (!household.getOwner().getAccount().getId().equals(accountId)) {
            throw new SecurityException("Only the household owner can delete the household");
        }

        Inventory sharedInventory = household.getSharedInventory();
        ShoppingList sharedShoppingList = household.getSharedShoppingList();

        notificationRepository.deleteAllByHouseholdId(householdId);
        transactionsRepository.deleteAllByHouseholdId(householdId);
        eventsRepository.deleteAllByHouseholdId(householdId);

        household.setOwner(null);
        household.setSharedInventory(null);
        household.setSharedShoppingList(null);
        householdRepository.save(household);

        householdRepository.delete(household);

        if (sharedInventory != null) inventoryRepository.deleteById(sharedInventory.getId());
        if (sharedShoppingList != null) shoppingListRepository.deleteById(sharedShoppingList.getId());

        log.info("Deleted household {}", householdId);
        return true;
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
     * @param profileId      ID of the profile to remove
     * @param authentication the authenticated user performing the removal
     * @return DTO of the removed profile (used by {@code @Notifiable} for notification data)
     * @throws SecurityException if the profile does not belong to the authenticated user
     */
    @Transactional
    @Notifiable(
      title = "Household Member Left",
      description = "#{#result.nickname} has left your household ",
      recipientProfileId = "#{#result.household.owner.id}"
    )
    @SuppressWarnings("UnusedReturnValue")
    public ProfileDTO removeMemberFromHousehold (String profileId, Authentication authentication) {
        Account account = authenticationService.loadAccountById(authentication.getName());
        log.info("User {} is attempting to leave household with profile ID {}", account.getId(), profileId);
        
        Profile profile = profileService.getProfileById(profileId);

        if (!profile.getAccount().getId().equals(account.getId())) {
            throw new IllegalArgumentException("Profile does not belong to the currently authenticated user");
        }

        Household household = profile.getHousehold();
        if (household.getOwner() != null && household.getOwner().getId().equals(profile.getId())) {
            throw new IllegalArgumentException("Household owner cannot leave the household. Delete the household or transfer ownership first.");
        }

        householdService.removeMemberFromHousehold(profile);
        log.info("Removed member {} from household {}", profile.getId(), profile.getHousehold().getId());
        return profile.toDTO();
    }
}

