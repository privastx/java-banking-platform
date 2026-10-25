# AI Implementation Checklist

Phased implementation plan for adding the AI customer support assistant to the existing banking platform. Each phase builds on the previous one. Existing modules (`customer/`, `account/`, `transaction/`, `support/`, `common/`) remain untouched.

---

## Phase 0 — Foundation

- [ ] **Add Maven dependencies** to `pom.xml`:
  - [ ] `langchain4j` (chat model, embeddings, tools, AiService)
  - [ ] `langchain4j-open-ai` (OpenAI-compatible LLM client)
  - [ ] `langchain4j-qdrant` (Qdrant embedding store)
  - [ ] `langgraph4j` (stateful graphs, nodes, edges, checkpoints)
  - [ ] `onnxruntime` + `all-MiniLM-L6-v2` model (in-process embedding)
  - [ ] Pin all versions in `<properties>`
- [ ] **Add configuration properties** to `application.yml`:
  - [ ] `langchain4j.open-ai.chat-model.*` (chat model, base URL, timeouts, log-requests, log-responses)
  - [ ] `langchain4j.qdrant.*` (url, collection-name, distance)
  - [ ] `ai.rag.*` (retrieval, context, chunking, knowledge-base-dir)
  - [ ] `ai.http.connect-timeout-seconds`
  - [ ] `ai.memory.max-messages`
  - Note: Embedding model is auto-configured by `langchain4j-embeddings-all-minilm-l6-v2` starter — no manual config needed
- [ ] **Create `AiProperties.java`** with `@ConfigurationProperties` binding for all AI settings
- [ ] **Verify Spring Boot starts** with new dependencies and configuration (no AI logic yet)

---

## Phase 1 — Knowledge Base Infrastructure

### 1.1 — Domain Model

- [ ] Create `ai/documents/models/KnowledgeDocument.java` (JPA entity):
  - [ ] `id`, `documentId` (unique), `filename`, `title`, `category`, `checksum` (SHA-256), `lastModified`, `ingestedAt`
- [ ] Create `ai/documents/models/KnowledgeChunk.java` (JPA entity):
  - [ ] `id`, `document` (FK, LAZY), `chunkIndex`, `content` (TEXT), `contentHash` (SHA-256), `vectorStoreId` (Qdrant point ID, unique, nullable), `createdAt`
- [ ] Create `ai/documents/repository/KnowledgeDocumentRepository.java`:
  - [ ] `findByDocumentId(String)`
  - [ ] `findByCategory(DocumentCategory)`
  - [ ] `findAllOrderedByIngestedAt()`
- [ ] Create `ai/documents/repository/KnowledgeChunkRepository.java`:
  - [ ] `findByDocumentIdOrderByChunkIndexAsc(Long)`
  - [ ] `findByVectorStoreId(String)`
  - [ ] `deleteByDocumentId(Long)`
  - [ ] `countByDocumentId(Long)`
- [ ] Create `ai/documents/core/DocumentCategory.java` enum:
  - [ ] `ACCOUNT_POLICY`, `PAYMENT_POLICY`, `SECURITY_POLICY`, `TRANSACTION_POLICY`, `FAQ`, `PRODUCT_DOCUMENTATION`, `GENERAL`

### 1.2 — Markdown Parser

- [ ] Create `ai/documents/core/MarkdownParser.java`:
  - [ ] Extract title from frontmatter `title:` or first `# heading` or fallback to filename
  - [ ] Extract category from frontmatter `category:` or default to `GENERAL`
  - [ ] Compute SHA-256 checksum of raw content
  - [ ] Return `ParsedDocument{filename, title, category, checksum, lastModified, content}`
- [ ] Chunking logic embedded in `DocumentIngestionService.splitByParagraphs()`:
  - [ ] Split content into chunks (~800 chars, 150 overlap)
  - [ ] Smart boundaries: prefer newlines, then spaces
  - [ ] Trim whitespace, skip empty chunks
- [ ] Create `ai/documents/core/ChunkingStrategy.java`:
  - [ ] Generic chunking utility with configurable chunkSize, overlap, maxIterations
  - [ ] `findSmartBoundary()` prefers newlines over spaces

### 1.3 — Seed Content

