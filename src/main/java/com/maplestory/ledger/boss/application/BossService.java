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
import com.maplestory.ledger.boss.infrastructure.BossDropMasterRepository;
import com.maplestory.ledger.boss.infrastructure.BossDropRecordRepository;
import com.maplestory.ledger.boss.infrastructure.BossKillRepository;
import com.maplestory.ledger.boss.infrastructure.BossMasterRepository;
import com.maplestory.ledger.boss.infrastructure.DopingMasterRepository;
import com.maplestory.ledger.boss.infrastructure.projection.BossStatsProjection;
import com.maplestory.ledger.boss.presentation.dto.BossDropMasterResponse;
import com.maplestory.ledger.boss.presentation.dto.BossDropRecordResponse;
import com.maplestory.ledger.boss.presentation.dto.BossKillResponse;
import com.maplestory.ledger.boss.presentation.dto.BossKillUpdateRequest;
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

        // 같은 캐릭터가 이번 주에 동일 보스를 이미 처치한 경우 차단
        if (character != null && bossKillRepository.existsByCharacterIdAndBossNameAndDifficultyAndWeekStart(
                character.getId(), cmd.bossName(), cmd.difficulty(), weekStart)) {
            throw new IllegalStateException(
                    character.getName() + "은(는) 이번 주에 이미 " + cmd.bossName() + " " + cmd.difficulty() + "을(를) 처치했습니다.");
        }

        // 주간 보스 12개 제한
        if (character != null && bossMaster.getResetType() != null
                && bossMaster.getResetType().equals("weekly")) {
            int weeklyCount = bossKillRepository.countWeeklyBossesByCharacter(character.getId(), weekStart);
            if (weeklyCount >= 12) {
                throw new IllegalStateException(
                        character.getName() + "은(는) 이번 주 주간 보스 12개를 모두 처치했습니다.");
            }
        }

        int partySize = (cmd.partySize() != null && cmd.partySize() > 1) ? cmd.partySize() : 1;
        long income = bossMaster.getCrystalPrice() / partySize;
        String description = cmd.bossName() + " " + cmd.difficulty() + " 결정석"
                + (partySize > 1 ? " (" + partySize + "인 파티 1/" + partySize + ")" : "");

        LedgerEntry ledgerEntry = ledgerEntryRepository.save(
                LedgerEntry.create(user, character,
                        LedgerEntry.EntryType.income, LedgerEntry.EntryCategory.boss,
                        income, description, cmd.killDate(), weekStart)
        );

        long newInventory = Math.max(0, user.getInventoryMeso() + income);
        user.updateMesoBalance(newInventory, user.getStorageMeso());
        userRepository.save(user);

        BossKill kill = bossKillRepository.save(
                BossKill.create(user, character, ledgerEntry,
                        cmd.bossName(), cmd.difficulty(), bossMaster.getCrystalPrice(),
                        cmd.killDate(), weekStart, cmd.partySize(), 0L,
                        bossMaster.getResetType())
        );

        // 도핑비 등 인라인 지출 처리 — boss_kill_id로 연결
        long totalExpense = 0L;
        for (RecordBossKillCommand.InlineExpense exp : cmd.expenses()) {
            LedgerEntry.EntryCategory cat;
            try { cat = LedgerEntry.EntryCategory.valueOf(exp.category()); }
            catch (IllegalArgumentException ignored) { cat = LedgerEntry.EntryCategory.other; }
            LedgerEntry expEntry = ledgerEntryRepository.save(LedgerEntry.create(user, character,
                    LedgerEntry.EntryType.expense, cat, exp.amount(),
                    exp.description(), cmd.killDate(), weekStart));
            expEntry.linkBossKill(kill.getId());
            ledgerEntryRepository.save(expEntry);
            totalExpense += exp.amount();
        }

        if (totalExpense > 0) {
            user.updateMesoBalance(Math.max(0, user.getInventoryMeso() - totalExpense), user.getStorageMeso());
            userRepository.save(user);
            kill.updateTotalExpense(totalExpense);
            bossKillRepository.save(kill);
        }

        return BossKillResponse.from(kill);
    }

    @Transactional
    public void deleteBossKill(Long userId, Long killId) {
        BossKill kill = bossKillRepository.findByIdAndUserId(killId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("보스 처치 기록을 찾을 수 없습니다."));

        long bossIncome = kill.getLedgerEntry() != null ? kill.getLedgerEntry().getAmount() : 0L;
        long totalExpense = kill.getTotalExpense() != null ? kill.getTotalExpense() : 0L;
        Long incomeEntryId = kill.getLedgerEntry() != null ? kill.getLedgerEntry().getId() : null;

        // 판매 완료된 드랍 기록의 경매 LedgerEntry 수집 후 역산 준비
        List<BossDropRecord> drops = bossDropRecordRepository.findByBossKillId(killId);
        long auctionIncome = drops.stream()
                .filter(d -> d.getStatus() == BossDropRecord.DropStatus.sold && d.getLedgerEntry() != null)
                .mapToLong(d -> d.getLedgerEntry().getAmount())
                .sum();
        List<Long> auctionEntryIds = drops.stream()
                .filter(d -> d.getStatus() == BossDropRecord.DropStatus.sold && d.getLedgerEntry() != null)
                .map(d -> d.getLedgerEntry().getId())
                .toList();

        // 드랍 기록 삭제 (boss_kill_id FK 제약 해소)
        bossDropRecordRepository.deleteByBossKillId(killId);

        // 도핑 지출 LedgerEntry 삭제 (boss_kill_id로 연결된 것들)
        ledgerEntryRepository.deleteByBossKillId(killId);

        // BossKill 삭제 (boss_kills.ledger_entry_id FK가 있으므로 소득 항목보다 먼저)
        bossKillRepository.delete(kill);

        // 소득 LedgerEntry 삭제
        if (incomeEntryId != null) {
            ledgerEntryRepository.deleteById(incomeEntryId);
        }

        // 판매된 드랍의 경매 LedgerEntry 삭제
        for (Long entryId : auctionEntryIds) {
            ledgerEntryRepository.deleteById(entryId);
        }

        // 메소 역산: 보스 수익 제거 + 도핑 지출 복구 + 경매 수익 제거
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
        user.updateMesoBalance(
                Math.max(0, user.getInventoryMeso() - bossIncome + totalExpense - auctionIncome),
                user.getStorageMeso()
        );
        userRepository.save(user);
    }

    @Transactional
    public BossKillResponse updateBossKill(Long userId, Long killId, BossKillUpdateRequest req) {
        BossKill kill = bossKillRepository.findByIdAndUserId(killId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("보스 처치 기록을 찾을 수 없습니다."));

        int oldPartySize = kill.getPartySize() != null && kill.getPartySize() > 1 ? kill.getPartySize() : 1;
        int newPartySize = req.partySize() != null && req.partySize() > 1 ? req.partySize() : 1;

        if (oldPartySize != newPartySize) {
            long oldIncome = kill.getCrystalPrice() / oldPartySize;
            long newIncome = kill.getCrystalPrice() / newPartySize;
            long delta = newIncome - oldIncome;

            if (kill.getLedgerEntry() != null) {
                LedgerEntry entry = kill.getLedgerEntry();
                String newDesc = kill.getBossName() + " " + kill.getDifficulty() + " 결정석"
                        + (newPartySize > 1 ? " (" + newPartySize + "인 파티 1/" + newPartySize + ")" : "");
                entry.update(entry.getType(), entry.getCategory(), newIncome, newDesc,
                        entry.getEntryDate(), entry.getWeekStart(), entry.getSolErdaFragments());
                ledgerEntryRepository.save(entry);
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
            user.updateMesoBalance(Math.max(0, user.getInventoryMeso() + delta), user.getStorageMeso());
            userRepository.save(user);

            kill.updatePartySize(newPartySize);
        }

        return BossKillResponse.from(bossKillRepository.save(kill));
    }

    @Transactional(readOnly = true)
    public List<BossKillResponse> getWeeklyBossKills(Long userId, LocalDate weekStartParam, Long characterId) {
        LocalDate weekStart = weekStartParam != null ? weekStartParam : WeekUtil.getWeekStart();
        List<BossKill> kills = characterId != null
                ? bossKillRepository.findByUserIdAndWeekStartAndCharacterIdOrderByKillDateDesc(userId, weekStart, characterId)
                : bossKillRepository.findByUserIdAndWeekStartOrderByKillDateDesc(userId, weekStart);
        return kills.stream().map(kill -> {
            List<BossDropRecordResponse> drops = bossDropRecordRepository.findByBossKillId(kill.getId())
                    .stream().map(BossDropRecordResponse::from).toList();
            return BossKillResponse.from(kill, drops);
        }).toList();
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
