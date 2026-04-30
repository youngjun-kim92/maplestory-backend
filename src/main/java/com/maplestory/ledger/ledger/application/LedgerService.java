package com.maplestory.ledger.ledger.application;

import com.maplestory.ledger.auth.domain.User;
import com.maplestory.ledger.auth.infrastructure.UserRepository;
import com.maplestory.ledger.character.domain.MapleCharacter;
import com.maplestory.ledger.boss.infrastructure.BossDropRecordRepository;
import com.maplestory.ledger.boss.infrastructure.BossKillRepository;
import com.maplestory.ledger.character.infrastructure.CharacterRepository;
import com.maplestory.ledger.common.exception.ResourceNotFoundException;
import com.maplestory.ledger.common.util.WeekUtil;
import com.maplestory.ledger.goal.application.GoalService;
import com.maplestory.ledger.goal.application.GoalWarning;
import com.maplestory.ledger.hunting.infrastructure.HuntingSessionRepository;
import com.maplestory.ledger.ledger.application.command.AddLedgerEntryCommand;
import com.maplestory.ledger.ledger.application.command.UpdateLedgerEntryCommand;
import com.maplestory.ledger.ledger.domain.LedgerEntry;
import com.maplestory.ledger.ledger.domain.LedgerEntry.EntryCategory;
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
    private final BossKillRepository bossKillRepository;
    private final BossDropRecordRepository bossDropRecordRepository;
    private final HuntingSessionRepository huntingSessionRepository;

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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
        MapleCharacter character = resolveCharacter(userId, cmd.characterId());
        LocalDate weekStart = WeekUtil.getWeekStart(cmd.entryDate());

        int fragments = cmd.solErdaFragments() != null ? cmd.solErdaFragments() : 0;
        LedgerEntry entry = ledgerEntryRepository.save(
                LedgerEntry.create(user, character, cmd.type(), cmd.category(),
                        cmd.amount(), cmd.description(), cmd.entryDate(), weekStart, fragments)
        );

        // 수익/지출 발생 시 인벤토리 메소 즉시 반영
        long delta = cmd.type() == EntryType.income ? cmd.amount() : -cmd.amount();
        long newInventory = Math.max(0, user.getInventoryMeso() + delta);
        user.updateMesoBalance(newInventory, user.getStorageMeso());
        userRepository.save(user);

        // 사냥 수익 기록 시 캐릭터의 솔 에르다 조각 수 누적
        if (cmd.category() == EntryCategory.hunting && character != null && fragments > 0) {
            character.addSolErdaFragments(fragments);
            characterRepository.save(character);
        }

        List<GoalWarning> warnings = List.of();
        if (cmd.type() == EntryType.expense) {
            warnings = goalService.checkGoalDelays(userId, cmd.amount());
        }
        return new AddEntryResponse(LedgerEntryResponse.from(entry), warnings);
    }

    @Transactional
    public LedgerEntryResponse updateEntry(Long userId, Long entryId, UpdateLedgerEntryCommand cmd) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
        LedgerEntry entry = ledgerEntryRepository.findByIdAndUserId(entryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("항목을 찾을 수 없습니다."));

        // 메소 변화량: 기존 효과 역산 후 새 효과 적용
        long oldEffect = entry.getType() == EntryType.income ? entry.getAmount() : -entry.getAmount();
        long newEffect = cmd.type() == EntryType.income ? cmd.amount() : -cmd.amount();
        long newInventory = Math.max(0, user.getInventoryMeso() - oldEffect + newEffect);
        user.updateMesoBalance(newInventory, user.getStorageMeso());
        userRepository.save(user);

        // 솔 에르다 조각 변화량 캐릭터에 반영
        int oldFragments = entry.getSolErdaFragments() != null ? entry.getSolErdaFragments() : 0;
        int newFragments = cmd.solErdaFragments() != null ? cmd.solErdaFragments() : 0;
        if (entry.getCharacter() != null && entry.getCategory() == EntryCategory.hunting) {
            int fragmentDelta = newFragments - oldFragments;
            if (fragmentDelta != 0) {
                entry.getCharacter().addSolErdaFragments(fragmentDelta);
                characterRepository.save(entry.getCharacter());
            }
        }

        LocalDate weekStart = WeekUtil.getWeekStart(cmd.entryDate());
        entry.update(cmd.type(), cmd.category(), cmd.amount(),
                cmd.description(), cmd.entryDate(), weekStart, newFragments);
        return LedgerEntryResponse.from(ledgerEntryRepository.save(entry));
    }

    @Transactional
    public void deleteEntry(Long userId, Long entryId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
        LedgerEntry entry = ledgerEntryRepository.findByIdAndUserId(entryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("항목을 찾을 수 없습니다."));

        // FK 참조 해제 (BossKill, HuntingSession, BossDropRecord → LedgerEntry)
        bossKillRepository.clearLedgerEntryRef(entryId);
        huntingSessionRepository.clearLedgerEntryRef(entryId);
        bossDropRecordRepository.clearLedgerEntryRef(entryId);

        // 삭제 시 메소 역산
        long delta = entry.getType() == EntryType.income ? -entry.getAmount() : entry.getAmount();
        long newInventory = Math.max(0, user.getInventoryMeso() + delta);
        user.updateMesoBalance(newInventory, user.getStorageMeso());
        userRepository.save(user);

        // 사냥 기록 삭제 시 캐릭터의 솔 에르다 조각 수 차감
        int fragments = entry.getSolErdaFragments() != null ? entry.getSolErdaFragments() : 0;
        if (entry.getCategory() == EntryCategory.hunting && entry.getCharacter() != null && fragments > 0) {
            entry.getCharacter().addSolErdaFragments(-fragments);
            characterRepository.save(entry.getCharacter());
        }

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