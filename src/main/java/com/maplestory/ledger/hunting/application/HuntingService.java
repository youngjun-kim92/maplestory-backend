package com.maplestory.ledger.hunting.application;

import com.maplestory.ledger.auth.domain.User;
import com.maplestory.ledger.auth.infrastructure.UserRepository;
import com.maplestory.ledger.character.domain.MapleCharacter;
import com.maplestory.ledger.character.infrastructure.CharacterRepository;
import com.maplestory.ledger.common.exception.ResourceNotFoundException;
import com.maplestory.ledger.common.util.WeekUtil;
import com.maplestory.ledger.hunting.domain.HuntingSession;
import com.maplestory.ledger.hunting.infrastructure.HuntingSessionRepository;
import com.maplestory.ledger.hunting.presentation.dto.HuntingSessionRequest;
import com.maplestory.ledger.hunting.presentation.dto.HuntingSessionResponse;
import com.maplestory.ledger.hunting.presentation.dto.HuntingStatsResponse;
import com.maplestory.ledger.ledger.domain.LedgerEntry;
import com.maplestory.ledger.ledger.infrastructure.LedgerEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HuntingService {

    private final HuntingSessionRepository huntingSessionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final CharacterRepository characterRepository;
    private final UserRepository userRepository;

    /**
     * 사냥 세션을 기록합니다. boss/hunting 도메인은 LedgerEntry와 강하게 결합되어
     * 동일 트랜잭션 내에서 LedgerEntry를 직접 생성합니다.
     * (도메인 이벤트 방식 대신 실용적 결합을 선택한 의도적 설계 결정)
     */
    @Transactional
    public HuntingSessionResponse recordSession(Long userId, HuntingSessionRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
        MapleCharacter character = resolveCharacter(userId, req.characterId());

        int fragments = req.solErdaFragments() != null ? req.solErdaFragments() : 0;
        long solErdaMesoValue = fragments * user.getSolErdaFragmentPrice();
        long totalIncome = req.income() + solErdaMesoValue;
        LocalDate weekStart = WeekUtil.getWeekStart(req.sessionDate());

        LedgerEntry ledgerEntry = ledgerEntryRepository.save(
                LedgerEntry.create(user, character,
                        LedgerEntry.EntryType.income, LedgerEntry.EntryCategory.hunting,
                        totalIncome,
                        req.mapName() + " " + req.durationMinutes() + "분 사냥",
                        req.sessionDate(), weekStart)
        );

        user.updateMesoBalance(user.getInventoryMeso() + totalIncome, user.getStorageMeso());

        HuntingSession session = huntingSessionRepository.save(
                HuntingSession.create(user, character, ledgerEntry,
                        req.mapName(), req.durationMinutes(), req.income(),
                        fragments, solErdaMesoValue, req.sessionDate(), weekStart)
        );
        return HuntingSessionResponse.from(session);
    }

    @Transactional(readOnly = true)
    public List<HuntingSessionResponse> getWeeklySessions(Long userId, LocalDate weekStartParam) {
        LocalDate weekStart = weekStartParam != null ? weekStartParam : WeekUtil.getWeekStart();
        return huntingSessionRepository
                .findByUserIdAndWeekStartOrderBySessionDateDesc(userId, weekStart)
                .stream().map(HuntingSessionResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<HuntingStatsResponse> getHuntingStats(Long userId) {
        List<Object[]> raw = huntingSessionRepository.findHuntingStatsByMap(userId);
        List<HuntingStatsResponse> result = new ArrayList<>();
        for (Object[] row : raw) {
            result.add(new HuntingStatsResponse(
                    (String) row[0],
                    ((Number) row[1]).longValue(),
                    ((Number) row[2]).longValue(),
                    ((Number) row[3]).longValue(),
                    row[4] != null ? ((Number) row[4]).longValue() : 0L
            ));
        }
        return result;
    }

    private MapleCharacter resolveCharacter(Long userId, Long characterId) {
        if (characterId == null) return null;
        return characterRepository.findByIdAndUserId(characterId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("캐릭터를 찾을 수 없습니다."));
    }
}