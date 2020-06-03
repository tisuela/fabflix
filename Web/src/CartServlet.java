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


// Declaring a WebServlet called CartServlet, which maps to url "/api/cart"
@WebServlet(name = "CartServlet", urlPatterns = "/api/cart")
public class CartServlet extends HttpServlet {
    // important, just google this if u don't get it
    private static final long serialVersionUID = 2L;


    private int totalPrice = 0;


    // Check and execute cart actions from URL parameter
    private void checkCartActions(String action, User user, String movieId, JsonObject jsonObject){
        if (action != null){
            if (action.equals("add_to_cart")){
                user.addToCart(movieId);
            }
            else if (action.equals("decrease_from_cart")){
                user.decreaseQuantity(movieId);
            }
            else if (action.equals("remove_from_cart")){
                user.removeFromCart(movieId);
            }
            // record the edited movieId
            jsonObject.addProperty("cartMovieId", movieId);
        }
    }



    // Gets cart information from session
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        this.totalPrice = 0;

        try{
            JsonObject jsonObject = new JsonObject();
            JsonArray moviesArray = new JsonArray();
            Connection dbcon = MyUtils.getReadConnection();

            // Get user object, since user object stores the cart
            User user = (User)request.getSession().getAttribute("user");

           // Check parameters for action to be done
            String action = request.getParameter("action");
            String actionMovieId = request.getParameter("id");
            this.checkCartActions(action, user, actionMovieId, jsonObject);

            // Get cart
            HashMap<String, Integer> cart = new HashMap<String, Integer>(user.getCart());
            for(String id: cart.keySet()){
                // Get set
                MyQuery query = new MyQuery(dbcon, "SELECT * FROM movies");
                query.addWhereConditions("%s = ?", "id", id);
                ResultSet movieSet = query.execute();

                // Get the one result
                movieSet.first();
                String title = movieSet.getString("title");
                String price = "5";
                String quantity = cart.get(id).toString();

                // increment price
                int priceInt = Integer.parseInt(price);
                this.totalPrice += priceInt * cart.get(id);

                // Put it in JSON
                JsonObject movieJson = new JsonObject();
                movieJson.addProperty("id", id);
                movieJson.addProperty("title", title);
                movieJson.addProperty("price", price);
                movieJson.addProperty("quantity", quantity);

                moviesArray.add(movieJson);
                query.close();
            }
            jsonObject.add("movies", moviesArray);
            jsonObject.addProperty("totalPrice", this.totalPrice);

            user.setTotalPrice(this.totalPrice);

            jsonObject.addProperty("errorMessage","success");
            out.write(jsonObject.toString());
            response.setStatus(200);

            dbcon.close();
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
