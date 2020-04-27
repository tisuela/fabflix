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


// Declaring a WebServlet called CartServlet, which maps to url "/api/cart"
@WebServlet(name = "CartServlet", urlPatterns = "/api/cart")
public class CartServlet extends HttpServlet {
    // important, just google this if u don't get it
    private static final long serialVersionUID = 2L;

    // mySql resource
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    // Gets cart information from session
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        try{
            JsonObject jsonObject = new JsonObject();

            // Get user object, since user object stores the cart
            User user = (User)request.getSession().getAttribute("user");


            jsonObject.addProperty("errorMessage","no errors :D");

            System.out.println(jsonObject.toString());
            out.write(jsonObject.toString());
            response.setStatus(200);
        }
        catch(Exception e){
            e.printStackTrace();

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
