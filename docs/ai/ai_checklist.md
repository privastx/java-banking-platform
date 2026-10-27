# AI Implementation Checklist

Phased implementation plan for adding the AI customer support assistant to the existing banking platform. Each phase builds on the previous one. Existing modules (`customer/`, `account/`, `transaction/`, `support/`, `common/`) remain untouched.

---

## Phase 0 — Foundation

- [x] **Add Maven dependencies** to `pom.xml`:
  - [x] `langchain4j` (chat model, embeddings, tools, AiService)
  - [x] `langchain4j-open-ai` (OpenAI-compatible LLM client)
  - [x] `langchain4j-qdrant` (Qdrant embedding store)
  - [x] `langgraph4j` (stateful graphs, nodes, edges, checkpoints)
  - [x] `onnxruntime` + `all-MiniLM-L6-v2` model (in-process embedding)
  - [x] Pin all versions in `<properties>`
- [x] **Add configuration properties** to `application.yml`:
  - [x] `langchain4j.open-ai.chat-model.*` (chat model, base URL, timeouts, log-requests, log-responses)
  - [x] `langchain4j.qdrant.*` (url, collection-name, distance)
  - [x] `ai.rag.*` (retrieval, context, chunking, knowledge-base-dir)
  - [x] `ai.http.connect-timeout-seconds`
  - [x] `ai.memory.max-messages`
  - Note: Embedding model is auto-configured by `langchain4j-embeddings-all-minilm-l6-v2` starter — no manual config needed
- [x] **Create `AiProperties.java`** with `@ConfigurationProperties` binding for all AI settings
- [x] **Verify Spring Boot starts** with new dependencies and configuration (no AI logic yet)

---

## Phase 1 — Knowledge Base Infrastructure

### 1.1 — Domain Model

- [x] Create `ai/documents/models/KnowledgeDocument.java` (JPA entity):
  - [x] `id`, `documentId` (unique), `filename`, `title`, `category`, `checksum` (SHA-256), `lastModified`, `ingestedAt`
- [x] Create `ai/documents/models/KnowledgeChunk.java` (JPA entity):
  - [x] `id`, `document` (FK, LAZY), `chunkIndex`, `content` (TEXT), `contentHash` (SHA-256), `vectorStoreId` (Qdrant point ID, unique, nullable), `createdAt`
- [x] Create `ai/documents/repository/KnowledgeDocumentRepository.java`:
  - [x] `findByDocumentId(String)`
  - [x] `findByCategory(DocumentCategory)`
  - [x] `findAllOrderedByIngestedAt()`
- [x] Create `ai/documents/repository/KnowledgeChunkRepository.java`:
  - [x] `findByDocumentIdOrderByChunkIndexAsc(Long)`
  - [x] `findByVectorStoreId(String)`
  - [x] `deleteByDocumentId(Long)`
  - [x] `countByDocumentId(Long)`
- [x] Create `ai/documents/core/DocumentCategory.java` enum:
  - [x] `ACCOUNT_POLICY`, `PAYMENT_POLICY`, `SECURITY_POLICY`, `TRANSACTION_POLICY`, `FAQ`, `PRODUCT_DOCUMENTATION`, `GENERAL`

### 1.2 — Markdown Parser

- [x] Create `ai/documents/core/MarkdownParser.java`:
  - [x] Extract title from frontmatter `title:` or first `# heading` or fallback to filename
  - [x] Extract category from frontmatter `category:` or default to `GENERAL`
  - [x] Compute SHA-256 checksum of raw content
  - [x] Return `ParsedDocument{filename, title, category, checksum, lastModified, content}`
- [x] Chunking logic embedded in `DocumentIngestionService.splitByParagraphs()`:
  - [x] Split content into chunks (~800 chars, 150 overlap)
  - [x] Smart boundaries: prefer newlines, then spaces
  - [x] Trim whitespace, skip empty chunks
- [x] Create `ai/documents/core/ChunkingStrategy.java`:
  - [x] Generic chunking utility with configurable chunkSize, overlap, maxIterations
  - [x] `findSmartBoundary()` prefers newlines over spaces

### 1.3 — Seed Content

