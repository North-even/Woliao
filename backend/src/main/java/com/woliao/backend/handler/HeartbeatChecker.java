package com.woliao.backend.handler; // 请确保这个包名和您项目的实际路径一致

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class HeartbeatChecker {

    private static final Logger logger = LoggerFactory.getLogger(HeartbeatChecker.class);
    private static final long HEARTBEAT_TIMEOUT = 45 * 1000; // 45秒超时

    @Autowired
    private ChatWebSocketHandler chatWebSocketHandler;

    @Scheduled(fixedRate = 15 * 1000) // 每15秒检查一次
    public void checkHeartbeats() {
        long now = System.currentTimeMillis();

        // 【核心修改】调用正确的方法名 getLastHeartbeatMap()
        Map<Long, Long> lastHeartbeatMap = chatWebSocketHandler.getLastHeartbeatMap();
        Map<Long, WebSocketSession> sessions = chatWebSocketHandler.getSessions();

        // 为了安全，我们复制一份key的集合来遍历，防止在遍历时修改map
        ConcurrentHashMap<Long, Long> heartbeatsToCheck = new ConcurrentHashMap<>(lastHeartbeatMap);

        heartbeatsToCheck.forEach((userId, lastBeat) -> {
            if (now - lastBeat > HEARTBEAT_TIMEOUT) {
                WebSocketSession session = sessions.get(userId);
                if (session != null && session.isOpen()) {
                    try {
                        logger.warn("User {} heartbeat timeout. Closing session.", userId);
                        session.close(CloseStatus.GOING_AWAY.withReason("Heartbeat timeout"));
                    } catch (IOException e) {
                        logger.error("Error closing timeout session for user: {}", userId, e);
                    }
                }
                // 从原始的map中移除
                lastHeartbeatMap.remove(userId);
                sessions.remove(userId);
            }
        });
    }
}