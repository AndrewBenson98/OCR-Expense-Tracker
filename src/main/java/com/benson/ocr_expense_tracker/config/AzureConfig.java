package com.benson.ocr_expense_tracker.config;

import com.azure.ai.documentintelligence.DocumentIntelligenceClient;
import com.azure.ai.documentintelligence.DocumentIntelligenceClientBuilder;
import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
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
    }

    @Data
    public static class OpenAI {
        private String endpoint;
        private String deploymentName;
        private String apiVersion;
    }

    @Bean
    public DefaultAzureCredential getDefaultAzureCredential() {
        return new DefaultAzureCredentialBuilder().build();
    }

    @Bean
    public DocumentIntelligenceClient documentIntelligenceClient(DefaultAzureCredential defaultAzureCredential) {
        return new DocumentIntelligenceClientBuilder()
                .endpoint(documentIntelligence.getEndpoint())
                .credential(defaultAzureCredential)
                .buildClient();
    }

    @Bean
    public OpenAIClient openAIClient(DefaultAzureCredential defaultAzureCredential) {
        return new OpenAIClientBuilder()
                .endpoint(openAI.getEndpoint())
                .credential(defaultAzureCredential)
                .buildClient();
    }
}
