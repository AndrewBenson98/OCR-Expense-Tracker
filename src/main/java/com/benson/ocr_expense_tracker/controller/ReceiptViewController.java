package com.benson.ocr_expense_tracker.controller;

import com.benson.ocr_expense_tracker.service.DocumentIntelligenceService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ReceiptViewController {

    private final DocumentIntelligenceService documentIntelligenceService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/")
    public String showUploadForm(Model model) {
        model.addAttribute("extractedData", new LinkedHashMap<String, String>());
        model.addAttribute("extractedDataJson", "{}");
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
            Map<String, String> extractedData = documentIntelligenceService.analyzeReceipt(file.getBytes());
            model.addAttribute("filename", file.getOriginalFilename());
            model.addAttribute("extractedData", extractedData);
            model.addAttribute("extractedDataJson", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(extractedData));
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
