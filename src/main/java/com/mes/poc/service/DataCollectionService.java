package com.mes.poc.service;

import com.mes.poc.config.OpcUaConfigProperties;
import com.mes.poc.model.*;
import com.mes.poc.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;

// Data Collection Service
@Slf4j
@Service
@RequiredArgsConstructor
public class DataCollectionService {

    private final OpcUaClientService opcUaClient;
    private final OpcUaConfigProperties config;
    private final ProcessDataValueService processDataValueService;

    @PostConstruct
    public void initializeDataCollection() {
        if (opcUaClient.isConnected()) {
            setupSubscriptions();
        }
    }

    private void setupSubscriptions() {
        List<String> nodeIds = config.getNodes().stream()
                .map(OpcUaConfigProperties.NodeConfiguration::getNodeId)
                .toList();

        opcUaClient.createSubscription(nodeIds, this::handleDataChange);
        log.info("Subscriptions created for {} nodes", nodeIds.size());
    }

    @Async
    public void handleDataChange(String nodeId, DataValue dataValue) {
        try {
            Optional<OpcUaConfigProperties.NodeConfiguration> nodeConfig =
                    config.getNodes().stream()
                            .filter(node -> node.getNodeId().equals(nodeId))
                            .findFirst();

            assert dataValue.getStatusCode() != null;
            if (!dataValue.getStatusCode().isGood()) {
                log.warn("Received bad status code for node {}: {}", nodeId, dataValue.getStatusCode());
                return;
            }

            if (nodeConfig.isPresent()) {
                processDataValueService.processDataValue(nodeConfig.get(), dataValue);
            }
        } catch (Exception e) {
            log.error("Error handling data change for node: {}", nodeId, e);
        }
    }

    @Scheduled(fixedRateString = "${scheduler.data-collection.fixed-rate:5000}")
    public void periodicDataCollection() {
        if (!opcUaClient.isConnected()) {
            log.warn("OPC-UA client not connected, attempting reconnection");
            opcUaClient.reconnect();
            return;
        }

        // Collect additional data that's not subscribed
        collectBatchData();
    }

    private void collectBatchData() {
        List<String> nodeIds = config.getNodes().stream()
                .map(OpcUaConfigProperties.NodeConfiguration::getNodeId)
                .toList();

        opcUaClient.readValues(nodeIds)
                .whenComplete((dataValues, throwable) -> {
                    if (throwable != null) {
                        log.error("Error reading batch data", throwable);
                    } else {
                        for (int i = 0; i < dataValues.size(); i++) {
                            DataValue dataValue = dataValues.get(i);
                            OpcUaConfigProperties.NodeConfiguration nodeConfig = config.getNodes().get(i);

                            assert dataValue.getStatusCode() != null;
                            if (dataValue.getStatusCode().isGood()) {
                                processDataValueService.processDataValue(nodeConfig, dataValue);
                            }
                        }
                    }
                });
    }
}
