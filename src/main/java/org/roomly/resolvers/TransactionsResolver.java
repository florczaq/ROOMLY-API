package org.roomly.resolvers;

import lombok.RequiredArgsConstructor;
import org.roomly.services.TransactionsService;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class TransactionsResolver {
    private final TransactionsService transactionsService;
    
    
    public String transactions () {
        return "TransactionsResolver";
    }
    
    public String addTransactions () {
        return "TransactionsResolver";
    }
    
    public String updateTransactions () {
        return "TransactionsResolver";
    }
    
    public String deleteTransactions () {
        return "TransactionsResolver";
    }
    
}

