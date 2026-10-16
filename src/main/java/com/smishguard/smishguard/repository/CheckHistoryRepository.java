package com.smishguard.smishguard.repository;

import com.smishguard.smishguard.entity.CheckHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.Instant;
import java.util.List;

public interface CheckHistoryRepository extends JpaRepository<CheckHistory, Long> {
    List<CheckHistory> findTop20ByOrderByCreatedAtDesc();
    long countByVerdict(String verdict);
    long countByCreatedAtAfter(Instant after);

    @Query("SELECT h.scamType, COUNT(h) FROM CheckHistory h WHERE h.scamType IS NOT NULL GROUP BY h.scamType ORDER BY COUNT(h) DESC")
    List<Object[]> countByScamType();

    @Query("SELECT h.verdict, COUNT(h) FROM CheckHistory h WHERE h.createdAt > ?1 GROUP BY h.verdict")
    List<Object[]> countByVerdictSince(Instant since);
}
