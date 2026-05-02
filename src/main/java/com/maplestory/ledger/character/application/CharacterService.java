package com.maplestory.ledger.character.application;

import com.maplestory.ledger.auth.domain.User;
import com.maplestory.ledger.auth.infrastructure.UserRepository;
import com.maplestory.ledger.boss.infrastructure.BossKillRepository;
import com.maplestory.ledger.character.domain.MapleCharacter;
import com.maplestory.ledger.character.infrastructure.CharacterRepository;
import com.maplestory.ledger.character.presentation.dto.CharacterRequest;
import com.maplestory.ledger.character.presentation.dto.CharacterROIResponse;
import com.maplestory.ledger.character.presentation.dto.CharacterResponse;
import com.maplestory.ledger.character.presentation.dto.CharacterStatsResponse;
import com.maplestory.ledger.common.exception.ResourceNotFoundException;
import com.maplestory.ledger.hunting.infrastructure.HuntingSessionRepository;
import com.maplestory.ledger.ledger.infrastructure.LedgerEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CharacterService {

    private final CharacterRepository characterRepository;
    private final UserRepository userRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final BossKillRepository bossKillRepository;
    private final HuntingSessionRepository huntingSessionRepository;

    @Transactional
    public CharacterResponse createCharacter(Long userId, CharacterRequest req) {
        if (Boolean.TRUE.equals(req.isMain())) {
            characterRepository.findByUserIdAndIsMainTrue(userId)
                    .ifPresent(prev -> { prev.unsetMain(); characterRepository.save(prev); });
        }
        User user = userRepository.getReferenceById(userId);
        MapleCharacter character = MapleCharacter.create(
                user, req.name(), req.jobClass(), req.level(), req.isMain(), req.initialInvestment()
        );
        return CharacterResponse.from(characterRepository.save(character));
    }

    @Transactional
    public List<CharacterResponse> createCharacters(Long userId, List<CharacterRequest> requests) {
        return requests.stream().map(req -> createCharacter(userId, req)).toList();
    }

    @Transactional(readOnly = true)
    public List<CharacterResponse> getCharacters(Long userId) {
        return characterRepository.findByUserIdOrderByIsMainDescCreatedAtAsc(userId)
                .stream().map(CharacterResponse::from).toList();
    }

    @Transactional
    public CharacterResponse updateCharacter(Long userId, Long characterId, CharacterRequest req) {
        MapleCharacter character = characterRepository.findByIdAndUserId(characterId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("캐릭터를 찾을 수 없습니다."));
        if (Boolean.TRUE.equals(req.isMain()) && !Boolean.TRUE.equals(character.getIsMain())) {
            characterRepository.findByUserIdAndIsMainTrue(userId)
                    .ifPresent(prev -> { prev.unsetMain(); characterRepository.save(prev); });
        }
        character.update(req.name(), req.jobClass(), req.level(), req.isMain(), req.initialInvestment(), req.solErdaFragments());
        return CharacterResponse.from(characterRepository.save(character));
    }

    @Transactional
    public void deleteCharacter(Long userId, Long characterId) {
        MapleCharacter character = characterRepository.findByIdAndUserId(characterId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("캐릭터를 찾을 수 없습니다."));
        ledgerEntryRepository.clearCharacterRef(characterId);
        bossKillRepository.clearCharacterRef(characterId);
        huntingSessionRepository.clearCharacterRef(characterId);
        characterRepository.delete(character);
    }

    @Transactional(readOnly = true)
    public CharacterROIResponse getCharacterROI(Long userId, Long characterId) {
        MapleCharacter character = characterRepository.findByIdAndUserId(characterId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("캐릭터를 찾을 수 없습니다."));

        List<Object[]> weeklyData = ledgerEntryRepository.findCharacterBossIncomeByWeek(userId, characterId);
        long cumulativeIncome = weeklyData.stream()
                .mapToLong(row -> ((Number) row[1]).longValue()).sum();
        long weeklyAvgIncome = weeklyData.isEmpty() ? 0L
                : cumulativeIncome / weeklyData.size();

        long initialInvestment = character.getInitialInvestment() != null ? character.getInitialInvestment() : 0L;
        Long weeksToBreakEven = null;
        if (weeklyAvgIncome > 0 && initialInvestment > 0) {
            weeksToBreakEven = (long) Math.ceil((double) initialInvestment / weeklyAvgIncome);
        }
        boolean isBreakEvenReached = initialInvestment == 0 || cumulativeIncome >= initialInvestment;
        long remainingToBreakEven = Math.max(0, initialInvestment - cumulativeIncome);

        return new CharacterROIResponse(
                character.getId(), character.getName(), initialInvestment,
                cumulativeIncome, weeklyAvgIncome, weeksToBreakEven,
                isBreakEvenReached, remainingToBreakEven
        );
    }

    @Transactional(readOnly = true)
    public List<CharacterStatsResponse> getCharacterStats(Long userId) {
        return ledgerEntryRepository.findCharacterStats(userId)
                .stream().map(CharacterStatsResponse::from).toList();
    }
}