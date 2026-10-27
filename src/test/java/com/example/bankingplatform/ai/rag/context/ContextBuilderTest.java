package com.example.bankingplatform.ai.rag.context;

import com.example.bankingplatform.ai.rag.RagService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ContextBuilderTest {

    private final ContextBuilder contextBuilder = new ContextBuilder();

    @Test
    void buildContext_null_chunks_returns_empty() {
        String result = contextBuilder.buildContext(null);
        assertEquals("", result);
    }

    @Test
    void buildContext_empty_chunks_returns_empty() {
        String result = contextBuilder.buildContext(List.of());
        assertEquals("", result);
    }

    @Test
    void buildContext_single_chunk_single_document() {
        RagService.RetrievedChunk chunk = new RagService.RetrievedChunk(
                "This is the content.",
                "doc-1",
                "Test Document",
                "FAQ",
                0.95
        );

        String result = contextBuilder.buildContext(List.of(chunk));

        assertTrue(result.contains("<knowledge_base>"));
        assertTrue(result.contains("<document>"));
        assertTrue(result.contains("<title>Test Document</title>"));
        assertTrue(result.contains("<category>FAQ</category>"));
        assertTrue(result.contains("<content>This is the content.</content>"));
        assertTrue(result.contains("</document>"));
        assertTrue(result.contains("</knowledge_base>"));
    }

    @Test
    void buildContext_multiple_chunks_same_document() {
        String docId = UUID.randomUUID().toString();
        RagService.RetrievedChunk chunk1 = new RagService.RetrievedChunk(
                "First part of the content.",
                docId,
                "Multi-Part Doc",
                "ACCOUNT_POLICY",
                0.90
        );
        RagService.RetrievedChunk chunk2 = new RagService.RetrievedChunk(
                "Second part of the content.",
                docId,
                "Multi-Part Doc",
                "ACCOUNT_POLICY",
                0.85
        );

        String result = contextBuilder.buildContext(List.of(chunk1, chunk2));

        assertTrue(result.contains("<title>Multi-Part Doc</title>"));
        assertTrue(result.contains("<category>ACCOUNT_POLICY</category>"));
        assertTrue(result.contains("<chunk index=\"1\">"));
        assertTrue(result.contains("First part of the content."));
        assertTrue(result.contains("<chunk index=\"2\">"));
        assertTrue(result.contains("Second part of the content."));
    }

    @Test
    void buildContext_multiple_documents_grouped() {
        RagService.RetrievedChunk chunk1 = new RagService.RetrievedChunk(
                "Content for doc 1.",
                "doc-1",
                "Document One",
                "FAQ",
                0.90
        );
        RagService.RetrievedChunk chunk2 = new RagService.RetrievedChunk(
                "Content for doc 2.",
                "doc-2",
                "Document Two",
                "SECURITY_POLICY",
                0.85
        );

        String result = contextBuilder.buildContext(List.of(chunk1, chunk2));

        assertTrue(result.contains("<title>Document One</title>"));
        assertTrue(result.contains("<title>Document Two</title>"));
        assertTrue(result.contains("<category>FAQ</category>"));
        assertTrue(result.contains("<category>SECURITY_POLICY</category>"));
        assertTrue(result.contains("Content for doc 1."));
        assertTrue(result.contains("Content for doc 2."));
    }

    @Test
    void buildContext_escapes_ampersand() {
        RagService.RetrievedChunk chunk = new RagService.RetrievedChunk(
                "A & B are related.",
                "doc-1",
                "Title & Test",
                "FAQ",
                0.90
        );

        String result = contextBuilder.buildContext(List.of(chunk));

        assertTrue(result.contains("&amp;"));
        assertFalse(result.contains("<title>Title & Test</title>"));
    }

    @Test
    void buildContext_escapes_less_than() {
        RagService.RetrievedChunk chunk = new RagService.RetrievedChunk(
                "Use <tag> for formatting.",
                "doc-1",
                "Less Than Test",
                "FAQ",
                0.90
        );

        String result = contextBuilder.buildContext(List.of(chunk));

        assertTrue(result.contains("&lt;tag&gt;"));
    }

    @Test
    void buildContext_escapes_greater_than() {
        RagService.RetrievedChunk chunk = new RagService.RetrievedChunk(
                "x > y is true.",
                "doc-1",
                "Greater Than Test",
                "FAQ",
                0.90
        );

        String result = contextBuilder.buildContext(List.of(chunk));

        assertTrue(result.contains("&gt;"));
    }

    @Test
    void buildContext_escapes_double_quotes() {
        RagService.RetrievedChunk chunk = new RagService.RetrievedChunk(
                "He said \"hello\".",
                "doc-1",
                "Quote Test",
                "FAQ",
                0.90
        );

        String result = contextBuilder.buildContext(List.of(chunk));

        assertTrue(result.contains("&quot;hello&quot;"));
    }

    @Test
    void buildContext_escapes_single_quotes() {
        RagService.RetrievedChunk chunk = new RagService.RetrievedChunk(
                "It's a test.",
                "doc-1",
                "Apostrophe Test",
                "FAQ",
                0.90
        );

        String result = contextBuilder.buildContext(List.of(chunk));

        assertTrue(result.contains("&apos;"));
    }

    @Test
    void buildContext_handles_null_content_field() {
        RagService.RetrievedChunk chunk = new RagService.RetrievedChunk(
                null,
                "doc-1",
                "Null Content Test",
                "FAQ",
                0.90
        );

        String result = contextBuilder.buildContext(List.of(chunk));

        assertTrue(result.contains("<content></content>"));
        assertTrue(result.contains("<title>Null Content Test</title>"));
    }

    @Test
    void buildContext_three_chunks_same_document() {
        String docId = UUID.randomUUID().toString();
        List<RagService.RetrievedChunk> chunks = List.of(
                new RagService.RetrievedChunk("Chunk 1", docId, "Three Chunks", "GENERAL", 0.95),
                new RagService.RetrievedChunk("Chunk 2", docId, "Three Chunks", "GENERAL", 0.90),
                new RagService.RetrievedChunk("Chunk 3", docId, "Three Chunks", "GENERAL", 0.85)
        );

        String result = contextBuilder.buildContext(chunks);

        assertTrue(result.contains("<chunk index=\"1\">"));
        assertTrue(result.contains("Chunk 1"));
        assertTrue(result.contains("<chunk index=\"2\">"));
        assertTrue(result.contains("Chunk 2"));
        assertTrue(result.contains("<chunk index=\"3\">"));
        assertTrue(result.contains("Chunk 3"));
    }

    @Test
    void buildContext_mixed_single_and_multi_chunk_documents() {
        RagService.RetrievedChunk chunk1 = new RagService.RetrievedChunk(
                "Single chunk doc.",
                "doc-1",
                "Single Doc",
                "FAQ",
                0.90
        );
        RagService.RetrievedChunk chunk2 = new RagService.RetrievedChunk(
                "Multi chunk 1.",
                "doc-2",
                "Multi Doc",
                "GENERAL",
                0.85
        );
        RagService.RetrievedChunk chunk3 = new RagService.RetrievedChunk(
                "Multi chunk 2.",
                "doc-2",
                "Multi Doc",
                "GENERAL",
                0.80
        );

        String result = contextBuilder.buildContext(List.of(chunk1, chunk2, chunk3));

        assertTrue(result.contains("<title>Single Doc</title>"));
        assertTrue(result.contains("<title>Multi Doc</title>"));
        assertTrue(result.contains("<content>Single chunk doc.</content>"));
        assertTrue(result.contains("<chunk index=\"1\">"));
        assertTrue(result.contains("Multi chunk 1."));
        assertTrue(result.contains("<chunk index=\"2\">"));
        assertTrue(result.contains("Multi chunk 2."));
    }

    @Test
    void buildContext_output_is_trimmed() {
        RagService.RetrievedChunk chunk = new RagService.RetrievedChunk(
                "Content.",
                "doc-1",
                "Title",
                "GENERAL",
                0.90
        );

        String result = contextBuilder.buildContext(List.of(chunk));

        assertEquals(result.trim(), result);
        assertFalse(result.startsWith("\n"));
        assertFalse(result.endsWith("\n"));
    }

    @Test
    void buildContext_single_document_no_chunk_tags() {
        RagService.RetrievedChunk chunk = new RagService.RetrievedChunk(
                "Only chunk.",
                "doc-1",
                "Single",
                "FAQ",
                0.90
        );

        String result = contextBuilder.buildContext(List.of(chunk));

        assertFalse(result.contains("<chunk index="));
        assertTrue(result.contains("<content>Only chunk.</content>"));
    }
}