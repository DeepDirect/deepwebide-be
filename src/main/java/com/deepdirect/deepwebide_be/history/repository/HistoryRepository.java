package com.deepdirect.deepwebide_be.history.repository;

import com.deepdirect.deepwebide_be.history.domain.History;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoryRepository extends JpaRepository<History, Long> {
}
