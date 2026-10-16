package com.smishguard.smishguard.repository;

import com.smishguard.smishguard.entity.UserReport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserReportRepository extends JpaRepository<UserReport, Long> {
    List<UserReport> findByReviewedFalseOrderByCreatedAtDesc();
}
