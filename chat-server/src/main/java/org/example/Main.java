package org.example;

import org.example.auth.InMemoryAuthenticationProvider;

public class Main {
    public static void main(String[] args) {
        int port = 8080;
        Server server = new Server(port, new InMemoryAuthenticationProvider());
        server.run();
    }
}
