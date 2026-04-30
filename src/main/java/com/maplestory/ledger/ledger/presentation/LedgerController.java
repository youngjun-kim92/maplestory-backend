package com.maplestory.ledger.ledger.presentation;

import com.maplestory.ledger.common.security.CustomUserDetails;
import com.maplestory.ledger.ledger.application.LedgerService;
import com.maplestory.ledger.ledger.application.command.AddLedgerEntryCommand;
import com.maplestory.ledger.ledger.application.command.UpdateLedgerEntryCommand;
import com.maplestory.ledger.ledger.presentation.dto.AddEntryResponse;
import com.maplestory.ledger.ledger.presentation.dto.CategoryStatResponse;
import com.maplestory.ledger.ledger.presentation.dto.IncomeSourceTrendResponse;
import com.maplestory.ledger.ledger.presentation.dto.LedgerEntryRequest;
import com.maplestory.ledger.ledger.presentation.dto.LedgerEntryResponse;
import com.maplestory.ledger.ledger.presentation.dto.WeekSummaryResponse;
import com.maplestory.ledger.ledger.presentation.dto.WeeklyLedgerResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/ledger")
@RequiredArgsConstructor
public class LedgerController {

    private final LedgerService ledgerService;

    @GetMapping
    public ResponseEntity<WeeklyLedgerResponse> getWeeklyLedger(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate week) {
        return ResponseEntity.ok(ledgerService.getWeeklyLedger(userDetails.getUserId(), week));
    }

    @PostMapping
    public ResponseEntity<AddEntryResponse> addEntry(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody LedgerEntryRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ledgerService.addEntry(userDetails.getUserId(), AddLedgerEntryCommand.from(req)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LedgerEntryResponse> updateEntry(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody LedgerEntryRequest req) {
        return ResponseEntity.ok(ledgerService.updateEntry(
                userDetails.getUserId(), id, UpdateLedgerEntryCommand.from(req)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEntry(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        ledgerService.deleteEntry(userDetails.getUserId(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/weeks")
    public ResponseEntity<List<WeekSummaryResponse>> getWeeksList(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ledgerService.getWeeksList(userDetails.getUserId()));
    }

    @GetMapping("/stats")
    public ResponseEntity<List<CategoryStatResponse>> getCategoryStats(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "4") int weeks) {
        return ResponseEntity.ok(ledgerService.getCategoryStats(userDetails.getUserId(), weeks));
    }

    @GetMapping("/income-trend")
    public ResponseEntity<List<IncomeSourceTrendResponse>> getIncomeSourceTrend(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "8") int weeks) {
        return ResponseEntity.ok(ledgerService.getIncomeSourceTrend(userDetails.getUserId(), weeks));
    }
}