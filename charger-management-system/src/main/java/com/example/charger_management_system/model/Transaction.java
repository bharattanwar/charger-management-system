package com.example.charger_management_system.model;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;

    private String chargerId;

    private Timestamp startTime;

    private Timestamp stopTime;

    private Integer meterStart;

    private Integer meterStop;

    public Transaction() {}

    public Transaction(String chargerId, Timestamp startTime, Timestamp stopTime, Integer meterStart, Integer meterStop) {
        this.chargerId = chargerId;
        this.startTime = startTime;
        this.stopTime = stopTime;
        this.meterStart = meterStart;
        this.meterStop = meterStop;
    }

    // Getters and setters...

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public String getChargerId() {
        return chargerId;
    }

    public void setChargerId(String chargerId) {
        this.chargerId = chargerId;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getStopTime() {
        return stopTime;
    }

    public void setStopTime(Timestamp stopTime) {
        this.stopTime = stopTime;
    }

    public Integer getMeterStart() {
        return meterStart;
    }

    public void setMeterStart(Integer meterStart) {
        this.meterStart = meterStart;
    }

    public Integer getMeterStop() {
        return meterStop;
    }

    public void setMeterStop(Integer meterStop) {
        this.meterStop = meterStop;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId=" + transactionId +
                ", chargerId='" + chargerId + '\'' +
                ", startTime=" + startTime +
                ", stopTime=" + stopTime +
                ", meterStart=" + meterStart +
                ", meterStop=" + meterStop +
                '}';
    }
}