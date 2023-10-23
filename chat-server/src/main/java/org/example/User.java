package org.example;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String login;
    private String password;
    private String username;
    private List<Roles> role;

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public List<Roles> getRole() {
        return role;
    }

    public User(String login, String password, String username, List<Roles> role) {
        this.login = login;
        this.password = password;
        this.username = username;
        this.role = new ArrayList<>();
    }
}
