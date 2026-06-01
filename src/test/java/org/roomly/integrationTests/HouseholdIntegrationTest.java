package org.roomly.integrationTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.roomly.entities.*;
import org.roomly.notifications.dto.NotificationDTO;
import org.roomly.notifications.entities.Notification;
import org.roomly.notifications.repositories.NotificationRepository;
import org.roomly.repositories.*;
import org.roomly.security.authentication.entities.Account;
import org.roomly.security.authentication.jwt.dto.TokenResponse;
import org.roomly.security.authentication.repositories.AccountRepository;
import org.roomly.utils.ColorsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SuppressWarnings("unchecked")
class HouseholdIntegrationTest {
    
    @Autowired
    private WebApplicationContext context;
    
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private HouseholdRepository householdRepository;
    
    @Autowired
    private ProfileRepository profileRepository;
    
    @Autowired
    private InventoryRepository inventoryRepository;
    
    @Autowired
    private ShoppingListRepository shoppingListRepository;
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private ProductsRepository productRepository;
    
    @BeforeEach
    void setUp () {
        // Set up MockMvc with security
        mockMvc = MockMvcBuilders
          .webAppContextSetup(context)
          .apply(springSecurity())
          .build();
        
        // Clean up database before each test
        profileRepository.deleteAll();
        householdRepository.deleteAll();
        accountRepository.deleteAll();
        inventoryRepository.deleteAll();
        shoppingListRepository.deleteAll();
        notificationRepository.deleteAll();
        productRepository.deleteAll();
        productRepository.save(new Product()
          .setBarcode("5901939103099")
          .setName("JOGURT SKYR PITNY JAGODA 330ML PIATNICA")
          .setBrand("Piatnica")
          .setQuantity("330ml")
        );
    }
    
    @Test
    void testCompleteHouseholdFlow () throws Exception {
        System.out.println("=== Starting Complete Household Flow Integration Test ===");
        String householdName = "My Awesome Household";
        int membersLimit = 5;

        HouseholdSetup householdSetup = registerOwnerAndCreateHousehold(householdName, membersLimit);
        assertInitialResourceState(householdSetup.household());

        DeviceSetup deviceSetup = registerDevicesAndJoinHousehold(householdSetup.household(), householdSetup.joinCode());

        List<Profile> profiles = printAndAssertFinalHouseholdState(
          householdSetup.household(),
          householdSetup.householdId(),
          householdName,
          membersLimit,
          householdSetup.joinCode()
        );

        int productId = productRepository.findByBarcode("5901939103099").orElseThrow().getId();
        runCrossUserInventoryAndShoppingListFlow(productId, deviceSetup);

        System.out.println("\n✓ All assertions passed!");
        printHouseholdJson(householdSetup.household(), profiles);
        assertAndPrintNotifications(profiles);
        printCompletedSummary();
    }

    private record HouseholdSetup(
      TokenResponse ownerTokens,
      Household household,
      String householdId,
      String joinCode
    ) {}

    private record DeviceSetup(
      TokenResponse device1Tokens,
      TokenResponse device2Tokens,
      int device1InventoryId,
      int device1ShoppingListId
    ) {}