- [x] Create `src/main/resources/knowledge-base/` directory
- [x] Write 6 markdown documents across the following categories:
  - [x] **Security Policy**: `lost-stolen-card.md` (lost/stolen card procedures, fraud reporting, PIN reset, account freeze, suspicious activity), `fraud-reporting.md`
  - [x] **Branch Info**: `branch-hours.md` (hours, weekend hours, holiday schedule, branch locations)
  - [x] **Transactions**: `transaction-processing.md` (processing times, pending transactions, daily limits, international transactions)
  - [x] **Accounts**: `accounts.md` (checking accounts, savings accounts, opening/closing accounts)
  - [x] **General FAQ**: `general-faq.md` (contact info, required documents, fees, identity verification)
- [x] **Verify** all documents parse correctly with expected categories and titles

### 1.4 — Document Ingestion Pipeline

- [x] Create `ai/documents/DocumentIngestionService.java`:
  - [x] Scan markdown directory for `.md` / `.markdown` files
  - [x] Parse each file (title, category, checksum) via `MarkdownParser`
  - [x] Skip files with unchanged checksum (unless force)
  - [x] Chunk parsed content using `splitByParagraphs()`
  - [x] Persist `KnowledgeDocument` and `KnowledgeChunk` entities to PostgreSQL
  - [x] Embed chunks via MiniLM `EmbeddingModel`
  - [x] Store embeddings in Qdrant via `EmbeddingStore` with metadata (documentId, title, category, chunkIndex)
  - [x] Save Qdrant point IDs back to `KnowledgeChunk.vectorStoreId`
  - [x] Wrap ingestion in `ReentrantLock` (throws if lock unavailable)
  - [x] `deleteDocumentEmbeddings(Long)` — removes Qdrant embeddings for all chunks of a document
  - [x] `clearQdrantCollection()` — clears Qdrant collection on force reindex

### 1.5 — Knowledge Base API

- [x] Create `ai/documents/KnowledgeDocumentController.java`:
  - [x] `GET /v1/api/kb` — list documents (optional `?category=` filter)
  - [x] `GET /v1/api/kb/{id}` — get document by ID with chunks
  - [x] `DELETE /v1/api/kb/{id}` — delete document:
    - [x] Retrieve chunks
    - [x] Delete Qdrant points via `EmbeddingStore.removeAll()`
    - [x] Delete chunks
    - [x] Delete document
  - [x] `POST /v1/api/kb/reindex` — reindex (optional `?force=true` for full clear)
  - [x] DTOs: `DocumentDto`, `DocumentDetailDto`, `ChunkDto`, `IngestionResponse`
- [x] **Swagger/OpenAPI** documentation for all endpoints

---

## Phase 2 — AI Core Configuration

### 2.1 — LangChain4j Configuration

- [x] Create `ai/config/LangChain4jConfig.java`:
  - [x] `openAiChatModelHttpClientBuilder()` — JDK HttpClient with HTTP/1.1 (prevents upgrade errors on uvicorn-based LLM servers)
  - [x] `embeddingStore()` — Qdrant EmbeddingStore via gRPC (host/port parsing, TLS support)
  - [x] `embeddingModel()` — AllMiniLmL6V2EmbeddingModel (ONNX)
  - [x] `ragChatMemoryProvider()` — ChatMemoryProvider for RagChat @AiService (MessageWindowChatMemory)
  - [x] `memorySaver()` — MemorySaver for LangGraph checkpoints
  - Note: Embedding model is auto-configured by `langchain4j-embeddings-all-minilm-l6-v2` starter — no manual bean needed
  - Note: Uses `@Value` injection (not `AiProperties`) for LLM/Qdrant config
- [x] **Verify** LLM endpoint is reachable (health check or startup validation)
- [x] **Verify** Qdrant connection is established
- [x] **Verify** MiniLM model initializes without errors

### 2.2 — LangGraph Configuration

- [x] Create `ai/agent/CustomerSupportGraph.java`:
  - [x] Define graph structure using `StateGraph<AppContext>` (extends `AgentState`)
  - [x] Register nodes: `resolve_identity`, `get_transactions`, `get_support_tickets`, `confirm_prompt`, `confirm`, `create_ticket`, `user_respond_required`
  - [x] Register conditional edges:
    - [x] `resolve_identity` — awaitingIdentity → respond, not resolved → loop, identity resolved → route by workflow
    - [x] `confirm` — confirmationPending → loop, CANCEL → respond, CONFIRM → create_ticket
  - [x] Register direct edges: `get_transactions` → `respond`, `get_support_tickets` → `respond`, `create_ticket` → `respond`, `respond` → END
  - [x] Return `CompiledGraph<AppContext>`
  - [x] Uses `ObjectStreamStateSerializer<AppContext>` with `AgentStateFactory<AppContext>`
