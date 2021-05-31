package com.example.spring.sandbox;

import java.io.IOException;

import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/ws/messages")
public class WebSocketEndpoint {
    @OnOpen
    public void onOpen(Session session, EndpointConfig endpointConfig) {

    }

    @OnMessage
    public void onMessage(Session session, String message) throws IOException {
        session.getBasicRemote().sendText("Responding to " + message);
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {

    }
}
