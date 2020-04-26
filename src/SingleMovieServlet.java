import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

// Declaring a WebServlet called SingleStarServlet, which maps to url "/api/single-movie"
@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet {
	private static final long serialVersionUID = 2L;

	// Create a dataSource which registered in web.xml
	@Resource(name = "jdbc/moviedb")
	private DataSource dataSource;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("application/json"); // Response mime type

		// Retrieve parameter id from url request.
		String id = request.getParameter("id");

		// Output stream to STDOUT
		PrintWriter out = response.getWriter();

		try {

			// Get a connection from dataSource
			Connection dbcon = dataSource.getConnection();

			String matchId = "%s = \"%s\"";

			BuildQuery starsQuery = new BuildQuery("SELECT *, COUNT(*) as totalMovies");

			// first get the stars in the movie
			starsQuery.addFromTables(String.format("stars JOIN stars_in_movies as in_movie ON (stars.id = starId and movieId = \"%s\")", id));

			// Join again with allstars to get all the movies the stars in THIS MOVIE starred in
			starsQuery.addFromTables("JOIN stars_in_movies as all_stars ON (in_movie.starId = all_stars.starId)");
			starsQuery.append("GROUP BY all_stars.starId ORDER BY totalMovies DESC, stars.name ASC");

			// Construct a query with parameter represented by "?"
			String movieQuery = "SELECT * FROM movies WHERE id = ?";
			String genreQuery = "SELECT genres.name FROM genres JOIN genres_in_movies ON genres.id = genreId WHERE movieId = ? ORDER BY name ASC";

			System.out.println("collecting single movie info");

			// Declare our statements

			PreparedStatement movieStatement = dbcon.prepareStatement(movieQuery);
			PreparedStatement genresStatement = dbcon.prepareStatement(genreQuery);

			System.out.println("setting String");
			// Set the parameter represented by "?" in the query to the id we get from url,
			// num 1 indicates the first "?" in the query

			movieStatement.setString(1,id);
			genresStatement.setString(1, id);

			// Perform the query
			System.out.println("Perform Query");
			System.out.println(starsQuery.getQuery());
			ExecuteQuery starExecute = new ExecuteQuery(dbcon, starsQuery.getQuery());
			ResultSet starsSet = starExecute.execute();
			ResultSet movieSet = movieStatement.executeQuery();
			ResultSet genreSet = genresStatement.executeQuery();

			System.out.println("create json objects");
			// Create JSON objects and Arrays
			JsonObject jsonObject = new JsonObject(); // final object
			JsonArray jsonStars = new JsonArray();
			JsonArray jsonGenres = new JsonArray();

			// Get movie info
			movieSet.last(); // IMPORTANT! Solved bug
			String movie_name = movieSet.getString("title");
			String movie_year = movieSet.getString("year");
			String movie_director = movieSet.getString("director");
			jsonObject.addProperty("movie_name", movie_name);
			jsonObject.addProperty("movie_year", movie_year);
			jsonObject.addProperty("movie_director", movie_director);

			System.out.println("Getting Stars Info");
			// Iterate through each row of stars
			while (starsSet.next()) {
				// Create a JsonObject based on the data we retrieve from starsSet
				JsonObject jsonStar = new JsonObject();

				String starId = starsSet.getString("starId");
				String starName = starsSet.getString("name");
				String starDob = starsSet.getString("birthYear");


				jsonStar.addProperty("star_id", starId);
				jsonStar.addProperty("star_name", starName);
				jsonStar.addProperty("star_dob", starDob);

				// add JSON to the array of stars
				jsonStars.add(jsonStar);
			}

			// Iterate through each row of genres
			while (genreSet.next()) {
				JsonObject jsonGenre = new JsonObject();

				String genre_name = genreSet.getString("name");
				jsonGenre.addProperty("genre_name", genre_name);

				jsonGenres.add(jsonGenre);
			}




			jsonObject.add("movie_stars", jsonStars);
			jsonObject.add("movie_genres", jsonGenres);
            // write JSON string to output
            out.write(jsonObject.toString());
            // set response status to 200 (OK)
            response.setStatus(200);

			starExecute.close();
			movieSet.close();
			genreSet.close();
			movieStatement.close();
			genresStatement.close();
			dbcon.close();
		} catch (Exception e) {
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