- [x] Create `ai/agent/GraphRoutes.java`:
  - [x] Node names: `RESOLVE_IDENTITY`, `GET_TRANSACTIONS`, `GET_SUPPORT_TICKETS`, `CONFIRM_PROMPT`, `CONFIRM`, `CREATE_TICKET`, `RESPOND`
  - [x] Edge targets: `RESOLVE_IDENTITY_LOOP`, `CONFIRM_LOOP`, `PROCEED_TO_TICKET`, `ABORT_FLOW`
  - [x] State keys: `SESSION_ID`, `WORKFLOW`, `USER_MESSAGE`, `CONFIRMATION_ACTION`, `AWAITING_IDENTITY`, `AWAITING_CONFIRMATION`, `CONFIRMATION_PENDING`, `CONFIRMATION_RESULT`, `CUSTOMER_IDENTITY`, `AGENT_REPLY`
- [x] Configure `MemorySaver` bean in `LangChain4jConfig`

---

## Phase 3 — Agent & Workflow

### 3.1 — State Model

- [x] Create `ai/agent/models/Workflow.java` enum:
  - [x] `TRANSACTION_HISTORY`, `LOST_OR_STOLEN_CARD`, `SUPPORT_TICKETS`
  - [x] `fromValue(String)` — case-insensitive lookup
- [x] Create `ai/agent/models/CustomerIdentity.java`:
  - [x] `record CustomerIdentity(Long id, String firstName, String lastName)` implements `Serializable`
- [x] **Graph uses `AppContext`** (extends `AgentState`) — `AppContext` with `Map<String, Object>` entries
- [x] **No custom state store** — LangGraph's `MemorySaver` handles session persistence and resume by `sessionId`

- [x] Create `ai/agent/AppContext.java`:
  - [x] Extends `AgentState`
  - [x] Typed accessors: `sessionId()`, `workflow()`, `userMessage()`, `confirmationAction()`, `customerIdentity()`, `awaitingIdentity()`, `awaitingConfirmation()`, `confirmationPending()`, `confirmationResult()`, `agentReply()`, `isIdentityResolved()`, `isComplete()`
  - [x] Immutable update methods: `withSessionId()`, `withWorkflow()`, `withUserMessage()`, `withConfirmationAction()`, `withCustomerIdentity()`, `withAwaitingIdentity()`, `withAwaitingConfirmation()`, `withConfirmationPending()`, `withConfirmationResult()`, `withAgentReply()`
  - [x] `merge(AppContext)` — merges incoming state flags
  - [x] `Builder` — fluent builder for constructing AppContext

- [x] Create `ai/agent/nodes/ResolveIdentityNode.java`:
  - [x] Extracts first/last name from message via whitespace split
  - [x] Check if `customerId` is already resolved
  - [x] If not, set `awaitingIdentity = true` and return prompt message
  - [x] On identity input (firstName + lastName), call `FindCustomerTool` to resolve
  - [x] Set `customerIdentity` and clear `awaitingIdentity`
  - [x] Return partial state: `{ customerIdentity, awaitingIdentity }`

- [x] Create `ai/agent/nodes/GetTransactionsNode.java`:
  - [x] Call `GetRecentTransactionsTool` with `customerId`
  - [x] Limit to 3 most recent transactions
  - [x] Format natural-language response
  - [x] Return partial state: `{ agent_response: "..." }`

- [x] Create `ai/agent/nodes/GetSupportTicketsNode.java`:
  - [x] Call `GetSupportTicketsTool` with `customerId`
  - [x] Format natural-language response
  - [x] Return partial state: `{ agent_response: "..." }`

