package com.ADO.ADOPersonal.Config;



import io.modelcontextprotocol.mcp.server.api.McpServer;
import io.modelcontextprotocol.mcp.server.api.McpServerBuilder;
import io.modelcontextprotocol.mcp.server.transport.sse.webmvc.SseWebMvcTransportProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpServerConfig {

    @Bean
    public McpServer mcpServer() {
        return McpServerBuilder.builder()
                .transportProvider(new SseWebMvcTransportProvider())
                .build();
    }
}
