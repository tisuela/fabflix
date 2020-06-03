
/* A servlet to display the contents of the MySQL movieDB database */

import utilities.MyUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@WebServlet(name = "FormReCaptcha", urlPatterns = "/form-recaptcha")
public class FormRecaptcha extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public String getServletInfo() {
        return "Servlet connects to MySQL database and displays result of a SELECT";
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        PrintWriter out = response.getWriter();

        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
        System.out.println("gRecaptchaResponse=" + gRecaptchaResponse);

        // Verify reCAPTCHA
        try {
            RecaptchaVerifyUtils.verify(gRecaptchaResponse);
        } catch (Exception e) {
            out.println("<html>");
            out.println("<head><title>Error</title></head>");
            out.println("<body>");
            out.println("<p>recaptcha verification error</p>");
            out.println("<p>" + e.getMessage() + "</p>");
            out.println("</body>");
            out.println("</html>");
            
            out.close();
            return;
        }



        response.setContentType("text/html"); // Response mime type

        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();

            // Create a new connection to database
            Connection dbCon = MyUtils.getReadConnection();

            // Declare a new statement
            Statement statement = dbCon.createStatement();

            // Retrieve parameter "name" from request, which refers to the value of <input name="name"> in index.html
            String name = request.getParameter("name");

            // Generate a SQL query
            String query = String.format("SELECT * from stars where name like '%s'", name);

            // Perform the query
            ResultSet rs = statement.executeQuery(query);
            
            // building page head with title
            out.println("<html><head><title>MovieDB: Found Records</title></head>");

            // building page body
            out.println("<body><h1>MovieDB: Found Records</h1>");

            // Create a html <table>
            out.println("<table border>");

            // Iterate through each row of rs and create a table row <tr>
            out.println("<tr><td>ID</td><td>Name</td></tr>");
            while (rs.next()) {
                String m_ID = rs.getString("ID");
                String m_Name = rs.getString("name");
                out.println(String.format("<tr><td>%s</td><td>%s</td></tr>", m_ID, m_Name));
            }
            out.println("</table>");
            
            out.println("</body></html>");


            // Close all structures
            rs.close();
            statement.close();
            dbCon.close();

        } catch (Exception e) {
            out.println("<html>");
            out.println("<head><title>Error</title></head>");
            out.println("<body>");
            out.println("<p>error:</p>");
            out.println("<p>" + e.getMessage() + "</p>");
            out.println("</body>");
            out.println("</html>");
            
            out.close();
            return;
        }
        out.close();
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        doGet(request, response);
    }
}
