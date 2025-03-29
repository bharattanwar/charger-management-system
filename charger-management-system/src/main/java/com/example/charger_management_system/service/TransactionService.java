package com.example.charger_management_system.service;

import com.example.charger_management_system.model.Transaction;
import com.example.charger_management_system.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    @Autowired
    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public Optional<Transaction> getTransactionById(Long transactionId) {
        return transactionRepository.findById(transactionId);
    }

    public Transaction saveTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    public void deleteTransaction(Long transactionId) {
        transactionRepository.deleteById(transactionId);
    }

    public Transaction updateTransaction(Long transactionId, Transaction updatedTransaction) {
        Optional<Transaction> existingTransaction = transactionRepository.findById(transactionId);
        if (existingTransaction.isPresent()) {
            updatedTransaction.setTransactionId(transactionId); // Ensure ID is consistent
            return transactionRepository.save(updatedTransaction);
        }
        return null;
    }
}
