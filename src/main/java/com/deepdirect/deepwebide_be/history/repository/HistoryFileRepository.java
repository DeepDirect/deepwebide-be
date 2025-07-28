package com.deepdirect.deepwebide_be.history.repository;

import com.deepdirect.deepwebide_be.history.domain.History;
import com.deepdirect.deepwebide_be.history.domain.HistoryFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoryFileRepository extends JpaRepository<HistoryFile, Long> {

    List<HistoryFile> findByHistory(History history);

}
