package client;

import common.Message;
import common.MessageType;

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
    private String serverIP;
    private Socket connection;

    // constructor
    public Client(String host){
        super("Client window");
        serverIP = host;

        userText = new JTextField();
        userText.setEditable(false);
        userText.addActionListener(e -> {
            Message message = new Message(MessageType.CHAT, e.getActionCommand(), getMyName());
            sendMessage(message);
            userText.setText("");
        });
        add(userText, BorderLayout.NORTH);

        chatWindow = new JTextArea();
        chatWindow.setEditable(false);
        add(new JScrollPane(chatWindow));

        setSize(300, 150);
        setVisible(true);
    }

    /**
     * send a message to the server
     * @param message message to be sent
     */
    private void sendMessage(Message message) {
        try{
            output.writeObject(message);
            output.flush();
            // i'm going to keep this commented
            // till i figure out a way to
            // showMessage("\nCLIENT - " + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the client side app working
     */
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

    /**
     * Close socket connection to server
     */
    private void closeConnections(){
        showMessage("\nDisconnecting from server..");
        ableToType(false);
    }

    /**
     * Show a message to the user screen
     * @param s message to be displayed to the user
     */
    private void showMessage(final String s) {
        SwingUtilities.invokeLater(() -> {
            chatWindow.append(s);
        });
    }

    /**
     * Connect to a server
     * @throws IOException
     */
    private void connectToServer() throws IOException {
        showMessage("\nConnecting to server...");
        connection = new Socket(InetAddress.getByName(serverIP), 3000);
        showMessage("\nConnected to " + connection.getInetAddress().getHostName());
    }

    /**
     * Setup streams to send data back and forth
     * @throws IOException
     */
    private void setupConnections() throws IOException {
        output = new ObjectOutputStream(connection.getOutputStream());
        output.flush();
        input = new ObjectInputStream(connection.getInputStream());
    }

    /**
     * Core loop to get data back from server
     */
    private void whileChatting(){
        ableToType(true);
        Message message = new Message(MessageType.CHAT, "","");
        do{
            try{
                message = (Message) input.readObject();
                showMessage("\n" + message.getData());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }while (!message.getData().equals("SERVER - END"));
    }


    /**
     * Method to give access / restrict the client user from typing
     * into the window
     *
     * Ideally, the user is not supposed to have access if the client
     * is not connected to the server
     * @param b boolean value
     */
    private void ableToType(boolean b) {
        SwingUtilities.invokeLater(() -> userText.setEditable(b));
    }

    private String getMyName(){
        return "CLIENT";
    }

}