- [x] Create `ai/agent/nodes/ConfirmNode.java`:
  - [x] Accept case-insensitive CONFIRM/CANCEL
  - [x] On CONFIRM: clear `confirmationPending`, clear `awaitingConfirmation`, set `confirmationResult: "CONFIRM"`, set reply
  - [x] On CANCEL: clear `confirmationPending`, clear `awaitingConfirmation`, set `confirmationResult: "CANCEL"`, set cancellation reply
  - [x] On unrecognized input: re-prompt with "Please respond with CONFIRM or CANCEL"
  - [x] Return partial state

- [x] Create `ai/agent/nodes/ConfirmPromptNode.java`:
  - [x] Set `confirmationPending = true`, `awaitingConfirmation = true`
  - [x] Return confirmation prompt message
  - [x] Return partial state

- [x] Create `ai/agent/nodes/CreateTicketNode.java`:
  - [x] Call `CreateSupportTicketTool` with `customerId`
  - [x] Set result in response
  - [x] Return partial state: `{ agent_response: "..." }`

- [x] Create `ai/agent/nodes/RespondNode.java`:
  - [x] Pass-through node; final response already set by prior nodes
  - [x] Return empty partial state

### 3.3 — Agent Service

- [x] Create `ai/chat/service/SupportAgentService.java`:
  - [x] Accept `sessionId`, `workflow`, `message`, optional `confirmationAction`
  - [x] Use LangGraph `CompiledGraph<AppContext>` to execute workflow
  - [x] `ConcurrentHashMap<String, AppContext>` session cache for state restoration
  - [x] LangGraph handles session resume via `MemorySaver` with `threadId = sessionId`
  - [x] Return `AppContext` (caller builds `ChatResponse`)
  - [x] `confirmationAction` is passed to the graph state as `userMessage` when present

- [x] Create `ai/agent/AppContext.java` (also serves as application context helper)

---

## Phase 4 — Tools

Tools are located in `ai/agent/tools/`.

### 4.1 — FindCustomerTool

- [x] Create `ai/agent/tools/FindCustomerTool.java`:
  - [x] `@Tool("Find a customer by first and last name")`
  - [x] Parameters: `firstName` (NotBlank), `lastName` (NotBlank)
  - [x] Look up customer via `CustomerRepository.findByFirstNameAndLastName()`
  - [x] Return `CustomerIdentity` DTO: `{customerId, firstName, lastName}`
  - [x] **Never** return: passwords, credentials, government IDs, full address, unrelated PII
  - [x] Handle "customer not found" gracefully (returns `CustomerIdentity` with null id)

### 4.2 — GetRecentTransactionsTool

- [x] Create `ai/agent/tools/GetRecentTransactionsTool.java`:
  - [x] `@Tool("Get the 3 most recent transactions for a customer")`
  - [x] Parameters: `customerId` (Positive)
  - [x] Retrieve transactions via `TransactionRepository.findByCustomerId()`
  - [x] Sort by `transactionTimestamp` descending, limit to 3
  - [x] Return `List<TransactionSummary>`: `{merchantName, amount, currency, transactionTimestamp}`
  - [x] **Never** expose raw JPA entities
  - [x] `record TransactionSummary(...)`

### 4.3 — GetSupportTicketsTool

- [x] Create `ai/agent/tools/GetSupportTicketsTool.java`:
  - [x] `@Tool("Get all support tickets for a customer")`
  - [x] Parameters: `customerId` (Positive)
  - [x] Retrieve tickets via `SupportTicketRepository.findByCustomerId()`
  - [x] Sort by `createdAt` descending
  - [x] Return `List<TicketSummary>`: `{id, subject, category, priority, status, createdAt}`
  - [x] **Never** expose raw JPA entities
  - [x] `record TicketSummary(...)`

### 4.4 — CreateSupportTicketTool

- [x] Create `ai/agent/tools/CreateSupportTicketTool.java`:
  - [x] `@Tool("Create a support ticket for a lost or stolen card")`
  - [x] Parameters: `customerId` (Positive)
  - [x] Only callable after explicit user confirmation (enforced by graph state, not tool itself)
  - [x] Create `SupportTicket` via `SupportTicketService`:
    - [x] Subject: "Lost/Stolen Card Report"
    - [x] Description: "Customer reported lost or stolen card"
    - [x] Category: `SupportCategory.SECURITY`
    - [x] Priority: `SupportPriority.HIGH`
    - [x] Status: `SupportStatus.OPEN`
  - [x] Uses `EntityManager` to fetch `Customer` entity
  - [x] Return `TicketResult{ticketId, status}`

