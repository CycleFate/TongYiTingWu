//package com.example.demo.service;
//
//import org.java_websocket.client.WebSocketClient;
//import org.java_websocket.handshake.ServerHandshake;
//
//import java.net.URI;
//
//public class WebSocketPushService {
//
//    private WebSocketClient webSocketClient;
//
//    public void connect(String meetingJoinUrl) {
//        try {
//            webSocketClient = new WebSocketClient(new URI(meetingJoinUrl)) {
//                @Override
//                public void onOpen(ServerHandshake handshakedata) {
//                    System.out.println("WebSocket Connected");
//                }
//
//                @Override
//                public void onMessage(String message) {
//                    // 处理接收到的转写结果
//                    System.out.println("Received: " + message);
//                    // TODO: 将识别结果存储到数据库
//                }
//
//                @Override
//                public void onClose(int code, String reason, boolean remote) {
//                    System.out.println("WebSocket Closed: " + reason);
//                }
//
//                @Override
//                public void onError(Exception ex) {
//                    ex.printStackTrace();
//                }
//            };
//            webSocketClient.connect();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void sendAudioData(byte[] audioData) {
//        if (webSocketClient != null && webSocketClient.isOpen()) {
//            webSocketClient.send(audioData);
//        }
//    }
//}