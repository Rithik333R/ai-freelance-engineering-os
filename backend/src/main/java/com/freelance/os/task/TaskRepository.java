package com.freelance.os.task;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findByProjectId(UUID projectId);
    Page<Task> findByProjectId(UUID projectId, Pageable pageable);
    Optional<Task> findByIdAndProjectUserId(UUID id, UUID userId);
    List<Task> findByProjectIdAndProjectUserId(UUID projectId, UUID userId);
}
