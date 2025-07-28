package com.deepdirect.deepwebide_be.history.repository;

import com.deepdirect.deepwebide_be.history.domain.HistoryFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoryFileRepository extends JpaRepository<HistoryFile, Long> {
}
