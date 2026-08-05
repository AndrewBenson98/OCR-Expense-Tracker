package com.benson.ocr_expense_tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.benson.ocr_expense_tracker.config.AzureConfig;

@SpringBootApplication
@EnableConfigurationProperties(AzureConfig.class)
public class OcrExpenseTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(OcrExpenseTrackerApplication.class, args);
	}

}
