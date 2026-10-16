package com.smishguard.smishguard.repository;

import com.smishguard.smishguard.entity.ReferenceMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReferenceMessageRepository extends JpaRepository<ReferenceMessage, Long> {
    List<ReferenceMessage> findByLabelAndBank(String label, String bank);
    List<ReferenceMessage> findTop3ByLabel(String label);
    List<ReferenceMessage> findTop3ByLabelAndBank(String label, String bank);
}
