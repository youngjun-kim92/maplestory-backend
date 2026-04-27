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
     * 기능 #5: 사냥 세션 기록. 솔 에르다 조각 자동 환산 포함.
     * 솔 에르다 조각 수 × 사용자 설정 단가 = 자동 합산 수익
     */
    @Transactional
    public HuntingSession recordSession(Long userId, HuntingSessionRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
        MapleCharacter character = null;
        if (req.characterId() != null) {
            character = characterRepository.findByIdAndUserId(req.characterId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("캐릭터를 찾을 수 없습니다."));
        }

        int fragments = req.solErdaFragments() != null ? req.solErdaFragments() : 0;
        long solErdaMesoValue = fragments * user.getSolErdaFragmentPrice();
        long totalIncome = req.income() + solErdaMesoValue;

        LocalDate weekStart = WeekUtil.getWeekStart(req.sessionDate());

        LedgerEntry ledgerEntry = new LedgerEntry();
        ledgerEntry.setUser(user);
        ledgerEntry.setCharacter(character);
        ledgerEntry.setType(LedgerEntry.EntryType.income);
        ledgerEntry.setCategory(LedgerEntry.EntryCategory.hunting);
        ledgerEntry.setAmount(totalIncome);
        ledgerEntry.setDescription(req.mapName() + " " + req.durationMinutes() + "분 사냥");
        ledgerEntry.setEntryDate(req.sessionDate());
        ledgerEntry.setWeekStart(weekStart);
        ledgerEntry = ledgerEntryRepository.save(ledgerEntry);

        HuntingSession session = new HuntingSession();
        session.setUser(user);
        session.setCharacter(character);
        session.setLedgerEntry(ledgerEntry);
        session.setMapName(req.mapName());
        session.setDurationMinutes(req.durationMinutes());
        session.setIncome(req.income());
        session.setSolErdaFragments(fragments);
        session.setSolErdaMesoValue(solErdaMesoValue);
        session.setSessionDate(req.sessionDate());
        session.setWeekStart(weekStart);
        return huntingSessionRepository.save(session);
    }

    @Transactional(readOnly = true)
    public List<HuntingSession> getWeeklySessions(Long userId, LocalDate weekStartParam) {
        LocalDate weekStart = weekStartParam != null ? weekStartParam : WeekUtil.getWeekStart();
        return huntingSessionRepository.findByUserIdAndWeekStartOrderBySessionDateDesc(userId, weekStart);
    }

    /**
     * 기능 #3: 사냥터별 시간당 수익 효율 통계
     */
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
}
