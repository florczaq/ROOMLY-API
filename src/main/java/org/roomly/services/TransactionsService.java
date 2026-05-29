package org.roomly.services;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.roomly.annotations.Notifiable;
import org.roomly.entities.Household;
import org.roomly.entities.Transaction;
import org.roomly.enums.TransactionType;
import org.roomly.repositories.HouseholdRepository;
import org.roomly.repositories.ProfileRepository;
import org.roomly.repositories.TransactionsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing financial transactions within a household.
 * <p>
 * Transactions represent money movements between household members and can be
 * of type {@link TransactionType#EXPENSE} or {@link TransactionType#INCOME}.
 * The authenticated user's profile within the relevant household is resolved
 * automatically as the sender.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class TransactionsService {
    private final TransactionsRepository transactionsRepository;
    private final ProfileRepository profileRepository;
    private final ProfileService profileService;
    private final HouseholdRepository householdRepository;

    /**
     * Creates a new transaction from the currently authenticated user to the specified recipient.
     * <p>
     * The sender is resolved from the authenticated user's profile within the recipient's household.
     * Both sender and recipient must belong to the same household. Sends a push notification to
     * the recipient upon success.
     * </p>
     *
     * @param title       human-readable label for the transaction
     * @param amount      monetary value of the transaction
     * @param recipientId ID of the profile receiving the transaction
     * @param type        transaction type as a string matching a {@link TransactionType} constant
     * @return the persisted {@link Transaction}
     * @throws EntityNotFoundException if the recipient or resolved sender profile does not exist
     * @throws IllegalArgumentException if {@code type} does not match any {@link TransactionType}
     */
    @Transactional
    @Notifiable(
      title = "#{#title}: #{#type == 'EXPENSE' ? '+' : '-'} #{#amount}",
      description = "A new transaction has been added to your household.",
      recipientProfileId = "#{#recipientId}"
    )
    public Transaction addTransaction (String title,
      double amount,
      String recipientId,
      String type
    ) {
        var recipient = profileRepository
          .findProfileById(recipientId)
          .orElseThrow(() -> new EntityNotFoundException("Recipient profile not found"));
        String senderId = profileService.getCurrentlyAuthenticatedUserProfile(recipient.getHousehold()).getId();
        var sender = profileRepository
          .findProfileById(senderId)
          .orElseThrow(() -> new EntityNotFoundException("Sender profile not found"));

        return transactionsRepository.save(
          new Transaction()
            .setTitle(title)
            .setAmount(amount)
            .setSender(sender)
            .setRecipient(recipient)
            .setType(TransactionType.valueOf(type)));

    }

    /**
     * Deletes a transaction by ID, restricted to the transaction's original sender.
     * <p>
     * The currently authenticated user must be the sender of the transaction; any other
     * profile will receive a {@link SecurityException}. Sends a push notification to the
     * recipient upon success.
     * </p>
     *
     * @param transactionId ID of the transaction to delete
     * @return the deleted {@link Transaction} (used by {@code @Notifiable} for notification data)
     * @throws EntityNotFoundException if no transaction exists with the given ID
     * @throws SecurityException       if the authenticated user is not the transaction's sender
     */
    @Notifiable(
      title = "Transaction Deleted: #{#result.title}",
      description = "#{#result.sender.nickname} deleted a transaction: #{resulty.type == 'EXPENSE' ? '+' : '-'} #{#result.amount}",
      recipientProfileId = "#{#result.recipient.id}"
    )
    public Transaction deleteTransaction (int transactionId) {
        Transaction transaction = this.getTransactionById(transactionId);
        var currentUserProfileId = profileService.getCurrentlyAuthenticatedUserProfile(
          transaction.getSender().getHousehold()
        ).getId();

        // Only the sender of the transaction can delete it
        if (!transaction.getSender().getId().equals(currentUserProfileId)) {
            throw new SecurityException("You are not authorized to delete this transaction.");
        }

        return transaction;
    }

    /**
     * Returns all transactions visible to the currently authenticated user within a household.
     *
     * @param householdId ID of the household to query
     * @return list of transactions involving the authenticated user's profile
     * @throws EntityNotFoundException if no household exists with the given ID
     */
    public List<Transaction> getTransactionsByHouseholdId (String householdId) {
        Household household = householdRepository.findById(householdId)
          .orElseThrow(() -> new EntityNotFoundException("Household not found with id: " + householdId));
        String profileId = profileService.getCurrentlyAuthenticatedUserProfile(household).getId();
        return transactionsRepository.findAllProfilesTransactions(profileId);
    }

    /**
     * Fetches a single transaction by its ID.
     *
     * @param transactionId ID of the transaction
     * @return the matching {@link Transaction}
     * @throws EntityNotFoundException if no transaction exists with the given ID
     */
    private Transaction getTransactionById (int transactionId) {
        return transactionsRepository.findById(transactionId)
          .orElseThrow(() -> new EntityNotFoundException("Transaction not found with id: " + transactionId));
    }
}
