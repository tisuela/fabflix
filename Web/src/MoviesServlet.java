import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import utilities.MyQuery;
import utilities.User;

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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;



// Declaring a WebServlet called MoviesServlet, which maps to url "/api/movies"
@WebServlet(name = "MoviesServlet", urlPatterns = "/api/movies")
public class MoviesServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // to determine what kind of search this is
    private boolean isFulltext = false;

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

        // have to join other tables to search for stars and genre
        query.addFromTables("JOIN (stars JOIN stars_in_movies ON id = starId) ON movies_with_rating.id = stars_in_movies.movieId");
        query.addFromTables("JOIN (genres JOIN genres_in_movies ON id = genreId) ON movies_with_rating.id = genres_in_movies.movieId");

        // NOT FULLY IMPLEMENTED: saving query under the user
        User user = (User) request.getSession().getAttribute("user");

        // Get title
        String title = request.getParameter("title");

        // Check what type of search this is
        if (isFulltext && this.notEmpty(title)){

            String[] keywords = title.split(" ");
            String keywordQuery = "";
            for(String keyword: keywords){
                keywordQuery += "+" + keyword + "*" + " ";
            }

            query.addWhereConditions("MATCH (%s) AGAINST (? IN BOOLEAN MODE)", "title", keywordQuery);
        }
        else {
            query.addParameters(request.getParameterMap());
        }
        user.setSavedQueryParameters(request.getParameterMap());

        // Add the WHERE conditions from parameters

        // This code block was adapted from:
        // https://www.java4s.com/java-servlet-tutorials/example-on-getparametermap-method-of-servlet-request-object/
        Map m = request.getParameterMap();
        Set s = m.entrySet();
        Iterator it = s.iterator();

        String url = "";

        while(it.hasNext()){
            Map.Entry<String,String[]> entry = (Map.Entry<String,String[]>)it.next();
            String parameter = "";
            String   key   = entry.getKey();
            String[] value = entry.getValue();
            parameter += key + "=" + value[0] + "&";
            url += parameter;
        }
        // end code block


        request.getSession().setAttribute("movieState", "index.html?" + url.substring(0, url.length() - 1));


        return query;

    }


    // run the request for index.html
    // {"movies": Json array of movies, "resultCount": int count}
    private JsonObject doIndexPage(Connection dbcon, ResultSet rs) throws SQLException {
        int resultSetCount = 0;

        // JSON Array with all movie entries
        JsonArray moviesJSON = new JsonArray();

        // Iterate through each row of rs
        while (rs.next()) {
            resultSetCount++;
            String movie_id = rs.getString("id");
            String movie_title = rs.getString("title");
            String movie_year = rs.getString("year");
            String movie_director = rs.getString("director");
            String movie_rating = rs.getString("rating");

            // movieEntry to be returned
            JsonObject jsonMovie = new JsonObject();

            // Movie stars and genres will be stored as arrays
            JsonArray jsonStars = new JsonArray();
            JsonArray jsonGenres = new JsonArray();

            // additional queries for genres and stars
            try {
                // Get list of first three genres
                MyQuery genreQuery = new MyQuery(dbcon);
                genreQuery.addSelectStr("genres.name");
                genreQuery.addFromTables("genres JOIN genres_in_movies ON (genres.id = genreId AND movieId = ?)", movie_id);
                genreQuery.append("ORDER BY genres.name LIMIT ?", "3", "int");
                ResultSet genreSet = genreQuery.execute();

                // get list of first three stars

                // Build Stars query
                MyQuery starsQuery = new MyQuery(dbcon, "SELECT *, COUNT(*) as totalMovies");

                // first get the stars in the movie
                starsQuery.addFromTables("stars JOIN stars_in_movies as in_movie ON (stars.id = starId and movieId = ?)", movie_id);

                // Join again with allstars to get all the movies the stars in THIS MOVIE starred in
                starsQuery.addFromTables("JOIN stars_in_movies as all_stars ON (in_movie.starId = all_stars.starId)");
                starsQuery.append("GROUP BY all_stars.starId ORDER BY totalMovies DESC, stars.name ASC LIMIT ?", 3);
                ResultSet starsSet = starsQuery.execute();

                // assemble genre list (as a JSON object)
                while (genreSet.next()) {
                    JsonObject jsonGenre = new JsonObject();
                    String genre_name = genreSet.getString("name");
                    jsonGenre.addProperty("genre_name", genre_name);

                    // add to the JSON array of stars
                    jsonGenres.add(jsonGenre);
                }

                // stars list (as JSON object)
                while (starsSet.next()) {
                    JsonObject jsonStar = new JsonObject();
                    String star_id = starsSet.getString("starId");
                    String star_name = starsSet.getString("name");
                    jsonStar.addProperty("star_id", star_id);
                    jsonStar.addProperty("star_name", star_name);

                    // add to the JSON array of stars
                    jsonStars.add(jsonStar);
                }

                // free resources
                genreQuery.close();
                starsQuery.close();


            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            jsonMovie.add("movie_genres", jsonGenres);
            jsonMovie.add("movie_stars", jsonStars);


            // Create a JsonObject based on the data we retrieve from rs
            jsonMovie.addProperty("movie_id", movie_id);
            jsonMovie.addProperty("movie_title", movie_title);
            jsonMovie.addProperty("movie_year", movie_year);
            jsonMovie.addProperty("movie_director", movie_director);
            jsonMovie.addProperty("movie_rating", movie_rating);
            moviesJSON.add(jsonMovie);
        }

        // {"movies": Json array of movies, "resultCount": int count}

        JsonObject resultSet = new JsonObject();
        resultSet.add("movies", moviesJSON);
        resultSet.addProperty("resultCount", resultSetCount);
        return resultSet;
    }


    // Do get request
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        try {
            // Check parameters for the type of search this is
            String fulltext = request.getParameter("fulltext");
            if (this.notEmpty(fulltext) && fulltext.equals("true")){
                isFulltext = true;
            }


            // --- Query execution --- //

            // Get a connection from dataSource
            Connection dbcon = dataSource.getConnection();

            // Build query
            MyQuery query = buildQuery(request, dbcon);

            // Perform the query
            ResultSet rs = query.execute();
            System.out.println("movie servlet statement = " + query.getStatement());


            // --- Writing to JSON --- //

            // write JSON string to output
            JsonObject resultSet =  doIndexPage(dbcon, rs);
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
