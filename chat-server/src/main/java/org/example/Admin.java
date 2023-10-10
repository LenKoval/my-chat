package org.example;

import java.util.Objects;

public class Admin {
    private final String login = "administrator";
    private final String name = "Admin";
    private final String password = "admin555";
    private static final Roles role = Roles.ADMIN;

    public String getLogin() {
        return login;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }
}
