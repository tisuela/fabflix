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


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        JsonObject responseJsonObject = new JsonObject();
        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
        System.out.println("gRecaptchaResponse=" + gRecaptchaResponse);

        // Verify reCAPTCHA
        try {
            RecaptchaVerifyUtils.verify(gRecaptchaResponse);
        } catch (Exception e) {
            String failedReCaptcha = "reCaptcha failed. Please try again";

            responseJsonObject.addProperty("status", "fail");
            responseJsonObject.addProperty("message", failedReCaptcha);
            response.getWriter().write(responseJsonObject.toString());

            // return now to prevent API abuse
            return;
        }

        // Verify username password
        VerifyPassword verifier = new VerifyPassword();
        try {
            if (verifier.verifyCredentials(username, password, "customers")){
                // create user
                request.getSession().setAttribute("user", new User(verifier.getName(), verifier.getId()));
                request.getSession().setAttribute("role", "customer");

                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", "successful login");
            }
            else{
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
