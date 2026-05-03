package com.maplestory.ledger.hunting.presentation;

import com.maplestory.ledger.common.security.CustomUserDetails;
import com.maplestory.ledger.hunting.application.HuntingService;
import com.maplestory.ledger.hunting.presentation.dto.HuntingSessionRequest;
import com.maplestory.ledger.hunting.presentation.dto.HuntingSessionResponse;
import com.maplestory.ledger.hunting.presentation.dto.HuntingStatsResponse;
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
@RequestMapping("/api/hunting")
@RequiredArgsConstructor
public class HuntingController {

    private final HuntingService huntingService;

    @PostMapping("/session")
    public ResponseEntity<HuntingSessionResponse> recordSession(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody HuntingSessionRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(huntingService.recordSession(userDetails.getUserId(), req));
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<HuntingSessionResponse>> getWeeklySessions(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate week,
            @RequestParam(required = false) Long characterId) {
        return ResponseEntity.ok(huntingService.getWeeklySessions(userDetails.getUserId(), week, characterId));
    }

    @GetMapping("/stats")
    public ResponseEntity<List<HuntingStatsResponse>> getHuntingStats(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(huntingService.getHuntingStats(userDetails.getUserId()));
    }
}