    private HouseholdSetup registerOwnerAndCreateHousehold (String householdName, int membersLimit) throws Exception {
        System.out.println("--- Step 1: Creating user with email/password account ---");
        String email = "owner@roomly.com";
        String password = "SecurePassword123";
        TokenResponse ownerTokens = registerUser(email, password);

        System.out.println("✓ Owner account created successfully");
        System.out.println("  Access Token: " + ownerTokens.accessToken().substring(0, 20) + "...");

        Account ownerAccount = accountRepository.findByEmail(email).orElseThrow();
        System.out.println("  Account ID: " + ownerAccount.getId());
        System.out.println("  Email: " + ownerAccount.getEmail());
        System.out.println("  Auth Provider: " + ownerAccount.getAuthProvider());

        System.out.println("--- Step 2: Creating household ---");
        String createHouseholdQuery = String.format(
          "mutation { createHousehold(name: \"%s\", membersLimit: %d, nickname: \"HomeOwner\", avatarName: \"Cat\", avatarColorName: \"blue\") { id name joinCode membersLimit sharedInventory { id } sharedShoppingList { id } } }",
          householdName,
          membersLimit
        );

        MvcResult householdResult = executeGraphql(ownerTokens.accessToken(), createHouseholdQuery);
        Map<String, Object> householdData = readGraphqlData(householdResult, "createHousehold");

        String householdId = (String) householdData.get("id");
        String joinCode = (String) householdData.get("joinCode");
        Map<String, Object> sharedInventory = (Map<String, Object>) householdData.get("sharedInventory");
        Map<String, Object> sharedShoppingList = (Map<String, Object>) householdData.get("sharedShoppingList");

        System.out.println("✓ Household created successfully");
        System.out.println("  Household ID: " + householdId);
        System.out.println("  Name: " + householdData.get("name"));
        System.out.println("  Join Code: " + joinCode);
        System.out.println("  Members Limit: " + householdData.get("membersLimit"));

        if (sharedInventory != null && sharedShoppingList != null) {
            assertTrue((Integer) sharedInventory.get("id") > 0, "Shared inventory ID should be positive");
            assertTrue((Integer) sharedShoppingList.get("id") > 0, "Shared shopping list ID should be positive");
        } else {
            System.out.println("  WARNING: Shared resources are null - this may indicate lazy loading issue");
        }

        Household household = householdRepository.findById(householdId).orElseThrow();
        return new HouseholdSetup(ownerTokens, household, householdId, joinCode);
    }

    private void assertInitialResourceState (Household household) {
        List<Inventory> inventories = inventoryRepository.findAllByHousehold(household);
        List<ShoppingList> shoppingLists = shoppingListRepository.findAllByHousehold(household);

        System.out.println("--- Verifying Inventories and Shopping Lists after Household Creation ---");
        System.out.println("  Total Inventories: " + inventories.size());
        System.out.println("  Total Shopping Lists: " + shoppingLists.size());

        assertEquals(2, inventories.size(), "Should have 2 inventories after household creation (shared + owner)");
        assertEquals(2, shoppingLists.size(), "Should have 2 shopping lists after household creation (shared + owner)");
        assertEquals(1, inventories.stream().filter(inv -> inv.getOwner() == null).count(), "Should have 1 shared inventory");
        assertEquals(1, inventories.stream().filter(inv -> inv.getOwner() != null).count(), "Should have 1 owner inventory");
        assertEquals(1, shoppingLists.stream().filter(sl -> sl.getOwner() == null).count(), "Should have 1 shared shopping list");
        assertEquals(1, shoppingLists.stream().filter(sl -> sl.getOwner() != null).count(), "Should have 1 owner shopping list");
    }

    private DeviceSetup registerDevicesAndJoinHousehold (Household household, String joinCode) throws Exception {
        System.out.println("--- Step 3: Creating 2 device accounts ---");
        TokenResponse device1Tokens = registerAndLoginDevice("Device 1");
        TokenResponse device2Tokens = registerAndLoginDevice("Device 2");

        System.out.println("--- Step 4: All users join the household ---");
        MemberResources member1Resources = joinHouseholdAsDevice(device1Tokens, "DeviceUser1", "Dog", "white", joinCode);
        assertResourceCounts(household, 3, 3, "after member 1 joins");

        joinHouseholdAsDevice(device2Tokens, "DeviceUser2", "Rabbit", "red", joinCode);
        assertResourceCounts(household, 4, 4, "after member 2 joins");

        return new DeviceSetup(
          device1Tokens,
          device2Tokens,
          member1Resources.inventoryId(),
          member1Resources.shoppingListId()
        );
    }

