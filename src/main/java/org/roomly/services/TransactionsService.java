package org.roomly.services;

import lombok.RequiredArgsConstructor;
import org.roomly.repositories.TransactionsRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionsService {
    private final TransactionsRepository transactionsRepository;
    
    public String getTransactions () {
        return "TransactionsService";
    }
    
    public String addTransactions () {
        return "TransactionsService";
    }
    
    public String updateTransactions () {
        return "TransactionsService";
    }
    
    public String deleteTransactions () {
        return "TransactionsService";
    }
    
}

