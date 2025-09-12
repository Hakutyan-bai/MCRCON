package com.example.demo.service;

import com.example.demo.config.ConfigManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@Service
public class RconService {

    private final ConfigManager configManager;

    @Autowired
    public RconService(ConfigManager configManager) {
        this.configManager = configManager;
    }

    private String rconHost;
    private int rconPort;
    private String rconPassword;
    private int rconTimeout;

    private static final int SERVERDATA_AUTH = 3;
    private static final int SERVERDATA_EXECCOMMAND = 2;
    private static final int SERVERDATA_AUTH_RESPONSE = 2;

    public String executeCommand(String command) throws Exception {
        rconHost = configManager.getRconHost();
        rconPort = configManager.getRconPort();
        rconPassword = configManager.getRconPassword();
        rconTimeout = configManager.getRconTimeout();

        try (Socket socket = new Socket(rconHost, rconPort)) {
            socket.setSoTimeout(rconTimeout);

            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());

            // 认证
            sendPacket(out, 1, SERVERDATA_AUTH, rconPassword);
            RconPacket authResponse = receivePacket(in);

            if (authResponse.getId() != 1) {
                throw new Exception("RCON认证失败");
            }

            // 执行命令
            sendPacket(out, 2, SERVERDATA_EXECCOMMAND, command);
            RconPacket response = receivePacket(in);

            return response.getBody();
        }
    }

    private void sendPacket(DataOutputStream out, int id, int type, String body) throws IOException {
        byte[] bodyBytes = body.getBytes("UTF-8");
        int length = 4 + 4 + bodyBytes.length + 2; // id + type + body + null terminators

        ByteBuffer buffer = ByteBuffer.allocate(4 + length);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(length);
        buffer.putInt(id);
        buffer.putInt(type);
        buffer.put(bodyBytes);
        buffer.put((byte) 0);
        buffer.put((byte) 0);

        out.write(buffer.array());
        out.flush();
    }

    private RconPacket receivePacket(DataInputStream in) throws IOException {
        int length = Integer.reverseBytes(in.readInt());
        int id = Integer.reverseBytes(in.readInt());
        int type = Integer.reverseBytes(in.readInt());

        byte[] bodyBytes = new byte[length - 10];
        in.readFully(bodyBytes);

        // 读取两个null终止符
        in.readByte();
        in.readByte();

        String body = new String(bodyBytes, "UTF-8");
        return new RconPacket(id, type, body);
    }

    public String addToWhitelist(String playerName) {
        try {
            String result = executeCommand("whitelist add " + playerName);
            return result.trim().isEmpty() ? "成功添加玩家 " + playerName + " 到白名单" : result;
        } catch (Exception e) {
            return "添加白名单失败: " + e.getMessage();
        }
    }

    public String removeFromWhitelist(String playerName) {
        try {
            String result = executeCommand("whitelist remove " + playerName);
            return result.trim().isEmpty() ? "成功从白名单移除玩家 " + playerName : result;
        } catch (Exception e) {
            return "移除白名单失败: " + e.getMessage();
        }
    }

    public String getWhitelistStatus() {
        try {
            return executeCommand("whitelist list");
        } catch (Exception e) {
            return "获取白名单状态失败: " + e.getMessage();
        }
    }

    public boolean testConnection() {
        try {
            executeCommand("list");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 内部类用于表示RCON数据包
    private static class RconPacket {
        private final int id;
        private final int type;
        private final String body;

        public RconPacket(int id, int type, String body) {
            this.id = id;
            this.type = type;
            this.body = body;
        }

        public int getId() { return id; }
        public int getType() { return type; }
        public String getBody() { return body; }
    }
}
