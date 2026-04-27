package com.maplestory.ledger.boss.presentation;

import com.maplestory.ledger.boss.application.BossService;
import com.maplestory.ledger.boss.application.command.RecordBossKillCommand;
import com.maplestory.ledger.boss.infrastructure.projection.BossStatsProjection;
import com.maplestory.ledger.boss.presentation.dto.BossKillRequest;
import com.maplestory.ledger.boss.presentation.dto.BossKillResponse;
import com.maplestory.ledger.boss.presentation.dto.BossMasterResponse;
import com.maplestory.ledger.common.security.CustomUserDetails;
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
@RequestMapping("/api/boss")
@RequiredArgsConstructor
public class BossController {

    private final BossService bossService;

    @GetMapping("/list")
    public ResponseEntity<List<BossMasterResponse>> getBossList() {
        return ResponseEntity.ok(bossService.getBossList());
    }

    @PostMapping("/kill")
    public ResponseEntity<BossKillResponse> recordBossKill(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody BossKillRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bossService.recordBossKill(userDetails.getUserId(), RecordBossKillCommand.from(req)));
    }

    @GetMapping("/weekly")
    public ResponseEntity<List<BossKillResponse>> getWeeklyBossKills(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate week) {
        return ResponseEntity.ok(bossService.getWeeklyBossKills(userDetails.getUserId(), week));
    }

    @GetMapping("/stats")
    public ResponseEntity<List<BossStatsProjection>> getBossStats(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(bossService.getBossStats(userDetails.getUserId()));
    }
}