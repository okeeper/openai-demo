package com.okeeper.client;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class OpenAIWebSocketListener extends WebSocketListener {

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        System.out.println("WebSocket opened");
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        System.out.println("Received text message: " + text);
        // 将响应结果发送给客户端
        // ...
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        System.out.println("Received binary message");
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        System.out.println("WebSocket failure: " + t.getMessage());
    }
}
