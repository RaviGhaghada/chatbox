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
    private CopyOnWriteArrayList<ConnectionToClient> clients;

    private LinkedBlockingQueue<String> messages;

    private JTextField userText;
    private JTextArea chatWindow;


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

    private void emitAll(String message){
        int i=0;
        while (i<clients.size()){
            clients.get(i).send(message);
            showMessage(message);
            i++;
        }
    }

    private void showMessage(final String text){
        SwingUtilities.invokeLater(() -> chatWindow.append("\n" + text));

    }


    private class ConnectionToClient {

        private Socket connection;
        private ObjectOutputStream out;
        private ObjectInputStream in;


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

        public String name(){
            return connection.getInetAddress().getHostName();
        }


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

        public boolean isClosed(){
            return connection.isClosed();
        }

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
