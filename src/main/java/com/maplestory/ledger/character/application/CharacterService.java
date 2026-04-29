package com.maplestory.ledger.character.application;

import com.maplestory.ledger.auth.domain.User;
import com.maplestory.ledger.auth.infrastructure.UserRepository;
import com.maplestory.ledger.character.domain.MapleCharacter;
import com.maplestory.ledger.character.infrastructure.CharacterRepository;
import com.maplestory.ledger.character.presentation.dto.CharacterRequest;
import com.maplestory.ledger.character.presentation.dto.CharacterROIResponse;
import com.maplestory.ledger.character.presentation.dto.CharacterResponse;
import com.maplestory.ledger.character.presentation.dto.CharacterStatsResponse;
import com.maplestory.ledger.common.exception.ResourceNotFoundException;
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

    @Transactional
    public CharacterResponse createCharacter(Long userId, CharacterRequest req) {
        User user = userRepository.getReferenceById(userId);
        MapleCharacter character = MapleCharacter.create(
                user, req.name(), req.jobClass(), req.level(), req.isMain(), req.initialInvestment()
        );
        return CharacterResponse.from(characterRepository.save(character));
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
        character.update(req.name(), req.jobClass(), req.level(), req.isMain(), req.initialInvestment());
        return CharacterResponse.from(characterRepository.save(character));
    }

    @Transactional
    public void deleteCharacter(Long userId, Long characterId) {
        MapleCharacter character = characterRepository.findByIdAndUserId(characterId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("캐릭터를 찾을 수 없습니다."));
        characterRepository.delete(character);
    }

    @Transactional(readOnly = true)
    public CharacterROIResponse getCharacterROI(Long userId, Long characterId) {
        MapleCharacter character = characterRepository.findByIdAndUserId(characterId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("캐릭터를 찾을 수 없습니다."));

        List<Object[]> weeklyData = ledgerEntryRepository.findCharacterBossIncomeByWeek(userId, characterId);
        long cumulativeBossIncome = weeklyData.stream()
                .mapToLong(row -> ((Number) row[1]).longValue()).sum();
        long weeklyAvgBossIncome = weeklyData.isEmpty() ? 0L
                : cumulativeBossIncome / weeklyData.size();

        Long weeksToBreakEven = null;
        if (weeklyAvgBossIncome > 0 && character.getInitialInvestment() > 0) {
            weeksToBreakEven = (long) Math.ceil(
                    (double) character.getInitialInvestment() / weeklyAvgBossIncome);
        }
        boolean isBreakEvenReached = cumulativeBossIncome >= character.getInitialInvestment();
        long remainingToBreakEven = Math.max(0, character.getInitialInvestment() - cumulativeBossIncome);

        return new CharacterROIResponse(
                character.getId(), character.getName(), character.getInitialInvestment(),
                cumulativeBossIncome, weeklyAvgBossIncome, weeksToBreakEven,
                isBreakEvenReached, remainingToBreakEven
        );
    }

    @Transactional(readOnly = true)
    public List<CharacterStatsResponse> getCharacterStats(Long userId) {
        return ledgerEntryRepository.findCharacterStats(userId)
                .stream().map(CharacterStatsResponse::from).toList();
    }
}