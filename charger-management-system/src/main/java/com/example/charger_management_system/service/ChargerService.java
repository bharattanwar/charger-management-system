package com.example.charger_management_system.service;

import com.example.charger_management_system.model.Charger;
import com.example.charger_management_system.repository.ChargerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class ChargerService {

    private final ChargerRepository chargerRepository;

    @Autowired
    public ChargerService(ChargerRepository chargerRepository) {
        this.chargerRepository = chargerRepository;
    }

    public List<Charger> getAllChargers() {
        return chargerRepository.findAll();
    }

    public Optional<Charger> getChargerById(String chargerId) {
        return chargerRepository.findById(chargerId);
    }

    public Charger saveCharger(Charger charger) {
        return chargerRepository.save(charger);
    }

    public void deleteCharger(String chargerId) {
        chargerRepository.deleteById(chargerId);
    }

    public Charger updateCharger(String chargerId, Charger updatedCharger) {
        Optional<Charger> existingCharger = chargerRepository.findById(chargerId);
        if (existingCharger.isPresent()) {
            updatedCharger.setChargerId(chargerId); // Ensure ID is consistent
            return chargerRepository.save(updatedCharger);
        }
        return null;
    }
}