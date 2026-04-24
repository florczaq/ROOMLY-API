package org.roomly.resolvers;

import lombok.RequiredArgsConstructor;
import org.roomly.dto.TransactionDTO;
import org.roomly.services.TransactionsService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class TransactionsResolver {
    private final TransactionsService transactionsService;
    
    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public TransactionDTO addTransaction (@Argument String title,
      @Argument double amount,
      @Argument String recipientId,
      @Argument String type
    ) {
        return transactionsService.addTransaction(title, amount, recipientId, type).toDTO();
    }
    
}

