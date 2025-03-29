package com.example.charger_management_system;

import com.example.charger_management_system.model.Charger;
import com.example.charger_management_system.repository.ChargerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;

@Component
public class DataLoader implements CommandLineRunner {

    private final ChargerRepository chargerRepository;

    @Autowired
    public DataLoader(ChargerRepository chargerRepository) {
        this.chargerRepository = chargerRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        loadInitialData();
    }

    private void loadInitialData() {
        // Create initial Charger data
        Charger charger1 = new Charger("CP001", "Available", Timestamp.from(Instant.now()));
        Charger charger2 = new Charger("CP002", "Available", Timestamp.from(Instant.now()));
        Charger charger3 = new Charger("CP003", "Available", Timestamp.from(Instant.now()));

        // Save Chargers to the database
        chargerRepository.save(charger1);
        chargerRepository.save(charger2);
        chargerRepository.save(charger3);

        System.out.println("Initial Charger data loaded.");
    }
}