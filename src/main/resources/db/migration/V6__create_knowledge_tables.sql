CREATE TABLE knowledge_documents (
    id BIGSERIAL PRIMARY KEY,
    document_id VARCHAR UNIQUE NOT NULL,
    filename VARCHAR NOT NULL,
    title VARCHAR NOT NULL,
    category VARCHAR,
    checksum VARCHAR NOT NULL,
    last_modified TIMESTAMP,
    ingested_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE knowledge_chunks (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT REFERENCES knowledge_documents(id),
    chunk_index INT NOT NULL,
    content TEXT NOT NULL,
    content_hash VARCHAR NOT NULL,
    vector_store_id VARCHAR UNIQUE
);

CREATE INDEX IF NOT EXISTS idx_knowledge_chunks_document_id ON knowledge_chunks(document_id);
CREATE INDEX IF NOT EXISTS idx_knowledge_chunks_vector_store_id ON knowledge_chunks(vector_store_id);
CREATE INDEX IF NOT EXISTS idx_knowledge_documents_category ON knowledge_documents(category);
CREATE INDEX IF NOT EXISTS idx_knowledge_documents_checksum ON knowledge_documents(checksum);