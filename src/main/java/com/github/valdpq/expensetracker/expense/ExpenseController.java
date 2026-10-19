package com.github.valdpq.expensetracker.expense;

import com.github.valdpq.expensetracker.category.Category;
import com.github.valdpq.expensetracker.expense.dto.CreateExpenseRequest;
import com.github.valdpq.expensetracker.expense.dto.ExpenseResponse;
import com.github.valdpq.expensetracker.expense.dto.PatchExpenseRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ExpenseResponse> createExpense(@RequestBody @Valid CreateExpenseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(expenseService.createExpense(request));
    }

    @GetMapping
    public ResponseEntity<Page<ExpenseResponse>> getMyExpenses(
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @PageableDefault(sort = {"expenseDate", "id"}, direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(expenseService.getMyExpenses(from, to, categoryId, pageable));
    }

    @PatchMapping("/{expenseId}")
    public ResponseEntity<ExpenseResponse> editExpense(@RequestBody @Valid PatchExpenseRequest request,
                                                       @PathVariable Long expenseId) {
        return ResponseEntity.status(HttpStatus.OK).body(expenseService.editExpense(request, expenseId));
    }

    @DeleteMapping("/{expenseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExpense(@PathVariable Long expenseId) {
        expenseService.deleteExpense(expenseId);
    }
}
