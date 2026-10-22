package com.github.valdpq.expensetracker.budget;

import com.github.valdpq.expensetracker.budget.dto.BudgetResponse;
import com.github.valdpq.expensetracker.budget.dto.BudgetStatusResponse;
import com.github.valdpq.expensetracker.budget.dto.CreateBudgetRequest;
import com.github.valdpq.expensetracker.budget.dto.UpdateBudgetRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping
    public ResponseEntity<BudgetResponse> createBudget(@RequestBody @Valid CreateBudgetRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(budgetService.createBudget(request));
    }

    @PatchMapping("/{budgetId}")
    public ResponseEntity<BudgetResponse> updateBudget(@RequestBody @Valid UpdateBudgetRequest request,
                                                       @PathVariable Long budgetId) {
        return ResponseEntity.status(HttpStatus.OK).body(budgetService.updateBudget(request, budgetId));
    }

    @DeleteMapping("/{budgetId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBudget(@PathVariable Long budgetId) {
        budgetService.deleteBudget(budgetId);
    }

    @GetMapping("/status")
    public ResponseEntity<List<BudgetStatusResponse>> getBudgetStatus(
            @RequestParam(value = "month", required = false) YearMonth month) {
        return ResponseEntity.status(HttpStatus.OK).body(budgetService.getBudgetStatus(month));
    }
}
