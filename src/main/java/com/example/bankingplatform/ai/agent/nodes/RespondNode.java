package com.example.bankingplatform.ai.agent.nodes;

import com.example.bankingplatform.ai.agent.AppContext;
import com.example.bankingplatform.ai.agent.GraphRoutes;
import org.bsc.langgraph4j.action.NodeAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RespondNode implements NodeAction<AppContext> {

    private static final Logger log = LoggerFactory.getLogger(RespondNode.class);

    @Override
    public Map<String, Object> apply(AppContext state) {
        return Map.of();
    }
}