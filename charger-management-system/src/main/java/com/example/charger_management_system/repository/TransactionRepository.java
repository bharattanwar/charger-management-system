package com.example.charger_management_system.repository;

import com.example.charger_management_system.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.sql.Timestamp;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}