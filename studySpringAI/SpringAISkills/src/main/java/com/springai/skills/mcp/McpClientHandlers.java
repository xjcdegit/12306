package com.springai.skills.mcp;

import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;
import io.modelcontextprotocol.spec.McpSchema.ProgressNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.mcp.annotation.McpLogging;
import org.springaicommunity.mcp.annotation.McpProgress;
import org.springframework.stereotype.Service;

/**
 * MCP 客户端通知处理器 - 处理来自 MCP Server 的通知
 * 
 * <p>McpClientHandlers 负责处理 MCP Server 发送的各种通知，
 * 包括进度更新、日志消息等。</p>
 * 
 * <h3>支持的通知类型：</h3>
 * <ul>
 *   <li>Progress - 操作进度更新</li>
 *   <li>Logging - 服务端日志消息</li>
 * </ul>
 * 
 * <h3>配置说明：</h3>
 * <p>clients 属性指定处理哪个 MCP Server 的通知，
 * 需要与 application.yml 中的连接名称对应。</p>
 * 
 * @author SpringAI Skills Framework
 */
@Service
public class McpClientHandlers {

    private static final Logger log = LoggerFactory.getLogger(McpClientHandlers.class);

    /**
     * 处理进度通知
     * 
     * <p>当 MCP Server 执行长时间操作时，会发送进度更新通知。</p>
     * 
     * @param notification 进度通知
     */
    @McpProgress(clients = {"filesystem-server", "git-server"})
    public void progressHandler(ProgressNotification notification) {
        log.info("MCP Progress: token={}, progress={}, total={}, message={}", 
            notification.progressToken(), 
            notification.progress(), 
            notification.total(),
            notification.message());
    }

    /**
     * 处理日志通知
     * 
     * <p>当 MCP Server 记录日志时，会发送日志消息通知。</p>
     * 
     * @param notification 日志通知
     */
    @McpLogging(clients = {"filesystem-server", "git-server"})
    public void loggingHandler(LoggingMessageNotification notification) {
        log.info("MCP Logging: level={}, data={}", 
            notification.level(), 
            notification.data());
    }
}
