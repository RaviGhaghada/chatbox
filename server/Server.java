package server;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;


public class Server extends JFrame{

    private ServerSocket server;

    /**
     * A thread-safe version of array-list
     * that stores ConnectionToClient objects
     */
    private CopyOnWriteArrayList<ConnectionToClient> clients;

    /**
     * Store all messages to be sent to clients
     */
    private LinkedBlockingQueue<String> messages;

    /**
     * Allow the server to type anything to be sent to the clients
     */
    private JTextField userText;

    /**
     * Chat window where the chat history is displayed
     */
    private JTextArea chatWindow;

    /**
     * Constructor to create a Server Object
     * This constructor primarily describes the graphical interface
     * of the server-side chat window
     */
    public Server(){
        super("Instant messenger");

        userText = new JTextField();
        userText.setEditable(true);
        userText.addActionListener(e -> {
            try {
                messages.put("SERVER - " + e.getActionCommand());
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            userText.setText("");
        });
        add(userText, BorderLayout.NORTH);

        chatWindow = new JTextArea();
        chatWindow.setEditable(false);
        add(new JScrollPane(chatWindow));
        setSize(300, 150);
        setVisible(true);

        messages = new LinkedBlockingQueue<>();
        clients = new CopyOnWriteArrayList<>();
    }

    /**
     * The method that starts everything
     */
    public void startRunning(){
        try{
            server = new ServerSocket(3000, 100);
            try{
                acceptClients();
                processMessages();
            }
            finally {
                closeConnections();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Contains a thread that accepts clients
     * and creates I/O ObjectStreams and Socket objects for each client
     */
    private void acceptClients() {
        Thread searchConn = new Thread(()-> {
            while (true){
                try {
                    Socket connection = server.accept();
                    ConnectionToClient client = new ConnectionToClient(connection);
                    clients.add(client);
                    messages.put(client.name() + " connected!");
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        searchConn.setDaemon(true);
        searchConn.start();

    }

    /**
     * Contains a thread that safely destroys disconnected client objects
     */
    private void closeConnections() {

        System.out.println("Here!");
        Thread closeConns = new Thread(() -> {
           while (true){
               int i=0;
               while (i<clients.size()){
                   ConnectionToClient conn = clients.get(i);
                   if (conn.isClosed()){
                       try {
                           messages.put(conn.name() + " disconnected.");
                       } catch (InterruptedException e) {
                           e.printStackTrace();
                       }
                       conn.close();
                       clients.remove(i);
                   }
                   i++;
               }
           }
        });
        closeConns.setDaemon(true);
        closeConns.start();
    }

    /**
     * Contains a thread that sends the collected messages to all clients
     */
    private void processMessages() {

        Thread thread = new Thread(() -> {
            while (true){
                if (!messages.isEmpty()){
                    try {
                        String message = messages.take();
                        emitAll(message);
                        showMessage(message);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Send a message to all connected clients
     * @param message string to be sent
     */
    private void emitAll(String message){
        int i=0;
        while (i<clients.size()){
            clients.get(i).send(message);
            showMessage(message);
            i++;
        }
    }

    /**
     * Shows a message to the server-gui-window
     * @param text string to be displayed
     */
    private void showMessage(final String text){
        SwingUtilities.invokeLater(() -> chatWindow.append("\n" + text));

    }

    /**
     * Represents a socket connection between a client and server
     * This contains a connection's personal I/O stream
     * and Socket object
     */
    private class ConnectionToClient {

        /**
         * Connection between socket and client
         */
        private Socket connection;

        /**
         * Outgoing object stream
         * SERVER to CLIENT
         */
        private ObjectOutputStream out;

        /**
         * Incoming object stream
         * CLIENT to SERVER
         */
        private ObjectInputStream in;


        /**
         * Constructor to create object
         * and set up object streams
         * @param connection socket object connecting server to client
         */
        public ConnectionToClient(Socket connection){
            this.connection = connection;
            try {
                this.in = new ObjectInputStream(connection.getInputStream());
                this.out = new ObjectOutputStream(connection.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }

            Thread getMessages = new Thread(()->{
                while (true) {
                    try {
                        String message = (String) in.readObject();
                        System.out.println(message);
                        messages.put(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            getMessages.setDaemon(true);
            getMessages.start();

        }

        /**
         * Get the host name of client
         * @return host name of client
         */
        public String name(){
            return connection.getInetAddress().getHostName();
        }

        /**
         * Send a string to the client
         * @param message message to be sent to client
         */
        public void send(String message){
            Thread sender = new Thread(()->{
                try {
                    out.writeObject(message);
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            sender.setDaemon(true);
            sender.start();
        }

        /**
         * Check if the socket connection between client and server
         * is closed or not
         * @return true if connection is closed, else false
         */
        public boolean isClosed(){
            return connection.isClosed();
        }

        /**
         * Close socket connection and all object streams
         */
        public void close(){
            try{
                out.close();
                in.close();
                connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
