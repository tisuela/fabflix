package edu.uci.ics.fabflixmobile;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ListViewActivity extends Activity {
    private String url = "http://tisuela-tower:8080/fabflix_war/api/";

    // set adapter as class attribute so we can update it later
    private MovieListViewAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listview);

        // Needs to be declared final
        final ArrayList<Movie> movies = new ArrayList<>();
        this.getMovies(movies);

        adapter = new MovieListViewAdapter(movies, this);
        ListView listView = findViewById(R.id.list);
        listView.setAdapter(adapter);

        // Lead user to single-movies using this listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Movie movie = movies.get(position);
                String message = String.format("Clicked on position: %d, title: %s, %d", position, movie.getTitle(), movie.getYear());
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // calls API for movies and inserts into ArrayList
    public void getMovies(ArrayList<Movie> movies) {

        // Use the same network queue across our application
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;

        //request type is POST
        final StringRequest moviesRequest = new StringRequest(Request.Method.GET, url + "movies", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("ListView.success", response);

                try {
                    JSONObject responseJson = new JSONObject(response);
                    parseMoviesJson(responseJson, movies);

                    // update adapter once this finishes
                    adapter.notifyDataSetChanged();

                } catch (JSONException e){
                    Log.d("ListView.error", e.toString());
                }
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("ListView.error", error.toString());
                    }
                }) {
        };

        // !important: queue.add is where the login request is actually sent
        queue.add(moviesRequest);
    }


    // inserts moviesJson into array list of movies
    public void parseMoviesJson(JSONObject moviesJson, ArrayList<Movie> movies) throws JSONException {
        // moviesJSON = {"movies": JSONArray of movies, "resultCount": int count}
        JSONArray moviesArray = moviesJson.getJSONArray("movies");

        // iterate over movies
        for (int i = 0; i < moviesArray.length(); ++i){
            /* movieJSON = {"movie_title": String, "movie_year": String, "movie_director": String,
             * "movie_genres": JSONArray of JSONObjects, "movie_stars": JSONArray of JSONObjects}
             */
            JSONObject movieJson = moviesArray.getJSONObject(i);

            // Extract JSON data
            String id = movieJson.getString("movie_id");
            String title = movieJson.getString("movie_title");
            Short year = (short) Integer.parseInt(movieJson.getString("movie_year"));
            String director = movieJson.getString("movie_director");

            Movie movie = new Movie(id, title, year, director);
            JSONArray genresArray = movieJson.getJSONArray("movie_genres");
            for (int g = 0; g < genresArray.length(); ++g){
                JSONObject genreJson = genresArray.getJSONObject(g);
                String genreName = genreJson.getString("genre_name");
                movie.addGenre(genreName);
            }

            // Insert into movie object and array
            movies.add(movie);
        }

    }


}