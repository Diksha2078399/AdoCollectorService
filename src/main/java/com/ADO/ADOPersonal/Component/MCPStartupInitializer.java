package com.ADO.ADOPersonal.Component;



import io.mcp.server.MCPServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class MCPStartupInitializer {

    @Autowired
    private MCPServer mcpServer;

    @EventListener(ApplicationReadyEvent.class)
    public void initializeMCP() {
        System.out.println("MCP Server running on port: " + mcpServer.getPort());
        System.out.println("MCP Repository path: " + mcpServer.getRepositoryPath());
    }
}
