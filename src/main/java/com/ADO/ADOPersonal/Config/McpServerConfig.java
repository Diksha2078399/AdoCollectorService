package com.ADO.ADOPersonal.Config;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.mcp.server.api.McpServer;
import io.modelcontextprotocol.mcp.server.api.McpServerBuilder;
import io.modelcontextprotocol.mcp.server.transport.sse.webmvc.WebMvcSseServerTransportProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpServerConfig {

    @Bean
    public McpServer mcpServer(ObjectMapper objectMapper) {
        return McpServerBuilder.sync()
                .transportProvider(new WebMvcSseServerTransportProvider(objectMapper, "/mcp/message"))
                .serverInfo("github-mcp-server", "1.0.0")
                .build();
    }
}