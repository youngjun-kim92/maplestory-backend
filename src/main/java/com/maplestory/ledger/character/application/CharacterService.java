package com.maplestory.ledger.character.application;

import com.maplestory.ledger.auth.domain.User;
import com.maplestory.ledger.auth.infrastructure.UserRepository;
import com.maplestory.ledger.boss.infrastructure.BossKillRepository;
import com.maplestory.ledger.character.domain.MapleCharacter;
import com.maplestory.ledger.character.infrastructure.CharacterRepository;
import com.maplestory.ledger.character.presentation.dto.CharacterRequest;
import com.maplestory.ledger.character.presentation.dto.CharacterROIResponse;
import com.maplestory.ledger.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CharacterService {

    private final CharacterRepository characterRepository;
    private final UserRepository userRepository;
    private final BossKillRepository bossKillRepository;

    @Transactional
    public MapleCharacter createCharacter(Long userId, CharacterRequest req) {
        User user = userRepository.getReferenceById(userId);
        MapleCharacter character = new MapleCharacter();
        character.setUser(user);
        character.setName(req.name());
        character.setJobClass(req.jobClass());
        character.setLevel(req.level() != null ? req.level() : 1);
        character.setIsMain(req.isMain() != null ? req.isMain() : false);
        character.setInitialInvestment(req.initialInvestment() != null ? req.initialInvestment() : 0L);
        return characterRepository.save(character);
    }

    @Transactional(readOnly = true)
    public List<MapleCharacter> getCharacters(Long userId) {
        return characterRepository.findByUserIdOrderByIsMainDescCreatedAtAsc(userId);
    }

    @Transactional
    public MapleCharacter updateCharacter(Long userId, Long characterId, CharacterRequest req) {
        MapleCharacter character = characterRepository.findByIdAndUserId(characterId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("캐릭터를 찾을 수 없습니다."));
        if (req.name() != null) character.setName(req.name());
        if (req.jobClass() != null) character.setJobClass(req.jobClass());
        if (req.level() != null) character.setLevel(req.level());
        if (req.isMain() != null) character.setIsMain(req.isMain());
        if (req.initialInvestment() != null) character.setInitialInvestment(req.initialInvestment());
        return characterRepository.save(character);
    }

    @Transactional
    public void deleteCharacter(Long userId, Long characterId) {
        MapleCharacter character = characterRepository.findByIdAndUserId(characterId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("캐릭터를 찾을 수 없습니다."));
        characterRepository.delete(character);
    }

    /**
     * 기능 #8: 부캐릭터 투자 손익분기점 계산.
     * 초기 투자비용 ÷ 주당 평균 보스 수익 = 손익분기점(주)
     */
    @Transactional(readOnly = true)
    public CharacterROIResponse getCharacterROI(Long userId, Long characterId) {
        MapleCharacter character = characterRepository.findByIdAndUserId(characterId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("캐릭터를 찾을 수 없습니다."));

        List<Object[]> weeklyData = bossKillRepository.findWeeklyIncomeByCharacter(userId, characterId);

        long cumulativeBossIncome = weeklyData.stream()
                .mapToLong(row -> ((Number) row[1]).longValue()).sum();

        long weeklyAvgBossIncome = weeklyData.isEmpty() ? 0L :
                cumulativeBossIncome / weeklyData.size();

        Long weeksToBreakEven = null;
        if (weeklyAvgBossIncome > 0 && character.getInitialInvestment() > 0) {
            weeksToBreakEven = (long) Math.ceil((double) character.getInitialInvestment() / weeklyAvgBossIncome);
        }

        boolean isBreakEvenReached = cumulativeBossIncome >= character.getInitialInvestment();
        long remainingToBreakEven = Math.max(0, character.getInitialInvestment() - cumulativeBossIncome);

        return new CharacterROIResponse(
                character.getId(), character.getName(), character.getInitialInvestment(),
                cumulativeBossIncome, weeklyAvgBossIncome, weeksToBreakEven,
                isBreakEvenReached, remainingToBreakEven
        );
    }
}
