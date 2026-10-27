package com.example.bankingplatform.ai.documents.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;

class MarkdownParserTest {

    private final MarkdownParser markdownParser = new MarkdownParser();

    @TempDir
    Path tempDir;

    @Test
    void parse_extracts_title_from_frontmatter() throws IOException {
        Path file = tempDir.resolve("test.md");
        String content = "---\ntitle: My Document Title\ncategory: FAQ\n---\n# Other Heading\n\nContent here.";
        Files.writeString(file, content);

        MarkdownParser.ParsedDocument parsed = markdownParser.parse(file);

        assertEquals("My Document Title", parsed.title());
    }

    @Test
    void parse_extracts_title_from_first_heading() throws IOException {
        Path file = tempDir.resolve("test.md");
        String content = "# First Heading\n\nContent here.";
        Files.writeString(file, content);

        MarkdownParser.ParsedDocument parsed = markdownParser.parse(file);

        assertEquals("First Heading", parsed.title());
    }

    @Test
    void parse_frontmatter_title_takes_precedence_over_heading() throws IOException {
        Path file = tempDir.resolve("test.md");
        String content = "---\ntitle: Frontmatter Title\n---\n# Heading Title\n\nContent.";
        Files.writeString(file, content);

        MarkdownParser.ParsedDocument parsed = markdownParser.parse(file);

        assertEquals("Frontmatter Title", parsed.title());
    }

    @Test
    void parse_fallback_to_filename_when_no_title() throws IOException {
        Path file = tempDir.resolve("my-document.md");
        String content = "No title or heading here.\n\nJust content.";
        Files.writeString(file, content);

        MarkdownParser.ParsedDocument parsed = markdownParser.parse(file);

        assertEquals("My Document", parsed.title());
    }

    @Test
    void parse_fallback_to_filename_with_underscore() throws IOException {
        Path file = tempDir.resolve("my_document.md");
        String content = "Content only.";
        Files.writeString(file, content);

        MarkdownParser.ParsedDocument parsed = markdownParser.parse(file);

        assertEquals("My Document", parsed.title());
    }

    @Test
    void parse_fallback_to_filename_with_markdown_extension() throws IOException {
        Path file = tempDir.resolve("my-document.markdown");
        String content = "Content only.";
        Files.writeString(file, content);

        MarkdownParser.ParsedDocument parsed = markdownParser.parse(file);

        assertEquals("My Document", parsed.title());
    }

    @Test
    void parse_extracts_category_from_frontmatter() throws IOException {
        Path file = tempDir.resolve("test.md");
        String content = "---\ntitle: Test\ncategory: SECURITY_POLICY\n---\nContent.";
        Files.writeString(file, content);

        MarkdownParser.ParsedDocument parsed = markdownParser.parse(file);

        assertEquals("SECURITY_POLICY", parsed.category());
    }

    @Test
    void parse_category_is_uppercased() throws IOException {
        Path file = tempDir.resolve("test.md");
        String content = "---\ntitle: Test\ncategory: security_policy\n---\nContent.";
        Files.writeString(file, content);

        MarkdownParser.ParsedDocument parsed = markdownParser.parse(file);

        assertEquals("SECURITY_POLICY", parsed.category());
    }

    @Test
    void parse_falls_back_to_general_category() throws IOException {
        Path file = tempDir.resolve("test.md");
        String content = "---\ntitle: Test\n---\nContent.";
        Files.writeString(file, content);

        MarkdownParser.ParsedDocument parsed = markdownParser.parse(file);

        assertEquals("GENERAL", parsed.category());
    }

    @Test
    void parse_without_frontmatter_falls_back_to_general() throws IOException {
        Path file = tempDir.resolve("test.md");
        String content = "# Title\n\nContent.";
        Files.writeString(file, content);

        MarkdownParser.ParsedDocument parsed = markdownParser.parse(file);

        assertEquals("GENERAL", parsed.category());
    }

    @Test
    void parse_computes_sha256_checksum() throws IOException, NoSuchAlgorithmException {
        Path file = tempDir.resolve("test.md");
        String content = "Hello, world!";
        Files.writeString(file, content);

        MarkdownParser.ParsedDocument parsed = markdownParser.parse(file);

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] expectedBytes = digest.digest(content.getBytes());
        String expectedChecksum = bytesToHex(expectedBytes);

