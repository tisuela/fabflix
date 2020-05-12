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

@WebServlet(name = "DashboardLoginServlet", urlPatterns = "/api/employeeAccess")
public class DashboardLoginServlet extends HttpServlet{

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        System.out.println("login details: " + username + " " + password);

        JsonObject responseJsonObject = new JsonObject();

        // Verify username password
        VerifyPassword verifier = new VerifyPassword();
        try {
            if (verifier.verifyCredentials(username, password, "employees")) {
                System.out.println("Successful employee access attempt");
                // create user
                request.getSession().setAttribute("role", "employee");

                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", "successful employee login");
            } else {
                System.out.println("Successful employee access attempt");
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "Incorrect username and/or password");
            }
        } catch (Exception e) {
            e.printStackTrace();
            responseJsonObject.addProperty("status", "fail");
            responseJsonObject.addProperty("message", "Database error");
        }

        response.getWriter().write(responseJsonObject.toString());
    }
}