- [ ] Create `src/main/resources/knowledge-base/` directory
- [ ] Write 6 markdown documents across the following categories:
  - [ ] **Security Policy**: `lost-stolen-card.md` (lost/stolen card procedures, fraud reporting, PIN reset, account freeze, suspicious activity), `fraud-reporting.md`
  - [ ] **Branch Info**: `branch-hours.md` (hours, weekend hours, holiday schedule, branch locations)
  - [ ] **Transactions**: `transaction-processing.md` (processing times, pending transactions, daily limits, international transactions)
  - [ ] **Accounts**: `accounts.md` (checking accounts, savings accounts, opening/closing accounts)
  - [ ] **General FAQ**: `general-faq.md` (contact info, required documents, fees, identity verification)
- [ ] **Verify** all documents parse correctly with expected categories and titles

### 1.4 — Document Ingestion Pipeline

- [ ] Create `ai/documents/DocumentIngestionService.java`:
  - [ ] Scan markdown directory for `.md` / `.markdown` files
  - [ ] Parse each file (title, category, checksum) via `MarkdownParser`
  - [ ] Skip files with unchanged checksum (unless force)
  - [ ] Chunk parsed content using `splitByParagraphs()`
  - [ ] Persist `KnowledgeDocument` and `KnowledgeChunk` entities to PostgreSQL
  - [ ] Embed chunks via MiniLM `EmbeddingModel`
  - [ ] Store embeddings in Qdrant via `EmbeddingStore` with metadata (documentId, title, category, chunkIndex)
  - [ ] Save Qdrant point IDs back to `KnowledgeChunk.vectorStoreId`
  - [ ] Wrap ingestion in `ReentrantLock` (throws if lock unavailable)
  - [ ] `deleteDocumentEmbeddings(Long)` — removes Qdrant embeddings for all chunks of a document
  - [ ] `clearQdrantCollection()` — clears Qdrant collection on force reindex

### 1.5 — Knowledge Base API

- [ ] Create `ai/documents/KnowledgeDocumentController.java`:
  - [ ] `GET /v1/api/kb` — list documents (optional `?category=` filter)
  - [ ] `GET /v1/api/kb/{id}` — get document by ID with chunks
  - [ ] `DELETE /v1/api/kb/{id}` — delete document:
    - [ ] Retrieve chunks
    - [ ] Delete Qdrant points via `EmbeddingStore.removeAll()`
    - [ ] Delete chunks
    - [ ] Delete document
  - [ ] `POST /v1/api/kb/reindex` — reindex (optional `?force=true` for full clear)
  - [ ] DTOs: `DocumentDto`, `DocumentDetailDto`, `ChunkDto`, `IngestionResponse`
- [ ] **Swagger/OpenAPI** documentation for all endpoints

---

## Phase 2 — AI Core Configuration

### 2.1 — LangChain4j Configuration

- [ ] Create `ai/config/LangChain4jConfig.java`:
  - [ ] `openAiChatModelHttpClientBuilder()` — JDK HttpClient with HTTP/1.1 (prevents upgrade errors on uvicorn-based LLM servers)
  - [ ] `embeddingStore()` — Qdrant EmbeddingStore via gRPC (host/port parsing, TLS support)
  - [ ] `embeddingModel()` — AllMiniLmL6V2EmbeddingModel (ONNX)
  - [ ] `ragChatMemoryProvider()` — ChatMemoryProvider for RagChat @AiService (MessageWindowChatMemory)
  - [ ] `memorySaver()` — MemorySaver for LangGraph checkpoints
  - Note: Embedding model is auto-configured by `langchain4j-embeddings-all-minilm-l6-v2` starter — no manual bean needed
  - Note: Uses `@Value` injection (not `AiProperties`) for LLM/Qdrant config
- [ ] **Verify** LLM endpoint is reachable (health check or startup validation)
- [ ] **Verify** Qdrant connection is established
- [ ] **Verify** MiniLM model initializes without errors

### 2.2 — LangGraph Configuration

- [ ] Create `ai/agent/CustomerSupportGraph.java`:
  - [ ] Define graph structure using `StateGraph<AppContext>` (extends `AgentState`)
  - [ ] Register nodes: `resolve_identity`, `get_transactions`, `get_support_tickets`, `confirm_prompt`, `confirm`, `create_ticket`, `user_respond_required`
  - [ ] Register conditional edges:
    - [ ] `resolve_identity` — awaitingIdentity → respond, not resolved → loop, identity resolved → route by workflow
    - [ ] `confirm` — confirmationPending → loop, CANCEL → respond, CONFIRM → create_ticket
  - [ ] Register direct edges: `get_transactions` → `respond`, `get_support_tickets` → `respond`, `create_ticket` → `respond`, `respond` → END
  - [ ] Return `CompiledGraph<AppContext>`
  - [ ] Uses `ObjectStreamStateSerializer<AppContext>` with `AgentStateFactory<AppContext>`
