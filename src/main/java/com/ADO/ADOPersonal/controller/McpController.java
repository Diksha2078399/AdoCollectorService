package com.ADO.ADOPersonal.controller;

import io.modelcontextprotocol.mcp.server.api.McpServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/mcp")
public class McpController {

    private final McpServer mcpServer;

    @Autowired
    public McpController(McpServer mcpServer) {
        this.mcpServer = mcpServer;
    }

    @GetMapping(value = "/message", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream() {
        return Flux.create(sink -> {
            mcpServer.start(exchange -> {
                exchange.getMessages().subscribe(
                        sink::next,
                        sink::error,
                        sink::complete
                );
            });
        });
    }
}