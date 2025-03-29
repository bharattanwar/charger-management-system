package com.example.charger_management_system.controller;

import com.example.charger_management_system.model.Charger;
import com.example.charger_management_system.model.Transaction;
import com.example.charger_management_system.service.ChargerService;
import com.example.charger_management_system.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/chargers")
public class ChargerController {

    private final ChargerService chargerService;

    @Autowired
    public ChargerController(ChargerService chargerService) {
        this.chargerService = chargerService;
    }

    @GetMapping
    public ResponseEntity<List<Charger>> getAllChargers() {
        return ResponseEntity.ok(chargerService.getAllChargers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Charger> getChargerById(@PathVariable String id) {
        Optional<Charger> charger = chargerService.getChargerById(id);
        return charger.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Charger> createCharger(@RequestBody Charger charger) {
        return ResponseEntity.status(HttpStatus.CREATED).body(chargerService.saveCharger(charger));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Charger> updateCharger(@PathVariable String id, @RequestBody Charger updatedCharger) {
        Charger charger = chargerService.updateCharger(id, updatedCharger);
        return charger != null ? ResponseEntity.ok(charger) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCharger(@PathVariable String id) {
        chargerService.deleteCharger(id);
        return ResponseEntity.noContent().build();
    }
}
