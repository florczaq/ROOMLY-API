package org.roomly.integrationTests;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.roomly.notifications.dto.NotificationDTO;
import org.roomly.notifications.service.NotificationService;
import org.roomly.repositories.ProfileRepository;
import org.roomly.security.authentication.jwt.dto.TokenResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class NotificationIntegrationTest {
    
    @Autowired
    private WebApplicationContext context;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private ProfileRepository profileRepository;
    
    @Autowired
    private NotificationService notificationService;

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    private MockMvc mockMvc;
    
    @BeforeEach
    void setUp () {
        mockMvc = MockMvcBuilders
          .webAppContextSetup(context)
          .apply(springSecurity())
          .build();

        cleanupDatabase();
    }

    private void cleanupDatabase () {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        try {
            List<String> tableNames = jdbcTemplate.queryForList(
              "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC' AND TABLE_TYPE = 'BASE TABLE'",
              String.class
            );

            for (String tableName : tableNames) {
                jdbcTemplate.execute("TRUNCATE TABLE \"" + tableName + "\"");
            }
        } finally {
            jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
        }
    }
    
    // ─── helpers ─────────────────────────────────────────────────────────────
    
    private TokenResponse registerAndCreateProfile (String email, String password,
      String nickname
    ) throws Exception {
        // Register
        MvcResult reg = mockMvc.perform(post("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(Map.of("email", email, "password", password))))
          .andExpect(status().isOk())
          .andReturn();
        
        TokenResponse tokens = objectMapper.readValue(
          reg.getResponse().getContentAsString(), TokenResponse.class);
        
        // Create household + profile so a Profile row exists for this account
        String mutation = String.format(
          """
          {
              "query": "mutation { createHousehold(name: \\"Home\\", membersLimit: 5, nickname: \\"%s\\", avatarName: \\"Cat\\", avatarColorName: \\"blue\\") { id } }"
          }
          """, nickname
        );
        
        mockMvc.perform(post("/graphql")
            .header("Authorization", "Bearer " + tokens.accessToken())
            .contentType(MediaType.APPLICATION_JSON)
            .content(mutation))
          .andExpect(status().isOk());
        
        return tokens;
    }
    
    private List<NotificationDTO> getNotifications (String accessToken) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/notifications")
            .header("Authorization", "Bearer " + accessToken))
          .andExpect(status().isOk())
          .andReturn();
        
        return objectMapper.readValue(
          result.getResponse().getContentAsString(),
          new TypeReference<>() {}
        );
    }
    
    private record HouseholdContext(
      TokenResponse ownerTokens,
      TokenResponse memberTokens,
      String householdId,
      String memberProfileId
    ) {}

    private TokenResponse registerUser (String email, String password) throws Exception {
        MvcResult reg = mockMvc.perform(post("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(Map.of("email", email, "password", password))))
          .andExpect(status().isOk())
          .andReturn();

        return objectMapper.readValue(reg.getResponse().getContentAsString(), TokenResponse.class);
    }

    private TokenResponse loginUser (String email, String password) throws Exception {
        MvcResult login = mockMvc.perform(post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(Map.of("email", email, "password", password))))
          .andExpect(status().isOk())
          .andReturn();

        return objectMapper.readValue(login.getResponse().getContentAsString(), TokenResponse.class);
    }

    private HouseholdContext createHouseholdWithTwoMembers (
      String ownerEmail,
      String ownerNickname,
      String memberEmail,
      String memberNickname
    ) throws Exception {
        TokenResponse ownerTokens = registerUser(ownerEmail, "Password123");

        String createHouseholdMutation = String.format(
          """
          {
              "query": "mutation { createHousehold(name: \\\"Home\\\", membersLimit: 5, nickname: \\\"%s\\\", avatarName: \\\"Cat\\\", avatarColorName: \\\"blue\\\") { id joinCode } }"
          }
          """, ownerNickname
        );

        MvcResult ownerCreateResult = mockMvc.perform(post("/graphql")
            .header("Authorization", "Bearer " + ownerTokens.accessToken())
            .contentType(MediaType.APPLICATION_JSON)
            .content(createHouseholdMutation))
          .andExpect(status().isOk())
          .andReturn();

        Map<String, Object> ownerPayload = objectMapper.readValue(ownerCreateResult.getResponse().getContentAsString(), Map.class);
        Map<String, Object> ownerData = (Map<String, Object>) ownerPayload.get("data");
        Map<String, Object> createdHousehold = (Map<String, Object>) ownerData.get("createHousehold");
        String householdId = (String) createdHousehold.get("id");
        String joinCode = (String) createdHousehold.get("joinCode");

        TokenResponse memberTokens = registerUser(memberEmail, "Password123");

        String joinHouseholdMutation = String.format(
          """
          {
              "query": "mutation { joinHousehold(nickname: \\\"%s\\\", avatarName: \\\"Dog\\\", avatarColorName: \\\"white\\\", joinCode: \\\"%s\\\") { id } }"
          }
          """, memberNickname, joinCode
        );

        mockMvc.perform(post("/graphql")
            .header("Authorization", "Bearer " + memberTokens.accessToken())
            .contentType(MediaType.APPLICATION_JSON)
            .content(joinHouseholdMutation))
          .andExpect(status().isOk());

        TokenResponse refreshedMemberTokens = loginUser(memberEmail, "Password123");

        String memberProfileId = profileRepository.findAll().stream()
          .filter(p -> p.getAccount().getEmail().equals(memberEmail))
          .findFirst()
          .orElseThrow()
          .getId();

        return new HouseholdContext(ownerTokens, refreshedMemberTokens, householdId, memberProfileId);
    }

    // ─── tests ────────────────────────────────────────────────────────────────
    
    @Test
    void getNotifications_unauthenticated_returns401 () throws Exception {
        mockMvc.perform(get("/api/notifications"))
          .andExpect(status().isUnauthorized());
    }
    
    @Test
    void getNotifications_noNotifications_returnsEmptyList () throws Exception {
        System.out.println("=== Test: get notifications – empty list ===");
        
        TokenResponse tokens = registerAndCreateProfile(
          "user@roomly.com", "Password123", "UserA");
        
        List<NotificationDTO> notifications = getNotifications(tokens.accessToken());
        
        System.out.println("  Notifications: " + notifications);
        assertTrue(notifications.isEmpty(), "Should return empty list when no notifications exist");
        System.out.println("✓ Empty list verified");
    }
    
    @Test
    void getNotifications_returnsOnlyUnread () throws Exception {
        System.out.println("=== Test: get notifications – only unread ===");
        
        TokenResponse tokens = registerAndCreateProfile(
          "user2@roomly.com", "Password123", "UserB");
        
        String profileId = profileRepository
          .findAll()
          .stream()
          .filter(p -> p.getAccount().getEmail().equals("user2@roomly.com"))
          .findFirst()
          .orElseThrow()
          .getId();
        
        // Create 3 notifications
        notificationService.createAndSaveNotification("Title 1", "Message 1", profileId);
        notificationService.createAndSaveNotification("Title 2", "Message 2", profileId);
        notificationService.createAndSaveNotification("Title 3", "Message 3", profileId);
        
        System.out.println("  Created 3 notifications for profile: " + profileId);
        
        List<NotificationDTO> notifications = getNotifications(tokens.accessToken());
        
        System.out.println("  Returned notifications: " + notifications.size());
        assertEquals(3, notifications.size(), "Should return all 3 unread notifications");
        
        // All should be for the correct profile
        notifications.forEach(n -> {
            assertEquals(profileId, n.profileId(), "Notification should belong to correct profile");
            assertNotNull(n.id(), "Notification ID should not be null");
            assertNotNull(n.timestamp(), "Timestamp should not be null");
        });
        
        // Verify titles
        List<String> titles = notifications.stream().map(NotificationDTO::title).toList();
        assertTrue(titles.contains("Title 1"));
        assertTrue(titles.contains("Title 2"));
        assertTrue(titles.contains("Title 3"));
        
        System.out.println("✓ All 3 unread notifications returned with correct data");
    }
    
    @Test
    void markAsRead_removesNotificationsFromList () throws Exception {
        System.out.println("=== Test: mark notifications as read ===");
        
        TokenResponse tokens = registerAndCreateProfile(
          "user3@roomly.com", "Password123", "UserC");
        
        String profileId = profileRepository
          .findAll()
          .stream()
          .filter(p -> p.getAccount().getEmail().equals("user3@roomly.com"))
          .findFirst()
          .orElseThrow()
          .getId();
        
        notificationService.createAndSaveNotification("Unread 1", "Msg 1", profileId);
        notificationService.createAndSaveNotification("Unread 2", "Msg 2", profileId);
        notificationService.createAndSaveNotification("Unread 3", "Msg 3", profileId);
        
        List<NotificationDTO> before = getNotifications(tokens.accessToken());
        assertEquals(3, before.size(), "Should have 3 unread before marking");
        
        // Mark first two as read
        List<String> toMark = before.stream().map(NotificationDTO::id).limit(2).toList();
        System.out.println("  Marking as read: " + toMark);
        
        mockMvc.perform(post("/api/notifications/markAsRead")
            .header("Authorization", "Bearer " + tokens.accessToken())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(toMark)))
          .andExpect(status().isOk());
        
        List<NotificationDTO> after = getNotifications(tokens.accessToken());
        System.out.println("  Notifications after marking: " + after.size());
        
        assertEquals(1, after.size(), "Should have 1 unread notification after marking 2 as read");
        assertNotEquals(toMark.get(0), after.getFirst().id(), "Remaining should not be one of the marked ones");
        assertNotEquals(toMark.get(1), after.getFirst().id(), "Remaining should not be one of the marked ones");
        
        System.out.println("✓ Marked notifications removed from unread list");
    }
    
    @Test
    void markAsRead_all_returnsEmptyList () throws Exception {
        System.out.println("=== Test: mark all notifications as read ===");
        
        TokenResponse tokens = registerAndCreateProfile(
          "user4@roomly.com", "Password123", "UserD");
        
        String profileId = profileRepository
          .findAll()
          .stream()
          .filter(p -> p.getAccount().getEmail().equals("user4@roomly.com"))
          .findFirst()
          .orElseThrow()
          .getId();
        
        notificationService.createAndSaveNotification("N1", "M1", profileId);
        notificationService.createAndSaveNotification("N2", "M2", profileId);
        
        List<NotificationDTO> all = getNotifications(tokens.accessToken());
        List<String> allIds = all.stream().map(NotificationDTO::id).toList();
        
        mockMvc.perform(post("/api/notifications/markAsRead")
            .header("Authorization", "Bearer " + tokens.accessToken())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(allIds)))
          .andExpect(status().isOk());
        
        List<NotificationDTO> empty = getNotifications(tokens.accessToken());
        assertTrue(empty.isEmpty(), "Should return empty list after all notifications are marked as read");
        
        System.out.println("✓ All notifications marked as read, list is empty");
    }
    
    @Test
    void notifications_areIsolatedBetweenAccounts () throws Exception {
        System.out.println("=== Test: notifications are isolated between accounts ===");
        
        TokenResponse tokensA = registerAndCreateProfile("a@roomly.com", "Password123", "UserA2");
        TokenResponse tokensB = registerAndCreateProfile("b@roomly.com", "Password123", "UserB2");
        
        String profileAId = profileRepository.findAll().stream()
          .filter(p -> p.getAccount().getEmail().equals("a@roomly.com"))
          .findFirst().orElseThrow().getId();
        
        notificationService.createAndSaveNotification("For A", "Only for A", profileAId);
        
        List<NotificationDTO> notifA = getNotifications(tokensA.accessToken());
        List<NotificationDTO> notifB = getNotifications(tokensB.accessToken());
        
        System.out.println("  Notifications for A: " + notifA.size());
        System.out.println("  Notifications for B: " + notifB.size());
        
        assertEquals(1, notifA.size(), "Account A should see 1 notification");
        assertTrue(notifB.isEmpty(), "Account B should see no notifications");
        
        System.out.println("✓ Notifications are correctly isolated between accounts");
    }

    @Test
    void addingEventAndTransaction_returnsTransactionNotificationExpressionErrorButKeepsMemberAuthenticated () throws Exception {
        HouseholdContext context = createHouseholdWithTwoMembers(
          "owner.notifications@roomly.com",
          "OwnerNotifications",
          "member.notifications@roomly.com",
          "MemberNotifications"
        );

        String addEventMutation = """
          {
              "query": "mutation { addEvent(name: \\\"House Meeting\\\", description: \\\"Weekly sync\\\", startTime: \\\"2030-01-10T18:00:00\\\", endTime: \\\"2030-01-10T19:00:00\\\") { id name } }"
          }
          """;

        mockMvc.perform(post("/graphql")
            .header("Authorization", "Bearer " + context.ownerTokens().accessToken())
            .contentType(MediaType.APPLICATION_JSON)
            .content(addEventMutation))
          .andExpect(status().isOk());

        String addTransactionMutation = String.format(
          """
          {
              "query": "mutation { addTransaction(title: \\\"Rent Share\\\", amount: 250.0, recipientId: \\\"%s\\\", type: \\\"EXPENSE\\\") { id title amount recipient { id } } }"
          }
          """, context.memberProfileId()
        );

        MvcResult addTransactionResult = mockMvc.perform(post("/graphql")
            .header("Authorization", "Bearer " + context.ownerTokens().accessToken())
            .contentType(MediaType.APPLICATION_JSON)
            .content(addTransactionMutation))
          .andExpect(status().isOk())
          .andReturn();

        String transactionResponse = addTransactionResult.getResponse().getContentAsString();
        assertTrue(
          transactionResponse.contains("\"errors\""),
          "Expected transaction mutation to return GraphQL errors with current notification expression configuration"
        );

        List<NotificationDTO> memberNotifications = getNotifications(context.memberTokens().accessToken());
        assertTrue(memberNotifications.isEmpty(), "Expected no notification when transaction mutation returns errors");
    }
}
