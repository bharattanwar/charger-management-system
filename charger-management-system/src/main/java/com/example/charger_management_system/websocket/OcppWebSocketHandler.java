package com.example.charger_management_system.websocket;

import com.example.charger_management_system.model.Charger;
import com.example.charger_management_system.model.Transaction;
import com.example.charger_management_system.repository.ChargerRepository;
import com.example.charger_management_system.repository.TransactionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
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
    private static final Logger logger = LoggerFactory.getLogger(OcppWebSocketHandler.class);

    public OcppWebSocketHandler(ChargerRepository chargerRepository, TransactionRepository transactionRepository) {
        this.chargerRepository = chargerRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String token = session.getUri().getQuery();
        if (token == null || !token.equals("validToken")) {
            session.close();
            logger.warn("Unauthorized connection attempt.");
            return;
        }
        logger.info("Connection established from: {}", session.getRemoteAddress());
        super.afterConnectionEstablished(session);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        logger.info("Received message: {}", message.getPayload());
        String payload = message.getPayload();
        try {
            JsonNode json = objectMapper.readTree(payload);
            processOcppMessage(session, json);
        } catch (Exception e) {
            logger.error("Error processing message: {}", e.getMessage(), e);
            sendErrorResponse(session, "Invalid message format.", 400);
        }
    }

    private void processOcppMessage(WebSocketSession session, JsonNode json) throws IOException {
        try {
            int messageType = json.get(0).asInt();
            String messageId = json.get(1).asText();
            String action = json.get(2).asText();
            JsonNode payload = json.get(3);

            logger.info("Processing action: {}", action);

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
                    logger.warn("Unknown action: {}", action);
                    sendErrorResponse(session, messageId, "Unknown action.", 400);
            }
        } catch (Exception e) {
            logger.error("Error processing action: {}", e.getMessage(), e);
            sendErrorResponse(session, "Internal server error.", 500);
        }
    }

    private void handleBootNotification(WebSocketSession session, String messageId, JsonNode payload) throws IOException {
        String chargerId = payload.get("chargePointSerialNumber").asText();
        sessions.put(chargerId, session);
        logger.info("BootNotification received from: {}. Current Sessions map: {}", chargerId, sessions);

        Charger charger = chargerRepository.findById(chargerId).orElse(new Charger(chargerId, "Available", Timestamp.from(Instant.now())));
        charger.setLastHeartbeat(Timestamp.from(Instant.now()));
        chargerRepository.save(charger);

        sendResponse(session, messageId, "BootNotification", "{\"status\":\"Accepted\"}");
    }

    private void handleHeartbeat(WebSocketSession session, String messageId, JsonNode payload) throws IOException {
        String chargerId = getChargerIdFromSession(session);
        if (chargerId != null) {
            logger.info("Heartbeat received from: {}", chargerId);
            Charger charger = chargerRepository.findById(chargerId).orElse(null);
            if (charger != null) {
                try {
                    chargerRepository.save(charger);
                    logger.info("Heartbeat saved successfully.");
                } catch (Exception e) {
                    logger.error("Error saving heartbeat: {}", e.getMessage(), e);
                    e.printStackTrace();
                }
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
        logger.info("handleStartTransaction called");
        String chargerId = getChargerIdFromSession(session);
        if (chargerId != null) {
            if (!payload.has("meterStart") || !payload.has("idTag")) {
                logger.warn("Invalid StartTransaction payload.");
                sendErrorResponse(session, messageId, "Invalid payload.", 400);
                return;
            }

            Timestamp startTime = Timestamp.from(Instant.now());
            int meterStart = payload.get("meterStart").asInt();
            Transaction transaction = new Transaction(chargerId, startTime, null, meterStart, null);

            logger.info("StartTransaction: ChargerId={}, meterStart={}", chargerId, meterStart);
            logger.info("Transaction to save: {}", transaction);
            try {
                saveTransactionAsync(transaction);
                logger.info("Transaction saved successfully.");
            } catch (Exception e) {
                logger.error("Error saving transaction: {}", e.getMessage(), e);
                e.printStackTrace();
            }

            sendResponse(session, messageId, "StartTransaction", "{\"idTagInfo\":{\"status\":\"Accepted\"},\"transactionId\":" + transaction.getTransactionId() + "}");
        }
    }

    private void handleStopTransaction(WebSocketSession session, String messageId, JsonNode payload) throws IOException {
        String chargerId = getChargerIdFromSession(session);
        if (chargerId != null) {
            Long transactionId = payload.get("transactionId").asLong();
            int meterStop = payload.get("meterStop").asInt();
            Transaction transaction = transactionRepository.findById(transactionId).orElse(null);
            if (transaction != null) {
                try {
                    transaction.setStopTime(Timestamp.from(Instant.now()));
                    transaction.setMeterStop(meterStop);
                    transactionRepository.save(transaction);
                    logger.info("Transaction stopped successfully.");
                } catch (Exception e) {
                    logger.error("Error stopping transaction: {}", e.getMessage(), e);
                    e.printStackTrace();
                }
            }
            sendResponse(session, messageId, "StopTransaction", "{\"idTagInfo\":{\"status\":\"Accepted\"}}");
        }
    }

    private void sendResponse(WebSocketSession session, String messageId, String action, String payload) throws IOException {
        String response = String.format("[2,\"%s\",\"%s\",%s]", messageId, action, payload);
        session.sendMessage(new TextMessage(response));
    }

    private String getChargerIdFromSession(WebSocketSession session) {
        logger.info("getChargerIdFromSession called. Current Sessions map: {}", sessions);
        for (Map.Entry<String, WebSocketSession> entry : sessions.entrySet()) {
            if (entry.getValue().equals(session)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void sendErrorResponse(WebSocketSession session, String errorMessage, int errorCode) throws IOException {
        String response = String.format("[4,{\"code\":%d,\"message\":\"%s\"}]", errorCode, errorMessage);
        session.sendMessage(new TextMessage(response));
    }

    private void sendErrorResponse(WebSocketSession session, String messageId, String errorMessage, int errorCode) throws IOException {
        String response = String.format("[4,\"%s\",{\"code\":%d,\"message\":\"%s\"}]", messageId, errorCode, errorMessage);
        session.sendMessage(new TextMessage(response));
    }

    @Async
    public void saveTransactionAsync(Transaction transaction) {
        transactionRepository.save(transaction);
        logger.info("Transaction saved asynchronously.");
    }
}