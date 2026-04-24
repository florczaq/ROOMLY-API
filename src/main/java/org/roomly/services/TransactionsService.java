package org.roomly.services;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
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
    
    public List<Transaction> getTransactionsByHouseholdId (String householdId) {
        Household household = householdRepository.findById(householdId)
          .orElseThrow(() -> new EntityNotFoundException("Household not found with id: " + householdId));
        String profileId = profileService.getCurrentlyAuthenticatedUserProfile(household).getId();
        return transactionsRepository.findAllProfilesTransactions(profileId);
    }
}
