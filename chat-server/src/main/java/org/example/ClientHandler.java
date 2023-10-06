package org.example;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Socket socket;

    private Server server;
    private DataInputStream in;
    private DataOutputStream out;

    private String username;

    private static int userCount = 0;
    private static final int maxUsersCount = 10;
    // реализовать ограничение количества пользователей

    public String getUsername() {
        return username;
    }

    public ClientHandler(Socket socket, Server server) throws IOException {
        this.socket = socket;
        this.server = server;
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        if (userCount < maxUsersCount) {
            username = "User" + userCount++;
        }
        server.subscribe(this);
        new Thread(() -> {
            try {
                while(true) {
                    // /exit -> disconnect()
                    // /w user message -> user

                    String message = in.readUTF();
                    if(message.startsWith("/")) {
                        if(message.equals("/exit")) {
                            break;
                        } else if (message.equals("/w")) {
                            String[] data = message.split("\\s", 3);
                            server.pointToPoint(this, data[1], data[2]);
                        }
                    }
                    server.broadcastMessage(message);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                disconnect();
            }
        }).start();
    }

    public void disconnect() {
        server.unsubscribe(this);
        if(socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
            disconnect();
        }
    }
}
