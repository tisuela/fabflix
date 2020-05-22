package edu.uci.ics.fabflixmobile;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Login extends ActionBarActivity {

    private EditText username;
    private EditText password;
    private TextView message;
    private Button loginButton;
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // upon creation, inflate and initialize the layout
        setContentView(R.layout.login);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        message = findViewById(R.id.message);
        loginButton = findViewById(R.id.login);
        /**
         * In Android, localhost is the address of the device or the emulator.
         * To connect to your machine, you need to use the below IP address
         * **/
        url = "http://tisuela-tower:8080/fabflix/api/";

        //assign a listener to call a function to handle the user request when clicking a button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });
    }

    public void login() {

        message.setText("Trying to login");
        // Use the same network queue across our application
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        //request type is POST
        final StringRequest loginRequest = new StringRequest(Request.Method.POST, url + "login", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    if (isValidLogin(response)) {

                        Log.d("login.success", response);

                        //initialize the activity(page)/destination
                        Intent listPage = new Intent(Login.this, ListViewActivity.class);
                        //without starting the activity/page, nothing would happen
                        startActivity(listPage);
                    }
                    // bad credentials
                    else {
                        Log.d("login.failed", response);
                        message.setText("Invalid credentials");
                    }
                } catch (JSONException e){
                    // response is not following expected format
                    Log.d("login.json.error", response);
                    message.setText("Database Error");
                }

            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("login.error", error.toString());
                        message.setText("Application Error");
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                // Post request form data
                final Map<String, String> params = new HashMap<>();
                params.put("username", username.getText().toString());
                params.put("password", password.getText().toString());
                params.put("platform", "mobile");

                return params;
            }
        };

        // !important: queue.add is where the login request is actually sent
        queue.add(loginRequest);

    }


    // Checks response to see if login is valid
    private boolean isValidLogin(String response) throws JSONException {
        boolean isValid = false;
        JSONObject loginJson = new JSONObject(response);

        String status = loginJson.getString("status");
        if (status.equals("success")) isValid = true;

        return isValid;
    }

}