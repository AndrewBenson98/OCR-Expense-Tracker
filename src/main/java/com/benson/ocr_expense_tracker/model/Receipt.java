package com.benson.ocr_expense_tracker.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Receipt {
    private String merchant;
    private LocalDate date;
    private Double totalAmount;
    private Double tax;
    private Category category;
    private List<Item> items;
}