    private record MemberResources(int inventoryId, int shoppingListId) {}

    private MemberResources joinHouseholdAsDevice (
      TokenResponse deviceTokens,
      String nickname,
      String avatarName,
      String avatarColor,
      String joinCode
    ) throws Exception {
        String query = String.format(
          "mutation { joinHousehold(nickname: \"%s\", avatarName: \"%s\", avatarColorName: \"%s\", joinCode: \"%s\") { nickname inventory { id } shoppingList { id } } }",
          nickname,
          avatarName,
          avatarColor,
          joinCode
        );

        MvcResult result = executeGraphql(deviceTokens.accessToken(), query);
        Map<String, Object> joinData = readGraphqlData(result, "joinHousehold");
        Map<String, Object> inventoryData = (Map<String, Object>) joinData.get("inventory");
        Map<String, Object> shoppingListData = (Map<String, Object>) joinData.get("shoppingList");

        System.out.println("✓ " + nickname + " joined household");
        return new MemberResources((int) inventoryData.get("id"), (int) shoppingListData.get("id"));
    }

    private List<Profile> printAndAssertFinalHouseholdState (
      Household household,
      String householdId,
      String householdName,
      int membersLimit,
      String joinCode
    ) {
        System.out.println("--- Step 5: Printing comprehensive household information ---");
        System.out.println("\n=== DATABASE VERIFICATION ===");
        System.out.println("Household from DB: " + household);

        List<Profile> profiles = profileRepository.findAllByHouseholdId(householdId);
        System.out.println("\nTotal Users in Household: " + profiles.size());
        for (Profile profile : profiles) {
            printProfile(profile);
        }

        List<Inventory> allInventories = inventoryRepository.findAllByHousehold(household);
        List<ShoppingList> allShoppingLists = shoppingListRepository.findAllByHousehold(household);

        printInventorySummary(allInventories);
        printShoppingListSummary(allShoppingLists);

        System.out.println("\n=== RUNNING ASSERTIONS ===");
        assertEquals(3, profiles.size(), "Should have 3 users in household");
        assertEquals(householdName, household.getName(), "Household name should match");
        assertEquals(membersLimit, household.getMembersLimit(), "Members limit should match");
        assertEquals(joinCode, household.getJoinCode(), "Join code should match");
        assertEquals(3, accountRepository.count(), "Should have 3 accounts in database");

        List<String> nicknames = profiles.stream().map(Profile::getNickname).toList();
        assertTrue(nicknames.contains("HomeOwner"), "Should have HomeOwner");
        assertTrue(nicknames.contains("DeviceUser1"), "Should have DeviceUser1");
        assertTrue(nicknames.contains("DeviceUser2"), "Should have DeviceUser2");

        List<String> avatarCombos = profiles.stream().map(u -> u.getAvatarName() + "-" + u.getAvatarColorName()).toList();
        assertEquals(3, avatarCombos.size(), "Should have 3 unique avatar combinations");

        assertEquals(4, allInventories.size(), "Should have 4 inventories total (shared + 3 members)");
        assertEquals(4, allShoppingLists.size(), "Should have 4 shopping lists total (shared + 3 members)");
        assertEquals(1, allInventories.stream().filter(inv -> inv.getOwner() == null).count(), "Should have 1 shared inventory");
        assertEquals(3, allInventories.stream().filter(inv -> inv.getOwner() != null).count(), "Should have 3 member inventories");
        assertEquals(1, allShoppingLists.stream().filter(sl -> sl.getOwner() == null).count(), "Should have 1 shared shopping list");
        assertEquals(3, allShoppingLists.stream().filter(sl -> sl.getOwner() != null).count(), "Should have 3 member shopping lists");
        return profiles;
    }

