package com.maplestory.ledger.character.infrastructure;

import com.maplestory.ledger.character.domain.MapleCharacter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CharacterRepository extends JpaRepository<MapleCharacter, Long> {
    List<MapleCharacter> findByUserIdOrderByIsMainDescCreatedAtAsc(Long userId);
    Optional<MapleCharacter> findByIdAndUserId(Long id, Long userId);
    void deleteByIdAndUserId(Long id, Long userId);
    void deleteByUserId(Long userId);
}
