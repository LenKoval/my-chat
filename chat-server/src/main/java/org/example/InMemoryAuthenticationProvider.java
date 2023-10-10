package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InMemoryAuthenticationProvider implements AuthenticationProvider {
    private final List<User> users;
    private final Admin admin;
    public InMemoryAuthenticationProvider() {
        //ConcurrentHashMap можно использовать
        this.users = new ArrayList<>();
        this.admin = new Admin();
    }

    @Override
    public String getUsernameByLoginAndPassword(String login, String password) {
        for (User user : users) {
            if (Objects.equals(user.getPassword(), password) && Objects.equals(user.getLogin(), login)) { //редактировать строку
                return user.getUsername();
            }
        }

        if (Objects.equals(login, admin.getLogin()) && Objects.equals(password, admin.getPassword())) {
            return admin.getName();
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
        users.add(new User(login, password, username));
        return true;
    }

    @Override
    public boolean isRole(String username) {
        if (username.equals(admin.getName())) {
            return true;
        }
        return false;
    }
}
