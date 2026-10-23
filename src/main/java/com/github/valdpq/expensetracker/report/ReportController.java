package com.github.valdpq.expensetracker.report;

import com.github.valdpq.expensetracker.report.dto.ReportResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/monthly")
    public ResponseEntity<ReportResponse> reportByMonth(
            @RequestParam(value = "month", required = false) YearMonth month) {
        return ResponseEntity.status(HttpStatus.OK).body(reportService.reportByMonth(month));
    }
}
