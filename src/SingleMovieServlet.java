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
import java.sql.ResultSet;

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

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		response.setContentType("application/json"); // Response mime type

		// Retrieve parameter id from url request.
		String id = request.getParameter("id");

		// Output stream to STDOUT
		PrintWriter out = response.getWriter();

		try {

			// Get a connection from dataSource
			Connection dbcon = dataSource.getConnection();

			String matchId = "%s = ?";

			// Build Stars query
			MyQuery starsQuery = new MyQuery(dbcon,"SELECT *, COUNT(*) as totalMovies");

			// first get the stars in the movie
			starsQuery.addFromTables("stars JOIN stars_in_movies as in_movie ON (stars.id = starId and movieId = ?)", id);

			// Join again with allstars to get all the movies the stars in THIS MOVIE starred in
			starsQuery.addFromTables("JOIN stars_in_movies as all_stars ON (in_movie.starId = all_stars.starId)");
			starsQuery.append("GROUP BY all_stars.starId ORDER BY totalMovies DESC, stars.name ASC");



			// Build movie query
			MyQuery movieQuery = new MyQuery(dbcon);
			movieQuery.setSelectStr("*");
			movieQuery.addFromTables("movies");
			movieQuery.addWhereConditions(matchId, "id", id);

			// Build genre query
			MyQuery genreQuery = new MyQuery(dbcon);
			genreQuery.setSelectStr("*");
			genreQuery.addFromTables("genres JOIN genres_in_movies ON genres.id = genreId");
			genreQuery.addWhereConditions(matchId, "movieId", id);
			genreQuery.append("ORDER BY genres.name ASC");

			// Perform the query
			ResultSet starsSet = starsQuery.execute();
			ResultSet movieSet = movieQuery.execute();
			ResultSet genreSet = genreQuery.execute();
			System.out.println("Stars query = " + starsQuery.getStatement());

			// Create JSON objects and Arrays
			JsonObject jsonObject = new JsonObject(); // final object
			JsonArray jsonStars = new JsonArray();
			JsonArray jsonGenres = new JsonArray();

			// Get movie info
			movieSet.last(); // IMPORTANT! Solved bug
			String movie_name = movieSet.getString("title");
			String movie_year = movieSet.getString("year");
			String movie_director = movieSet.getString("director");
			String movie_id = movieSet.getString("movies.id");
			jsonObject.addProperty("movie_name", movie_name);
			jsonObject.addProperty("movie_year", movie_year);
			jsonObject.addProperty("movie_director", movie_director);
			jsonObject.addProperty("movie_id", movie_id);

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

			String url = httpRequest.getSession().getAttribute("movieState").toString();


			jsonObject.add("movie_stars", jsonStars);
			jsonObject.add("movie_genres", jsonGenres);
			jsonObject.addProperty("movieState", url);
            // write JSON string to output
            out.write(jsonObject.toString());
            // set response status to 200 (OK)
            response.setStatus(200);

			starsQuery.close(); movieQuery.close(); genreQuery.close();

			dbcon.close();
		} catch (Exception e) {
			// write error message JSON object to output
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("errorMessage", e.getMessage());
			out.write(jsonObject.toString());
			e.printStackTrace();

			// set reponse status to 500 (Internal Server Error)
			response.setStatus(500);
		}
		out.close();

	}

}
