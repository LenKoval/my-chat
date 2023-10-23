package org.example;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ClientHandler {
    private Socket socket; // подключение
    private Server server; // сервер с клиентами
    private DataInputStream in; // входящее сооб
    private DataOutputStream out; // исх сооб
    private String username;
    private static int userCount = 0;
    private static final int maxUsersCount = 10;

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
        new Thread(() -> {
            try {
                authenticateUser(server);
                communicateWithUser(server);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                disconnect();
            }
        }).start();
    }

    private void communicateWithUser(Server server) throws IOException {
        while(true) {
            String message = in.readUTF();
            String[] args = message.split(" ");
            if (message.startsWith("/")) {
                if (message.equals("/exit")) {
                    break;
                }
                if (message.equals("/w")) {
                    String userName = args[1];
                    String mes = args[2];
                    server.pointToPoint(this, userName, mes);
                }
                if (message.equals("/list")) {
                    List<String> userList = server.getUsernameList();
                    String joinedUsers = String.join(", ", userList);
                    //userList.stream().collect(Collectors.joining(", "));
                    sendMessage(joinedUsers);
                }
                if (message.equals("/kick")) {
                    String[] data1 = message.split("\\s", 2);
                    if (server.getAuthenticationProvider().isAdmin(this)) {
                        server.kickUser(data1[1]);
                    } else {
                        sendMessage("У вас нет прав для операции.");
                    }
                }
            } else {
                server.broadcastMessage("Server: " + message);
            }
        }
    }

    private void authenticateUser(Server server) throws IOException {
        boolean isAuthenticated = false;
        while(!isAuthenticated) {
            String message = in.readUTF();
            String[] args = message.split(" ");
            String command = args[0];
            switch (command) {
                case "/auth": {
                    String login = args[1];
                    String password = args[2];
                    String username = server.getAuthenticationProvider().getUsernameByLoginAndPassword(login, password);
                    if (username == null || username.isBlank()) {
                        sendMessage("Указан неверный логин/пароль.");
                    } else {
                        this.username = username;
                        sendMessage(username + " , добро пожаловать в чат!");
                        server.subscribe(this);
                        isAuthenticated = true;
                    }
                    break;
                }
                case "/register": {
                    String login = args[1];
                    String nick = args[2];
                    String password = args[3];
                    boolean isRegister = server.getAuthenticationProvider().register(login, password, nick);
                    if (!isRegister) {
                        sendMessage("Указан неверный логин/пароль.");
                    } else {
                        this.username = nick;
                        sendMessage(nick + " , добро пожаловать в чат!");
                        server.subscribe(this);
                        isAuthenticated = true;
                    }
                    break;
                }
                default: {
                    sendMessage("Авторизуйтесь.");
                }
            }
        }
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
