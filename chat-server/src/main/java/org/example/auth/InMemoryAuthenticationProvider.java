package org.example.auth;

import org.example.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InMemoryAuthenticationProvider implements AuthenticationProvider {
    private final List<User> users;
    private BDConnector bdConnector;

    public InMemoryAuthenticationProvider() {
        //ConcurrentHashMap можно использовать
        this.users = new ArrayList<>();
        this.bdConnector = new BDConnector();
        users.addAll(bdConnector.selectUsers());
    }

    @Override
    public String getUsernameByLoginAndPassword(String login, String password) {
        for (User user : users) {
            if (Objects.equals(user.getPassword(), password)
                    && Objects.equals(user.getLogin(), login)
                    && Objects.equals(user.getBanned(), false)) {
                return user.getUsername();
            }
        }
        return null;
    }

    @Override
    public synchronized boolean register(String login, String password, String username) {
        for (User user : users) {
            if (Objects.equals(user.getUsername(), username) && Objects.equals(user.getLogin(), login)) {
                return false;
            }
        }
        users.add(new User(login, password, username, "user", false));
        bdConnector.insertUser(login, password, username);
        return true;
    }

    @Override
    public synchronized boolean checkAccess(String info) {
        String[] data = info.split(" ", 2);
        for (User user : users) {
            if (Objects.equals(user.getLogin(), data[0])
                    && Objects.equals(user.getPassword(), data[1])
                    && Objects.equals(user.getRole(), "admin")) {
                return true;
            }
        }
        return false;
    }

    public synchronized String[] changeUsername(String message) {
        String[] data = message.split(" ", 4);
        for (User user : users) {
            if (Objects.equals(user.getUsername(), data[3])) {
                return null;
            } else {
                if(Objects.equals(user.getUsername(), data[1])) {
                    user.setUsername(data[2]);
                    bdConnector.updateUser(data[2], data[3]);
                }
            }
        }
        return data;
    }
    @Override
    public synchronized String banUser(String message) {
        String[] data = message.split(" ", 2);
        for (User user : users) {
            if (Objects.equals(user.getUsername(), data[1])) {
                user.setBanned(true);
                bdConnector.updateBan(data[1]);
                return user.getUsername();
            }
        }
        return null;
    }
}