- [ ] Create `ai/agent/GraphRoutes.java`:
  - [ ] Node names: `RESOLVE_IDENTITY`, `GET_TRANSACTIONS`, `GET_SUPPORT_TICKETS`, `CONFIRM_PROMPT`, `CONFIRM`, `CREATE_TICKET`, `RESPOND`
  - [ ] Edge targets: `RESOLVE_IDENTITY_LOOP`, `CONFIRM_LOOP`, `PROCEED_TO_TICKET`, `ABORT_FLOW`
  - [ ] State keys: `SESSION_ID`, `WORKFLOW`, `USER_MESSAGE`, `CONFIRMATION_ACTION`, `AWAITING_IDENTITY`, `AWAITING_CONFIRMATION`, `CONFIRMATION_PENDING`, `CONFIRMATION_RESULT`, `CUSTOMER_IDENTITY`, `AGENT_REPLY`
- [ ] Configure `MemorySaver` bean in `LangChain4jConfig`

---

## Phase 3 — Agent & Workflow

### 3.1 — State Model

- [ ] Create `ai/agent/models/Workflow.java` enum:
  - [ ] `TRANSACTION_HISTORY`, `LOST_OR_STOLEN_CARD`, `SUPPORT_TICKETS`
  - [ ] `fromValue(String)` — case-insensitive lookup
- [ ] Create `ai/agent/models/CustomerIdentity.java`:
  - [ ] `record CustomerIdentity(Long id, String firstName, String lastName)` implements `Serializable`
- [ ] **Graph uses `AppContext`** (extends `AgentState`) — `AppContext` with `Map<String, Object>` entries
- [ ] **No custom state store** — LangGraph's `MemorySaver` handles session persistence and resume by `sessionId`

- [ ] Create `ai/agent/AppContext.java`:
  - [ ] Extends `AgentState`
  - [ ] Typed accessors: `sessionId()`, `workflow()`, `userMessage()`, `confirmationAction()`, `customerIdentity()`, `awaitingIdentity()`, `awaitingConfirmation()`, `confirmationPending()`, `confirmationResult()`, `agentReply()`, `isIdentityResolved()`, `isComplete()`
  - [ ] Immutable update methods: `withSessionId()`, `withWorkflow()`, `withUserMessage()`, `withConfirmationAction()`, `withCustomerIdentity()`, `withAwaitingIdentity()`, `withAwaitingConfirmation()`, `withConfirmationPending()`, `withConfirmationResult()`, `withAgentReply()`
  - [ ] `merge(AppContext)` — merges incoming state flags
  - [ ] `Builder` — fluent builder for constructing AppContext

- [ ] Create `ai/agent/nodes/ResolveIdentityNode.java`:
  - [ ] Extracts first/last name from message via whitespace split
  - [ ] Check if `customerId` is already resolved
  - [ ] If not, set `awaitingIdentity = true` and return prompt message
  - [ ] On identity input (firstName + lastName), call `FindCustomerTool` to resolve
  - [ ] Set `customerIdentity` and clear `awaitingIdentity`
  - [ ] Return partial state: `{ customerIdentity, awaitingIdentity }`

- [ ] Create `ai/agent/nodes/GetTransactionsNode.java`:
  - [ ] Call `GetRecentTransactionsTool` with `customerId`
  - [ ] Limit to 3 most recent transactions
  - [ ] Format natural-language response
  - [ ] Return partial state: `{ agent_response: "..." }`

- [ ] Create `ai/agent/nodes/GetSupportTicketsNode.java`:
  - [ ] Call `GetSupportTicketsTool` with `customerId`
  - [ ] Format natural-language response
  - [ ] Return partial state: `{ agent_response: "..." }`

- [ ] Create `ai/agent/nodes/ConfirmNode.java`:
  - [ ] Accept case-insensitive CONFIRM/CANCEL
  - [ ] On CONFIRM: clear `confirmationPending`, clear `awaitingConfirmation`, set `confirmationResult: "CONFIRM"`, set reply
  - [ ] On CANCEL: clear `confirmationPending`, clear `awaitingConfirmation`, set `confirmationResult: "CANCEL"`, set cancellation reply
  - [ ] On unrecognized input: re-prompt with "Please respond with CONFIRM or CANCEL"
  - [ ] Return partial state

