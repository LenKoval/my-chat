package org.example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class SocketConnector implements AutoCloseable {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private Callback callback;
    private static final Logger logger = LogManager.getLogger(SocketConnector.class.getName());

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void connect(int port) throws IOException {
        socket = new Socket("localhost", port);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        new Thread(() -> {
            try {
                while (true) {
                    String message = in.readUTF();
                    if (callback != null) {
                        callback.call(message);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                close();
            }
        }).start();
    }

    public void close()  {
        if(socket != null) {
            try {
                socket.close();
                logger.info("Сокет закрыт");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (in != null) {
            try {
                in.close();
                logger.info("Поток чтения закрыт");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (out != null) {
            try {
                out.close();
                logger.info("Поток записи закрыт");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void sendMessage(String msg) {
        try {
            out.writeUTF(msg);
            logger.info("Поток записи открыт");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

