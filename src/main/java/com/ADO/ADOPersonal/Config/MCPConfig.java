package com.ADO.ADOPersonal.Config;



import io.mcp.server.MCPServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MCPConfig {

    @Value("${mcp.server.port:8081}")
    private int mcpPort;

    @Value("${mcp.repository.path:./mcp-repos}")
    private String repositoryPath;

    @Bean
    public MCPServer mcpServer() {
        MCPServer server = new MCPServer(mcpPort);
        server.setRepositoryPath(repositoryPath);
        server.start();
        return server;
    }
}
