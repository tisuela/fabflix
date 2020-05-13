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
import java.util.ArrayList;


@WebServlet(name = "GenreServlet", urlPatterns = "/api/genres")
public class GenreServlet extends HttpServlet{

    // Create a dataSource which registered in web.xml
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        try {
            // Get a connection from dataSource
            Connection dbcon = dataSource.getConnection();

            // Build query
            MyQuery query = new MyQuery(dbcon,"SELECT name FROM genres ORDER BY name ASC");

            // Perform the query
            ResultSet rs = query.execute();

            JsonArray genres = new JsonArray();

            while(rs.next()){
                JsonObject genreEntry = new JsonObject();
                String genreName = rs.getString("name");
                genreEntry.addProperty("name", genreName);
                genres.add(genreEntry);
            }

            JsonObject result = new JsonObject();
            result.add("genres", genres);

            // write JSON string to output
            out.write(result.toString());
            // set response status to 200 (OK)
            response.setStatus(200);

            // free resources
            query.close();
            dbcon.close();

            // result structure
            // { genres : array of GenreEntry }
            // genreEntry structure
            // { name: String genre name }

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
    }
}