- [ ] Create `ai/agent/nodes/ConfirmPromptNode.java`:
  - [ ] Set `confirmationPending = true`, `awaitingConfirmation = true`
  - [ ] Return confirmation prompt message
  - [ ] Return partial state

- [ ] Create `ai/agent/nodes/CreateTicketNode.java`:
  - [ ] Call `CreateSupportTicketTool` with `customerId`
  - [ ] Set result in response
  - [ ] Return partial state: `{ agent_response: "..." }`

- [ ] Create `ai/agent/nodes/RespondNode.java`:
  - [ ] Pass-through node; final response already set by prior nodes
  - [ ] Return empty partial state

### 3.3 — Agent Service

- [ ] Create `ai/chat/service/SupportAgentService.java`:
  - [ ] Accept `sessionId`, `workflow`, `message`, optional `confirmationAction`
  - [ ] Use LangGraph `CompiledGraph<AppContext>` to execute workflow
  - [ ] `ConcurrentHashMap<String, AppContext>` session cache for state restoration
  - [ ] LangGraph handles session resume via `MemorySaver` with `threadId = sessionId`
  - [ ] Return `AppContext` (caller builds `ChatResponse`)
  - [ ] `confirmationAction` is passed to the graph state as `userMessage` when present

- [ ] Create `ai/agent/AppContext.java` (also serves as application context helper)

---

## Phase 4 — Tools

Tools are located in `ai/agent/tools/`.

### 4.1 — FindCustomerTool

- [ ] Create `ai/agent/tools/FindCustomerTool.java`:
  - [ ] `@Tool("Find a customer by first and last name")`
  - [ ] Parameters: `firstName` (NotBlank), `lastName` (NotBlank)
  - [ ] Look up customer via `CustomerRepository.findByFirstNameAndLastName()`
  - [ ] Return `CustomerIdentity` DTO: `{customerId, firstName, lastName}`
  - [ ] **Never** return: passwords, credentials, government IDs, full address, unrelated PII
  - [ ] Handle "customer not found" gracefully (returns `CustomerIdentity` with null id)

### 4.2 — GetRecentTransactionsTool

- [ ] Create `ai/agent/tools/GetRecentTransactionsTool.java`:
  - [ ] `@Tool("Get the 3 most recent transactions for a customer")`
  - [ ] Parameters: `customerId` (Positive)
  - [ ] Retrieve transactions via `TransactionRepository.findByCustomerId()`
  - [ ] Sort by `transactionTimestamp` descending, limit to 3
  - [ ] Return `List<TransactionSummary>`: `{merchantName, amount, currency, transactionTimestamp}`
  - [ ] **Never** expose raw JPA entities
  - [ ] `record TransactionSummary(...)`

### 4.3 — GetSupportTicketsTool

- [ ] Create `ai/agent/tools/GetSupportTicketsTool.java`:
  - [ ] `@Tool("Get all support tickets for a customer")`
  - [ ] Parameters: `customerId` (Positive)
  - [ ] Retrieve tickets via `SupportTicketRepository.findByCustomerId()`
  - [ ] Sort by `createdAt` descending
  - [ ] Return `List<TicketSummary>`: `{id, subject, category, priority, status, createdAt}`
  - [ ] **Never** expose raw JPA entities
  - [ ] `record TicketSummary(...)`

### 4.4 — CreateSupportTicketTool

- [ ] Create `ai/agent/tools/CreateSupportTicketTool.java`:
  - [ ] `@Tool("Create a support ticket for a lost or stolen card")`
  - [ ] Parameters: `customerId` (Positive)
  - [ ] Only callable after explicit user confirmation (enforced by graph state, not tool itself)
  - [ ] Create `SupportTicket` via `SupportTicketService`:
    - [ ] Subject: "Lost/Stolen Card Report"
    - [ ] Description: "Customer reported lost or stolen card"
    - [ ] Category: `SupportCategory.SECURITY`
    - [ ] Priority: `SupportPriority.HIGH`
    - [ ] Status: `SupportStatus.OPEN`
  - [ ] Uses `EntityManager` to fetch `Customer` entity
  - [ ] Return `TicketResult{ticketId, status}`

