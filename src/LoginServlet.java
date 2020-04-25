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
import java.sql.SQLException;
import java.sql.Statement;

@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        JsonObject responseJsonObject = new JsonObject();
        Connection dbcon = null;
        try {
            dbcon = dataSource.getConnection();

            // Find the matching customer from the moviedb
            Statement findUserStatement = dbcon.createStatement();
            String findUserQuery = String.format("SELECT email,password FROM customers WHERE email = \"%s\"", username);
            ResultSet userSet = findUserStatement.executeQuery(findUserQuery);

            String failureMessage = "Failed login attempt. Please verify your username and password";
            // If user exists (by user ResultSet.isBeforeFirst()), then check password
            if (userSet.isBeforeFirst()) {
                // get first result
                userSet.first();

                // Get password of user found in database
                String userPassword = userSet.getString("password");

                // If password is right, proceed to login
                if(password.equals(userPassword)){
                    request.getSession().setAttribute("user", new User(username));
                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", "success");
                }
                // Send error message for incorrect password
                else{
                    responseJsonObject.addProperty("status", "fail");
                    responseJsonObject.addProperty("message", failureMessage);
                }

            }
            else{
                responseJsonObject.addProperty("status", "fail");
                // Giving users information such as their username is correct but their password is a security risk
                // so failure messages should be consistent with each other, only to let users know that their login attempt failed.
                responseJsonObject.addProperty("message", failureMessage);
            }

            // close resources
            userSet.close();
            findUserStatement.close();
            dbcon.close();

        } catch (Exception e) {
            e.printStackTrace();
            responseJsonObject.addProperty("status", "fail");
            responseJsonObject.addProperty("message", "Database error");
        }





        response.getWriter().write(responseJsonObject.toString());
    }
}
