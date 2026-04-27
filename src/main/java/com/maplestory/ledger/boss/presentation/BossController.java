package com.maplestory.ledger.boss.presentation;

import com.maplestory.ledger.boss.application.BossService;
import com.maplestory.ledger.boss.domain.BossKill;
import com.maplestory.ledger.boss.domain.BossMaster;
import com.maplestory.ledger.boss.infrastructure.projection.BossStatsProjection;
import com.maplestory.ledger.boss.presentation.dto.BossKillRequest;
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

    /** 기능 #4: 보스 목록 조회 (결정석 가격 포함) */
    @GetMapping("/list")
    public ResponseEntity<List<BossMaster>> getBossList() {
        return ResponseEntity.ok(bossService.getBossList());
    }

    /** 기능 #4: 보스 처치 기록 → 결정석 가격 자동 계산 후 가계부 합산 */
    @PostMapping("/kill")
    public ResponseEntity<BossKill> recordBossKill(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody BossKillRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bossService.recordBossKill(userDetails.getUserId(), req));
    }

    @GetMapping("/weekly")
    public ResponseEntity<List<BossKill>> getWeeklyBossKills(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate week) {
        return ResponseEntity.ok(bossService.getWeeklyBossKills(userDetails.getUserId(), week));
    }

    /** 기능 #3: 보스별 수익 효율 통계 */
    @GetMapping("/stats")
    public ResponseEntity<List<BossStatsProjection>> getBossStats(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(bossService.getBossStats(userDetails.getUserId()));
    }
}
