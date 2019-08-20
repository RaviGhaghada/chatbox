package com.example.client;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public abstract class AbstractClient {

    private ObjectOutputStream output;
    private ObjectInputStream input;
    private Socket connection;
    private String serverIP;
    private int port;

    private String mClass;
    private void start(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
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
        });
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Get the client side app working
     */
    public void start(String serverIP, int port, String c){
        this.serverIP = serverIP;
        this.port = port;
        this.mClass = c;
        this.start();
    }


    /**
     * Connect to a server
     * @throws IOException
     */
    private void connectToServer() {
        showMessage("\nConnecting to server...");

        while (connection == null){
            try {
                connection = new Socket(InetAddress.getByName(serverIP), port);
                showMessage("\nConnected to " + connection.getInetAddress().getHostName());
            }catch (Exception e){
                e.printStackTrace();
                connection = null;
            }
        }
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
     * Close socket connection to server
     */
    private void closeConnections(){
        showMessage("\nDisconnecting from server..");
        try {
            if (connection!=null)
                connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //ableToType(false);
    }


    /**
     * Core loop to get data back from server
     */
    private void whileChatting(){
        //ableToType(true);
        MessageInterface message = null;

        do{
            try {
                message = (MessageInterface) Class.forName("com.example.client.Message").newInstance();
                message.createFromJSON(new JSONObject((String) input.readObject()));
                handleIncomingMessage(message);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }while (true);
    }

    protected abstract void handleIncomingMessage(MessageInterface message);

    public void sendMessage(MessageInterface m){
        try {
            output.writeObject(m.json().toString());
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Give the client a name, could be overriden to use actual real names
     * @return
     */
    protected String getMyName() {
        return connection.getLocalAddress().getHostName();
    }


    abstract protected void showMessage(final String s);

}
