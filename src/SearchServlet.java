import com.google.gson.JsonObject;

import javax.annotation.Resource;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@WebServlet(name = "SearchServlet", urlPatterns = "/api/search")
public class SearchServlet extends HttpServlet {
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("title");
        String password = request.getParameter("year");

        JsonObject responseJsonObject = new JsonObject();
        Connection dbcon = null;
        try {
            dbcon = dataSource.getConnection();

            // Find the matching movies from the moviedb
            Statement findMoviesStatement = dbcon.createStatement();


            // close resources
            findMoviesStatement.close();
            dbcon.close();
            responseJsonObject.addProperty("status", "success");
            responseJsonObject.addProperty("message", "haven't implemented fully lol");
            System.out.println("success with search servlet");

        } catch (Exception e) {
            e.printStackTrace();
            responseJsonObject.addProperty("status", "fail");
            responseJsonObject.addProperty("message", "Database error");
        }





        response.getWriter().write(responseJsonObject.toString());
    }
}