### 4.5 — SearchDocumentTool

- [x] Create `ai/agent/tools/SearchDocumentTool.java`:
  - [x] `@Tool("Search the knowledge base for relevant documents")`
  - [x] Parameters: `query` (NotBlank)
  - [x] Embed query via `EmbeddingModel`
  - [x] Search Qdrant via `EmbeddingStore` with `EmbeddingSearchRequest`
  - [x] Filter by top-k and similarity threshold (from `AiProperties.Rag.Retrieval`)
  - [x] Return `List<DocumentResult>`: `{documentId, title, category, similarity}`
  - [x] `record DocumentResult(...)`

---

## Phase 5 — RAG Services

**Note:** RAG is a standalone service — no LangGraph involved. Each request is independent. No multi-turn state, no graph.

### 5.1 — RAG Service

- [x] Create `ai/chat/RagChat.java`:
  - [x] LangChain4j `@AiService` interface
  - [x] `wiringMode = EXPLICIT`
  - [x] `chatModel = "openAiChatModel"`, `chatMemoryProvider = "ragChatMemoryProvider"`
  - [x] `@SystemMessage(fromResource = "/prompts/system-prompt.txt")` — instructs JSON output with `thinking` and `answer` fields
  - [x] `chat(@MemoryId String sessionId, @UserMessage String message)`

- [x] Create `ai/chat/service/RagService.java`:
  - [x] `ask(String sessionId, String question)` method
  - [x] Adds user message to chat history via `ChatHistoryService`
  - [x] Retrieves chunks via `DocumentRetriever`
  - [x] If no chunks found, returns fallback message
  - [x] Builds context via `ContextBuilder`
  - [x] Calls `RagChat.chat()` to get raw response
  - [x] Parses JSON response via `parseJsonResponse()` — extracts `thinking` and `answer`
  - [x] If parse fails (contains "unable to parse response"), retries with strict prompt via `askWithStrictPrompt()`
  - [x] Adds AI message to chat history
  - [x] Returns `ChatResponse`
  - [x] `record RetrievedChunk(content, documentId, title, category, similarity)`
  - [x] `record ParsedResponse(thinking, answer)` (private)

### 5.2 — Knowledge Retriever

- [x] Create `ai/rag/retrieval/DocumentRetriever.java`:
  - [x] Embed question via `EmbeddingModel`
  - [x] Search Qdrant via `EmbeddingStore` with `EmbeddingSearchRequest` (maxResults, minScore from `AiProperties`)
  - [x] Filter by top-k and similarity threshold
  - [x] Return `List<RagService.RetrievedChunk>` with content, metadata, similarity score
  - [x] `toRetrievedChunk(EmbeddingMatch<TextSegment>)` — extracts metadata (documentId, title, category)

### 5.3 — Context Builder

- [x] Create `ai/rag/context/ContextBuilder.java`:
  - [x] Assemble retrieved chunks into LLM-ready context
  - [x] Group chunks by source document via `Collectors.groupingBy(RetrievedChunk::documentId)`
  - [x] Format as XML with `<knowledge_base>`, `<document>`, `<title>`, `<category>`, `<chunk index="...">` tags
  - [x] Single-chunk documents: inline `<content>` tag
  - [x] Multi-chunk documents: numbered `<chunk>` tags
  - [x] `escapeXml()` — escapes `&`, `<`, `>`, `"`, `'`

### 5.4 — System Prompts

- [x] Create `src/main/resources/prompts/system-prompt.txt`:
  - [x] Instructs JSON output with `thinking` and `answer` fields
  - [x] Plain text only (no markdown formatting)
  - [x] Fallback message for insufficient knowledge
  - [x] Used by `RagChat` `@AiService` via `@SystemMessage(fromResource = ...)`
- [x] Create `src/main/resources/prompts/system-prompt-strict.txt`:
  - [x] Stricter JSON enforcement (no text before/after JSON, no code fences)
  - [x] Used as fallback in `RagService.askWithStrictPrompt()` when initial JSON parsing fails
  - [x] Loaded via `@Value("classpath:prompts/system-prompt-strict.txt")`

---

## Phase 6 — Conversation Management