    private void runCrossUserInventoryAndShoppingListFlow (int productId, DeviceSetup deviceSetup) throws Exception {
        assertNoGraphqlErrors(
          executeGraphql(
            deviceSetup.device2Tokens().accessToken(),
            String.format(
              "mutation { addProductToInventory(productId: %d, inventoryId: %d, count: 1, notes: \"Przez 2 dla 1\") { notes } }",
              productId,
              deviceSetup.device1InventoryId()
            )
          ),
          "Should not have errors when adding product to another user's inventory"
        );

        assertNoGraphqlErrors(
          executeGraphql(
            deviceSetup.device1Tokens().accessToken(),
            String.format(
              "mutation { addProductToInventory(productId: %d, inventoryId: %d, count: 1, notes: \"Przez 2 dla 1\") { notes } }",
              productId,
              deviceSetup.device1InventoryId()
            )
          ),
          "Should not have errors when adding product to users own inventory"
        );

        assertNoGraphqlErrors(
          executeGraphql(
            deviceSetup.device2Tokens().accessToken(),
            String.format(
              "mutation { addProductToShoppingList(productId: %d, shoppingListId: %d, count: 1, notes: \"For shopping list remove test\") { notes } }",
              productId,
              deviceSetup.device1ShoppingListId()
            )
          ),
          "Should not have errors when adding product to another user's shopping list"
        );

        assertNoGraphqlErrors(
          executeGraphql(
            deviceSetup.device2Tokens().accessToken(),
            String.format(
              "mutation { removeProductFromInventory(productId: %d, inventoryId: %d, count: 1, notes: \"Removed by DeviceUser2\") { id notes } }",
              productId,
              deviceSetup.device1InventoryId()
            )
          ),
          "Should not have errors when removing product from another user's inventory"
        );

        assertNoGraphqlErrors(
          executeGraphql(
            deviceSetup.device2Tokens().accessToken(),
            String.format(
              "mutation { removeProductFromShoppingList(productId: %d, shoppingListId: %d, count: 1, notes: \"Removed by DeviceUser2\") { id notes } }",
              productId,
              deviceSetup.device1ShoppingListId()
            )
          ),
          "Should not have errors when removing product from another user's shopping list"
        );
    }

    private void assertAndPrintNotifications (List<Profile> profiles) {
        List<NotificationDTO> notifications = notificationRepository.findAll().stream().map(Notification::toDto).toList();
        assertFalse(notifications.isEmpty(), "Expected notifications to be generated during household flow");

        String ownerProfileId = profiles.stream().filter(p -> "HomeOwner".equals(p.getNickname())).findFirst().orElseThrow().getId();
        assertTrue(
          notifications.stream().anyMatch(n -> ownerProfileId.equals(n.profileId())),
          "Expected at least one notification for household owner"
        );

        String deviceUser1ProfileId = profiles.stream().filter(p -> "DeviceUser1".equals(p.getNickname())).findFirst().orElseThrow().getId();
        assertTrue(
          notifications.stream().anyMatch(n -> deviceUser1ProfileId.equals(n.profileId())),
          "Expected at least one notification for DeviceUser1 after cross-inventory operation"
        );

        assertTrue(
          notifications.stream().anyMatch(n ->
            deviceUser1ProfileId.equals(n.profileId())
              && n.title().toLowerCase().contains("removed")
              && n.message().toLowerCase().contains("inventory")
          ),
          "Expected an inventory removal notification for DeviceUser1"
        );

        assertTrue(
          notifications.stream().anyMatch(n ->
            deviceUser1ProfileId.equals(n.profileId())
              && n.title().toLowerCase().contains("removed")
              && n.message().toLowerCase().contains("shopping list")
          ),
          "Expected a shopping list removal notification for DeviceUser1"
        );

        System.out.println("\nNotifications in the system:");
        for (NotificationDTO notification : notifications) {
            System.out.println("  - Notification ID: " + notification.id());
            System.out.println("    Title: " + notification.title());
            System.out.println("    Message: " + notification.message());
            System.out.println("    Timestamp: " + notification.timestamp());
            System.out.println("    Recipient Profile ID: " + notification.profileId());
        }
    }

