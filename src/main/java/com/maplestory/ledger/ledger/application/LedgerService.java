package com.maplestory.ledger.ledger.application;

import com.maplestory.ledger.auth.domain.User;
import com.maplestory.ledger.auth.infrastructure.UserRepository;
import com.maplestory.ledger.character.domain.MapleCharacter;
import com.maplestory.ledger.character.infrastructure.CharacterRepository;
import com.maplestory.ledger.common.exception.ResourceNotFoundException;
import com.maplestory.ledger.common.util.WeekUtil;
import com.maplestory.ledger.goal.application.GoalService;
import com.maplestory.ledger.goal.application.GoalWarning;
import com.maplestory.ledger.ledger.domain.LedgerEntry;
import com.maplestory.ledger.ledger.domain.LedgerEntry.EntryType;
import com.maplestory.ledger.ledger.infrastructure.LedgerEntryRepository;
import com.maplestory.ledger.ledger.infrastructure.projection.WeeklySummaryProjection;
import com.maplestory.ledger.ledger.presentation.dto.LedgerEntryRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LedgerService {

    private final LedgerEntryRepository ledgerEntryRepository;
    private final UserRepository userRepository;
    private final CharacterRepository characterRepository;
    private final GoalService goalService;

    @Transactional(readOnly = true)
    public Map<String, Object> getWeeklyLedger(Long userId, LocalDate weekStartParam) {
        LocalDate weekStart = weekStartParam != null ? weekStartParam : WeekUtil.getWeekStart();
        List<LedgerEntry> entries = ledgerEntryRepository
                .findByUserIdAndWeekStartOrderByEntryDateDescCreatedAtDesc(userId, weekStart);

        long totalIncome = entries.stream()
                .filter(e -> e.getType() == EntryType.income)
                .mapToLong(LedgerEntry::getAmount).sum();
        long totalExpense = entries.stream()
                .filter(e -> e.getType() == EntryType.expense)
                .mapToLong(LedgerEntry::getAmount).sum();

        return Map.of(
                "weekStart", weekStart,
                "entries", entries,
                "summary", Map.of(
                        "totalIncome", totalIncome,
                        "totalExpense", totalExpense,
                        "netProfit", totalIncome - totalExpense
                )
        );
    }

    @Transactional
    public Map<String, Object> addEntry(Long userId, LedgerEntryRequest req) {
        User user = userRepository.getReferenceById(userId);
        MapleCharacter character = null;
        if (req.characterId() != null) {
            character = characterRepository.findByIdAndUserId(req.characterId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("캐릭터를 찾을 수 없습니다."));
        }

        LocalDate weekStart = WeekUtil.getWeekStart(req.entryDate());
        LedgerEntry entry = new LedgerEntry();
        entry.setUser(user);
        entry.setCharacter(character);
        entry.setType(req.type());
        entry.setCategory(req.category());
        entry.setAmount(req.amount());
        entry.setDescription(req.description());
        entry.setEntryDate(req.entryDate());
        entry.setWeekStart(weekStart);
        entry = ledgerEntryRepository.save(entry);

        List<GoalWarning> warnings = List.of();
        if (req.type() == EntryType.expense) {
            warnings = goalService.checkGoalDelays(userId, req.amount());
        }

        return Map.of("entry", entry, "goalWarnings", warnings);
    }

    @Transactional
    public void deleteEntry(Long userId, Long entryId) {
        LedgerEntry entry = ledgerEntryRepository.findByIdAndUserId(entryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("항목을 찾을 수 없습니다."));
        ledgerEntryRepository.delete(entry);
    }

    @Transactional(readOnly = true)
    public List<WeeklySummaryProjection> getWeeksList(Long userId) {
        return ledgerEntryRepository.findWeeklySummaries(userId);
    }

    @Transactional(readOnly = true)
    public List<Object[]> getCategoryStats(Long userId, int weeks) {
        LocalDate startDate = WeekUtil.getWeekStart().minusWeeks(weeks - 1L);
        return ledgerEntryRepository.findCategoryStats(userId, startDate);
    }
}
