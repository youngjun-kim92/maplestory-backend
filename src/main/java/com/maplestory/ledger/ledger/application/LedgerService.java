package com.maplestory.ledger.ledger.application;

import com.maplestory.ledger.auth.domain.User;
import com.maplestory.ledger.auth.infrastructure.UserRepository;
import com.maplestory.ledger.character.domain.MapleCharacter;
import com.maplestory.ledger.character.infrastructure.CharacterRepository;
import com.maplestory.ledger.common.exception.ResourceNotFoundException;
import com.maplestory.ledger.common.util.WeekUtil;
import com.maplestory.ledger.goal.application.GoalService;
import com.maplestory.ledger.goal.application.GoalWarning;
import com.maplestory.ledger.ledger.application.command.AddLedgerEntryCommand;
import com.maplestory.ledger.ledger.domain.LedgerEntry;
import com.maplestory.ledger.ledger.domain.LedgerEntry.EntryType;
import com.maplestory.ledger.ledger.infrastructure.LedgerEntryRepository;
import com.maplestory.ledger.ledger.presentation.dto.AddEntryResponse;
import com.maplestory.ledger.ledger.presentation.dto.CategoryStatResponse;
import com.maplestory.ledger.ledger.presentation.dto.IncomeSourceTrendResponse;
import com.maplestory.ledger.ledger.presentation.dto.LedgerEntryResponse;
import com.maplestory.ledger.ledger.presentation.dto.WeekSummaryResponse;
import com.maplestory.ledger.ledger.presentation.dto.WeeklyLedgerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LedgerService {

    private final LedgerEntryRepository ledgerEntryRepository;
    private final UserRepository userRepository;
    private final CharacterRepository characterRepository;
    private final GoalService goalService;

    @Transactional(readOnly = true)
    public WeeklyLedgerResponse getWeeklyLedger(Long userId, LocalDate weekStartParam) {
        LocalDate weekStart = weekStartParam != null ? weekStartParam : WeekUtil.getWeekStart();
        List<LedgerEntry> entries = ledgerEntryRepository
                .findByUserIdAndWeekStartOrderByEntryDateDescCreatedAtDesc(userId, weekStart);

        long totalIncome = entries.stream()
                .filter(e -> e.getType() == EntryType.income)
                .mapToLong(LedgerEntry::getAmount).sum();
        long totalExpense = entries.stream()
                .filter(e -> e.getType() == EntryType.expense)
                .mapToLong(LedgerEntry::getAmount).sum();

        return new WeeklyLedgerResponse(
                weekStart,
                entries.stream().map(LedgerEntryResponse::from).toList(),
                new WeeklyLedgerResponse.Summary(totalIncome, totalExpense, totalIncome - totalExpense)
        );
    }

    @Transactional
    public AddEntryResponse addEntry(Long userId, AddLedgerEntryCommand cmd) {
        User user = userRepository.getReferenceById(userId);
        MapleCharacter character = resolveCharacter(userId, cmd.characterId());
        LocalDate weekStart = WeekUtil.getWeekStart(cmd.entryDate());

        LedgerEntry entry = ledgerEntryRepository.save(
                LedgerEntry.create(user, character, cmd.type(), cmd.category(),
                        cmd.amount(), cmd.description(), cmd.entryDate(), weekStart)
        );

        List<GoalWarning> warnings = List.of();
        if (cmd.type() == EntryType.expense) {
            warnings = goalService.checkGoalDelays(userId, cmd.amount());
        }
        return new AddEntryResponse(LedgerEntryResponse.from(entry), warnings);
    }

    @Transactional
    public void deleteEntry(Long userId, Long entryId) {
        LedgerEntry entry = ledgerEntryRepository.findByIdAndUserId(entryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("항목을 찾을 수 없습니다."));
        ledgerEntryRepository.delete(entry);
    }

    @Transactional(readOnly = true)
    public List<WeekSummaryResponse> getWeeksList(Long userId) {
        return ledgerEntryRepository.findWeeklySummaries(userId)
                .stream().map(WeekSummaryResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryStatResponse> getCategoryStats(Long userId, int weeks) {
        LocalDate startDate = WeekUtil.getWeekStart().minusWeeks(weeks - 1L);
        return ledgerEntryRepository.findCategoryStats(userId, startDate)
                .stream()
                .map(row -> new CategoryStatResponse(
                        (String) row[0],
                        (String) row[1],
                        ((Number) row[2]).longValue(),
                        ((Number) row[3]).longValue(),
                        ((Number) row[4]).longValue()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<IncomeSourceTrendResponse> getIncomeSourceTrend(Long userId, int weeks) {
        LocalDate startDate = WeekUtil.getWeekStart().minusWeeks(weeks - 1L);
        return ledgerEntryRepository.findIncomeSourceTrend(userId, startDate)
                .stream()
                .map(row -> new IncomeSourceTrendResponse(
                        ((java.sql.Date) row[0]).toLocalDate(),
                        ((Number) row[1]).longValue(),
                        ((Number) row[2]).longValue(),
                        ((Number) row[3]).longValue(),
                        ((Number) row[4]).longValue()
                ))
                .toList();
    }

    private MapleCharacter resolveCharacter(Long userId, Long characterId) {
        if (characterId == null) return null;
        return characterRepository.findByIdAndUserId(characterId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("캐릭터를 찾을 수 없습니다."));
    }
}