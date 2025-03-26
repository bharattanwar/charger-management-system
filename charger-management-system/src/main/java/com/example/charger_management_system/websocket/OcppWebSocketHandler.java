package com.example.charger_management_system.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class OcppWebSocketHandler extends TextWebSocketHandler {

    private final Map<String, WebSocketSession> sessions = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        String payload = message.getPayload();
        try {
            JsonNode json = objectMapper.readTree(payload);
            processOcppMessage(session, json);
        } catch (Exception e) {
            e.printStackTrace();
            // Handle JSON parsing or other errors
        }
    }

    private void processOcppMessage(WebSocketSession session, JsonNode json) throws IOException {
        int messageType = json.get(0).asInt();
        String messageId = json.get(1).asText();
        String action = json.get(2).asText();
        JsonNode payload = json.get(3);

        switch (action) {
            case "BootNotification":
                handleBootNotification(session, messageId, payload);
                break;
            case "Heartbeat":
                handleHeartbeat(session, messageId, payload);
                break;
            // Add other OCPP message handling here (StatusNotification, StartTransaction, StopTransaction)
            default:
                System.out.println("Unknown action: " + action);
        }
    }

    private void handleBootNotification(WebSocketSession session, String messageId, JsonNode payload) throws IOException {
        String chargerId = payload.get("chargePointSerialNumber").asText();
        sessions.put(chargerId, session);
        System.out.println("BootNotification received from: " + chargerId);
        // Implement logic to store charger information in the database
        // Send BootNotification response
        sendResponse(session, messageId, "BootNotification", "{\"status\":\"Accepted\"}");
    }

    private void handleHeartbeat(WebSocketSession session, String messageId, JsonNode payload) throws IOException {
        String chargerId = getChargerIdFromSession(session);
        if (chargerId != null) {
            System.out.println("Heartbeat received from: " + chargerId);
            // Implement logic to update lastHeartbeat in the database
            // Send Heartbeat response
            sendResponse(session, messageId, "Heartbeat", "{}");
        }
    }

    private void sendResponse(WebSocketSession session, String messageId, String action, String payload) throws IOException {
        String response = String.format("[2,\"%s\",\"%s\",%s]", messageId, action, payload);
        session.sendMessage(new TextMessage(response));
    }

    private String getChargerIdFromSession(WebSocketSession session){
        for(Map.Entry<String, WebSocketSession> entry : sessions.entrySet()){
            if(entry.getValue().equals(session)){
                return entry.getKey();
            }
        }
        return null;
    }
}