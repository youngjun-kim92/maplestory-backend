package com.maplestory.ledger.goal.infrastructure;

import com.maplestory.ledger.goal.domain.Goal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GoalRepository extends JpaRepository<Goal, Long> {
    List<Goal> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Goal> findByUserIdAndIsAchievedFalse(Long userId);
    Optional<Goal> findByIdAndUserId(Long id, Long userId);
    void deleteByUserId(Long userId);
}