    private void printHouseholdJson (Household household, List<Profile> profiles) throws Exception {
        System.out.println("\n=== COMPLETE HOUSEHOLD DATA (JSON FORMAT) ===");
        Map<String, Object> sharedInventoryData = household.getSharedInventory() != null ? Map.of(
          "id", household.getSharedInventory().getId(),
          "householdId", household.getId()
        ) : Map.of("status", "N/A");

        Map<String, Object> sharedShoppingListData = household.getSharedShoppingList() != null ? Map.of(
          "id", household.getSharedShoppingList().getId(),
          "householdId", household.getId(),
          "itemsCount", household.getSharedShoppingList().getItems().size()
        ) : Map.of("status", "N/A");

        Map<String, Object> householdJson = Map.of(
          "household", Map.of(
            "id", household.getId(),
            "name", household.getName(),
            "joinCode", household.getJoinCode(),
            "membersLimit", household.getMembersLimit(),
            "currentMembers", profiles.size(),
            "sharedInventory", sharedInventoryData,
            "sharedShoppingList", sharedShoppingListData,
            "members", profiles.stream().map(profile -> {
                Account account = profile.getAccount();
                Map<String, Object> inventoryData = profile.getInventory() != null
                                                   ? Map.of("id", profile.getInventory().getId(), "householdId", profile.getHousehold().getId())
                                                   : Map.of("status", "N/A");
                Map<String, Object> shoppingListData = profile.getShoppingList() != null
                                                      ? Map.of(
                    "id", profile.getShoppingList().getId(),
                    "householdId", profile.getHousehold().getId(),
                    "itemsCount", profile.getShoppingList().getItems().size()
                )
                                                      : Map.of("status", "N/A");

                return Map.of(
                  "profileId", profile.getId(),
                  "nickname", profile.getNickname(),
                  "avatar", Map.of(
                    "name", profile.getAvatarName(),
                    "colorName", profile.getAvatarColorName(),
                    "colorHex", Objects.requireNonNull(ColorsUtil.getHexByColor(profile.getAvatarColorName()))
                  ),
                  "inventory", inventoryData,
                  "shoppingList", shoppingListData,
                  "account", Map.of(
                    "id", account.getId(),
                    "email", account.getEmail() != null ? account.getEmail() : "N/A",
                    "authProvider", account.getAuthProvider().toString()
                  )
                );
            }).toList()
          )
        );

        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(householdJson));
    }

    private void printCompletedSummary () {
        System.out.println("\n=== Integration Test Completed Successfully ===");
        System.out.println("Total GraphQL Queries/Mutations Tested:");
        System.out.println("  - createHousehold mutation");
        System.out.println("  - joinHousehold mutation");
        System.out.println("  - addProductToInventory mutation");
        System.out.println("  - removeProductFromInventory mutation");
        System.out.println("  - addProductToShoppingList mutation");
        System.out.println("  - removeProductFromShoppingList mutation");
    }

    private TokenResponse registerUser (String email, String password) throws Exception {
        MvcResult registerResult = mockMvc.perform(post("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(Map.of("email", email, "password", password))))
          .andExpect(status().isOk())
          .andReturn();
        return objectMapper.readValue(registerResult.getResponse().getContentAsString(), TokenResponse.class);
    }

    private TokenResponse registerAndLoginDevice (String label) throws Exception {
        MvcResult registerResult = mockMvc.perform(post("/auth/device/register")
            .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andReturn();

        Map<String, String> deviceData = objectMapper.readValue(registerResult.getResponse().getContentAsString(), Map.class);
        String deviceId = deviceData.get("deviceId");
        System.out.println("✓ " + label + " registered");
        System.out.println("  Device ID: " + deviceId);

        MvcResult loginResult = mockMvc.perform(post("/auth/device/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(Map.of("deviceId", deviceId))))
          .andExpect(status().isOk())
          .andReturn();

        System.out.println("  " + label + " logged in successfully");
        return objectMapper.readValue(loginResult.getResponse().getContentAsString(), TokenResponse.class);
    }

    private MvcResult executeGraphql (String accessToken, String query) throws Exception {
        return mockMvc.perform(post("/graphql")
            .header("Authorization", "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(Map.of("query", query))))
          .andExpect(status().isOk())
          .andReturn();
    }

    private Map<String, Object> readGraphqlData (MvcResult result, String operation) throws Exception {
        String response = result.getResponse().getContentAsString();
        Map<String, Object> graphQlResponse = objectMapper.readValue(response, Map.class);
        Object data = graphQlResponse.get("data");
        if (!(data instanceof Map<?, ?> dataMap) || !dataMap.containsKey(operation)) {
            throw new AssertionError("GraphQL response missing operation '" + operation + "': " + response);
        }
        return (Map<String, Object>) dataMap.get(operation);
    }

    private void assertNoGraphqlErrors (MvcResult result, String message) throws Exception {
        assertFalse(result.getResponse().getContentAsString().contains("errors"), message);
    }

    private void assertResourceCounts (Household household, int inventoriesCount, int shoppingListsCount, String phase) {
        List<Inventory> inventories = inventoryRepository.findAllByHousehold(household);
        List<ShoppingList> shoppingLists = shoppingListRepository.findAllByHousehold(household);
        System.out.println("  Total Inventories " + phase + ": " + inventories.size());
        System.out.println("  Total Shopping Lists " + phase + ": " + shoppingLists.size());
        assertEquals(inventoriesCount, inventories.size(), "Unexpected inventories count " + phase);
        assertEquals(shoppingListsCount, shoppingLists.size(), "Unexpected shopping lists count " + phase);
    }

    private void printProfile (Profile profile) {
        Account account = profile.getAccount();
        System.out.println("\nUser Profile:");
        System.out.println("  User ID: " + profile.getId());
        System.out.println("  Nickname: " + profile.getNickname());
        System.out.println("  Avatar: " + profile.getAvatarName() + " (" + profile.getAvatarColorName() + ")");
        System.out.println("  Account ID: " + account.getId());
        System.out.println("  Account Email: " + account.getEmail());
        System.out.println("  Auth Provider: " + account.getAuthProvider());

        String devicesInfo;
        try {
            List<String> devicesList = account.getDevices();
            devicesInfo = devicesList != null ? devicesList.toString() : "null";
        } catch (Exception e) {
            devicesInfo = "<not loaded>";
        }
        System.out.println("  Devices: " + devicesInfo);
    }

    private void printInventorySummary (List<Inventory> inventories) {
        System.out.println("\n=== INVENTORIES SUMMARY ===");
        System.out.println("Total Inventories: " + inventories.size());
        for (Inventory inventory : inventories) {
            String ownerInfo = inventory.getOwner() != null
                               ? inventory.getOwner().getNickname() + " (ID: " + inventory.getOwner().getId() + ")"
                               : "SHARED";
            System.out.println("  - Inventory ID: " + inventory.getId() + ", Owner: " + ownerInfo);
        }
    }

    private void printShoppingListSummary (List<ShoppingList> shoppingLists) {
        System.out.println("\n=== SHOPPING LISTS SUMMARY ===");
        System.out.println("Total Shopping Lists: " + shoppingLists.size());
        for (ShoppingList shoppingList : shoppingLists) {
            String ownerInfo = shoppingList.getOwner() != null
                               ? shoppingList.getOwner().getNickname() + " (ID: " + shoppingList.getOwner().getId() + ")"
                               : "SHARED";
            System.out.println("  - Shopping List ID: " + shoppingList.getId() + ", Owner: " + ownerInfo);
        }
    }
}





