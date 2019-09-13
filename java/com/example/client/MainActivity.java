package com.example.client;

import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.app.Activity;
import android.os.StrictMode;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
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

        userText.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (keyboardShown(userText.getRootView())) {
                    if (adapter.getCount()<=8) {
                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                    } else {
                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
                    }
                }
            }
        });

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
        client.start("192.168.100.7", 3000, "com.example.client.Message");
    }

    private boolean isLastVisible() {
        if (adapter.getCount()==0)
            return false;
        else
            return getViewByPosition(adapter.getCount()-1, listView).isShown();
    }

    private boolean keyboardShown(View rootView) {

        final int softKeyboardHeight = 100;
        Rect r = new Rect();
        rootView.getWindowVisibleDisplayFrame(r);
        DisplayMetrics dm = rootView.getResources().getDisplayMetrics();
        int heightDiff = rootView.getBottom() - r.bottom;

        return heightDiff > softKeyboardHeight * dm.density;
    }


    private class Client extends AbstractClient {
        @Override
        protected void handleIncomingMessage(MessageInterface message) {
            Message m = (Message) message;
            switch (m.getType()){
                case "CHAT":
                    showMessage(m.toString());
                    break;
                case "TYPING":

            }
        }

        @Override
        protected void showMessage(final String s) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.add(s);
                    listView.setSelection(adapter.getCount() - 1);
                }
            });
        }
    }


    public View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }


    private void changegbColor(int color){
        View view = findViewById(R.id.editText);
        view.getRootView().setBackgroundColor(color);
    }

}