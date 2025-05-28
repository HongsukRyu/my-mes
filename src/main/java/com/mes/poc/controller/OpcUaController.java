package com.mes.poc.controller;

import com.mes.poc.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// OPC-UA Control Controller
@Slf4j
@RestController
@RequestMapping("/api/opcua")
@RequiredArgsConstructor
public class OpcUaController {

    private final OpcUaClientService opcUaClient;

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getConnectionStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("connected", opcUaClient.isConnected());
        status.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(status);
    }

    @PostMapping("/reconnect")
    public ResponseEntity<Map<String, String>> reconnect() {
        opcUaClient.reconnect();

        Map<String, String> response = new HashMap<>();
        response.put("message", "Reconnection initiated");
        response.put("timestamp", LocalDateTime.now().toString());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/read")
    public ResponseEntity<Map<String, Object>> readNode(@RequestParam String nodeId) {
        try {
            var future = opcUaClient.readValue(nodeId);
            var dataValue = future.get(); // This is blocking - in production, handle async properly

            Map<String, Object> response = new HashMap<>();
            response.put("nodeId", nodeId);
            response.put("value", dataValue.getValue().getValue());
            response.put("statusCode", dataValue.getStatusCode().toString());
            response.put("timestamp", dataValue.getServerTime());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error reading node: {}", nodeId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/write-async")
    public ResponseEntity<Map<String, String>> writeNodeAsync(
            @RequestParam String nodeId,
            @RequestBody Map<String, Object> request) {

//        try {
//            Object value = request.get("value");
//            var future = opcUaClient.writeValue(nodeId, value);
//            var statusCode = future.get(); // This is blocking - in production, handle async properly
//
//            Map<String, String> response = new HashMap<>();
//            response.put("nodeId", nodeId);
//            response.put("statusCode", statusCode.toString());
//            response.put("message", statusCode.isGood() ? "Write successful" : "Write failed");
//
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            log.error("Error writing to node: {}", nodeId, e);
//            return ResponseEntity.badRequest().build();
//        }

        // Asynchronous write operation
        Object value = request.get("value");
        Map<String, String> response = new HashMap<>();

        opcUaClient.writeValue(nodeId, value).thenApply(statusCode -> {
            response.put("nodeId", nodeId);
            response.put("statusCode", statusCode.toString());
            response.put("message", statusCode.isGood() ? "Write successful" : "Write failed");
            return ResponseEntity.ok(response);
        }).exceptionally(e -> {;
            log.error("Error writing to node: {}", nodeId, e);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        });
        return ResponseEntity.ok(response);
    }

    @PostMapping("/subscribe")
    public ResponseEntity<String> subscribe(@RequestBody Map<String, Object> request) {
        List<String> nodeIds = (List<String>) request.get("nodeIds");
        opcUaClient.createSubscription(nodeIds, (nodeId, dataValue) -> {
            log.info("[Subscription] Node: {} → {}", nodeId, dataValue.getValue().getValue());
            // 추후: Kafka 연동 또는 WebSocket broadcast 등 확장 가능
        });
        return ResponseEntity.ok("Subscribed to nodes: " + nodeIds);
    }
}
