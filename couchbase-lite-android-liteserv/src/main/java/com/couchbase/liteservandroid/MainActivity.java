package com.couchbase.liteservandroid;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Manager;
import com.couchbase.lite.View;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.javascript.JavaScriptViewCompiler;
import com.couchbase.lite.listener.Credentials;
import com.couchbase.lite.listener.LiteListener;
import com.couchbase.lite.listener.LiteServlet;

import java.io.IOException;

public class MainActivity extends Activity {

    private static final int DEFAULT_LISTEN_PORT = 5984;
    private static final String DATABASE_NAME = "cblite-test";
    private static final String LISTEN_PORT_PARAM_NAME = "listen_port";

    private static final String LISTEN_LOGIN_PARAM_NAME = "username";
    private static final String LISTEN_PASSWORD_PARAM_NAME = "password";

    public static String TAG = "LiteServ";
    private Credentials allowedCredentials;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Register the JavaScript view compiler
        View.setCompiler(new JavaScriptViewCompiler());

        try {
            int port = startCBLListener(getListenPort());
            showListenPort(port);
            showListenCredentials();
        } catch (Exception e) {
            TextView listenPortTextView = (TextView)findViewById(R.id.listen_port_textview);
            listenPortTextView.setText(String.format("Error starting LiteServ"));
            Log.e(TAG, "Error starting LiteServ", e);
        }

    }

    private void showListenPort(int listenPort) {
        Log.d(TAG, "listenPort: " + listenPort);
        TextView listenPortTextView = (TextView)findViewById(R.id.listen_port_textview);
        listenPortTextView.setText(String.format("Listening on port: %d.  Db: %s", listenPort, DATABASE_NAME));
    }

    private void showListenCredentials() {
        TextView listenCredentialsTextView = (TextView)findViewById(R.id.listen_credentials_textview);
        String credentialsDisplay = String.format(
                "login: %s password: %s",
                allowedCredentials.getLogin(),
                allowedCredentials.getPassword()
        );
        Log.v(TAG, credentialsDisplay);
        listenCredentialsTextView.setText(credentialsDisplay);

    }

    private int startCBLListener(int suggestedListenPort) throws IOException, CouchbaseLiteException {

        Manager manager = startCBLite();
        startDatabase(manager, DATABASE_NAME);


        if (getLogin()!=null && getPassword()!=null){
            allowedCredentials = new Credentials(getLogin(), getPassword());
        } else{
            allowedCredentials = new Credentials();
        }

        LiteListener listener = new LiteListener(manager, suggestedListenPort, allowedCredentials);

        int port = listener.getListenPort();
        Thread thread = new Thread(listener);
        thread.start();

        return port;

    }

    protected Manager startCBLite() throws IOException {
        Manager manager;
        manager = new Manager(new AndroidContext(this), Manager.DEFAULT_OPTIONS);
        return manager;
    }

    protected void startDatabase(Manager manager, String databaseName) throws CouchbaseLiteException {
        Database database = manager.getDatabase(databaseName);
        database.open();
    }

    private int getListenPort() {
        return getIntent().getIntExtra(LISTEN_PORT_PARAM_NAME, DEFAULT_LISTEN_PORT);
    }

    private String getLogin() {
        return getIntent().getStringExtra(LISTEN_LOGIN_PARAM_NAME);
    }

    private String getPassword() {
        return getIntent().getStringExtra(LISTEN_PASSWORD_PARAM_NAME);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
