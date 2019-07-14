package client;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client extends JFrame{

    private JTextField userText;
    private JTextArea chatWindow;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private String message = "";
    private String serverIP;
    private Socket connection;

    // constructor
    public Client(String host){
        super("Client window");
        serverIP = host;

        userText = new JTextField();
        userText.setEditable(false);
        userText.addActionListener(e -> {
            sendMessage(e.getActionCommand());
            userText.setText("");
        });
        add(userText, BorderLayout.NORTH);

        chatWindow = new JTextArea();
        chatWindow.setEditable(false);
        add(new JScrollPane(chatWindow));

        setSize(300, 150);
        setVisible(true);
    }

    private void sendMessage(String message) {
        try{
            output.writeObject("CLIENT - " + message);
            output.flush();
            // i'm going to keep this commented
            // till i figure out a way to
            // showMessage("\nCLIENT - " + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startRunning(){
        try{
            connectToServer();
            setupConnections();
            whileChatting();
        } catch (UnknownHostException e) {
            showMessage("\nError: couldn't connect to server");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeConnections();
        }

    }

    private void closeConnections(){
        showMessage("\nDisconnecting from server..");
        ableToType(false);
    }
    private void showMessage(final String s) {
        SwingUtilities.invokeLater(() -> chatWindow.append(s));
    }

    private void connectToServer() throws IOException {
        showMessage("\nConnecting to server...");
        connection = new Socket(InetAddress.getByName(serverIP), 3000);
        showMessage("\nConnected to " + connection.getInetAddress().getHostName());
    }

    private void setupConnections() throws IOException {
        output = new ObjectOutputStream(connection.getOutputStream());
        output.flush();
        input = new ObjectInputStream(connection.getInputStream());
    }

    private void whileChatting(){
        ableToType(true);
        String message = "";
        do{
            try{
                message = (String) input.readObject();
                showMessage("\n" + message);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }while (!message.equals("SERVER - END"));
    }


    private void ableToType(boolean b) {
        SwingUtilities.invokeLater(() -> userText.setEditable(b));
    }

}