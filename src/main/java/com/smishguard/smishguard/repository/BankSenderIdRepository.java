package com.smishguard.smishguard.repository;

import com.smishguard.smishguard.entity.BankSenderId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BankSenderIdRepository extends JpaRepository<BankSenderId, Long> {
    Optional<BankSenderId> findBySenderCode(String senderCode);
    boolean existsBySenderCode(String senderCode);
}
