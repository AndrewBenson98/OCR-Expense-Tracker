package com.benson.ocr_expense_tracker.service;

//import com.azure.ai.openai.OpenAIClient;
//import com.azure.ai.openai.OpenAIClientBuilder;
//import com.azure.ai.openai.models.ChatCompletions;
//import com.azure.ai.openai.models.ChatCompletionsOptions;
//import com.azure.ai.openai.models.ChatRequestMessage;
//import com.azure.ai.openai.models.ChatRequestSystemMessage;
//import com.azure.ai.openai.models.ChatRequestUserMessage;
//import com.azure.core.credential.AzureKeyCredential;

import com.azure.ai.inference.ChatCompletionsClient;
import com.azure.ai.inference.ChatCompletionsClientBuilder;
//import com.azure.ai.inference.models.*;
import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.*;
import com.azure.core.credential.AzureKeyCredential;
import com.benson.ocr_expense_tracker.config.AzureConfig;
import com.benson.ocr_expense_tracker.model.Receipt;
import com.benson.ocr_expense_tracker.model.Category;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class LLMExtractionService {

    private final OpenAIClient client;
    private final String deploymentName;
    private final ObjectMapper objectMapper;

    public LLMExtractionService(AzureConfig azureConfig) {
        this.client = new OpenAIClientBuilder()
                .endpoint(azureConfig.getOpenAI().getEndpoint())
//                .credential(new AzureKeyCredential(azureConfig.getOpenAI().getApiKey()))
                .credential(azureConfig.getDefaultAzureCredential())
                .buildClient();
        this.deploymentName = azureConfig.getOpenAI().getDeploymentName();
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    }

//    public String testllm(){
//        String prompt = "Hi how are you?";
//        List<ChatRequestMessage> messages = new ArrayList<>();
//        messages.add(new ChatRequestSystemMessage(
//                "You are a helpful assistant."));
//        messages.add(new ChatRequestUserMessage(prompt));
//        ChatCompletionsOptions options = new ChatCompletionsOptions(messages)
//                .setTemperature(0.0);
//        return client.getChatCompletions(deploymentName,options).getChoices().get(0).getMessage().getContent();
//    }


    public Receipt extractReceiptData(Map<String, String> ocrData) {
        log.info("Extracting receipt data using Azure OpenAI");
        log.debug("OCR Data: " +ocrData);
        try {
            String prompt = buildExtractionPrompt(ocrData);
            
            List<ChatRequestMessage> messages = new ArrayList<>();
            messages.add(new ChatRequestSystemMessage(
                    "You are an expert receipt parser. Extract receipt information and return ONLY valid JSON, no markdown, no code blocks."));
            messages.add(new ChatRequestUserMessage(prompt));

            log.debug("Prompt sent to LLM: {}", prompt);

            ChatCompletionsOptions options = new ChatCompletionsOptions(messages)
                    .setTemperature(0.0);

            log.debug("Calling Azure OpenAI with deployment: {}", deploymentName);
            ChatCompletions response = client.getChatCompletions(deploymentName,options);

            String responseText = response.getChoices()
                    .get(0)
                    .getMessage()
                    .getContent();

            log.debug("LLM Response: {}", responseText);

            Receipt receipt = parseReceiptJson(responseText);
            log.info("Receipt extraction completed. Merchant: {}, Total: {}",
                    receipt.getMerchant(), receipt.getTotalAmount());
            return receipt;
        } catch (Exception e) {
            log.error("Error extracting receipt data with LLM", e);
            throw new RuntimeException("Failed to extract receipt data with LLM: " + e.getMessage(), e);
        }
    }

    private String buildExtractionPrompt(Map<String, String> ocrData) throws Exception {
        return String.format("""
                Extract receipt information from the following OCR data and return ONLY a JSON object with this exact structure:
                {
                  "merchant": "string",
                  "date": "YYYY-MM-DD",
                  "totalAmount": number,
                  "tax": number,
                  "category": "GROCERIES|ENTERTAINMENT|TRANSPORT|DINING|OTHER",
                  "items": [{"name": "string", "price": number}]
                }
                
                OCR Data:
                %s
                
                Rules:
                - merchant: Name of the store/restaurant. If not found, use "Unknown"
                - date: Extract and format as YYYY-MM-DD. If not found, use 1970-01-01
                - totalAmount: Total amount paid (must be a number, remove currency symbols). If not found, use 0
                - tax: Tax amount (must be a number, remove currency symbols). Default to 0 if not found
                - category: Choose ONE category based on merchant type. Default to OTHER
                - items: List of items purchased with names and prices. Use empty array [] if none found
                
                Return ONLY valid JSON, no markdown, no code blocks, no explanations.
                """, objectMapper.writeValueAsString(ocrData));
    }

    private Receipt parseReceiptJson(String jsonResponse) throws Exception {
        String cleanedJson = jsonResponse
                .replaceAll("^```json\\s*", "")
                .replaceAll("^```\\s*", "")
                .replaceAll("\\s*```$", "")
                .trim();

        log.debug("Cleaned JSON: {}", cleanedJson);

        try {
            Receipt receipt = objectMapper.readValue(cleanedJson, Receipt.class);
            
            // Validate and set defaults
            if (receipt.getMerchant() == null || receipt.getMerchant().isEmpty()) {
                receipt.setMerchant("Unknown Merchant");
            }
            
            if (receipt.getDate() == null) {
                receipt.setDate(LocalDate.of(1970, 1, 1));
            }
            
            if (receipt.getTotalAmount() == null) {
                receipt.setTotalAmount(0.0);
            }
            
            if (receipt.getTax() == null) {
                receipt.setTax(0.0);
            }
            
            if (receipt.getCategory() == null) {
                receipt.setCategory(Category.OTHER);
            }
            
            if (receipt.getItems() == null) {
                receipt.setItems(new ArrayList<>());
            }
            
            return receipt;
        } catch (Exception e) {
            log.error("Failed to parse receipt JSON. Response: {}", cleanedJson, e);
            throw new RuntimeException("Invalid JSON response from LLM: " + e.getMessage(), e);
        }
    }
}

