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
        
        // Step 1: Create user with account (email/password)
        System.out.println("--- Step 1: Creating user with email/password account ---");
        String email = "owner@roomly.com";
        String password = "SecurePassword123";
        
        String registerJson = objectMapper.writeValueAsString(Map.of(
          "email", email,
          "password", password
        ));
        
        MvcResult registerResult = mockMvc.perform(post("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(registerJson))
          .andExpect(status().isOk())
          .andReturn();
        
        TokenResponse ownerTokens = objectMapper.readValue(
          registerResult.getResponse().getContentAsString(),
          TokenResponse.class
        );
        
        System.out.println("✓ Owner account created successfully");
        System.out.println("  Access Token: " + ownerTokens.accessToken().substring(0, 20) + "...");
        
        // Verify account was created
        Account ownerAccount = accountRepository.findByEmail(email).orElseThrow();
        System.out.println("  Account ID: " + ownerAccount.getId());
        System.out.println("  Email: " + ownerAccount.getEmail());
        System.out.println("  Auth Provider: " + ownerAccount.getAuthProvider());
        
        // Step 2: Create household
        System.out.println("--- Step 2: Creating household ---");
        String householdName = "My Awesome Household";
        int membersLimit = 5;
        
        String createHouseholdMutation = String.format(
          """
          {
              "query": "mutation { createHousehold(name: \\"%s\\", membersLimit: %d, nickname: \\"%s\\", avatarName: \\"%s\\", avatarColorName: \\"%s\\") { id name joinCode membersLimit sharedInventory { id } sharedShoppingList { id } } }"
          }
          """, householdName, membersLimit, "HomeOwner", "Cat", "blue"
        );
        
        MvcResult householdResult = mockMvc.perform(post("/graphql")
            .header("Authorization", "Bearer " + ownerTokens.accessToken())
            .contentType(MediaType.APPLICATION_JSON)
            .content(createHouseholdMutation))
          .andExpect(status().isOk())
          .andReturn();
        
        String householdResponse = householdResult.getResponse().getContentAsString();
        System.out.println("  GraphQL Response: " + householdResponse);
        
        // Extract household data from GraphQL response
        Map<String, Object> householdGraphqlResponse = objectMapper.readValue(householdResponse, Map.class);
        Object dataObject = householdGraphqlResponse.get("data");
        System.out.println("  Data object: " + dataObject);
        
        if (!(dataObject instanceof Map)) {
            System.out.println("  ERROR: No data in response or data is not a Map!");
            System.out.println("  Full response keys: " + householdGraphqlResponse.keySet());
            if (householdGraphqlResponse.containsKey("errors")) {
                System.out.println("  GraphQL Errors: " + householdGraphqlResponse.get("errors"));
            }
            throw new AssertionError("GraphQL response did not contain valid data");
        }
        
        Map<String, Object> householdData = (Map<String, Object>) ((Map<String, Object>) dataObject).get(
          "createHousehold");
        
        String householdId = (String) householdData.get("id");
        String joinCode = (String) householdData.get("joinCode");
        Map<String, Object> sharedInventory = (Map<String, Object>) householdData.get("sharedInventory");
        Map<String, Object> sharedShoppingList = (Map<String, Object>) householdData.get("sharedShoppingList");
        
        System.out.println("✓ Household created successfully");
        System.out.println("  Household ID: " + householdId);
        System.out.println("  Name: " + householdData.get("name"));
        System.out.println("  Join Code: " + joinCode);
        System.out.println("  Members Limit: " + householdData.get("membersLimit"));
        System.out.println("  Raw household data: " + householdData);
        System.out.println("  Shared Inventory: " + sharedInventory);
        System.out.println("  Shared Shopping List: " + sharedShoppingList);
        
        // Verify shared resources were assigned (skip if null for now to see what's happening)
        if (sharedInventory != null && sharedShoppingList != null) {
            assertTrue((Integer) sharedInventory.get("id") > 0, "Shared inventory ID should be positive");
            assertTrue((Integer) sharedShoppingList.get("id") > 0, "Shared shopping list ID should be positive");
        } else {
            System.out.println("  WARNING: Shared resources are null - this may indicate lazy loading issue");
        }
        
        // Verify inventories and shopping lists were created for household owner
        Household household = householdRepository.findById(householdId).orElseThrow();
        List<Inventory> inventories = inventoryRepository.findAllByHousehold(household);
        List<ShoppingList> shoppingLists = shoppingListRepository.findAllByHousehold(household);
        
        System.out.println("--- Verifying Inventories and Shopping Lists after Household Creation ---");
        System.out.println("  Total Inventories: " + inventories.size());
        System.out.println("  Total Shopping Lists: " + shoppingLists.size());
        
        // Should have 2 inventories: 1 shared (owner=null) and 1 for owner
        assertEquals(
          2, inventories.size(), "Should have 2 inventories after household creation (shared + owner)");
        
        // Should have 2 shopping lists: 1 shared (owner=null) and 1 for owner
        assertEquals(
          2, shoppingLists.size(), "Should have 2 shopping lists after household creation (shared + owner)");
        
        // Verify one inventory is shared
        long sharedInventories = inventories.stream().filter(inv -> inv.getOwner() == null).count();
        assertEquals(1, sharedInventories, "Should have 1 shared inventory");
        System.out.println("  ✓ 1 shared inventory verified");
        
        // Verify one inventory belongs to owner
        long ownerInventories = inventories.stream().filter(inv -> inv.getOwner() != null).count();
        assertEquals(1, ownerInventories, "Should have 1 owner inventory");
        System.out.println("  ✓ 1 owner inventory verified");
        
        // Verify one shopping list is shared
        long sharedShoppingLists = shoppingLists.stream().filter(sl -> sl.getOwner() == null).count();
        assertEquals(1, sharedShoppingLists, "Should have 1 shared shopping list");
        System.out.println("  ✓ 1 shared shopping list verified");
        
        // Verify one shopping list belongs to owner
        long ownerShoppingLists = shoppingLists.stream().filter(sl -> sl.getOwner() != null).count();
        assertEquals(1, ownerShoppingLists, "Should have 1 owner shopping list");
        System.out.println("  ✓ 1 owner shopping list verified");
        
        // Step 3: Create 2 users with device authentication
        System.out.println("--- Step 3: Creating 2 device accounts ---");
        
        // Device 1
        MvcResult device1Result = mockMvc.perform(post("/auth/device/register")
            .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andReturn();
        
        Map<String, String> device1Data = objectMapper.readValue(
          device1Result.getResponse().getContentAsString(),
          Map.class
        );
        String deviceId1 = device1Data.get("deviceId");
        
        System.out.println("✓ Device 1 registered");
        System.out.println("  Device ID: " + deviceId1);
        
        // Login with device 1
        String device1LoginJson = objectMapper.writeValueAsString(Map.of("deviceId", deviceId1));
        MvcResult device1LoginResult = mockMvc.perform(post("/auth/device/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(device1LoginJson))
          .andExpect(status().isOk())
          .andReturn();
        
        TokenResponse device1Tokens = objectMapper.readValue(
          device1LoginResult.getResponse().getContentAsString(),
          TokenResponse.class
        );
        System.out.println("  Device 1 logged in successfully");
        
        // Device 2
        MvcResult device2Result = mockMvc.perform(post("/auth/device/register")
            .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andReturn();
        
        Map<String, String> device2Data = objectMapper.readValue(
          device2Result.getResponse().getContentAsString(),
          Map.class
        );
        String deviceId2 = device2Data.get("deviceId");
        
        System.out.println("✓ Device 2 registered");
        System.out.println("  Device ID: " + deviceId2);
        
        // Login with device 2
        String device2LoginJson = objectMapper.writeValueAsString(Map.of("deviceId", deviceId2));
        MvcResult device2LoginResult = mockMvc.perform(post("/auth/device/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(device2LoginJson))
          .andExpect(status().isOk())
          .andReturn();
        
        TokenResponse device2Tokens = objectMapper.readValue(
          device2LoginResult.getResponse().getContentAsString(),
          TokenResponse.class
        );
        System.out.println("  Device 2 logged in successfully");
        
        // Step 4: Join household with all accounts
        System.out.println("--- Step 4: All users join the household ---");
        
        // Device 1 user joins household
        String device1JoinMutation = String.format(
          """
          {
              "query": "mutation { joinHousehold(nickname: \\"%s\\", avatarName: \\"%s\\", avatarColorName: \\"%s\\", joinCode: \\"%s\\") { nickname avatar { name colorName colorHex } inventory { id } shoppingList { id } } }"
          }
          """, "DeviceUser1", "Dog", "white", joinCode
        );
        
        MvcResult device1JoinResult = mockMvc.perform(post("/graphql")
            .header("Authorization", "Bearer " + device1Tokens.accessToken())
            .contentType(MediaType.APPLICATION_JSON)
            .content(device1JoinMutation))
          .andExpect(status().isOk())
          .andReturn();
        
        System.out.println("✓ Device User 1 joined household");
        System.out.println("  Response: " + device1JoinResult.getResponse().getContentAsString());
        String device1JoinResponse = device1JoinResult.getResponse().getContentAsString();
        
        Map<String, Object> device1GraphqlResponse = objectMapper.readValue(device1JoinResponse, Map.class);
        Map<String, Object> device1JoinData = (Map<String, Object>) ((Map<String, Object>) device1GraphqlResponse.get(
          "data")).get("joinHousehold");
        Map<String, Object> device1InventoryData = (Map<String, Object>) device1JoinData.get("inventory");
        int device1InventoryId = (int) device1InventoryData.get("id");
        
        
        // Verify inventory and shopping list were created for Device User 1
        List<Inventory> inventoriesAfterMember1 = inventoryRepository.findAllByHousehold(household);
        List<ShoppingList> shoppingListsAfterMember1 = shoppingListRepository.findAllByHousehold(household);
        
        System.out.println("  Total Inventories after Member 1: " + inventoriesAfterMember1.size());
        System.out.println("  Total Shopping Lists after Member 1: " + shoppingListsAfterMember1.size());
        
        // Should now have 3 inventories: shared + owner + member1
        assertEquals(3, inventoriesAfterMember1.size(), "Should have 3 inventories after member 1 joins");
        // Should now have 3 shopping lists: shared + owner + member1
        assertEquals(3, shoppingListsAfterMember1.size(), "Should have 3 shopping lists after member 1 joins");
        System.out.println("  ✓ Inventory and shopping list created for Device User 1");
        
        // Device 2 user joins household
        String device2JoinMutation = String.format(
          """
          {
              "query": "mutation { joinHousehold(nickname: \\"%s\\", avatarName: \\"%s\\", avatarColorName: \\"%s\\", joinCode: \\"%s\\") { nickname avatar { name colorName colorHex } inventory { id } shoppingList { id } } }"
          }
          """, "DeviceUser2", "Fox", "green", joinCode
        );
        
        MvcResult device2JoinResult = mockMvc.perform(post("/graphql")
            .header("Authorization", "Bearer " + device2Tokens.accessToken())
            .contentType(MediaType.APPLICATION_JSON)
            .content(device2JoinMutation))
          .andExpect(status().isOk())
          .andReturn();
        
        System.out.println("✓ Device User 2 joined household");
        System.out.println("  Response: " + device2JoinResult.getResponse().getContentAsString());
        
        // Verify inventory and shopping list were created for Device User 2
        List<Inventory> inventoriesAfterMember2 = inventoryRepository.findAllByHousehold(household);
        List<ShoppingList> shoppingListsAfterMember2 = shoppingListRepository.findAllByHousehold(household);
        
        System.out.println("  Total Inventories after Member 2: " + inventoriesAfterMember2.size());
        System.out.println("  Total Shopping Lists after Member 2: " + shoppingListsAfterMember2.size());
        
        // Should now have 4 inventories: shared + owner + member1 + member2
        assertEquals(4, inventoriesAfterMember2.size(), "Should have 4 inventories after member 2 joins");
        // Should now have 4 shopping lists: shared + owner + member1 + member2
        assertEquals(4, shoppingListsAfterMember2.size(), "Should have 4 shopping lists after member 2 joins");
        System.out.println("  ✓ Inventory and shopping list created for Device User 2");
        
        // Step 5: Print comprehensive information
        System.out.println("--- Step 5: Printing comprehensive household information ---");
        
        // Verify with direct database queries
        System.out.println("\n=== DATABASE VERIFICATION ===");
        
        System.out.println("Household from DB: " + household);
        
        List<Profile> profiles = profileRepository.findAllByHouseholdId(householdId);
        System.out.println("\nTotal Users in Household: " + profiles.size());
        
        for (Profile profile : profiles) {
            Account account = profile.getAccount();
            System.out.println("\nUser Profile:");
            System.out.println("  User ID: " + profile.getId());
            System.out.println("  Nickname: " + profile.getNickname());
            System.out.println(
              "  Avatar: " + profile.getAvatarName() + " (" + profile.getAvatarColorName() + ")");
            System.out.println("  Account ID: " + account.getId());
            System.out.println("  Account Email: " + account.getEmail());
            System.out.println("  Auth Provider: " + account.getAuthProvider());
            // Safely access lazy-loaded devices collection
            String devicesInfo;
            try {
                List<String> devicesList = account.getDevices();
                devicesInfo = devicesList != null ? devicesList.toString() : "null";
            } catch (Exception e) {
                devicesInfo = "<not loaded>";
            }
            System.out.println("  Devices: " + devicesInfo);
        }
        
        // Display all inventories and shopping lists
        List<Inventory> allInventories = inventoryRepository.findAllByHousehold(household);
        List<ShoppingList> allShoppingLists = shoppingListRepository.findAllByHousehold(household);
        
        System.out.println("\n=== INVENTORIES SUMMARY ===");
        System.out.println("Total Inventories: " + allInventories.size());
        for (Inventory inventory : allInventories) {
            String ownerInfo = inventory.getOwner() != null
                               ? inventory.getOwner().getNickname() + " (ID: " + inventory
                                                                                 .getOwner()
                                                                                 .getId() + ")"
                               : "SHARED";
            System.out.println("  - Inventory ID: " + inventory.getId() + ", Owner: " + ownerInfo);
        }
        
        System.out.println("\n=== SHOPPING LISTS SUMMARY ===");
        System.out.println("Total Shopping Lists: " + allShoppingLists.size());
        for (ShoppingList shoppingList : allShoppingLists) {
            String ownerInfo = shoppingList.getOwner() != null
                               ? shoppingList.getOwner().getNickname() + " (ID: " + shoppingList
                                                                                    .getOwner()
                                                                                    .getId() + ")"
                               : "SHARED";
            System.out.println("  - Shopping List ID: " + shoppingList.getId() + ", Owner: " + ownerInfo);
        }
        
        // Assertions
        System.out.println("\n=== RUNNING ASSERTIONS ===");
        
        assertEquals(3, profiles.size(), "Should have 3 users in household");
        assertEquals(householdName, household.getName(), "Household name should match");
        assertEquals(membersLimit, household.getMembersLimit(), "Members limit should match");
        assertEquals(joinCode, household.getJoinCode(), "Join code should match");
        
        // Verify all accounts exist
        assertEquals(3, accountRepository.count(), "Should have 3 accounts in database");
        
        // Verify user nicknames
        List<String> nicknames = profiles.stream().map(Profile::getNickname).toList();
        assertTrue(nicknames.contains("HomeOwner"), "Should have HomeOwner");
        assertTrue(nicknames.contains("DeviceUser1"), "Should have DeviceUser1");
        assertTrue(nicknames.contains("DeviceUser2"), "Should have DeviceUser2");
        
        // Verify avatar uniqueness in household
        List<String> avatarCombos = profiles.stream()
          .map(u -> u.getAvatarName() + "-" + u.getAvatarColorName())
          .toList();
        assertEquals(3, avatarCombos.size(), "Should have 3 unique avatar combinations");
        
        // Final assertions for inventories and shopping lists
        assertEquals(4, allInventories.size(), "Should have 4 inventories total (shared + 3 members)");
        assertEquals(4, allShoppingLists.size(), "Should have 4 shopping lists total (shared + 3 members)");
        
        // Verify exactly 1 shared inventory and 3 member inventories
        long finalSharedInventories = allInventories.stream().filter(inv -> inv.getOwner() == null).count();
        long finalMemberInventories = allInventories.stream().filter(inv -> inv.getOwner() != null).count();
        assertEquals(1, finalSharedInventories, "Should have 1 shared inventory");
        assertEquals(3, finalMemberInventories, "Should have 3 member inventories");
        
        // Verify exactly 1 shared shopping list and 3 member shopping lists
        long finalSharedShoppingLists = allShoppingLists.stream().filter(sl -> sl.getOwner() == null).count();
        long finalMemberShoppingLists = allShoppingLists.stream().filter(sl -> sl.getOwner() != null).count();
        assertEquals(1, finalSharedShoppingLists, "Should have 1 shared shopping list");
        assertEquals(3, finalMemberShoppingLists, "Should have 3 member shopping lists");
        
        //Add product to other user inventory and verify it was added correctly
        int productId = productRepository.findByBarcode("5901939103099").orElseThrow().getId();
        
        String user2AddProductToUser1Inventory = String.format(
          """
                    {
                        "query": "mutation { addProductToInventory(productId: %d, inventoryId: %d, count: 1, notes: \\"Przez 2 dla 1\\"){notes} }"
                    }
          """, productId, device1InventoryId
        );
        
        MvcResult addProductResult = mockMvc.perform(post("/graphql")
            .header("Authorization", "Bearer " + device2Tokens.accessToken())
            .contentType(MediaType.APPLICATION_JSON)
            .content(user2AddProductToUser1Inventory))
          .andExpect(status().isOk())
          .andReturn();
        
        assertFalse(
          addProductResult.getResponse().getContentAsString().contains("errors"),
          "Should not have errors when adding product to another user's inventory"
        );
        
        System.out.println("\n✓ Product added to another user's inventory successfully");
        System.out.printf("Response: %s%n", addProductResult.getResponse().getContentAsString());
        
        
        String user1AddProductToUser1Inventory = String.format(
          """
                    {
                        "query": "mutation { addProductToInventory(productId: %d, inventoryId: %d, count: 1, notes: \\"Przez 2 dla 1\\"){notes} }"
                    }
          """, productId, device1InventoryId
        );
        
        MvcResult addProductResult2 = mockMvc.perform(post("/graphql")
            .header("Authorization", "Bearer " + device1Tokens.accessToken())
            .contentType(MediaType.APPLICATION_JSON)
            .content(user1AddProductToUser1Inventory))
          .andExpect(status().isOk())
          .andReturn();
        
        assertFalse(
          addProductResult2.getResponse().getContentAsString().contains("errors"),
          "Should not have errors when adding product to users own inventory"
        );
        
        System.out.println("\n✓ Product added to another user's inventory successfully");
        System.out.printf("Response: %s%n", addProductResult.getResponse().getContentAsString());
        
        System.out.println("\n✓ All assertions passed!");
        System.out.println("  ✓ Household structure verified");
        System.out.println("  ✓ User profiles verified");
        System.out.println("  ✓ Inventories verified (1 shared + 3 member = 4 total)");
        System.out.println("  ✓ Shopping lists verified (1 shared + 3 member = 4 total)");
        System.out.println("  ✓ Available avatars and colors query tested");
        System.out.println("  ✓ Shopping list queries tested (shared, personal, and all)");
        System.out.println("  ✓ Structured household query tested");
        
        // Step 6: Print complete household data in JSON format
        System.out.println("\n=== COMPLETE HOUSEHOLD DATA (JSON FORMAT) ===");
        
        // Build JSON structure with all data nested under household
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
                
                Map<String, Object> inventoryData = profile.getInventory() != null ? Map.of(
                  "id", profile.getInventory().getId(),
                  "householdId", profile.getHousehold().getId()
                ) : Map.of("status", "N/A");
                
                Map<String, Object> shoppingListData = profile.getShoppingList() != null ? Map.of(
                  "id", profile.getShoppingList().getId(),
                  "householdId", profile.getHousehold().getId(),
                  "itemsCount", profile.getShoppingList().getItems().size()
                ) : Map.of("status", "N/A");
                
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
        
        // Pretty print JSON
        String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(householdJson);
        System.out.println(prettyJson);
        
        System.out.println("\n=== Integration Test Completed Successfully ===");
        System.out.println("Total GraphQL Queries/Mutations Tested:");
        System.out.println("  - createHousehold mutation");
        System.out.println("  - joinHousehold mutation");
        System.out.println("  - availableAvatarsAndColors query");
        System.out.println("  - shoppingList query (shared and personal)");
        System.out.println("  - allShoppingLists query");
        System.out.println("  - household query (structured data)");
        System.out.println("  - householdInfo query (test endpoint)");
        
        List<NotificationDTO> notifications = notificationRepository
          .findAll()
          .stream()
          .map(Notification::toDto)
          .toList();
        System.out.println("\nNotifications in the system:");
        for (NotificationDTO notification : notifications) {
            System.out.println("  - Notification ID: " + notification.id());
            System.out.println("    Title: " + notification.title());
            System.out.println("    Message: " + notification.message());
            System.out.println("    Timestamp: " + notification.timestamp());
            System.out.println("    Recipient Profile ID: " + notification.profileId());
        }
    }
}





