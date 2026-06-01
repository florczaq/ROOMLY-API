package org.roomly.services;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.roomly.entities.Household;
import org.roomly.entities.Profile;
import org.roomly.entities.Transaction;
import org.roomly.enums.TransactionType;
import org.roomly.repositories.HouseholdRepository;
import org.roomly.repositories.ProfileRepository;
import org.roomly.repositories.TransactionsRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionsServiceTest {

    @Mock
    private TransactionsRepository transactionsRepository;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private ProfileService profileService;

    @Mock
    private HouseholdRepository householdRepository;

    @InjectMocks
    private TransactionsService transactionsService;

    private static Authentication auth(String accountId) {
        return new UsernamePasswordAuthenticationToken(accountId, null, emptyList());
    }

    @Test
    void addTransactionSavesExpenseWithAuthenticatedSenderAndRecipient() {
        Authentication authentication = auth("account-1");
        Household household = new Household().setId("household-1");
        Profile recipient = new Profile().setId("recipient-1").setHousehold(household);
        Profile sender = new Profile().setId("sender-1").setHousehold(household);

        when(profileRepository.findProfileById("recipient-1")).thenReturn(Optional.of(recipient));
        when(profileService.getCurrentlyAuthenticatedUserProfile(eq(household), any(Authentication.class))).thenReturn(sender);
        when(profileRepository.findProfileById("sender-1")).thenReturn(Optional.of(sender));
        when(transactionsRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Transaction saved = transactionsService.addTransaction("Rent", 1250.0, "recipient-1", "EXPENSE", authentication);

        assertEquals("Rent", saved.getTitle());
        assertEquals(1250.0, saved.getAmount());
        assertSame(sender, saved.getSender());
        assertSame(recipient, saved.getRecipient());
        assertEquals(TransactionType.EXPENSE, saved.getType());
        verify(transactionsRepository).save(any(Transaction.class));
    }

    @Test
    void addTransactionThrowsWhenRecipientProfileDoesNotExist() {
        when(profileRepository.findProfileById("missing-recipient")).thenReturn(Optional.empty());

        assertThrows(
          EntityNotFoundException.class,
          () -> transactionsService.addTransaction("Rent", 100.0, "missing-recipient", "INCOME", auth("account-1"))
        );
    }

    @Test
    void addTransactionThrowsWhenAuthenticatedSenderProfileCannotBeLoaded() {
        Authentication authentication = auth("account-1");
        Household household = new Household().setId("household-1");
        Profile recipient = new Profile().setId("recipient-1").setHousehold(household);
        Profile authenticated = new Profile().setId("sender-1").setHousehold(household);

        when(profileRepository.findProfileById("recipient-1")).thenReturn(Optional.of(recipient));
        when(profileService.getCurrentlyAuthenticatedUserProfile(eq(household), any(Authentication.class))).thenReturn(authenticated);
        when(profileRepository.findProfileById("sender-1")).thenReturn(Optional.empty());

        assertThrows(
          EntityNotFoundException.class,
          () -> transactionsService.addTransaction("Rent", 100.0, "recipient-1", "INCOME", authentication)
        );
    }

    @Test
    void addTransactionThrowsWhenTypeIsInvalid() {
        Authentication authentication = auth("account-1");
        Household household = new Household().setId("household-1");
        Profile recipient = new Profile().setId("recipient-1").setHousehold(household);
        Profile sender = new Profile().setId("sender-1").setHousehold(household);

        when(profileRepository.findProfileById("recipient-1")).thenReturn(Optional.of(recipient));
        when(profileService.getCurrentlyAuthenticatedUserProfile(eq(household), any(Authentication.class))).thenReturn(sender);
        when(profileRepository.findProfileById("sender-1")).thenReturn(Optional.of(sender));

        assertThrows(
          IllegalArgumentException.class,
          () -> transactionsService.addTransaction("Rent", 100.0, "recipient-1", "UNKNOWN", authentication)
        );
    }

    @Test
    void deleteTransactionReturnsTransactionWhenCurrentUserIsSender() {
        Authentication authentication = auth("account-1");
        Household household = new Household().setId("household-1");
        Profile sender = new Profile().setId("sender-1").setHousehold(household);
        Profile recipient = new Profile().setId("recipient-1").setHousehold(household);
        Transaction transaction = new Transaction()
          .setId(10)
          .setSender(sender)
          .setRecipient(recipient)
          .setType(TransactionType.EXPENSE)
          .setTitle("Electricity")
          .setAmount(80.0);

        when(transactionsRepository.findById(10)).thenReturn(Optional.of(transaction));
        when(profileService.getCurrentlyAuthenticatedUserProfile(eq(household), any(Authentication.class))).thenReturn(sender);

        Transaction result = transactionsService.deleteTransaction(10, authentication);

        assertSame(transaction, result);
        verify(transactionsRepository).delete(transaction);
    }

    @Test
    void deleteTransactionThrowsWhenCurrentUserIsNotSender() {
        Authentication authentication = auth("account-2");
        Household household = new Household().setId("household-1");
        Profile sender = new Profile().setId("sender-1").setHousehold(household);
        Profile recipient = new Profile().setId("recipient-1").setHousehold(household);
        Profile otherUser = new Profile().setId("other-1").setHousehold(household);
        Transaction transaction = new Transaction().setId(10).setSender(sender).setRecipient(recipient);

        when(transactionsRepository.findById(10)).thenReturn(Optional.of(transaction));
        when(profileService.getCurrentlyAuthenticatedUserProfile(eq(household), any(Authentication.class))).thenReturn(otherUser);

        assertThrows(SecurityException.class, () -> transactionsService.deleteTransaction(10, authentication));
    }

    @Test
    void deleteTransactionThrowsWhenTransactionDoesNotExist() {
        when(transactionsRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> transactionsService.deleteTransaction(999, auth("account-1")));
    }

    @Test
    void getTransactionsByHouseholdIdReturnsTransactionsForAuthenticatedProfile() {
        Authentication authentication = auth("account-1");
        Household household = new Household().setId("household-1");
        Profile currentProfile = new Profile().setId("profile-1").setHousehold(household);
        List<Transaction> expected = List.of(
          new Transaction().setId(1).setTitle("One"),
          new Transaction().setId(2).setTitle("Two")
        );

        when(householdRepository.findById("household-1")).thenReturn(Optional.of(household));
        when(profileService.getCurrentlyAuthenticatedUserProfile(eq(household), any(Authentication.class))).thenReturn(currentProfile);
        when(transactionsRepository.findAllProfilesTransactions("profile-1")).thenReturn(expected);

        List<Transaction> result = transactionsService.getTransactionsByHouseholdId("household-1", authentication);

        assertEquals(expected, result);
    }

    @Test
    void getTransactionsByHouseholdIdThrowsWhenHouseholdDoesNotExist() {
        when(householdRepository.findById("missing-household")).thenReturn(Optional.empty());

        assertThrows(
          EntityNotFoundException.class,
          () -> transactionsService.getTransactionsByHouseholdId("missing-household", auth("account-1"))
        );
    }
}

