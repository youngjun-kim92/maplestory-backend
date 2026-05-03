package com.maplestory.ledger.boss.application;

import com.maplestory.ledger.auth.domain.User;
import com.maplestory.ledger.auth.infrastructure.UserRepository;
import com.maplestory.ledger.boss.application.command.RecordBossKillCommand;
import com.maplestory.ledger.boss.application.command.RecordDropCommand;
import com.maplestory.ledger.boss.application.command.SellDropCommand;
import com.maplestory.ledger.boss.domain.BossDropMaster;
import com.maplestory.ledger.boss.domain.BossDropRecord;
import com.maplestory.ledger.boss.domain.BossKill;
import com.maplestory.ledger.boss.domain.BossMaster;
import com.maplestory.ledger.boss.domain.DopingMaster;
import com.maplestory.ledger.boss.infrastructure.BossDropMasterRepository;
import com.maplestory.ledger.boss.infrastructure.BossDropRecordRepository;
import com.maplestory.ledger.boss.infrastructure.BossKillRepository;
import com.maplestory.ledger.boss.infrastructure.BossMasterRepository;
import com.maplestory.ledger.boss.infrastructure.DopingMasterRepository;
import com.maplestory.ledger.boss.infrastructure.projection.BossStatsProjection;
import com.maplestory.ledger.boss.presentation.dto.BossDropMasterResponse;
import com.maplestory.ledger.boss.presentation.dto.BossDropRecordResponse;
import com.maplestory.ledger.boss.presentation.dto.BossKillResponse;
import com.maplestory.ledger.boss.presentation.dto.BossMasterResponse;
import com.maplestory.ledger.boss.presentation.dto.DopingMasterResponse;
import com.maplestory.ledger.character.domain.MapleCharacter;
import com.maplestory.ledger.character.infrastructure.CharacterRepository;
import com.maplestory.ledger.common.exception.ResourceNotFoundException;
import com.maplestory.ledger.common.util.WeekUtil;
import com.maplestory.ledger.ledger.domain.LedgerEntry;
import com.maplestory.ledger.ledger.infrastructure.LedgerEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BossService {

    private final BossKillRepository bossKillRepository;
    private final BossMasterRepository bossMasterRepository;
    private final BossDropMasterRepository bossDropMasterRepository;
    private final BossDropRecordRepository bossDropRecordRepository;
    private final DopingMasterRepository dopingMasterRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final CharacterRepository characterRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<BossMasterResponse> getBossList() {
        return bossMasterRepository.findAllByOrderByBossNameAscDifficultyAsc()
                .stream().map(BossMasterResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<DopingMasterResponse> getDopingList() {
        return dopingMasterRepository.findAllByOrderBySortOrderAsc()
                .stream().map(DopingMasterResponse::from).toList();
    }

    /**
     * 보스 처치를 기록합니다. boss/hunting 도메인은 가계부 항목(LedgerEntry)과 강하게 결합되어
     * 동일 트랜잭션 내에서 LedgerEntry를 직접 생성합니다.
     * (도메인 이벤트 방식 대신 실용적 결합을 선택한 의도적 설계 결정)
     */
    @Transactional
    public BossKillResponse recordBossKill(Long userId, RecordBossKillCommand cmd) {
        BossMaster bossMaster = bossMasterRepository
                .findByBossNameAndDifficulty(cmd.bossName(), cmd.difficulty())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "보스 정보를 찾을 수 없습니다: " + cmd.bossName() + " " + cmd.difficulty()));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
        MapleCharacter character = resolveCharacter(userId, cmd.characterId());
        LocalDate weekStart = WeekUtil.getWeekStart(cmd.killDate());

        int partySize = (cmd.partySize() != null && cmd.partySize() > 1) ? cmd.partySize() : 1;
        long income = bossMaster.getCrystalPrice() / partySize;
        String description = cmd.bossName() + " " + cmd.difficulty() + " 결정석"
                + (partySize > 1 ? " (" + partySize + "인 파티 1/" + partySize + ")" : "");

        LedgerEntry ledgerEntry = ledgerEntryRepository.save(
                LedgerEntry.create(user, character,
                        LedgerEntry.EntryType.income, LedgerEntry.EntryCategory.boss,
                        income, description, cmd.killDate(), weekStart)
        );

        // 도핑비 등 인라인 지출 처리
        long totalExpense = 0L;
        for (RecordBossKillCommand.InlineExpense exp : cmd.expenses()) {
            LedgerEntry.EntryCategory cat;
            try { cat = LedgerEntry.EntryCategory.valueOf(exp.category()); }
            catch (IllegalArgumentException ignored) { cat = LedgerEntry.EntryCategory.other; }
            ledgerEntryRepository.save(LedgerEntry.create(user, character,
                    LedgerEntry.EntryType.expense, cat, exp.amount(),
                    exp.description(), cmd.killDate(), weekStart));
            totalExpense += exp.amount();
        }

        long newInventory = Math.max(0, user.getInventoryMeso() + income - totalExpense);
        user.updateMesoBalance(newInventory, user.getStorageMeso());
        userRepository.save(user);

        BossKill kill = bossKillRepository.save(
                BossKill.create(user, character, ledgerEntry,
                        cmd.bossName(), cmd.difficulty(), bossMaster.getCrystalPrice(),
                        cmd.killDate(), weekStart, cmd.partySize())
        );
        return BossKillResponse.from(kill);
    }

    @Transactional(readOnly = true)
    public List<BossKillResponse> getWeeklyBossKills(Long userId, LocalDate weekStartParam) {
        LocalDate weekStart = weekStartParam != null ? weekStartParam : WeekUtil.getWeekStart();
        return bossKillRepository
                .findByUserIdAndWeekStartOrderByKillDateDesc(userId, weekStart)
                .stream().map(BossKillResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<BossStatsProjection> getBossStats(Long userId) {
        return bossKillRepository.findBossStats(userId);
    }

    // ── 드랍 마스터 ──────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<BossDropMasterResponse> getBossDropItems(String bossName, String difficulty) {
        return bossDropMasterRepository.findByBossNameAndDifficulty(bossName, difficulty)
                .stream().map(BossDropMasterResponse::from).toList();
    }

    // ── 드랍 기록 ────────────────────────────────────────────────────────────

    @Transactional
    public BossDropRecordResponse listDrop(Long userId, Long dropRecordId) {
        BossDropRecord dropRecord = bossDropRecordRepository.findByIdAndUserId(dropRecordId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("드랍 기록을 찾을 수 없습니다."));
        dropRecord.list();
        return BossDropRecordResponse.from(dropRecord);
    }

    @Transactional
    public BossDropRecordResponse recordDrop(Long userId, RecordDropCommand cmd) {
        BossKill bossKill = bossKillRepository.findByIdAndUserId(cmd.bossKillId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("보스 처치 기록을 찾을 수 없습니다."));

        BossDropMaster.ItemCategory category = bossDropMasterRepository
                .findByBossNameAndDifficulty(bossKill.getBossName(), bossKill.getDifficulty())
                .stream()
                .filter(m -> m.getItemName().equals(cmd.itemName()))
                .findFirst()
                .map(BossDropMaster::getItemCategory)
                .orElse(BossDropMaster.ItemCategory.other);

        BossDropRecord record = bossDropRecordRepository.save(
                BossDropRecord.create(
                        bossKill.getUser(), bossKill.getCharacter(), bossKill,
                        cmd.itemName(), category, bossKill.getWeekStart()
                )
        );
        return BossDropRecordResponse.from(record);
    }

    @Transactional
    public BossDropRecordResponse sellDrop(Long userId, SellDropCommand cmd) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
        BossDropRecord dropRecord = bossDropRecordRepository.findByIdAndUserId(cmd.dropRecordId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("드랍 기록을 찾을 수 없습니다."));

        LocalDate weekStart = WeekUtil.getWeekStart(cmd.saleDate());

        double feeRate = calcAuctionFeeRate(user, cmd.isPcCafe());
        long netAmount = Math.round(cmd.saleAmount() * (1.0 - feeRate));
        int feePercent = (int) Math.round(feeRate * 100);

        String description = dropRecord.getItemName() + " 경매장 판매 ("
                + dropRecord.getBossKill().getBossName() + " "
                + dropRecord.getBossKill().getDifficulty()
                + ", 수수료 " + feePercent + "% 적용)";

        LedgerEntry ledgerEntry = ledgerEntryRepository.save(
                LedgerEntry.create(dropRecord.getUser(), dropRecord.getCharacter(),
                        LedgerEntry.EntryType.income, LedgerEntry.EntryCategory.auction,
                        netAmount, description, cmd.saleDate(), weekStart)
        );

        user.updateMesoBalance(user.getInventoryMeso() + netAmount, user.getStorageMeso());
        userRepository.save(user);

        dropRecord.sell(cmd.saleAmount(), cmd.saleDate(), ledgerEntry);
        return BossDropRecordResponse.from(dropRecord);
    }

    @Transactional(readOnly = true)
    public List<BossDropRecordResponse> getWeeklyDropRecords(Long userId, LocalDate weekStartParam) {
        LocalDate weekStart = weekStartParam != null ? weekStartParam : WeekUtil.getWeekStart();
        return bossDropRecordRepository.findByUserIdAndWeekStartOrderByCreatedAtDesc(userId, weekStart)
                .stream().map(BossDropRecordResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<BossDropRecordResponse> getDropsByBossKill(Long userId, Long bossKillId) {
        bossKillRepository.findByIdAndUserId(bossKillId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("보스 처치 기록을 찾을 수 없습니다."));
        return bossDropRecordRepository.findByBossKillId(bossKillId)
                .stream().map(BossDropRecordResponse::from).toList();
    }

    private MapleCharacter resolveCharacter(Long userId, Long characterId) {
        if (characterId == null) return null;
        return characterRepository.findByIdAndUserId(characterId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("캐릭터를 찾을 수 없습니다."));
    }

    private double calcAuctionFeeRate(User user, Boolean isPcCafe) {
        if (Boolean.TRUE.equals(isPcCafe)) return 0.03;
        if (user == null || user.getMvpGrade() == null) return 0.05;
        return user.getMvpGrade().auctionFeeRate();
    }
}
