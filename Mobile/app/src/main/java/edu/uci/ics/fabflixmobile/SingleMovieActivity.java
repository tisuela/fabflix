package edu.uci.ics.fabflixmobile;

import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SingleMovieActivity extends AppCompatActivity {
    private final List<Star> stars = new ArrayList<>();
    private final Movie movie = new Movie();
    private SingleMovieAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.singlemovie);

        adapter = new SingleMovieAdapter(stars);
        RecyclerView starListView = findViewById(R.id.star_list);
        starListView.setAdapter(adapter);
        starListView.setLayoutManager(new LinearLayoutManager(this));

        this.getStars(stars, "tt0468569");

    }


    private void setHeader(){
        TextView movieTitle = findViewById(R.id.single_movie_title);
        movieTitle.setText(movie.getTitle());
    }



    // --- API calls & JSON parsing --- //

    // calls API for movies and inserts into ArrayList
    public void getStars(List<Star> stars, String movieId) {

        stars.clear();

        // Use the same network queue across our application
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;

        String params = String.format("single-movie?id=%s", movieId);

        //request type is POST
        final StringRequest starsRequest = new StringRequest(Request.Method.GET, Constants.localUrl + params, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("SingleMovie.success", response);

                try {
                    JSONObject responseJson = new JSONObject(response);
                    parseMovieJson(responseJson);
                    parseStarsJson(responseJson, stars);

                    // now that we have the data, set the header
                    setHeader();

                    // update adapter once this finishes
                    adapter.notifyDataSetChanged();

                } catch (JSONException e){
                    Log.d("SingleMovie.error", e.toString());
                }
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("SingleMovie.error", error.toString());
                    }
                }) {
        };

        // !important: queue.add is where the login request is actually sent
        queue.add(starsRequest);
    }


    // get information about this Movie
    public void parseMovieJson(JSONObject movieJson) throws JSONException {
        String id = movieJson.getString("movie_id");
        String title = movieJson.getString("movie_name");
        String year = movieJson.getString("movie_year");
        String director = movieJson.getString("movie_director");

        movie.setId(id);
        movie.setTitle(title);
        movie.setYear((short) Integer.parseInt(year));
        movie.setDirector(director);

        // get genres
        JSONArray genresArray = movieJson.getJSONArray("movie_genres");
        for (int g = 0; g < genresArray.length(); ++g){
            JSONObject genreJson = genresArray.getJSONObject(g);
            String genreName = genreJson.getString("genre_name");
            movie.addGenre(genreName);
        }
    }

    // inserts starsJson into array list of stars
    public void parseStarsJson(JSONObject movieJson, List<Star> stars) throws JSONException {
        // moviesJSON = {"movies": JSONArray of movies, "resultCount": int count}
        JSONArray starsArray = movieJson.getJSONArray("movie_stars");

        // iterate over movies
        for (int i = 0; i < starsArray.length(); ++i){
            /* movieJSON = {"movie_title": String, "movie_year": String, "movie_director": String,
             * "movie_genres": JSONArray of JSONObjects, "movie_stars": JSONArray of JSONObjects}
             */
            JSONObject starJson = starsArray.getJSONObject(i);

            // Extract JSON data
            String id = starJson.getString("star_id");
            String name = starJson.getString("star_name");
            String year = starJson.getString("star_dob");

            Star star = new Star(id, name, year);

            // Insert into movie object and array
            stars.add(star);
        }

    }

}
