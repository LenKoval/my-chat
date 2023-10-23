package org.example;

public interface AuthenticationProvider {
    String getUsernameByLoginAndPassword(String login, String password);
    boolean register(String login, String password, String username);
    boolean isAdmin(ClientHandler clientHandler);
}
