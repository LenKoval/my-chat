package org.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Server {
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

    public void start() {
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

    public synchronized void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
        broadcastMessage("Клиент: " + clientHandler.getUsername() + " вошел в чат");
    }

    public synchronized void broadcastMessage(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public synchronized void pointToPoint(ClientHandler from, String userNameTo, String message) {
        for (ClientHandler client : clients) {
            if (client.getUsername().equals(userNameTo)) {
                client.sendMessage("сообщение от " + from.getUsername() + message);
                from.sendMessage("сообщение " + message + " доставлено " + userNameTo);
                break;
            }
        }
    }

    public synchronized void kickUser(String username) {
        for (ClientHandler client : clients) {
            if (client.getUsername().equals(username)) {
                client.disconnect();
            }
        }
    }

    public synchronized void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        broadcastMessage("Клиент: " + clientHandler.getUsername() + " вышел из чата");
    }

    public synchronized List<String> getUsernameList() {
        /*var lis = new ArrayList<String>();
        for (ClientHandler client : clients) {
            lis.add(client.getUsername());
        }
        return lis;*/
        return clients.stream()
                .map(ClientHandler::getUsername)
                //.map(client -> client.getUsername())
                .collect(Collectors.toList());
    }
}

