package com.smishguard.smishguard.repository;

import com.smishguard.smishguard.entity.TrustedDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TrustedDomainRepository extends JpaRepository<TrustedDomain, Long> {
    Optional<TrustedDomain> findByDomain(String domain);
    boolean existsByDomain(String domain);
}
