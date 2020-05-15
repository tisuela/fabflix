import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import utilities.MyQuery;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;


// Declaring a WebServlet called AutocompeleteServlet, with api url api/autocomplete
@WebServlet(name = "AutocompleteServlet", urlPatterns = "/api/autocomplete")
public class AutocompleteServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.xml
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */

    private boolean notEmpty(String s){
        return (s != null && !s.equals(""));
    }

    // Build query for MYSQL from request parameters
    private MyQuery buildQuery(HttpServletRequest request, Connection dbcon){
        MyQuery query = new MyQuery(dbcon);
        query.addSelectStr("title, year, director, movies_with_rating.id, rating");

        // add FROM conditions
        query.addFromTables("movies_with_rating");

        // Get title
        String title = request.getParameter("title");

        // Check what type of search this is
        // query.addSelectStr();  // should be in select statement (later problem lol)
        query.addWhereConditions("MATCH (%s) AGAINST (? IN BOOLEAN MODE)", "title", title + "*");
        query.append("LIMIT 15");

        return query;
    }


    /* run the request for autocomplete
     * JSON object structure:
     *  [
     *      {"value": "Movie Title", "data": {"movie_id": "Movie ID", "movie_year": "Movie Year",
     *                                          "movie_director": "Movie Director",
     *                                          "movie_rating": "Movie Rating"}
     *      },
     *
     *      {"value": "Another Movie Title", "data": {"movie_id": "Another Movie ID",
     *                                          "movie_year": " Another Movie Year",
     *                                          "movie_director": "Another Movie Director",
     *                                          "movie_rating": "Another Movie Rating"}
     *      }
     *  ]
     */
    private JsonObject doAutoComplete(Connection dbcon, ResultSet rs) throws SQLException {

        // JSON Array with all movie entries
        JsonArray moviesJSON = new JsonArray();

        // Iterate through each row of rs
        while (rs.next()) {

            String movie_id = rs.getString("id");
            String movie_title = rs.getString("title");
            String movie_year = rs.getString("year");
            String movie_director = rs.getString("director");
            String movie_rating = rs.getString("rating");

            // movieEntry to be returned
            JsonObject jsonMovie = new JsonObject();

            // Additional data about movie
            JsonObject jsonMovieData = new JsonObject();

            // Assemble the movie data JSON object
            jsonMovieData.addProperty("movie_id", movie_id);
            jsonMovieData.addProperty("movie_year", movie_year);
            jsonMovieData.addProperty("movie_director", movie_director);
            jsonMovieData.addProperty("movie_rating", movie_rating);

            // Assemble the final JSOON object
            jsonMovie.addProperty("value", movie_title);
            jsonMovie.add("data", jsonMovieData);
            moviesJSON.add(jsonMovie);
        }

        JsonObject resultSet = new JsonObject();
        resultSet.add("movies", moviesJSON);

        return resultSet;
    }


    // Do get request
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        try {
            // --- Query execution --- //

            // Get a connection from dataSource
            Connection dbcon = dataSource.getConnection();

            // Build query
            MyQuery query = buildQuery(request, dbcon);

            // Perform the query
            ResultSet rs = query.execute();
            System.out.println("autocomplete servlet statement = " + query.getStatement());


            // --- Writing to JSON --- //

            // write JSON string to output
            JsonObject resultSet =  doAutoComplete(dbcon, rs);
            out.write(resultSet.toString());

            // set response status to 200 (OK)
            response.setStatus(200);

            // free resources
            query.close();
            dbcon.close();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("error");
            // write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // set reponse status to 500 (Internal Server Error)
            response.setStatus(500);

        }
        out.close();

    }
}
