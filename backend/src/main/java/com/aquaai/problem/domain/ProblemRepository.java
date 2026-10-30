package com.aquaai.problem.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProblemRepository extends JpaRepository<ProblemReport, UUID> {

    List<ProblemReport> findAllByStatus(ProblemStatus status);

    Page<ProblemReport> findAllByReportedBy(UUID reportedBy, Pageable pageable);

    long countByStatus(ProblemStatus status);
}
