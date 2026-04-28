package com.maplestory.ledger.boss.presentation;

import com.maplestory.ledger.boss.application.BossService;
import com.maplestory.ledger.boss.application.command.RecordBossKillCommand;
import com.maplestory.ledger.boss.application.command.RecordDropCommand;
import com.maplestory.ledger.boss.application.command.SellDropCommand;
import com.maplestory.ledger.boss.infrastructure.projection.BossStatsProjection;
import com.maplestory.ledger.boss.presentation.dto.BossDropMasterResponse;
import com.maplestory.ledger.boss.presentation.dto.BossDropRecordResponse;
import com.maplestory.ledger.boss.presentation.dto.BossKillRequest;
import com.maplestory.ledger.boss.presentation.dto.BossKillResponse;
import com.maplestory.ledger.boss.presentation.dto.BossMasterResponse;
import com.maplestory.ledger.boss.presentation.dto.RecordDropRequest;
import com.maplestory.ledger.boss.presentation.dto.SellDropRequest;
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

    // ── 드랍 마스터 ──────────────────────────────────────────────────────────

    /** 해당 보스/난이도에서 드랍 가능한 물욕템 목록 */
    @GetMapping("/drops/items")
    public ResponseEntity<List<BossDropMasterResponse>> getBossDropItems(
            @RequestParam String bossName,
            @RequestParam String difficulty) {
        return ResponseEntity.ok(bossService.getBossDropItems(bossName, difficulty));
    }

    // ── 드랍 기록 ────────────────────────────────────────────────────────────

    /** 특정 처치 기록에 드랍된 아이템 추가 */
    @PostMapping("/kills/{killId}/drops")
    public ResponseEntity<BossDropRecordResponse> recordDrop(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long killId,
            @Valid @RequestBody RecordDropRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bossService.recordDrop(userDetails.getUserId(), RecordDropCommand.from(killId, req)));
    }

    /** 드랍 아이템 경매장 판매 처리 → auction LedgerEntry 자동 생성 */
    @PatchMapping("/drops/{id}/sell")
    public ResponseEntity<BossDropRecordResponse> sellDrop(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody SellDropRequest req) {
        return ResponseEntity.ok(
                bossService.sellDrop(userDetails.getUserId(), SellDropCommand.from(id, req)));
    }

    /** 이번 주 드랍 기록 조회 */
    @GetMapping("/drops/weekly")
    public ResponseEntity<List<BossDropRecordResponse>> getWeeklyDropRecords(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate week) {
        return ResponseEntity.ok(bossService.getWeeklyDropRecords(userDetails.getUserId(), week));
    }

    /** 특정 처치 기록의 드랍 아이템 목록 */
    @GetMapping("/kills/{killId}/drops")
    public ResponseEntity<List<BossDropRecordResponse>> getDropsByBossKill(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long killId) {
        return ResponseEntity.ok(bossService.getDropsByBossKill(userDetails.getUserId(), killId));
    }
}
