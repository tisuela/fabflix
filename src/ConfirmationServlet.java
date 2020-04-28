import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;

// Declaring a WebServlet called CartServlet, which maps to url "/api/confirmation"
@WebServlet(name = "ConfirmationServlet", urlPatterns = "/api/confirmation")
public class ConfirmationServlet  extends HttpServlet {
    // important, just google this if u don't get it
    private static final long serialVersionUID = 2L;

    // mySql resource
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;


    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

       try{

       } catch (Exception e){

       }



    }

}
