package com.example.client;

import android.os.Bundle;
import android.app.Activity;
import android.os.StrictMode;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;


public class MainActivity extends Activity {
    // Array of strings...
    String[] array = {"Hello!"};
    ArrayList<String> mobileArray= new ArrayList<>(Arrays.asList(array));
    private ArrayAdapter<String> adapter;

    private EditText userText;
    private ListView listView;

    private Client client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);


        adapter = new ArrayAdapter<>(this,
                R.layout.activity_list_view, mobileArray);

        listView = findViewById(R.id.mobile_list);
        listView.setAdapter(adapter);

        userText = findViewById(R.id.editText);
        userText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int k, KeyEvent e) {
                if (k == EditorInfo.IME_ACTION_DONE){
                    Message message = new Message("CHAT", userText.getText().toString(), client.getMyName());
                    if (message.getData().length() > 0) {
                        client.sendMessage(message);
                        userText.getText().clear();
                        return true;
                    }
                }
                return false;
            }
        });

        client = new Client();
        client.start("192.168.100.7", 3000, "Message");
    }




    private class Client extends AbstractClient {
        @Override
        protected void handleIncomingMessage(MessageInterface message) {
            Message m = (Message) message;
            switch (m.getType()){
                case "CHAT":
                    showMessage(m.toString());
                    break;
            }
        }

        @Override
        protected void showMessage(final String s) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.add(s);
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }
}