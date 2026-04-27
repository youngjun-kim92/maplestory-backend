package com.maplestory.ledger.auth.presentation;

import com.maplestory.ledger.auth.application.AuthService;
import com.maplestory.ledger.auth.presentation.dto.AuthResponse;
import com.maplestory.ledger.auth.presentation.dto.LoginRequest;
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

    /** 기능 #1: 닉네임+비밀번호 간편 회원가입 */
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

    /** 기능 #5: 솔 에르다 조각 낱개 가격 설정 */
    @PutMapping("/sol-erda-price")
    public ResponseEntity<Void> updateSolErdaPrice(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam @Min(0) Long price) {
        authService.updateSolErdaPrice(userDetails.getUserId(), price);
        return ResponseEntity.noContent().build();
    }
}
