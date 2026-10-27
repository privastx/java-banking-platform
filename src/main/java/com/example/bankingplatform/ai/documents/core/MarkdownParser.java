package com.example.bankingplatform.ai.documents.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarkdownParser {

    private static final Logger log = LoggerFactory.getLogger(MarkdownParser.class);

    private static final Pattern FRONTMATTER_PATTERN = Pattern.compile("^---\\s*\\n(.*?)\\n---", Pattern.DOTALL);
    private static final Pattern FRONTMATTER_KEY_PATTERN = Pattern.compile("^\\s*(\\w+):\\s*(.+)$", Pattern.MULTILINE);
    private static final Pattern FIRST_HEADING_PATTERN = Pattern.compile("^#\\s+(.+)$", Pattern.MULTILINE);

    public ParsedDocument parse(Path filePath) {
        try {
            String content = Files.readString(filePath);
            String title = extractTitle(content, filePath);
            String category = extractCategory(content);
            String checksum = computeSha256(content);

            log.info("Parsed document: {} -> title={}, category={}, checksum={}",
                    filePath.getFileName(), title, category, checksum);

            return new ParsedDocument(
                    filePath.getFileName().toString(),
                    title,
                    category,
                    checksum,
                    Files.getLastModifiedTime(filePath).toInstant().toEpochMilli(),
                    content
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse markdown file: " + filePath, e);
        }
    }

    private String extractTitle(String content, Path filePath) {
        Matcher fmMatcher = FRONTMATTER_PATTERN.matcher(content);
        if (fmMatcher.find()) {
            String fmContent = fmMatcher.group(1);
            Matcher keyMatcher = FRONTMATTER_KEY_PATTERN.matcher(fmContent);
            while (keyMatcher.find()) {
                String key = keyMatcher.group(1).trim().toLowerCase();
                String value = keyMatcher.group(2).trim();
                if ("title".equals(key)) {
                    return value;
                }
            }
        }

        Matcher headingMatcher = FIRST_HEADING_PATTERN.matcher(content);
        if (headingMatcher.find()) {
            return headingMatcher.group(1).trim();
        }

        String name = filePath.getFileName().toString();
        name = name.replace(".md", "").replace(".markdown", "");
        name = name.replaceAll("[-_]", " ");
        name = capitalizeWords(name);
        return name;
    }

    private String extractCategory(String content) {
        Matcher fmMatcher = FRONTMATTER_PATTERN.matcher(content);
        if (fmMatcher.find()) {
            String fmContent = fmMatcher.group(1);
            Matcher keyMatcher = FRONTMATTER_KEY_PATTERN.matcher(fmContent);
            while (keyMatcher.find()) {
                String key = keyMatcher.group(1).trim().toLowerCase();
                String value = keyMatcher.group(2).trim();
                if ("category".equals(key)) {
                    return value.toUpperCase();
                }
            }
        }
        return "GENERAL";
    }

    private String capitalizeWords(String text) {
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;
        for (char c : text.toCharArray()) {
            if (Character.isWhitespace(c)) {
                result.append(c);
                capitalizeNext = true;
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(c);
                capitalizeNext = false;
            }
        }
        return result.toString();
    }

    private static String computeSha256(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    public record ParsedDocument(
            String filename,
            String title,
            String category,
            String checksum,
            long lastModified,
            String content
    ) {}
}