### 4.5 — SearchDocumentTool

- [ ] Create `ai/agent/tools/SearchDocumentTool.java`:
  - [ ] `@Tool("Search the knowledge base for relevant documents")`
  - [ ] Parameters: `query` (NotBlank)
  - [ ] Embed query via `EmbeddingModel`
  - [ ] Search Qdrant via `EmbeddingStore` with `EmbeddingSearchRequest`
  - [ ] Filter by top-k and similarity threshold (from `AiProperties.Rag.Retrieval`)
  - [ ] Return `List<DocumentResult>`: `{documentId, title, category, similarity}`
  - [ ] `record DocumentResult(...)`

---

## Phase 5 — RAG Services

**Note:** RAG is a standalone service — no LangGraph involved. Each request is independent. No multi-turn state, no graph.

### 5.1 — RAG Service

- [ ] Create `ai/chat/RagChat.java`:
  - [ ] LangChain4j `@AiService` interface
  - [ ] `wiringMode = EXPLICIT`
  - [ ] `chatModel = "openAiChatModel"`, `chatMemoryProvider = "ragChatMemoryProvider"`
  - [ ] `@SystemMessage(fromResource = "/prompts/system-prompt.txt")` — instructs JSON output with `thinking` and `answer` fields
  - [ ] `chat(@MemoryId String sessionId, @UserMessage String message)`

- [ ] Create `ai/chat/service/RagService.java`:
  - [ ] `ask(String sessionId, String question)` method
  - [ ] Adds user message to chat history via `ChatHistoryService`
  - [ ] Retrieves chunks via `DocumentRetriever`
  - [ ] If no chunks found, returns fallback message
  - [ ] Builds context via `ContextBuilder`
  - [ ] Calls `RagChat.chat()` to get raw response
  - [ ] Parses JSON response via `parseJsonResponse()` — extracts `thinking` and `answer`
  - [ ] If parse fails (contains "unable to parse response"), retries with strict prompt via `askWithStrictPrompt()`
  - [ ] Adds AI message to chat history
  - [ ] Returns `ChatResponse`
  - [ ] `record RetrievedChunk(content, documentId, title, category, similarity)`
  - [ ] `record ParsedResponse(thinking, answer)` (private)

### 5.2 — Knowledge Retriever

- [ ] Create `ai/rag/retrieval/DocumentRetriever.java`:
  - [ ] Embed question via `EmbeddingModel`
  - [ ] Search Qdrant via `EmbeddingStore` with `EmbeddingSearchRequest` (maxResults, minScore from `AiProperties`)
  - [ ] Filter by top-k and similarity threshold
  - [ ] Return `List<RagService.RetrievedChunk>` with content, metadata, similarity score
  - [ ] `toRetrievedChunk(EmbeddingMatch<TextSegment>)` — extracts metadata (documentId, title, category)

### 5.3 — Context Builder

- [ ] Create `ai/rag/context/ContextBuilder.java`:
  - [ ] Assemble retrieved chunks into LLM-ready context
  - [ ] Group chunks by source document via `Collectors.groupingBy(RetrievedChunk::documentId)`
  - [ ] Format as XML with `<knowledge_base>`, `<document>`, `<title>`, `<category>`, `<chunk index="...">` tags
  - [ ] Single-chunk documents: inline `<content>` tag
  - [ ] Multi-chunk documents: numbered `<chunk>` tags
  - [ ] `escapeXml()` — escapes `&`, `<`, `>`, `"`, `'`

### 5.4 — System Prompts

- [ ] Create `src/main/resources/prompts/system-prompt.txt`:
  - [ ] Instructs JSON output with `thinking` and `answer` fields
  - [ ] Plain text only (no markdown formatting)
  - [ ] Fallback message for insufficient knowledge
  - [ ] Used by `RagChat` `@AiService` via `@SystemMessage(fromResource = ...)`
- [ ] Create `src/main/resources/prompts/system-prompt-strict.txt`:
  - [ ] Stricter JSON enforcement (no text before/after JSON, no code fences)
  - [ ] Used as fallback in `RagService.askWithStrictPrompt()` when initial JSON parsing fails
  - [ ] Loaded via `@Value("classpath:prompts/system-prompt-strict.txt")`

---

## Phase 6 — Conversation Management

### 6.1 — Confirmation Handling

