package com.example.client;

import org.json.JSONObject;

public interface MessageInterface {

    void createFromJSON(JSONObject obj);
    /**
     * @return a json object expressing this class
     */
     JSONObject json();
}
