package org.roomly.resolvers;

import lombok.RequiredArgsConstructor;
import org.roomly.dto.TransactionDTO;
import org.roomly.entities.Transaction;
import org.roomly.services.TransactionsService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class TransactionsResolver {
    private final TransactionsService transactionsService;
    
    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public TransactionDTO addTransaction (@Argument String title,
      @Argument double amount,
      @Argument String recipientId,
      @Argument String type,
      Authentication authentication
    ) {
        return transactionsService.addTransaction(title, amount, recipientId, type, authentication).toDTO();
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<TransactionDTO> transactions (@Argument String householdId, Authentication authentication) {
        return transactionsService
          .getTransactionsByHouseholdId(householdId, authentication)
          .stream()
          .map(Transaction::toDTO)
          .toList();
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public boolean deleteTransaction (@Argument int transactionId, Authentication authentication) {
        transactionsService.deleteTransaction(transactionId, authentication);
        return true;
    }
}

