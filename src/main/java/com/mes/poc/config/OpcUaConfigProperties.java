package com.mes.poc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "opcua")
public class OpcUaConfigProperties {

    private String endpoint;
    private String securityPolicy;
    private String messageSecurityMode;
    private String applicationName;
    private String applicationUri;
    private String productUri;
    private long requestTimeout;
    private long sessionTimeout;
    private double subscriptionPublishingInterval;
    private List<NodeConfiguration> nodes;

    @Data
    public static class NodeConfiguration {
        private String nodeId;
        private String name;
        private String dataType;
    }
}

