package com.maplestory.ledger.stats.presentation;

import com.maplestory.ledger.common.security.CustomUserDetails;
import com.maplestory.ledger.stats.application.StatsService;
import com.maplestory.ledger.stats.presentation.dto.ExpCalculatorRequest;
import com.maplestory.ledger.stats.presentation.dto.ExpCalculatorResponse;
import com.maplestory.ledger.stats.presentation.dto.StatsComparisonResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    /** 기능 #9: 익명 기반 유저 평균 수익 백분위 비교 */
    @GetMapping("/comparison")
    public ResponseEntity<StatsComparisonResponse> getUserComparison(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(statsService.getUserComparison(userDetails.getUserId()));
    }

    /** 기능 #7: 레벨업 경험치 계산기 */
    @PostMapping("/exp-calculator")
    public ResponseEntity<ExpCalculatorResponse> calculateExp(
            @Valid @RequestBody ExpCalculatorRequest req) {
        return ResponseEntity.ok(statsService.calculateExp(req));
    }
}
