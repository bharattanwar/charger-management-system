package com.example.charger_management_system.repository;

import com.example.charger_management_system.model.Charger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChargerRepository extends JpaRepository<Charger, String> {
}