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

@Service
@RequiredArgsConstructor
public class TransactionsService {
    private final TransactionsRepository transactionsRepository;
    private final ProfileRepository profileRepository;
    private final ProfileService profileService;
    private final HouseholdRepository householdRepository;
    
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
    
    
    public List<Transaction> getTransactionsByHouseholdId (String householdId) {
        Household household = householdRepository.findById(householdId)
          .orElseThrow(() -> new EntityNotFoundException("Household not found with id: " + householdId));
        String profileId = profileService.getCurrentlyAuthenticatedUserProfile(household).getId();
        return transactionsRepository.findAllProfilesTransactions(profileId);
    }
    
    private Transaction getTransactionById (int transactionId) {
        return transactionsRepository.findById(transactionId)
          .orElseThrow(() -> new EntityNotFoundException("Transaction not found with id: " + transactionId));
    }
}
