package org.example.analyticsservice.repository;

import org.example.analyticsservice.model.UserPostStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPostStatisticsRepository extends JpaRepository<UserPostStatistics, Long> {
    Optional<UserPostStatistics> findByUserId(Long userId);
}
