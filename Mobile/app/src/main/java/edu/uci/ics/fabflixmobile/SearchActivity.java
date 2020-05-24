package edu.uci.ics.fabflixmobile;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
// import android.support.v7.app.ActionBarActivity;
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


public class SearchActivity extends AppCompatActivity{

    private EditText searchInput;
    private Button searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // initialize/inflate the layout
        setContentView(R.layout.search);

        searchInput  = findViewById(R.id.searchBox);
        searchButton = findViewById(R.id.searchButton);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (searchInput.getText().toString().length() > 0) {
                    search();
                }
            }
        });
    }

    public void search() {
        Bundle bundle = new Bundle();
        bundle.putString("searchInput", searchInput.getText().toString());
        Intent intent = new Intent(this, ListViewActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }
}