### 6.1 — Confirmation Handling

- [x] Confirmation logic implemented in graph nodes (`ConfirmNode.java` and `ConfirmPromptNode.java`):
  - [x] `ConfirmNode.apply()` handles case-insensitive CONFIRM/CANCEL
  - [x] Normalizes input to uppercase before matching
  - [x] CONFIRM: clears `confirmationPending`, `awaitingConfirmation`, sets `confirmationResult: "CONFIRM"`
  - [x] CANCEL: clears `confirmationPending`, `awaitingConfirmation`, sets `confirmationResult: "CANCEL"`
  - [x] Unrecognized input: re-prompts with "Please respond with CONFIRM or CANCEL"
  - [x] Free-form text ("yes", "go ahead", etc.) is **not** treated as confirmation

### 6.2 — Chat History

- [x] Create `ai/history/ChatHistoryEntity.java`:
  - [x] `id`, `sessionId`, `role` (USER/ASSISTANT), `content` (TEXT), `tokenCount`, `createdAt`
- [x] Create `ai/history/ChatHistoryRepository.java`:
  - [x] `findBySessionIdOrderByCreatedAtAsc(String)`
  - [x] `deleteBySessionId(String)`
- [x] Create `ai/history/ChatHistoryService.java`:
  - [x] `addMessage(String sessionId, ChatMessage)` — converts LangChain4j ChatMessage to entity
  - [x] `addMessages(String sessionId, List<ChatMessage>)`
  - [x] `getHistory(String sessionId)` — read-only transaction
  - [x] `clearHistory(String sessionId)`
  - [x] `toRole()`, `toContent()`, `estimateTokens()` helpers

---

## Phase 7 — API Layer

### 7.1 — DTOs

- [x] Create `ai/chat/models/ChatRequest.java`:
  - [x] `record ChatRequest(@NotBlank String sessionId, @NotBlank @Size(max = 1000) String message, String workflow, String confirmationAction)`
- [x] Create `ai/chat/models/ChatResponse.java`:
  - [x] `record ChatResponse(String sessionId, String message, String state, String workflow, List<String> availableActions)`
  - [x] `state` — `AWAITING_CUSTOMER_IDENTITY`, `AWAITING_CONFIRMATION`, `COMPLETED`
  - [x] `workflow` — null for RAG queries, workflow name for LangGraph workflows

### 7.2 — Unified Chat API

- [x] Create `ai/chat/controller/ChatController.java`:
  - [x] `POST /api/ai/chat` — unified endpoint for both RAG and workflow conversations
  - [x] Accept `ChatRequest` with validation
  - [x] Route to `ChatService.handleRequest()`
  - [x] Return `ChatResponse`
  - [x] Handle exceptions: `InvalidChatRequestException` → 400, `AiServiceUnavailableException` → 503, `AiExecutionException` → 500
  - [x] **Swagger/OpenAPI** documentation with `@Tag` and `@Operation`

- [x] Create `ai/chat/service/ChatService.java`:
  - [x] `handleRequest(String sessionId, String message, String workflow, String confirmationAction)`
  - [x] If `workflow` is present: route to `handleWorkflow()` (LangGraph)
  - [x] If `workflow` is null/blank: route to `handleRag()` (RAG service)
  - [x] `handleWorkflow()`: converts workflow string to enum, calls `SupportAgentService`, builds `ChatResponse`
  - [x] `handleRag()`: calls `RagService.ask()`, wraps in `ChatResponse`
  - [x] `buildResponse()`: extracts state (`AWAITING_CUSTOMER_IDENTITY`, `AWAITING_CONFIRMATION`, `COMPLETED`), available actions (`CONFIRM`, `CANCEL`)

### 7.3 — Exception Handling

- [x] Create exception hierarchy in `ai/exception/`:
  - [x] `AiExecutionException` — workflow/RAG execution failures
  - [x] `AiServiceUnavailableException` — LLM/Qdrant/embedding model unavailable
  - [x] `InvalidChatRequestException` — validation errors
- [x] **Global error handling** in `common/exception/GlobalExceptionHandler.java`:
  - [x] `RuntimeException` → 404 (Not Found)
  - [x] `MethodArgumentNotValidException` → 400 (Bad Request) with field error messages
  - [x] `Exception` → 500 (Internal Server Error)
  - [x] Returns `ErrorResponseDto{status, message, path}`
