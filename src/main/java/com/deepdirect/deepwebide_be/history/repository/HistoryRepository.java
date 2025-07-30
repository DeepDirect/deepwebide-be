package com.deepdirect.deepwebide_be.history.repository;

import com.deepdirect.deepwebide_be.history.domain.History;
import com.deepdirect.deepwebide_be.repository.domain.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoryRepository extends JpaRepository<History, Long> {

    List<History> findByRepositoryOrderByCreatedAtDesc(Repository repository);

}