- [ ] Confirmation logic implemented in graph nodes (`ConfirmNode.java` and `ConfirmPromptNode.java`):
  - [ ] `ConfirmNode.apply()` handles case-insensitive CONFIRM/CANCEL
  - [ ] Normalizes input to uppercase before matching
  - [ ] CONFIRM: clears `confirmationPending`, `awaitingConfirmation`, sets `confirmationResult: "CONFIRM"`
  - [ ] CANCEL: clears `confirmationPending`, `awaitingConfirmation`, sets `confirmationResult: "CANCEL"`
  - [ ] Unrecognized input: re-prompts with "Please respond with CONFIRM or CANCEL"
  - [ ] Free-form text ("yes", "go ahead", etc.) is **not** treated as confirmation

### 6.2 — Chat History

- [ ] Create `ai/history/ChatHistoryEntity.java`:
  - [ ] `id`, `sessionId`, `role` (USER/ASSISTANT), `content` (TEXT), `tokenCount`, `createdAt`
- [ ] Create `ai/history/ChatHistoryRepository.java`:
  - [ ] `findBySessionIdOrderByCreatedAtAsc(String)`
  - [ ] `deleteBySessionId(String)`
- [ ] Create `ai/history/ChatHistoryService.java`:
  - [ ] `addMessage(String sessionId, ChatMessage)` — converts LangChain4j ChatMessage to entity
  - [ ] `addMessages(String sessionId, List<ChatMessage>)`
  - [ ] `getHistory(String sessionId)` — read-only transaction
  - [ ] `clearHistory(String sessionId)`
  - [ ] `toRole()`, `toContent()`, `estimateTokens()` helpers

---

## Phase 7 — API Layer

### 7.1 — DTOs

- [ ] Create `ai/chat/models/ChatRequest.java`:
  - [ ] `record ChatRequest(@NotBlank String sessionId, @NotBlank @Size(max = 1000) String message, String workflow, String confirmationAction)`
- [ ] Create `ai/chat/models/ChatResponse.java`:
  - [ ] `record ChatResponse(String sessionId, String message, String state, String workflow, List<String> availableActions)`
  - [ ] `state` — `AWAITING_CUSTOMER_IDENTITY`, `AWAITING_CONFIRMATION`, `COMPLETED`
  - [ ] `workflow` — null for RAG queries, workflow name for LangGraph workflows

### 7.2 — Unified Chat API

- [ ] Create `ai/chat/controller/ChatController.java`:
  - [ ] `POST /api/ai/chat` — unified endpoint for both RAG and workflow conversations
  - [ ] Accept `ChatRequest` with validation
  - [ ] Route to `ChatService.handleRequest()`
  - [ ] Return `ChatResponse`
  - [ ] Handle exceptions: `InvalidChatRequestException` → 400, `AiServiceUnavailableException` → 503, `AiExecutionException` → 500
  - [ ] **Swagger/OpenAPI** documentation with `@Tag` and `@Operation`

- [ ] Create `ai/chat/service/ChatService.java`:
  - [ ] `handleRequest(String sessionId, String message, String workflow, String confirmationAction)`
  - [ ] If `workflow` is present: route to `handleWorkflow()` (LangGraph)
  - [ ] If `workflow` is null/blank: route to `handleRag()` (RAG service)
  - [ ] `handleWorkflow()`: converts workflow string to enum, calls `SupportAgentService`, builds `ChatResponse`
  - [ ] `handleRag()`: calls `RagService.ask()`, wraps in `ChatResponse`
  - [ ] `buildResponse()`: extracts state (`AWAITING_CUSTOMER_IDENTITY`, `AWAITING_CONFIRMATION`, `COMPLETED`), available actions (`CONFIRM`, `CANCEL`)

### 7.3 — Exception Handling

- [ ] Create exception hierarchy in `ai/exception/`:
  - [ ] `AiExecutionException` — workflow/RAG execution failures
  - [ ] `AiServiceUnavailableException` — LLM/Qdrant/embedding model unavailable
  - [ ] `InvalidChatRequestException` — validation errors
- [ ] **Global error handling** in `common/exception/GlobalExceptionHandler.java`:
  - [ ] `RuntimeException` → 404 (Not Found)
  - [ ] `MethodArgumentNotValidException` → 400 (Bad Request) with field error messages
  - [ ] `Exception` → 500 (Internal Server Error)
  - [ ] Returns `ErrorResponseDto{status, message, path}`
