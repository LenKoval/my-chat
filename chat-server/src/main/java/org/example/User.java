package org.example;

public class User {
    private String login;
    private String password;
    private String username;
    private static final Roles role = Roles.USER;
    /*private final String adminLogin = "admin";
    private final String adminPassword = "admin555";*/

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public Roles getRole() {
        return role;
    }

    /*public String getAdminLogin() {
        return adminLogin;
    }

    public String getAdminPassword() {
        return adminPassword;
    }*/

    public User(String login, String password, String username) {
        this.login = login;
        this.password = password;
        this.username = username;
    }
}