        assertEquals(expectedChecksum, parsed.checksum());
    }

    @Test
    void parse_checksum_changes_with_content() throws IOException {
        Path file1 = tempDir.resolve("test1.md");
        Files.writeString(file1, "Content A");

        Path file2 = tempDir.resolve("test2.md");
        Files.writeString(file2, "Content B");

        MarkdownParser.ParsedDocument parsed1 = markdownParser.parse(file1);
        MarkdownParser.ParsedDocument parsed2 = markdownParser.parse(file2);

        assertNotEquals(parsed1.checksum(), parsed2.checksum());
    }

    @Test
    void parse_stores_filename() throws IOException {
        Path file = tempDir.resolve("my-file.md");
        Files.writeString(file, "Content.");

        MarkdownParser.ParsedDocument parsed = markdownParser.parse(file);

        assertEquals("my-file.md", parsed.filename());
    }

    @Test
    void parse_stores_content() throws IOException {
        Path file = tempDir.resolve("test.md");
        String content = "# Title\n\nThis is the full content.\n\nWith multiple paragraphs.";
        Files.writeString(file, content);

        MarkdownParser.ParsedDocument parsed = markdownParser.parse(file);

        assertEquals(content, parsed.content());
    }

    @Test
    void parse_stores_last_modified_time() throws IOException {
        Path file = tempDir.resolve("test.md");
        Files.writeString(file, "Content.");

        MarkdownParser.ParsedDocument parsed = markdownParser.parse(file);

        assertNotNull(parsed.lastModified());
        assertTrue(parsed.lastModified() > 0);
    }

    @Test
    void parse_all_valid_categories() throws IOException {
        String[] categories = {
                "ACCOUNT_POLICY",
                "PAYMENT_POLICY",
                "SECURITY_POLICY",
                "TRANSACTION_POLICY",
                "FAQ",
                "PRODUCT_DOCUMENTATION",
                "GENERAL"
        };

        for (String category : categories) {
            Path file = tempDir.resolve("test.md");
            String content = String.format("---\ntitle: Test\ncategory: %s\n---\nContent.", category);
            Files.writeString(file, content);

            MarkdownParser.ParsedDocument parsed = markdownParser.parse(file);

            assertEquals(category, parsed.category());
        }
    }

    @Test
    void parse_title_with_special_characters() throws IOException {
        Path file = tempDir.resolve("test.md");
        String content = "---\ntitle: What's Banking?\n---\nContent.";
        Files.writeString(file, content);

        MarkdownParser.ParsedDocument parsed = markdownParser.parse(file);

        assertEquals("What's Banking?", parsed.title());
    }

    @Test
    void parse_title_with_numbers() throws IOException {
        Path file = tempDir.resolve("test.md");
        String content = "---\ntitle: Version 2.0 Guide\n---\nContent.";
        Files.writeString(file, content);

        MarkdownParser.ParsedDocument parsed = markdownParser.parse(file);

        assertEquals("Version 2.0 Guide", parsed.title());
    }

    @Test
    void parse_empty_content() throws IOException {
        Path file = tempDir.resolve("test.md");
        Files.writeString(file, "");

        MarkdownParser.ParsedDocument parsed = markdownParser.parse(file);

        assertEquals("Test", parsed.title());
        assertEquals("GENERAL", parsed.category());
        assertNotNull(parsed.checksum());
    }

    @Test
    void parse_frontmatter_with_standard_format() throws IOException {
        Path file = tempDir.resolve("test.md");
        String content = "---\ntitle: My Title\ncategory: FAQ\n---\nContent.";
        Files.writeString(file, content);

        MarkdownParser.ParsedDocument parsed = markdownParser.parse(file);

        assertEquals("My Title", parsed.title());
        assertEquals("FAQ", parsed.category());
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            String hexChar = Integer.toHexString(0xff & b);
            if (hexChar.length() == 1) {
                hex.append('0');
            }
            hex.append(hexChar);
        }
        return hex.toString();
    }
}