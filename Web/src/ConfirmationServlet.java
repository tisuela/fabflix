import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import utilities.MyQuery;
import utilities.MyUtils;
import utilities.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashMap;

// Declaring a WebServlet called CartServlet, which maps to url "/api/confirmation"
@WebServlet(name = "ConfirmationServlet", urlPatterns = "/api/confirmation")
public class ConfirmationServlet  extends HttpServlet {
    // important, just google this if u don't get it
    private static final long serialVersionUID = 2L;




    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

       try{
           JsonObject jsonObject = new JsonObject();
           JsonArray moviesArray = new JsonArray();
           User user = (User) request.getSession().getAttribute("user");
           Connection dbcon = MyUtils.getConnection();

           String transactionId = request.getParameter("transaction_id");


           // Get cart
           HashMap<String, Integer> cart = new HashMap<String, Integer>(user.getCart());
           for(String id: cart.keySet()){
               // Get set
               MyQuery query = new MyQuery(dbcon, "SELECT * FROM movies JOIN sales ON movies.id = sales.movieId JOIN transactions ON saleId = sales.id");
               query.addWhereConditions("%s = ?", "movies.id", id);
               query.addWhereConditions("%s = ?", "transactionId", transactionId);
               ResultSet movieSet = query.execute();

               // Get the one result
               movieSet.first();
               String title = movieSet.getString("title");
               String price = "5";
               String quantity = cart.get(id).toString();
               String sailId =  String.valueOf(movieSet.getInt("sales.id"));

               // increment price
               int priceInt = Integer.parseInt(price);

               // Put it in JSON
               JsonObject movieJson = new JsonObject();
               movieJson.addProperty("id", id); // movieId
               movieJson.addProperty("sailId", sailId);
               movieJson.addProperty("title", title);
               movieJson.addProperty("price", price);
               movieJson.addProperty("quantity", quantity);

               moviesArray.add(movieJson);
               query.close();
           }
           jsonObject.add("movies", moviesArray);

           String totalPrice = String.valueOf(user.getTotalPrice());

           jsonObject.addProperty("totalPrice", totalPrice);

           user.emptyCart();
           user.setTotalPrice(0);
           jsonObject.addProperty("errorMessage","success");

           dbcon.close();
           out.write(jsonObject.toString());
           response.setStatus(200);
       } catch (Exception e){
           e.printStackTrace();

           // write error message JSON object to output
           JsonObject jsonObject = new JsonObject();
           jsonObject.addProperty("errorMessage", e.getMessage());
           out.write(jsonObject.toString());

           // set reponse status to 500 (Internal Server Error)
           response.setStatus(500);
       }



    }

}
