package com.benson.ocr_expense_tracker.service;

import com.azure.ai.documentintelligence.DocumentIntelligenceClient;
import com.azure.ai.documentintelligence.DocumentIntelligenceClientBuilder;
import com.azure.ai.documentintelligence.models.AnalyzeDocumentOptions;
import com.azure.core.credential.AzureKeyCredential;
import com.benson.ocr_expense_tracker.config.AzureConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class DocumentIntelligenceService {

    private final DocumentIntelligenceClient client;
    private final ObjectMapper objectMapper;

    public DocumentIntelligenceService(AzureConfig azureConfig) {
        this.client = new DocumentIntelligenceClientBuilder()
                .endpoint(azureConfig.getDocumentIntelligence().getEndpoint())
                .credential(new AzureKeyCredential(azureConfig.getDocumentIntelligence().getApiKey()))
                .buildClient();
        this.objectMapper = new ObjectMapper();
    }

    public Map<String, String> analyzeReceipt(byte[] imageData) {
        log.info("Analyzing receipt image with Azure Document Intelligence");

        try {
            Map<String, String> extractedData = new HashMap<>();


            // Call Azure Document Intelligence prebuilt receipt model
            com.azure.core.util.BinaryData binary = com.azure.core.util.BinaryData.fromBytes(imageData);
            AnalyzeDocumentOptions options = new AnalyzeDocumentOptions(binary);

            try {
                com.azure.ai.documentintelligence.models.AnalyzeResult result =
                client.beginAnalyzeDocument("prebuilt-receipt",options).getFinalResult();

                if (result != null && result.getDocuments() != null && !result.getDocuments().isEmpty()) {
                    com.azure.ai.documentintelligence.models.AnalyzedDocument doc = result.getDocuments().get(0);
                    Map<String, com.azure.ai.documentintelligence.models.DocumentField> fields = doc.getFields();

                    if (fields != null) {
                        // Merchant
                        com.azure.ai.documentintelligence.models.DocumentField merchantField = fields.get("MerchantName");
                        if (merchantField != null) {
                            String merchant = merchantField.getContent() != null ? merchantField.getContent() : Objects.toString(merchantField.getValueString(), "");
                            extractedData.put("merchant", merchant != null ? merchant : "");
                        } else {
                            extractedData.put("merchant", "");
                        }

                        // Date
                        com.azure.ai.documentintelligence.models.DocumentField dateField = fields.get("TransactionDate");
                        if (dateField != null) {
                            String date = dateField.getContent() != null ? dateField.getContent() : Objects.toString(dateField.getValueString(), "");
                            extractedData.put("date", date != null ? date : "");
                        } else if (fields.containsKey("Date")) {
                            com.azure.ai.documentintelligence.models.DocumentField altDate = fields.get("Date");
                            extractedData.put("date", altDate.getContent() != null ? altDate.getContent() : Objects.toString(altDate.getValueString(), ""));
                        } else {
                            extractedData.put("date", "");
                        }

                        // Total
                        com.azure.ai.documentintelligence.models.DocumentField totalField = fields.get("Total");
                        if (totalField != null) {
                            String total = totalField.getContent() != null ? totalField.getContent() : Objects.toString(totalField.getValueString(), "0.0");
                            extractedData.put("totalAmount", total != null ? total : "0.0");
                        } else {
                            extractedData.put("totalAmount", "0.0");
                        }

                        //Subtotal
                        com.azure.ai.documentintelligence.models.DocumentField subtotalField = fields.get("Subtotal");
                        if (subtotalField != null) {
                            String subtotal = subtotalField.getContent() != null ? subtotalField.getContent() : Objects.toString(subtotalField.getValueString(), "0.0");
                            extractedData.put("subtotal", subtotal != null ? subtotal : "0.0");
                        } else {
                            extractedData.put("subtotal", "0.0");
                        }


                        // Tax
                        com.azure.ai.documentintelligence.models.DocumentField taxField = fields.get("TotalTax");
                        if (taxField != null) {
                            String tax = taxField.getContent() != null ? taxField.getContent() : Objects.toString(taxField.getValueString(), "0.0");
                            extractedData.put("tax", tax != null ? tax : "0.0");
                        } else {
                            extractedData.put("tax", "0.0");
                        }

                        // Items - extract only the item fields requested by the Azure receipt model
                        com.azure.ai.documentintelligence.models.DocumentField itemsField = fields.get("Items");
                        if (itemsField != null) {
                            try {
                                List<Map<String, String>> extractedItems = new ArrayList<>();
                                Object rawItems = itemsField.getValueList();

                                if (rawItems instanceof List) {
                                    for (Object itemObj : (List<?>) rawItems) {
                                        if (!(itemObj instanceof com.azure.ai.documentintelligence.models.DocumentField)) {
                                            continue;
                                        }

                                        com.azure.ai.documentintelligence.models.DocumentField itemField =
                                                (com.azure.ai.documentintelligence.models.DocumentField) itemObj;
                                        Map<String, com.azure.ai.documentintelligence.models.DocumentField> itemFields = itemField.getValueMap();
                                        if (itemFields == null) {
                                            continue;
                                        }

                                        Map<String, String> itemDetails = new LinkedHashMap<>();
                                        itemDetails.put("Description", getDocumentFieldText(itemFields.get("Description")));
                                        itemDetails.put("Quantity", getDocumentFieldNumber(itemFields.get("Quantity")));
                                        itemDetails.put("Price", getDocumentFieldCurrency(itemFields.get("Price")));
                                        itemDetails.put("TotalPrice", getDocumentFieldCurrency(itemFields.get("TotalPrice")));
                                        extractedItems.add(itemDetails);
                                    }
                                }

                                extractedData.put("items", objectMapper.writeValueAsString(extractedItems));
                            } catch (Exception ex) {
                                log.warn("Failed to extract item details from receipt, falling back to raw item content", ex);
                                String itemsContent = itemsField.getContent();
                                extractedData.put("items", itemsContent != null ? itemsContent : "[]");
                            }
                        } else {
                            extractedData.put("items", "[]");
                        }
                    }
                } else {
                    // No documents found
                    extractedData.put("merchant", "");
                    extractedData.put("date", "");
                    extractedData.put("totalAmount", "0.0");
                    extractedData.put("tax", "0.0");
                    extractedData.put("items", "[]");
                }
            } catch (Exception ex) {
                // bubble up after logging - original catch will handle runtime exception
                log.error("Document Intelligence analysis failed", ex);
                throw ex;
            }
            
            // Log successful analysis
            log.info("Receipt analysis completed. Ready for LLM extraction.");
            log.debug(" Data: " + extractedData);
            return extractedData;
        } catch (Exception e) {
            log.error("Error analyzing receipt with Document Intelligence", e);
            throw new RuntimeException("Failed to analyze receipt with Document Intelligence: " + e.getMessage(), e);
        }
    }

    private String getDocumentFieldText(com.azure.ai.documentintelligence.models.DocumentField field) {
        if (field == null) {
            return "";
        }
        if (field.getContent() != null && !field.getContent().isBlank()) {
            return field.getContent();
        }
        if (field.getValueString() != null && !field.getValueString().isBlank()) {
            return field.getValueString();
        }
        if (field.getValueNumber() != null) {
            return String.valueOf(field.getValueNumber());
        }
        if (field.getValueCurrency() != null ) {
            return String.valueOf(field.getValueCurrency().getAmount());
        }
        return "";
    }

    private String getDocumentFieldNumber(com.azure.ai.documentintelligence.models.DocumentField field) {
        if (field == null) {
            return "";
        }
        if (field.getContent() != null && !field.getContent().isBlank()) {
            return field.getContent();
        }
        if (field.getValueNumber() != null) {
            return String.valueOf(field.getValueNumber());
        }
        if (field.getValueString() != null && !field.getValueString().isBlank()) {
            return field.getValueString();
        }
        return "";
    }

    private String getDocumentFieldCurrency(com.azure.ai.documentintelligence.models.DocumentField field) {
        if (field == null) {
            return "";
        }
        if (field.getContent() != null && !field.getContent().isBlank()) {
            return field.getContent();
        }
        if (field.getValueCurrency() != null) {
            return String.valueOf(field.getValueCurrency().getAmount());
        }
        if (field.getValueString() != null && !field.getValueString().isBlank()) {
            return field.getValueString();
        }
        if (field.getValueNumber() != null) {
            return String.valueOf(field.getValueNumber());
        }
        return "";
    }
}
