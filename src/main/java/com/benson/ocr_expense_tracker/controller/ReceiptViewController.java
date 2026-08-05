package com.benson.ocr_expense_tracker.controller;

import com.benson.ocr_expense_tracker.model.Receipt;
import com.benson.ocr_expense_tracker.service.DocumentIntelligenceService;
import com.benson.ocr_expense_tracker.service.LLMExtractionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ReceiptViewController {

    private final DocumentIntelligenceService documentIntelligenceService;
    private final LLMExtractionService llmExtractionService;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @GetMapping("/")
    public String showUploadForm(Model model) {
        model.addAttribute("receipt", null);
        model.addAttribute("receiptJson", "{}");
        return "receipt-upload";
    }

    @PostMapping("/upload")
    public String processUpload(@RequestParam("file") MultipartFile file, Model model) {
        log.info("Received receipt upload from web UI: {}", file.getOriginalFilename());

        if (file.isEmpty()) {
            model.addAttribute("error", "Please choose a receipt image to upload.");
            return "receipt-upload";
        }

        try {
            Map<String, String> ocrData = documentIntelligenceService.analyzeReceipt(file.getBytes());
            log.info("OCR analysis completed, extracting structured data");

            Receipt receipt = llmExtractionService.extractReceiptData(ocrData);
            log.info("Receipt extracted: merchant={}, total={}", receipt.getMerchant(), receipt.getTotalAmount());

            model.addAttribute("filename", file.getOriginalFilename());
            model.addAttribute("receipt", receipt);
            model.addAttribute("receiptJson", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(receipt));
            model.addAttribute("message", "Receipt processed successfully.");
            return "receipt-upload";
        } catch (IOException e) {
            log.error("Error reading uploaded file", e);
            model.addAttribute("error", "Unable to read the uploaded file: " + e.getMessage());
            return "receipt-upload";
        } catch (Exception e) {
            log.error("Error processing receipt from web UI", e);
            model.addAttribute("error", "Receipt processing failed: " + e.getMessage());
            return "receipt-upload";
        }
    }
}
