import com.google.gson.JsonObject;

import javax.annotation.Resource;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Enumeration;

@WebServlet(name = "DashboardServlet", urlPatterns = "/api/dashboard")
public class DashboardServlet extends HttpServlet {
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    private boolean notEmpty(String s){
        return (s != null && !s.equals(""));
    }

    // check if star already exists
    private boolean starExists(String fullName, Connection dbcon){
        MyQuery query = new MyQuery(dbcon, "SELECT * FROM stars");
        query.append("WHERE stars.name = ?", fullName);
        query.execute();
        return query.exists();
    }


    // add new star to database
    private void addStar(HttpServletRequest request, JsonObject responseJson){
        try{
            String fullName = request.getParameter("fullName");
            String birthYear = request.getParameter("birthYear");

            // Check if full name specified
            if (notEmpty(fullName)) {
                Connection dbcon = dataSource.getConnection();

                // Check if it already exists
                if (this.starExists(fullName, dbcon)) {
                    responseJson.addProperty("status", "fail");
                    responseJson.addProperty("message", "star already exists");
                }
                // else, we're good to add the new star
                else {
                    PreparedStatement callStatement = dbcon.prepareStatement("CALL add_star(?, ?)");
                    callStatement.setString(1, fullName);

                    // year is optional
                    int yearInt = 0;
                    if (notEmpty(birthYear)) {
                        yearInt = Integer.parseInt(birthYear);
                        callStatement.setInt(2, yearInt);
                    }
                    // Set year to null if it is not specified
                    else{
                        callStatement.setNull(2, java.sql.Types.INTEGER);
                    }

                    callStatement.execute();
                    callStatement.close();

                    responseJson.addProperty("status", "success");
                    responseJson.addProperty("message", "successfully added");
                }
            }
            else{
                responseJson.addProperty("status", "fail");
                responseJson.addProperty("message", "Full name not specified");
            }

        }
        catch (Exception e){
            e.printStackTrace();
            responseJson.addProperty("status", "fail");
            responseJson.addProperty("message", "database error");
        }
    }


    // insert respective data to database from the user-submitted forms
    private void insertToDatabase(HttpServletRequest request, JsonObject responseJson){
        String formType = request.getParameter("form");

        // Check which form was submitted
        if (formType.equals("add_star")){
            this.addStar(request, responseJson);
        }
        else if (formType.equals("add_movie")){

        }
        else{
            responseJson.addProperty("status", "fail");
            responseJson.addProperty("message", "no form submitted / invalid form");
        }

    }


    // Post request, from the submitted forms
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        JsonObject responseJsonObject = new JsonObject();

        insertToDatabase(request, responseJsonObject);


        response.getWriter().write(responseJsonObject.toString());
    }


}
