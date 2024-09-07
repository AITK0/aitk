package org.ai.toolkit.aitk.websocket;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.springframework.stereotype.Component;

@Component
@ServerEndpoint("/websocket/{userId}")
public class WebSocketManager {
    private static ConcurrentHashMap<String, Session> SESSION_POOL = new ConcurrentHashMap<String, Session>();

    public WebSocketManager() {
        System.out.println(this);
    }

    private String userId;

    @OnOpen
    public void onOpen(Session session, @PathParam(value = "userId") String userId) {
        this.userId = userId;
        SESSION_POOL.put(userId, session);
    }

    @OnClose
    public void onClose() {
        SESSION_POOL.remove(this.userId);
    }

    public void sendMessage(String userId, String message) throws IOException {
        Session session = SESSION_POOL.get(userId);
        if (session != null && session.isOpen()) {
            session.getBasicRemote().sendText(message);
        }
    }
}
