package com.example.bankingplatform.ai.rag.context;

import com.example.bankingplatform.ai.rag.RagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ContextBuilder {

    private static final Logger log = LoggerFactory.getLogger(ContextBuilder.class);

    public String buildContext(List<RagService.RetrievedChunk> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            return "";
        }

        Map<String, List<RagService.RetrievedChunk>> groupedByDoc = chunks.stream()
                .collect(Collectors.groupingBy(RagService.RetrievedChunk::documentId));

        StringBuilder sb = new StringBuilder();
        sb.append("<knowledge_base>\n");

        for (Map.Entry<String, List<RagService.RetrievedChunk>> entry : groupedByDoc.entrySet()) {
            String docId = entry.getKey();
            List<RagService.RetrievedChunk> docChunks = entry.getValue();

            sb.append("  <document>\n");

            if (docChunks.size() == 1) {
                RagService.RetrievedChunk chunk = docChunks.get(0);
                sb.append("    <title>").append(escapeXml(chunk.title())).append("</title>\n");
                sb.append("    <category>").append(escapeXml(chunk.category())).append("</category>\n");
                sb.append("    <content>").append(escapeXml(chunk.content())).append("</content>\n");
            } else {
                RagService.RetrievedChunk first = docChunks.get(0);
                sb.append("    <title>").append(escapeXml(first.title())).append("</title>\n");
                sb.append("    <category>").append(escapeXml(first.category())).append("</category>\n");

                for (int i = 0; i < docChunks.size(); i++) {
                    RagService.RetrievedChunk chunk = docChunks.get(i);
                    sb.append("    <chunk index=\"").append(i + 1).append("\">\n");
                    sb.append("      ").append(escapeXml(chunk.content())).append("\n");
                    sb.append("    </chunk>\n");
                }
            }

            sb.append("  </document>\n");
        }

        sb.append("</knowledge_base>\n");

        String result = sb.toString().trim();
        log.info("Built context with {} documents, {} total chunks", groupedByDoc.size(), chunks.size());
        return result;
    }

    private String escapeXml(String text) {
        if (text == null) return "";
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;");
    }
}