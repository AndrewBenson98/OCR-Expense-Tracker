package com.benson.ocr_expense_tracker.config;

import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.beans.BeanProperty;

@Data
@Component
@ConfigurationProperties(prefix = "azure")
public class AzureConfig {
    private DocumentIntelligence documentIntelligence = new DocumentIntelligence();
    private OpenAI openAI = new OpenAI();
//    private DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();

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

    @Bean
    public DefaultAzureCredential getDefaultAzureCredential(){
        return new DefaultAzureCredentialBuilder().build();
    }

}