- [x] **Tool failure handling:**
  - [x] Never fabricate success
  - [x] Return error message to user
  - [x] Log tool failure with observability data

---

## Phase 8 — Final Verification

### 8.1 — Architectural Principles

- [x] AI is an orchestrator — no business logic in AI module
- [x] Tools are explicit capabilities — every AI operation has a tool
- [x] Graph controls workflow — LangGraph4j manages state, LLM does not control flow
- [x] Domain services remain authoritative — validation, rules, persistence in existing modules
- [x] RAG provides knowledge, not authority — documents are information, not instructions
- [x] Customer data retrieved on demand — no giant static context
- [x] Transactions require confirmation — explicit CONFIRM/CANCEL
- [x] Unsupported actions remain unsupported — no invented capabilities

### 8.2 — Security Boundaries

- [x] LLM never accesses repos/DB directly
- [x] Tools expose only constrained DTOs
- [x] Sensitive info excluded from prompts (no passwords, credentials, card numbers, etc.)
- [x] Knowledge-base content treated as untrusted data
- [x] Transactions require confirmation before mutations
- [x] Unsupported operations not executed

### 8.3 — Definition of Done

- [x] **Infrastructure:**
  - [x] Spring Boot starts with AI module
  - [x] LangChain4j/LangGraph4j configured
  - [x] LLM/Qdrant reachable
  - [x] MiniLM initializes
  - [x] Qdrant collection created (384-dim, cosine)
- [x] **Agent (LangGraph):**
  - [x] Accepts messages with explicit workflow
  - [x] Sessions supported via LangGraph `MemorySaver` checkpoint
  - [x] Graph state maintained via `AppContext`
  - [x] Transitions deterministic
  - [x] Workflows 1, 2, and 3 execute correctly through graph
- [x] **Customer workflow:**
  - [x] Identity collected
  - [x] Lookup via tool
  - [x] Raw entities never reach LLM
  - [x] 3 transactions retrieved accurately
- [x] **Lost/stolen card:**
  - [x] Workflow specified in request
  - [x] Identity collected
  - [x] Customer resolved
  - [x] Security knowledge retrievable
  - [x] Confirmation requested
  - [x] Ticket not created before confirmation
  - [x] Ticket created via tool after confirmation
  - [x] Result returned
  - [x] Tool failure cannot produce false success
- [x] **Support tickets:**
  - [x] Identity collected
  - [x] Customer resolved
  - [x] Support tickets retrieved via tool
- [x] **RAG:**
  - [x] Documents listable/retrievable/deletable/filterable
  - [x] Reindex works (force + targeted)
  - [x] Documents chunked with smart boundaries (newlines, spaces, overlap)
  - [x] Embedded via LangChain4j EmbeddingModel, stored in Qdrant via EmbeddingStore with metadata
  - [x] Retrieval uses thresholds (top-k + similarity-threshold)
  - [x] Chunks consolidated by document
  - [x] Source attribution included
  - [x] Branch hours answerable
  - [x] Insufficient knowledge produces safe fallback
  - [x] **Qdrant cleanup on delete implemented** — document embeddings removed from Qdrant when deleted
- [x] **Chat history:**
  - [x] Messages persisted to PostgreSQL `chat_history` table
  - [x] Messages retrievable by sessionId
  - [x] History cleared on demand
- [x] **Existing application:**
  - [x] All existing modules unchanged
  - [x] AI functionality entirely within `ai/` module
  - [x] Existing tests still pass
  - [x] Existing APIs still work

---

## Portfolio Demonstration Scenarios

- [x] **Scenario A — Transaction Info:** UI selects "Transactions" → "What was my latest transaction?" → asks name → "John Smith" → shows 3 transactions
- [x] **Scenario B — Lost Card:** UI selects "Lost Card" → "I lost my card." → asks name → "John Smith" → offers to open claim → CONFIRM → ticket created
- [x] **Scenario C — Support Tickets:** UI selects "Support Tickets" → "What are my open tickets?" → asks name → "John Smith" → shows support tickets
- [x] **Scenario D — Document/RAG:** UI selects "General Questions" → "What hours are branches open?" → answers with source attribution (Branch Hours)
