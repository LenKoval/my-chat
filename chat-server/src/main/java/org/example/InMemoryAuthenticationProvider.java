package org.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class InMemoryAuthenticationProvider implements AuthenticationProvider {
    private final List<User> users;
    private static final String adminLogin = "admin";
    private static final String adminPassword = "admin555";
    private static final String adminUserName = "administrator";

    public InMemoryAuthenticationProvider() {
        //ConcurrentHashMap можно использовать
        this.users = new ArrayList<>();
        users.add(new User(adminLogin, adminPassword, adminUserName, Arrays.asList(Roles.ADMIN, Roles.USER)));
    }

    @Override
    public String getUsernameByLoginAndPassword(String login, String password) {
        for (User user : users) {
            if (Objects.equals(user.getPassword(), password) && Objects.equals(user.getLogin(), login)) { //редактировать строку
                return user.getUsername();
            }
        }
        return null;
    }

    @Override
    public synchronized boolean register(String login, String password, String username) {
        for (User user : users) {
            if (Objects.equals(user.getUsername(), username) && Objects.equals(user.getLogin(), login)) { //редактировать строку
                return false;
            }
        }
        users.add(new User(login, password, username, Arrays.asList(Roles.USER)));
        return true;
    }

    @Override
    public synchronized boolean isAdmin(ClientHandler clientHandler) {
        for (User user : users) {
            if (Objects.equals(user.getUsername(), clientHandler.getUsername()) && user.getRole().equals(Roles.ADMIN)) {
                return true;
            }
        }
        return false;
    }
}
