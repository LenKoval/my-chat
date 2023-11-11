package org.example;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ClientHandler implements AutoCloseable {
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

    public String setUsername(String username) {
        this.username = username;
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
                socket.setSoTimeout(50000);
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
        while (true) {
            String message = in.readUTF();
            if (message.startsWith("/")) {
                if (message.equals("/exit")) {
                    break;
                } else if (message.startsWith("/w")) {
                    server.pointToPoint(this, message);
                } else if (message.equals("/list")) {
                    List<String> userList = server.getUsernameList();
                    String joinedUsers = String.join(", ", userList);
                    sendMessage(joinedUsers);
                } else if (message.startsWith("/kick")) {
                    sendMessage("Введите логин и пароль.");
                    String info = in.readUTF();
                    if (server.getAuthenticationProvider().checkAccess(info)) {
                        server.kickUser(message);
                    } else {
                        sendMessage("У вас нет прав для операции.");
                    }
                } else if (message.startsWith("/changeNick")) {
                    if (!server.changeNick(server.getAuthenticationProvider().changeUsername(message))) {
                        sendMessage("Вы ввели некорректные данные или такой ник уже занят. Попоробуйте еще раз.");
                    }
                } else if (message.startsWith("/ban")) {
                    sendMessage("Введите логин и пароль.");
                    String info = in.readUTF();
                    if (server.getAuthenticationProvider().checkAccess(info)) {
                        server.banClient(server.getAuthenticationProvider().banUser(message));
                    } else {
                        sendMessage("У вас нет прав для операции.");
                    }
                } else if (message.startsWith("/shutdown")) {
                    sendMessage("Введите логин и пароль");
                    String info = in.readUTF();
                    if (server.getAuthenticationProvider().checkAccess(info)) {
                        disconnect();
                    } else {
                        sendMessage("У вас нет прав для операции.");
                    }
                }
            } else {
                server.broadcastMessage(username + ": " + message);
            }
        }
    }

    private void authenticateUser(Server server) throws IOException {
        boolean isAuthenticated = false;
        while (!isAuthenticated) {
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
                        System.out.println(username + " вошел в чат.");
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
                        System.out.println(nick + " вошел в чат.");
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

    public void sendMessage(String message) {
        String dateTime = DateTimeFormatter.ofPattern("HH:mm:ss")
                .format(LocalDateTime.now());
        try {
            out.writeUTF(dateTime + " " + message + "\r\n");
        } catch (IOException e) {
            e.printStackTrace();
            disconnect();
        }
    }

    public void disconnect() {
        server.unsubscribe(this);
        if (socket != null) {
            try{
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if(in !=null) {
            try {
                in.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if(out !=null) {
            try {
                out.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void close() {
    }
}