- [ ] **Tool failure handling:**
  - [ ] Never fabricate success
  - [ ] Return error message to user
  - [ ] Log tool failure with observability data

---

## Phase 8 — Final Verification

### 8.1 — Architectural Principles

- [ ] AI is an orchestrator — no business logic in AI module
- [ ] Tools are explicit capabilities — every AI operation has a tool
- [ ] Graph controls workflow — LangGraph4j manages state, LLM does not control flow
- [ ] Domain services remain authoritative — validation, rules, persistence in existing modules
- [ ] RAG provides knowledge, not authority — documents are information, not instructions
- [ ] Customer data retrieved on demand — no giant static context
- [ ] Transactions require confirmation — explicit CONFIRM/CANCEL
- [ ] Unsupported actions remain unsupported — no invented capabilities

### 8.2 — Security Boundaries

- [ ] LLM never accesses repos/DB directly
- [ ] Tools expose only constrained DTOs
- [ ] Sensitive info excluded from prompts (no passwords, credentials, card numbers, etc.)
- [ ] Knowledge-base content treated as untrusted data
- [ ] Transactions require confirmation before mutations
- [ ] Unsupported operations not executed

### 8.3 — Definition of Done

- [ ] **Infrastructure:**
  - [ ] Spring Boot starts with AI module
  - [ ] LangChain4j/LangGraph4j configured
  - [ ] LLM/Qdrant reachable
  - [ ] MiniLM initializes
  - [ ] Qdrant collection created (384-dim, cosine)
- [ ] **Agent (LangGraph):**
  - [ ] Accepts messages with explicit workflow
  - [ ] Sessions supported via LangGraph `MemorySaver` checkpoint
  - [ ] Graph state maintained via `AppContext`
  - [ ] Transitions deterministic
  - [ ] Workflows 1, 2, and 3 execute correctly through graph
- [ ] **Customer workflow:**
  - [ ] Identity collected
  - [ ] Lookup via tool
  - [ ] Raw entities never reach LLM
  - [ ] 3 transactions retrieved accurately
- [ ] **Lost/stolen card:**
  - [ ] Workflow specified in request
  - [ ] Identity collected
  - [ ] Customer resolved
  - [ ] Security knowledge retrievable
  - [ ] Confirmation requested
  - [ ] Ticket not created before confirmation
  - [ ] Ticket created via tool after confirmation
  - [ ] Result returned
  - [ ] Tool failure cannot produce false success
- [ ] **Support tickets:**
  - [ ] Identity collected
  - [ ] Customer resolved
  - [ ] Support tickets retrieved via tool
- [ ] **RAG:**
  - [ ] Documents listable/retrievable/deletable/filterable
  - [ ] Reindex works (force + targeted)
  - [ ] Documents chunked with smart boundaries (newlines, spaces, overlap)
  - [ ] Embedded via LangChain4j EmbeddingModel, stored in Qdrant via EmbeddingStore with metadata
  - [ ] Retrieval uses thresholds (top-k + similarity-threshold)
  - [ ] Chunks consolidated by document
  - [ ] Source attribution included
  - [ ] Branch hours answerable
  - [ ] Insufficient knowledge produces safe fallback
  - [ ] **Qdrant cleanup on delete implemented** — document embeddings removed from Qdrant when deleted
- [ ] **Chat history:**
  - [ ] Messages persisted to PostgreSQL `chat_history` table
  - [ ] Messages retrievable by sessionId
  - [ ] History cleared on demand
- [ ] **Existing application:**
  - [ ] All existing modules unchanged
  - [ ] AI functionality entirely within `ai/` module
  - [ ] Existing tests still pass
  - [ ] Existing APIs still work

---

## Portfolio Demonstration Scenarios

- [ ] **Scenario A — Transaction Info:** UI selects "Transactions" → "What was my latest transaction?" → asks name → "John Smith" → shows 3 transactions
- [ ] **Scenario B — Lost Card:** UI selects "Lost Card" → "I lost my card." → asks name → "John Smith" → offers to open claim → CONFIRM → ticket created
- [ ] **Scenario C — Support Tickets:** UI selects "Support Tickets" → "What are my open tickets?" → asks name → "John Smith" → shows support tickets
- [ ] **Scenario D — Document/RAG:** UI selects "General Questions" → "What hours are branches open?" → answers with source attribution (Branch Hours)
