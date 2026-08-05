//package com.benson.ocr_expense_tracker.service;
//
//import com.benson.ocr_expense_tracker.config.AzureConfig;
//import com.benson.ocr_expense_tracker.model.Category;
//import com.benson.ocr_expense_tracker.model.Receipt;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.time.LocalDate;
//import java.util.HashMap;
//import java.util.Map;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@ExtendWith(MockitoExtension.class)
//class LLMExtractionServiceTest {
//
//    private LLMExtractionService service;
//
//    @BeforeEach
//    void setUp() {
//        AzureConfig config = new AzureConfig();
//        AzureConfig.OpenAI openAiConfig = new AzureConfig.OpenAI();
//        openAiConfig.setEndpoint("https://test.openai.azure.com/");
//        openAiConfig.setApiKey("test-key");
//        openAiConfig.setDeploymentName("test-deployment");
//        openAiConfig.setApiVersion("2024-02-15-preview");
//        config.setOpenAI(openAiConfig);
//
//        service = new LLMExtractionService(config);
//    }
//
//    @Test
//    void testParseReceiptJson_ValidJson() throws Exception {
//        String validJson = """
//                {
//                  "merchant": "Whole Foods",
//                  "date": "2026-01-15",
//                  "totalAmount": 42.50,
//                  "tax": 3.50,
//                  "category": "GROCERIES",
//                  "items": [
//                    {"name": "Apples", "price": 5.99},
//                    {"name": "Milk", "price": 3.99}
//                  ]
//                }
//                """;
//
//        Receipt receipt = service.extractReceiptData(new HashMap<>());
//        assertNotNull(receipt);
//    }
//
//    @Test
//    void testBuildExtractionPrompt_ContainsRequiredFields() {
//        Map<String, String> ocrData = new HashMap<>();
//        ocrData.put("merchant", "Test Store");
//        ocrData.put("totalAmount", "50.00");
//
//        String prompt = service.extractReceiptData(ocrData).toString();
//        assertNotNull(prompt);
//    }
//
//    @Test
//    void testReceiptDeserializationWithValidData() {
//        Receipt receipt = new Receipt();
//        receipt.setMerchant("Test Merchant");
//        receipt.setDate(LocalDate.of(2026, 1, 15));
//        receipt.setTotalAmount(42.50);
//        receipt.setTax(3.50);
//        receipt.setCategory(Category.GROCERIES);
//
//        assertEquals("Test Merchant", receipt.getMerchant());
//        assertEquals(LocalDate.of(2026, 1, 15), receipt.getDate());
//        assertEquals(42.50, receipt.getTotalAmount());
//        assertEquals(3.50, receipt.getTax());
//        assertEquals(Category.GROCERIES, receipt.getCategory());
//    }
//}
