package org.example;

import org.example.auth.AuthenticationProvider;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Server implements Runnable {
    private int port;
    private List<ClientHandler> clients;
    private final AuthenticationProvider authenticationProvider;
    public AuthenticationProvider getAuthenticationProvider() {
        return authenticationProvider;
    }

    public Server(int port, AuthenticationProvider authenticationProvider) { //переделать на логирование
        this.port = port;
        clients = new ArrayList<>();
        this.authenticationProvider = authenticationProvider;
    }

    public synchronized void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
        broadcastMessage(clientHandler.getUsername() + " вошел в чат");
    }

    public synchronized void broadcastMessage(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public synchronized void pointToPoint(ClientHandler from, String message) {
        String[] data = message.split(" ", 3);
        for (ClientHandler client : clients) {
            if (client.getUsername().equals(data[1])) {
                client.sendMessage("сообщение от " + from.getUsername() + " : " + data[2]);
            }
        }
    }

    public synchronized void kickUser(String message) {
        String[] data = message.split(" ", 2);
        ClientHandler kickedClient = null;
        for (ClientHandler client : clients) {
            if (client.getUsername().equals(data[1])) {
                kickedClient = client;
            }
        }
        if (kickedClient != null) {
            kickedClient.sendMessage("Вы удалены из чата.");
            kickedClient.close();
        }
    }

    public synchronized void banClient(String username) {
        ClientHandler bannedUser = null;
        for (ClientHandler client : clients) {
            if (client.getUsername().equals(username));
            bannedUser = client;
        }
        if (bannedUser != null) {
            bannedUser.sendMessage("Вы заблокированы Администратором.");
            bannedUser.close();
        }
    }

    public synchronized void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        broadcastMessage(clientHandler.getUsername() + " вышел из чата");
    }

    public synchronized List<String> getUsernameList() {
        return clients.stream().map(ClientHandler::getUsername).collect(Collectors.toList());
    }

    public synchronized boolean changeNick(String[] data) {
        for (ClientHandler client : clients) {
            if (data[1] == null) {
                return false;
            } else {
                client.setUsername(data[3]);
                client.sendMessage("Вы успешно изменили ник на " + client.getUsername());
            }
        }
        return true;
    }

    @Override
    public void run() {
        try(ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                System.out.println("Сервер запущен на порту " + port);
                Socket socket = serverSocket.accept();
                new ClientHandler(socket, this);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}

