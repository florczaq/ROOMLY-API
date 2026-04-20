package org.roomly.integrationTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.roomly.entities.Household;
import org.roomly.entities.Profile;
import org.roomly.repositories.HouseholdRepository;
import org.roomly.repositories.ProfileRepository;
import org.roomly.security.authentication.entities.Account;
import org.roomly.security.authentication.jwt.dto.TokenResponse;
import org.roomly.security.authentication.repositories.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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
              "query": "mutation { createHousehold(name: \\"%s\\", membersLimit: %d, nickname: \\"%s\\", avatarName: \\"%s\\", avatarColorName: \\"%s\\") { id name joinCode membersLimit } }"
          }
          """, householdName, membersLimit, "HomeOwner", "cat", "blue"
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
        Map<String, Object> householdData = (Map<String, Object>) ((Map<String, Object>) householdGraphqlResponse.get(
          "data")).get("createHousehold");
        
        String householdId = (String) householdData.get("id");
        String joinCode = (String) householdData.get("joinCode");
        
        System.out.println("✓ Household created successfully");
        System.out.println("  Household ID: " + householdId);
        System.out.println("  Name: " + householdData.get("name"));
        System.out.println("  Join Code: " + joinCode);
        System.out.println("  Members Limit: " + householdData.get("membersLimit"));
        
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
              "query": "mutation { joinHousehold(nickname: \\"%s\\", avatarName: \\"%s\\", avatarColorName: \\"%s\\", joinCode: \\"%s\\") { nickname avatar { name colorName colorHex } } }"
          }
          """, "DeviceUser1", "dog", "red", joinCode
        );
        
        MvcResult device1JoinResult = mockMvc.perform(post("/graphql")
            .header("Authorization", "Bearer " + device1Tokens.accessToken())
            .contentType(MediaType.APPLICATION_JSON)
            .content(device1JoinMutation))
          .andExpect(status().isOk())
          .andReturn();
        
        System.out.println("✓ Device User 1 joined household");
        System.out.println("  Response: " + device1JoinResult.getResponse().getContentAsString());
        
        // Device 2 user joins household
        String device2JoinMutation = String.format(
          """
          {
              "query": "mutation { joinHousehold(nickname: \\"%s\\", avatarName: \\"%s\\", avatarColorName: \\"%s\\", joinCode: \\"%s\\") { nickname avatar { name colorName colorHex } } }"
          }
          """, "DeviceUser2", "bird", "green", joinCode
        );
        
        MvcResult device2JoinResult = mockMvc.perform(post("/graphql")
            .header("Authorization", "Bearer " + device2Tokens.accessToken())
            .contentType(MediaType.APPLICATION_JSON)
            .content(device2JoinMutation))
          .andExpect(status().isOk())
          .andReturn();
        
        System.out.println("✓ Device User 2 joined household");
        System.out.println("  Response: " + device2JoinResult.getResponse().getContentAsString());
        
        // Step 5: Print comprehensive information
        System.out.println("--- Step 5: Printing comprehensive household information ---");
        
        // Get household info using the test endpoint
        String householdInfoQuery = String.format(
          """
          {
              "query": "{ householdInfo(householdId: \\"%s\\") }"
          }
          """, householdId
        );
        
        MvcResult infoResult = mockMvc.perform(post("/graphql")
            .header("Authorization", "Bearer " + ownerTokens.accessToken())
            .contentType(MediaType.APPLICATION_JSON)
            .content(householdInfoQuery))
          .andExpect(status().isOk())
          .andReturn();
        
        String infoResponse = infoResult.getResponse().getContentAsString();
        Map<String, Object> infoGraphqlResponse = objectMapper.readValue(infoResponse, Map.class);
        String householdInfo = (String) ((Map<String, Object>) infoGraphqlResponse.get("data")).get(
          "householdInfo");
        
        System.out.println("\n=== HOUSEHOLD COMPLETE INFO ===");
        System.out.println(householdInfo);
        
        // Verify with direct database queries
        System.out.println("\n=== DATABASE VERIFICATION ===");
        
        Household household = householdRepository.findById(householdId).orElseThrow();
        System.out.println("Household from DB: " + household);
        
        List<Profile> profiles = profileRepository.findAllByHouseholdId(householdId);
        System.out.println("\nTotal Users in Household: " + profiles.size());
        
        for (Profile profile : profiles) {
            Account account = profile.getAccount();
            System.out.println("\nUser Profile:");
            System.out.println("  User ID: " + profile.getId());
            System.out.println("  Nickname: " + profile.getNickname());
            System.out.println("  Avatar: " + profile.getAvatarName() + " (" + profile.getAvatarColorName() + ")");
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
        
        System.out.println("\n✓ All assertions passed!");
        System.out.println("\n=== Integration Test Completed Successfully ===");
    }
}





