package com.maplestory.ledger.boss.infrastructure;

import com.maplestory.ledger.boss.domain.DopingMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DopingMasterRepository extends JpaRepository<DopingMaster, Long> {
    List<DopingMaster> findAllByOrderBySortOrderAsc();
}
