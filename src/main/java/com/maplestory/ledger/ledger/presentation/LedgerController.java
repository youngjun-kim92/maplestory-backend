package com.maplestory.ledger.ledger.presentation;

import com.maplestory.ledger.common.security.CustomUserDetails;
import com.maplestory.ledger.ledger.application.LedgerService;
import com.maplestory.ledger.ledger.infrastructure.projection.WeeklySummaryProjection;
import com.maplestory.ledger.ledger.presentation.dto.LedgerEntryRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ledger")
@RequiredArgsConstructor
public class LedgerController {

    private final LedgerService ledgerService;

    /** 기능 #2: 목요일 기준 주간 가계부 조회 */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getWeeklyLedger(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate week) {
        return ResponseEntity.ok(ledgerService.getWeeklyLedger(userDetails.getUserId(), week));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> addEntry(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody LedgerEntryRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ledgerService.addEntry(userDetails.getUserId(), req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEntry(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        ledgerService.deleteEntry(userDetails.getUserId(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/weeks")
    public ResponseEntity<List<WeeklySummaryProjection>> getWeeksList(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ledgerService.getWeeksList(userDetails.getUserId()));
    }

    @GetMapping("/stats")
    public ResponseEntity<List<Object[]>> getCategoryStats(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "4") int weeks) {
        return ResponseEntity.ok(ledgerService.getCategoryStats(userDetails.getUserId(), weeks));
    }
}
