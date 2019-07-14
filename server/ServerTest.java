package server;

import javax.swing.*;

/**
 * Run an instance of a Server
 */
public class ServerTest {

    public static void main(String[] args) {
        Server server = new Server();
        server.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        server.startRunning();
    }
}
