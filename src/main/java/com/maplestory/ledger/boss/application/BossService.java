package com.maplestory.ledger.boss.application;

import com.maplestory.ledger.auth.domain.User;
import com.maplestory.ledger.auth.infrastructure.UserRepository;
import com.maplestory.ledger.boss.domain.BossKill;
import com.maplestory.ledger.boss.domain.BossMaster;
import com.maplestory.ledger.boss.infrastructure.BossKillRepository;
import com.maplestory.ledger.boss.infrastructure.BossMasterRepository;
import com.maplestory.ledger.boss.infrastructure.projection.BossStatsProjection;
import com.maplestory.ledger.boss.presentation.dto.BossKillRequest;
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
    private final LedgerEntryRepository ledgerEntryRepository;
    private final CharacterRepository characterRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<BossMaster> getBossList() {
        return bossMasterRepository.findAllByOrderByBossNameAscDifficultyAsc();
    }

    /**
     * 기능 #4: 보스 이름+난이도 선택만으로 결정석 가격을 자동 계산하여 가계부에 합산합니다.
     */
    @Transactional
    public BossKill recordBossKill(Long userId, BossKillRequest req) {
        BossMaster bossMaster = bossMasterRepository.findByBossNameAndDifficulty(req.bossName(), req.difficulty())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "보스 정보를 찾을 수 없습니다: " + req.bossName() + " " + req.difficulty()));

        User user = userRepository.getReferenceById(userId);
        MapleCharacter character = null;
        if (req.characterId() != null) {
            character = characterRepository.findByIdAndUserId(req.characterId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("캐릭터를 찾을 수 없습니다."));
        }

        LocalDate weekStart = WeekUtil.getWeekStart(req.killDate());

        LedgerEntry ledgerEntry = new LedgerEntry();
        ledgerEntry.setUser(user);
        ledgerEntry.setCharacter(character);
        ledgerEntry.setType(LedgerEntry.EntryType.income);
        ledgerEntry.setCategory(LedgerEntry.EntryCategory.boss);
        ledgerEntry.setAmount(bossMaster.getCrystalPrice());
        ledgerEntry.setDescription(req.bossName() + " " + req.difficulty() + " 결정석");
        ledgerEntry.setEntryDate(req.killDate());
        ledgerEntry.setWeekStart(weekStart);
        ledgerEntry = ledgerEntryRepository.save(ledgerEntry);

        BossKill kill = new BossKill();
        kill.setUser(user);
        kill.setCharacter(character);
        kill.setLedgerEntry(ledgerEntry);
        kill.setBossName(req.bossName());
        kill.setDifficulty(req.difficulty());
        kill.setCrystalPrice(bossMaster.getCrystalPrice());
        kill.setKillDate(req.killDate());
        kill.setWeekStart(weekStart);
        return bossKillRepository.save(kill);
    }

    @Transactional(readOnly = true)
    public List<BossKill> getWeeklyBossKills(Long userId, LocalDate weekStartParam) {
        LocalDate weekStart = weekStartParam != null ? weekStartParam : WeekUtil.getWeekStart();
        return bossKillRepository.findByUserIdAndWeekStartOrderByKillDateDesc(userId, weekStart);
    }

    @Transactional(readOnly = true)
    public List<BossStatsProjection> getBossStats(Long userId) {
        return bossKillRepository.findBossStats(userId);
    }
}
