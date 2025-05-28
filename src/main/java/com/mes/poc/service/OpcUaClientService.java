package com.mes.poc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mes.poc.config.OpcUaConfigProperties;
import com.mes.poc.config.OpcUaWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfig;
import org.eclipse.milo.opcua.sdk.client.api.identity.AnonymousProvider;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaMonitoredItem;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscription;
import org.eclipse.milo.opcua.stack.client.DiscoveryClient;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.*;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MonitoringMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

@Slf4j
@Service
public class OpcUaClientService {

    private final OpcUaConfigProperties config;
    private OpcUaClient client;
    private UaSubscription subscription;

    private final OpcUaWebSocketHandler webSocketHandler;

    @Autowired
    public OpcUaClientService(OpcUaConfigProperties config, OpcUaWebSocketHandler webSocketHandler) {
        this.config = config;
        this.webSocketHandler = webSocketHandler;
    }

    @PostConstruct
    public void initialize() {
        try {
            connect();
            log.info("OPC-UA Client initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize OPC-UA Client", e);
        }
    }

    @PreDestroy
    public void cleanup() {
        disconnect();
    }

    private void connect() throws Exception {
        EndpointDescription endpoint = chooseEndpoint(config.getEndpoint(), config.getSecurityPolicy());
        if (endpoint == null) {
            throw new IllegalArgumentException("No suitable endpoint found for " + config.getEndpoint());
        }
        OpcUaClientConfig clientConfig = OpcUaClientConfig.builder()
                .setApplicationName(LocalizedText.english(config.getApplicationName()))
                .setApplicationUri(config.getApplicationUri())
                .setProductUri(config.getProductUri())
                .setEndpoint(endpoint)
                .setIdentityProvider(new AnonymousProvider())
                .setRequestTimeout(UInteger.valueOf(config.getRequestTimeout()))
                .setSessionTimeout(UInteger.valueOf(config.getSessionTimeout()))
                .build();

        client = OpcUaClient.create(clientConfig);
        client.connect().get();

        log.info("Connected to OPC-UA Server: {}", config.getEndpoint());
    }

    private EndpointDescription chooseEndpoint(String url, String securityPolicyUri) throws Exception {
        List<EndpointDescription> endpoints = DiscoveryClient.getEndpoints(url).get();

        return endpoints.stream()
                .filter(e -> e.getSecurityPolicyUri().equals(securityPolicyUri))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Endpoint not found"));
    }

    public void disconnect() {
        if (subscription != null) {
            try {
                client.getSubscriptionManager().deleteSubscription(subscription.getSubscriptionId()).get();
                log.info("Subscription deleted");
            } catch (Exception e) {
                log.error("Error deleting subscription", e);
            }
        }

        if (client != null) {
            try {
                client.disconnect().get();
                log.info("Disconnected from OPC-UA Server");
            } catch (Exception e) {
                log.error("Error disconnecting from OPC-UA Server", e);
            }
        }
    }

    public CompletableFuture<DataValue> readValue(String nodeId) {
        try {
            NodeId node = NodeId.parse(nodeId);
            return client.readValue(0, TimestampsToReturn.Both, node);
        } catch (Exception e) {
            log.error("Error reading node: {}", nodeId, e);
            CompletableFuture<DataValue> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    public CompletableFuture<List<DataValue>> readValues(List<String> nodeIds) {
        try {
            List<NodeId> nodes = nodeIds.stream()
                    .map(NodeId::parse)
                    .toList();

            List<ReadValueId> readValueIds = nodes.stream()
                    .map(nodeId -> new ReadValueId(nodeId, AttributeId.Value.uid(), null, QualifiedName.NULL_VALUE))
                    .toList();

            //
            return client.read(0, TimestampsToReturn.Both, readValueIds)
                    .thenApply(readResponse -> Arrays.asList(readResponse.getResults()));
        } catch (Exception e) {
            log.error("Error reading multiple nodes", e);
            CompletableFuture<List<DataValue>> future = new CompletableFuture<List<DataValue>>();
            future.completeExceptionally(e);
            return future;
        }
    }

    public CompletableFuture<StatusCode> writeValue(String nodeId, Object value) {
        try {
            NodeId node = NodeId.parse(nodeId);
            DataValue dataValue = new DataValue(new Variant(value));
            WriteValue writeValue = new WriteValue(node, AttributeId.Value.uid(), null, dataValue);

            return client.write(List.of(writeValue))
                    .thenApply(results -> results.getResults()[0]);
        } catch (Exception e) {
            log.error("Error writing to node: {}", nodeId, e);
            CompletableFuture<StatusCode> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    public void createSubscription(List<String> nodeIds, BiConsumer<String, DataValue> dataChangeHandler) {
        try {
            // Create subscription
            subscription = client.getSubscriptionManager()
                    .createSubscription(config.getSubscriptionPublishingInterval())
                    .get();

            // Create monitored items
            List<MonitoredItemCreateRequest> requests = nodeIds.stream()
                    .map(nodeId -> {
                        NodeId node = NodeId.parse(nodeId);
                        ReadValueId readValueId = new ReadValueId(
                                node, AttributeId.Value.uid(), null, QualifiedName.NULL_VALUE);

                        MonitoringParameters parameters = new MonitoringParameters(
                                UInteger.valueOf(1), // client handle
                                1000.0, // sampling interval
                                null, // filter
                                UInteger.valueOf(10), // queue size
                                true // discard oldest
                        );

                        return new MonitoredItemCreateRequest(
                                readValueId, MonitoringMode.Reporting, parameters);
                    })
                    .toList();

            List<UaMonitoredItem> items = subscription.createMonitoredItems(
                    TimestampsToReturn.Both, requests).get();

            // Set up data change listeners
            for (int i = 0; i < items.size(); i++) {
                UaMonitoredItem item = items.get(i);
                String nodeId = nodeIds.get(i);

                item.setValueConsumer((monitoredItem, dataValue) -> {
                    log.debug("Data change for node {}: {}", nodeId, dataValue.getValue());
                    dataChangeHandler.accept(nodeId, dataValue);
                    String payload = null;
                    try {
                        payload = new ObjectMapper().writeValueAsString(Map.of(
                                "nodeId", monitoredItem.getReadValueId().getNodeId().toParseableString(),
                                "value", dataValue.getValue().getValue(),
                                "timestamp", LocalDateTime.now().toString()
                        ));
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }

                    webSocketHandler.broadcast(payload);
                });
            }

            log.info("Created subscription with {} monitored items", items.size());

        } catch (Exception e) {
            log.error("Error creating subscription", e);
        }
    }

    public boolean isConnected() {
//        return client != null &&
//                client.getSession().isDone() &&
//                !client.getSession().isCompletedExceptionally();
        // check the session id
        return client != null &&
                client.getSession().isDone() &&
                client.getSession().join() != null;
    }

    public void reconnect() {
        log.info("Attempting to reconnect to OPC-UA Server");
        disconnect();
        try {
            Thread.sleep(5000); // Wait 5 seconds before reconnecting
            connect();
        } catch (Exception e) {
            log.error("Failed to reconnect to OPC-UA Server", e);
        }
    }
}
