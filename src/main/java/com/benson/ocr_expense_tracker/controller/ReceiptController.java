package com.benson.ocr_expense_tracker.controller;

import com.benson.ocr_expense_tracker.model.Receipt;
import com.benson.ocr_expense_tracker.service.DocumentIntelligenceService;
import com.benson.ocr_expense_tracker.service.LLMExtractionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/receipts")
@RequiredArgsConstructor
public class ReceiptController {

    private final DocumentIntelligenceService documentIntelligenceService;
    private final LLMExtractionService llmExtractionService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadReceipt(@RequestParam("file") MultipartFile file) {
        log.info("Received receipt upload: {}", file.getOriginalFilename());

        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
            }

            byte[] imageData = file.getBytes();

            Map<String, String> ocrData = documentIntelligenceService.analyzeReceipt(imageData);
            log.info("OCR analysis completed, extracting structured data");


//            Receipt receipt = llmExtractionService.extractReceiptData(ocrData);
//            log.info("Receipt extracted: merchant={}, total={}", receipt.getMerchant(), receipt.getTotalAmount());

//            return ResponseEntity.ok(receipt);
            return ResponseEntity.ok(ocrData);
        } catch (IOException e) {
            log.error("Error reading file", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to read file: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Error processing receipt", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to process receipt: " + e.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of("status", "healthy"));
    }
}
