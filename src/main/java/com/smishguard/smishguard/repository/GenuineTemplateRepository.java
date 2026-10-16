package com.smishguard.smishguard.repository;

import com.smishguard.smishguard.entity.GenuineTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GenuineTemplateRepository extends JpaRepository<GenuineTemplate, Long> {
    List<GenuineTemplate> findByBankName(String bankName);
}
