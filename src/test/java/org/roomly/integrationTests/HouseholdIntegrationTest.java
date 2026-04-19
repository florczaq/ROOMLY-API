package org.roomly.integrationTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.roomly.entities.Household;
import org.roomly.entities.User;
import org.roomly.repositories.HouseholdRepository;
import org.roomly.repositories.UserRepository;
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
@Slf4j
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
    private UserRepository userRepository;
    
    @BeforeEach
    void setUp () {
        // Set up MockMvc with security
        mockMvc = MockMvcBuilders
          .webAppContextSetup(context)
          .apply(springSecurity())
          .build();
        
        // Clean up database before each test
        userRepository.deleteAll();
        householdRepository.deleteAll();
        accountRepository.deleteAll();
    }
    
    @Test
    void testCompleteHouseholdFlow () throws Exception {
        log.info("=== Starting Complete Household Flow Integration Test ===");
        
        // Step 1: Create user with account (email/password)
        log.info("--- Step 1: Creating user with email/password account ---");
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
        
        log.info("✓ Owner account created successfully");
        log.info("  Access Token: {}", ownerTokens.accessToken().substring(0, 20) + "...");
        
        // Verify account was created
        Account ownerAccount = accountRepository.findByEmail(email).orElseThrow();
        log.info("  Account ID: {}", ownerAccount.getId());
        log.info("  Email: {}", ownerAccount.getEmail());
        log.info("  Auth Provider: {}", ownerAccount.getAuthProvider());
        
        // Step 2: Create household
        log.info("--- Step 2: Creating household ---");
        String householdName = "My Awesome Household";
        int membersLimit = 5;
        
        String createHouseholdMutation = String.format(
          """
          {
              "query": "mutation { createHousehold(name: \\"%s\\", membersLimit: %d) { id name joinCode membersLimit } }"
          }
          """, householdName, membersLimit
        );
        
        MvcResult householdResult = mockMvc.perform(post("/graphql")
            .header("Authorization", "Bearer " + ownerTokens.accessToken())
            .contentType(MediaType.APPLICATION_JSON)
            .content(createHouseholdMutation))
          .andExpect(status().isOk())
          .andReturn();
        
        String householdResponse = householdResult.getResponse().getContentAsString();
        log.info("  GraphQL Response: {}", householdResponse);
        
        // Extract household data from GraphQL response
        Map<String, Object> householdGraphqlResponse = objectMapper.readValue(householdResponse, Map.class);
        Map<String, Object> householdData = (Map<String, Object>) ((Map<String, Object>) householdGraphqlResponse.get(
          "data")).get("createHousehold");
        
        String householdId = (String) householdData.get("id");
        String joinCode = (String) householdData.get("joinCode");
        
        log.info("✓ Household created successfully");
        log.info("  Household ID: {}", householdId);
        log.info("  Name: {}", householdData.get("name"));
        log.info("  Join Code: {}", joinCode);
        log.info("  Members Limit: {}", householdData.get("membersLimit"));
        
        // Step 3: Create 2 users with device authentication
        log.info("--- Step 3: Creating 2 device accounts ---");
        
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
        
        log.info("✓ Device 1 registered");
        log.info("  Device ID: {}", deviceId1);
        
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
        log.info("  Device 1 logged in successfully");
        
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
        
        log.info("✓ Device 2 registered");
        log.info("  Device ID: {}", deviceId2);
        
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
        log.info("  Device 2 logged in successfully");
        
        // Step 4: Join household with all 3 accounts
        log.info("--- Step 4: All users join the household ---");
        
        // Owner joins household
        String ownerJoinMutation = String.format(
          """
          {
              "query": "mutation { joinHousehold(nickname: \\"%s\\", avatarName: \\"%s\\", avatarColorName: \\"%s\\", joinCode: \\"%s\\") { nickname avatar { name colorName colorHex } } }"
          }
          """, "HomeOwner", "cat", "blue", joinCode
        );
        
        MvcResult ownerJoinResult = mockMvc.perform(post("/graphql")
            .header("Authorization", "Bearer " + ownerTokens.accessToken())
            .contentType(MediaType.APPLICATION_JSON)
            .content(ownerJoinMutation))
          .andExpect(status().isOk())
          .andReturn();
        
        log.info("✓ Owner joined household");
        log.info("  Response: {}", ownerJoinResult.getResponse().getContentAsString());
        
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
        
        log.info("✓ Device User 1 joined household");
        log.info("  Response: {}", device1JoinResult.getResponse().getContentAsString());
        
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
        
        log.info("✓ Device User 2 joined household");
        log.info("  Response: {}", device2JoinResult.getResponse().getContentAsString());
        
        // Step 5: Print comprehensive information
        log.info("--- Step 5: Printing comprehensive household information ---");
        
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
        
        log.info("\n=== HOUSEHOLD COMPLETE INFO ===");
        log.info(householdInfo);
        
        // Verify with direct database queries
        log.info("\n=== DATABASE VERIFICATION ===");
        
        Household household = householdRepository.findById(householdId).orElseThrow();
        log.info("Household from DB: {}", household);
        
        List<User> users = userRepository.findAllByHouseholdId(householdId);
        log.info("\nTotal Users in Household: {}", users.size());
        
        for (User user : users) {
            Account account = user.getAccount();
            log.info("\nUser Profile:");
            log.info("  User ID: {}", user.getId());
            log.info("  Nickname: {}", user.getNickname());
            log.info("  Avatar: {} ({})", user.getAvatarName(), user.getAvatarColorName());
            log.info("  Account ID: {}", account.getId());
            log.info("  Account Email: {}", account.getEmail());
            log.info("  Auth Provider: {}", account.getAuthProvider());
            log.info("  Devices: {}", account.getDevices());
        }
        
        // Assertions
        log.info("\n=== RUNNING ASSERTIONS ===");
        
        assertEquals(3, users.size(), "Should have 3 users in household");
        assertEquals(householdName, household.getName(), "Household name should match");
        assertEquals(membersLimit, household.getMembersLimit(), "Members limit should match");
        assertEquals(joinCode, household.getJoinCode(), "Join code should match");
        
        // Verify all accounts exist
        assertEquals(3, accountRepository.count(), "Should have 3 accounts in database");
        
        // Verify user nicknames
        List<String> nicknames = users.stream().map(User::getNickname).toList();
        assertTrue(nicknames.contains("HomeOwner"), "Should have HomeOwner");
        assertTrue(nicknames.contains("DeviceUser1"), "Should have DeviceUser1");
        assertTrue(nicknames.contains("DeviceUser2"), "Should have DeviceUser2");
        
        // Verify avatar uniqueness in household
        List<String> avatarCombos = users.stream()
          .map(u -> u.getAvatarName() + "-" + u.getAvatarColorName())
          .toList();
        assertEquals(3, avatarCombos.size(), "Should have 3 unique avatar combinations");
        
        log.info("\n✓ All assertions passed!");
        log.info("\n=== Integration Test Completed Successfully ===");
    }
}





