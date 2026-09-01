-- Migration V5: Add vector_embeddings table for AI semantic knowledge retrieval

CREATE TABLE vector_embeddings (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID NOT NULL,
    content TEXT NOT NULL,
    embedding_json TEXT NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE INDEX idx_vector_embeddings_user_id ON vector_embeddings(user_id);
CREATE INDEX idx_vector_embeddings_entity ON vector_embeddings(user_id, entity_type, entity_id);
