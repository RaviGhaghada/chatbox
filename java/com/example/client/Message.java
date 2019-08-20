package com.example.client;

import org.json.JSONException;
import org.json.JSONObject;


public class Message implements MessageInterface {

    private String type;
    private String data;
    private String source;

    /**
     * Zero argument instantiation
     */
    public Message(){

    }

    public Message(String type, String data, String source) {
        this.type = type;
        this.data = data;
        this.source = source;


        // BAD CODE. I am disappointed in myself!!!
        if (this.type.equals("TYPING")){
            this.data = source + " is typing ... ";
        }
    }

    public void createFromJSON(JSONObject json){
        try {
            this.type = (String) json.get("type");
            this.data = (String) json.get("data");
            this.source = (String) json.get("source");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String toString(){
        return this.getSource() + " - " + this.getData();
    }


    public JSONObject json(){
        JSONObject obj = new JSONObject();
        try {
            obj.put("type", type);
            obj.put("source", source);
            obj.put("data", data);
            return obj;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    public String getType() {
        return type;
    }

    public String getData() {
        return data;
    }

    public String getSource() {
        return source;
    }
}
