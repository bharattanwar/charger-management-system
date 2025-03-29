package com.example.charger_management_system.websocket;

import com.example.charger_management_system.model.Charger;
import com.example.charger_management_system.model.Transaction;
import com.example.charger_management_system.repository.ChargerRepository;
import com.example.charger_management_system.repository.TransactionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
public class OcppWebSocketHandler extends TextWebSocketHandler {

    private final Map<String, WebSocketSession> sessions = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ChargerRepository chargerRepository;
    private final TransactionRepository transactionRepository;

    public OcppWebSocketHandler(ChargerRepository chargerRepository, TransactionRepository transactionRepository) {
        this.chargerRepository = chargerRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        String payload = message.getPayload();
        try {
            JsonNode json = objectMapper.readTree(payload);
            processOcppMessage(session, json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processOcppMessage(WebSocketSession session, JsonNode json) throws IOException {
        int messageType = json.get(0).asInt();
        String messageId = json.get(1).asText();
        String action = json.get(2).asText();
        JsonNode payload = json.get(3);

        System.out.println("Processing action: " + action);
        System.out.println("Before switch statement");

        switch (action) {
            case "BootNotification":
                handleBootNotification(session, messageId, payload);
                break;
            case "Heartbeat":
                handleHeartbeat(session, messageId, payload);
                break;
            case "StatusNotification":
                handleStatusNotification(session, messageId, payload);
                break;
            case "StartTransaction":
                handleStartTransaction(session, messageId, payload);
                break;
            case "StopTransaction":
                handleStopTransaction(session, messageId, payload);
                break;
            default:
                System.out.println("Unknown action: " + action);
        }
        System.out.println("After switch statement");
    }

    private void handleBootNotification(WebSocketSession session, String messageId, JsonNode payload) throws IOException {
        String chargerId = payload.get("chargePointSerialNumber").asText();
        sessions.put(chargerId, session);
        System.out.println("BootNotification received from: " + chargerId);
        System.out.println("Current Sessions map: " + sessions);

        Charger charger = chargerRepository.findById(chargerId).orElse(new Charger(chargerId, "Available", Timestamp.from(Instant.now())));
        charger.setLastHeartbeat(Timestamp.from(Instant.now()));
        chargerRepository.save(charger);

        sendResponse(session, messageId, "BootNotification", "{\"status\":\"Accepted\"}");
    }

    private void handleHeartbeat(WebSocketSession session, String messageId, JsonNode payload) throws IOException {
        String chargerId = getChargerIdFromSession(session);
        if (chargerId != null) {
            System.out.println("Heartbeat received from: " + chargerId);
            Charger charger = chargerRepository.findById(chargerId).orElse(null);
            if (charger != null) {
                charger.setLastHeartbeat(Timestamp.from(Instant.now()));
                chargerRepository.save(charger);
            }
            sendResponse(session, messageId, "Heartbeat", "{}");
        }
    }

    private void handleStatusNotification(WebSocketSession session, String messageId, JsonNode payload) throws IOException {
        String chargerId = getChargerIdFromSession(session);
        if (chargerId != null) {
            String status = payload.get("status").asText();
            Charger charger = chargerRepository.findById(chargerId).orElse(null);
            if (charger != null) {
                charger.setStatus(status);
                chargerRepository.save(charger);
            }
            sendResponse(session, messageId, "StatusNotification", "{}");
        }
    }

    private void handleStartTransaction(WebSocketSession session, String messageId, JsonNode payload) throws IOException {
        System.out.println("handleStartTransaction called");
        String chargerId = getChargerIdFromSession(session);
        if (chargerId != null) {
            Timestamp startTime = Timestamp.from(Instant.now());
            int meterStart = payload.get("meterStart").asInt();
            Transaction transaction = new Transaction(chargerId, startTime, null, meterStart, null);

            System.out.println("StartTransaction: ChargerId=" + chargerId + ", meterStart=" + meterStart);
            System.out.println("Transaction to save: " + transaction);
            try {
                transactionRepository.save(transaction);
                System.out.println("Transaction saved successfully.");
            } catch (Exception e) {
                System.err.println("Error saving transaction: " + e.getMessage());
                e.printStackTrace();
            }

            sendResponse(session, messageId, "StartTransaction", "{\"idTagInfo\":{\"status\":\"Accepted\"},\"transactionId\":"+transaction.getTransactionId()+"}");
        }
    }

    private void handleStopTransaction(WebSocketSession session, String messageId, JsonNode payload) throws IOException {
        String chargerId = getChargerIdFromSession(session);
        if (chargerId != null) {
            Long transactionId = payload.get("transactionId").asLong();
            int meterStop = payload.get("meterStop").asInt();
            Transaction transaction = transactionRepository.findById(transactionId).orElse(null);
            if (transaction != null) {
                transaction.setStopTime(Timestamp.from(Instant.now()));
                transaction.setMeterStop(meterStop);
                transactionRepository.save(transaction);
            }
            sendResponse(session, messageId, "StopTransaction", "{\"idTagInfo\":{\"status\":\"Accepted\"}}");
        }
    }

    private void sendResponse(WebSocketSession session, String messageId, String action, String payload) throws IOException {
        String response = String.format("[2,\"%s\",\"%s\",%s]", messageId, action, payload);
        session.sendMessage(new TextMessage(response));
    }

    private String getChargerIdFromSession(WebSocketSession session) {
        System.out.println("getChargerIdFromSession called");
        System.out.println("Current Sessions map: " + sessions);
        for (Map.Entry<String, WebSocketSession> entry : sessions.entrySet()) {
            if (entry.getValue().equals(session)) {
                return entry.getKey();
            }
        }
        return null;
    }
}