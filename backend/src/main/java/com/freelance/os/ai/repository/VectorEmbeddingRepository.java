package com.freelance.os.ai.repository;

import com.freelance.os.ai.entity.VectorEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VectorEmbeddingRepository extends JpaRepository<VectorEmbedding, UUID> {

    List<VectorEmbedding> findByUserId(UUID userId);

    Optional<VectorEmbedding> findByUserIdAndEntityTypeAndEntityId(UUID userId, String entityType, UUID entityId);

    void deleteByUserIdAndEntityTypeAndEntityId(UUID userId, String entityType, UUID entityId);

    void deleteByUserId(UUID userId);
}
