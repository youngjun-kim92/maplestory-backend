package com.maplestory.ledger.boss.application;

import com.maplestory.ledger.auth.domain.User;
import com.maplestory.ledger.auth.infrastructure.UserRepository;
import com.maplestory.ledger.boss.application.command.RecordBossKillCommand;
import com.maplestory.ledger.boss.domain.BossKill;
import com.maplestory.ledger.boss.domain.BossMaster;
import com.maplestory.ledger.boss.infrastructure.BossKillRepository;
import com.maplestory.ledger.boss.infrastructure.BossMasterRepository;
import com.maplestory.ledger.boss.infrastructure.projection.BossStatsProjection;
import com.maplestory.ledger.boss.presentation.dto.BossKillResponse;
import com.maplestory.ledger.boss.presentation.dto.BossMasterResponse;
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
    public List<BossMasterResponse> getBossList() {
        return bossMasterRepository.findAllByOrderByBossNameAscDifficultyAsc()
                .stream().map(BossMasterResponse::from).toList();
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

        User user = userRepository.getReferenceById(userId);
        MapleCharacter character = resolveCharacter(userId, cmd.characterId());
        LocalDate weekStart = WeekUtil.getWeekStart(cmd.killDate());

        LedgerEntry ledgerEntry = ledgerEntryRepository.save(
                LedgerEntry.create(user, character,
                        LedgerEntry.EntryType.income, LedgerEntry.EntryCategory.boss,
                        bossMaster.getCrystalPrice(),
                        cmd.bossName() + " " + cmd.difficulty() + " 결정석",
                        cmd.killDate(), weekStart)
        );

        BossKill kill = bossKillRepository.save(
                BossKill.create(user, character, ledgerEntry,
                        cmd.bossName(), cmd.difficulty(), bossMaster.getCrystalPrice(),
                        cmd.killDate(), weekStart)
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

    private MapleCharacter resolveCharacter(Long userId, Long characterId) {
        if (characterId == null) return null;
        return characterRepository.findByIdAndUserId(characterId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("캐릭터를 찾을 수 없습니다."));
    }
}