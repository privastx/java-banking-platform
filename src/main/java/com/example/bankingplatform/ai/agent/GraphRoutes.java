package com.example.bankingplatform.ai.agent;

public final class GraphRoutes {

    private GraphRoutes() {}

    // Node names
    public static final String RESOLVE_IDENTITY = "resolve_identity";
    public static final String GET_TRANSACTIONS = "get_transactions";
    public static final String GET_SUPPORT_TICKETS = "get_support_tickets";
    public static final String CONFIRM_PROMPT = "confirm_prompt";
    public static final String CONFIRM = "confirm";
    public static final String CREATE_TICKET = "create_ticket";
    public static final String RESPOND = "user_respond_required";

    // Edge targets from resolve_identity
    public static final String RESOLVE_IDENTITY_LOOP = "resolve_identity";

    // Edge targets from confirm
    public static final String CONFIRM_LOOP = "confirm";
    public static final String PROCEED_TO_TICKET = "create_ticket";
    public static final String ABORT_FLOW = "respond_abort";

    // Variable keys
    public static final String SESSION_ID = "sessionId";
    public static final String WORKFLOW = "workflow";
    public static final String USER_MESSAGE = "userMessage";
    public static final String CONFIRMATION_ACTION = "confirmationAction";

    // State keys
    public static final String AWAITING_IDENTITY = "awaitingIdentity";
    public static final String AWAITING_CONFIRMATION = "awaitingConfirmation";
    public static final String CONFIRMATION_PENDING = "confirmationPending";
    public static final String CONFIRMATION_RESULT = "confirmationResult";
    public static final String CUSTOMER_IDENTITY = "customerIdentity";
    public static final String AGENT_REPLY = "agent_response";
}