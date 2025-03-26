package com.example.charger_management_system.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.security.Timestamp;

@Entity
@Table(name = "chargers")
public class Charger {

    @Id
    private String chargerId;

    private String status;

    private Timestamp lastHeartbeat;

    // Constructors, getters, setters
    public Charger(){}

    public Charger(String chargerId, String status, Timestamp lastHeartbeat) {
        this.chargerId = chargerId;
        this.status = status;
        this.lastHeartbeat = lastHeartbeat;
    }

    public String getChargerId() {
        return chargerId;
    }

    public void setChargerId(String chargerId) {
        this.chargerId = chargerId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(Timestamp lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }
}