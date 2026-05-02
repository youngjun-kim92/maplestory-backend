package com.maplestory.ledger.auth.application;

import com.maplestory.ledger.auth.domain.User;
import com.maplestory.ledger.auth.infrastructure.UserRepository;
import com.maplestory.ledger.auth.presentation.dto.AuthResponse;
import com.maplestory.ledger.auth.presentation.dto.LoginRequest;
import com.maplestory.ledger.auth.presentation.dto.RegisterRequest;
import com.maplestory.ledger.auth.presentation.dto.UserResponse;
import com.maplestory.ledger.boss.infrastructure.BossDropRecordRepository;
import com.maplestory.ledger.boss.infrastructure.BossKillRepository;
import com.maplestory.ledger.character.infrastructure.CharacterRepository;
import com.maplestory.ledger.common.exception.DuplicateNicknameException;
import com.maplestory.ledger.common.exception.InvalidCredentialsException;
import com.maplestory.ledger.common.exception.ResourceNotFoundException;
import com.maplestory.ledger.common.security.JwtTokenProvider;
import com.maplestory.ledger.favorite.infrastructure.FavoriteRepository;
import com.maplestory.ledger.goal.infrastructure.GoalRepository;
import com.maplestory.ledger.hunting.infrastructure.HuntingSessionRepository;
import com.maplestory.ledger.ledger.infrastructure.LedgerEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final BossDropRecordRepository bossDropRecordRepository;
    private final BossKillRepository bossKillRepository;
    private final HuntingSessionRepository huntingSessionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final GoalRepository goalRepository;
    private final CharacterRepository characterRepository;
    private final FavoriteRepository favoriteRepository;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByNickname(req.nickname())) {
            throw new DuplicateNicknameException("이미 사용 중인 닉네임입니다.");
        }
        User user = User.create(req.nickname(), passwordEncoder.encode(req.password()));
        user = userRepository.save(user);
        String token = jwtTokenProvider.generateToken(user.getId(), user.getNickname());
        return new AuthResponse(token, UserResponse.from(user));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByNickname(req.nickname())
                .orElseThrow(() -> new InvalidCredentialsException("닉네임 또는 비밀번호가 올바르지 않습니다."));
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("닉네임 또는 비밀번호가 올바르지 않습니다.");
        }
        String token = jwtTokenProvider.generateToken(user.getId(), user.getNickname());
        return new AuthResponse(token, UserResponse.from(user));
    }

    @Transactional(readOnly = true)
    public UserResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateMvpGrade(Long userId, User.MvpGrade grade) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
        user.updateMvpGrade(grade);
        return UserResponse.from(user);
    }

    @Transactional
    public void updateSolErdaPrice(Long userId, Long price) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
        user.updateSolErdaPrice(price);
    }

    @Transactional
    public UserResponse updateMesoBalance(Long userId, Long inventoryMeso, Long storageMeso) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
        user.updateMesoBalance(inventoryMeso, storageMeso);
        return UserResponse.from(user);
    }

    @Transactional
    public void resetAllData(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
        bossDropRecordRepository.deleteByUserId(userId);
        bossKillRepository.deleteByUserId(userId);
        huntingSessionRepository.deleteByUserId(userId);
        ledgerEntryRepository.deleteByUserId(userId);
        goalRepository.deleteByUserId(userId);
        characterRepository.deleteByUserId(userId);
        favoriteRepository.deleteByUserId(userId);
        user.updateMesoBalance(0L, 0L);
        user.updateSolErdaPrice(0L);
    }
}