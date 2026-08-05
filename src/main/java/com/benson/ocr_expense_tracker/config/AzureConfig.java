package com.benson.ocr_expense_tracker.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "azure")
public class AzureConfig {
    private DocumentIntelligence documentIntelligence = new DocumentIntelligence();
    private OpenAI openAI = new OpenAI();

    @Data
    public static class DocumentIntelligence {
        private String endpoint;
        private String apiKey;
    }

    @Data
    public static class OpenAI {
        private String endpoint;
        private String apiKey;
        private String deploymentName;
        private String apiVersion;
    }
}
