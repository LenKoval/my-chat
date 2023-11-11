package org.example.auth;

public interface AuthenticationProvider {
    String getUsernameByLoginAndPassword(String login, String password);
    boolean register(String login, String password, String username);
    boolean checkAccess(String str);
    String[] changeUsername(String str);
    String banUser(String str);
}
