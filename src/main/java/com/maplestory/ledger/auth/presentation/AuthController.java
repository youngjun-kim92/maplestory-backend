package com.maplestory.ledger.auth.presentation;

import com.maplestory.ledger.auth.application.AuthService;
import com.maplestory.ledger.auth.presentation.dto.AuthResponse;
import com.maplestory.ledger.auth.presentation.dto.LoginRequest;
import com.maplestory.ledger.auth.presentation.dto.MesoBalanceRequest;
import com.maplestory.ledger.auth.presentation.dto.RegisterRequest;
import com.maplestory.ledger.auth.presentation.dto.UserResponse;
import com.maplestory.ledger.common.security.CustomUserDetails;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(req));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(authService.getProfile(userDetails.getUserId()));
    }

    @PutMapping("/sol-erda-price")
    public ResponseEntity<Void> updateSolErdaPrice(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam @Min(0) Long price) {
        authService.updateSolErdaPrice(userDetails.getUserId(), price);
        return ResponseEntity.noContent().build();
    }

    /** 현재 보유 메소(인벤토리) 및 창고 메소를 기록합니다. */
    @PutMapping("/meso-balance")
    public ResponseEntity<UserResponse> updateMesoBalance(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody MesoBalanceRequest req) {
        return ResponseEntity.ok(
                authService.updateMesoBalance(userDetails.getUserId(), req.inventoryMeso(), req.storageMeso())
        );
    }

    @DeleteMapping("/reset")
    public ResponseEntity<Void> resetAllData(@AuthenticationPrincipal CustomUserDetails userDetails) {
        authService.resetAllData(userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }
}