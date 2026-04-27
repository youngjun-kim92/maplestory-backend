package com.maplestory.ledger.goal.presentation;

import com.maplestory.ledger.common.security.CustomUserDetails;
import com.maplestory.ledger.goal.application.GoalService;
import com.maplestory.ledger.goal.presentation.dto.GoalEstimateResponse;
import com.maplestory.ledger.goal.presentation.dto.GoalRequest;
import com.maplestory.ledger.goal.presentation.dto.GoalResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;

    @PostMapping
    public ResponseEntity<GoalResponse> createGoal(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody GoalRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(goalService.createGoal(userDetails.getUserId(), req));
    }

    @GetMapping
    public ResponseEntity<List<GoalResponse>> getGoals(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(goalService.getGoals(userDetails.getUserId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GoalResponse> updateGoal(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody GoalRequest req) {
        return ResponseEntity.ok(goalService.updateGoal(userDetails.getUserId(), id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGoal(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        goalService.deleteGoal(userDetails.getUserId(), id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/achieve")
    public ResponseEntity<GoalResponse> markAchieved(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(goalService.markAchieved(userDetails.getUserId(), id));
    }

    @GetMapping("/{id}/estimate")
    public ResponseEntity<GoalEstimateResponse> getGoalEstimate(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(goalService.getGoalEstimate(userDetails.getUserId(), id));
    }
}