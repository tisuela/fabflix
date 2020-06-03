import com.google.gson.JsonObject;
import utilities.MyQuery;
import utilities.MyUtils;
import utilities.User;


import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;

@WebServlet(name = "PaymentServlet", urlPatterns = "/api/payment")
public class PaymentServlet  extends HttpServlet {
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */


    // Checks if payment is valid and puts info into json object
    private boolean isValidPayment(HttpServletRequest request, JsonObject responseJsonObject){
        try {
            // get values
            String firstName = request.getParameter("firstName");
            String lastName = request.getParameter("lastName");
            String creditCardNumber = request.getParameter("creditCardNumber");
            String expirationDate = request.getParameter("expirationDate");

            Connection dbcon = MyUtils.getReadConnection();

            String equalsStr = "%1$s = ?";

            MyQuery cardQuery = new MyQuery(dbcon);
            cardQuery.addSelectStr("*");
            cardQuery.addFromTables("creditcards");
            cardQuery.addWhereConditions(equalsStr, "creditcards.id", creditCardNumber);
            cardQuery.addWhereConditions(equalsStr, "creditcards.expiration", expirationDate);

            System.out.println("Payment query = " + cardQuery.getQuery());
            System.out.println("Payment query where = " + String.format(equalsStr, creditCardNumber, expirationDate));

            ResultSet cardSet = cardQuery.execute();

            User user = (User) request.getSession().getAttribute("user");

            // Check if there is no matching card and that there are items in cart
            if (!cardSet.isBeforeFirst() || !MyUtils.notEmpty(creditCardNumber) || !MyUtils.notEmpty(expirationDate)){
                responseJsonObject.addProperty("message", "Invalid Payment Information");
                return false;
            }
            else if (user.notEmpty()){
                responseJsonObject.addProperty("message","Payment is Valid");
            }
            else{
                // cart is empty
                responseJsonObject.addProperty("message", "No items in cart -- please add items then proceed to payment");
                return false;
            }

            cardQuery.close();
            dbcon.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            responseJsonObject.addProperty("message", "Database error");

            return false;
        }
    }


    // does payment by inserting balues into moviedb.sales
    private void doPayment(HttpServletRequest request, JsonObject responseJsonObject){
        try{
            String firstName = request.getParameter("firstName");
            String lastName = request.getParameter("lastName");
            String creditCardNumber = request.getParameter("creditCardNumber");
            String expirationDate = request.getParameter("expirationDate");


            Connection dbcon = MyUtils.getWriteConnection();

            Date date = new Date();
            java.sql.Date sqlDate = new java.sql.Date(date.getTime());

            User user = (User)request.getSession().getAttribute("user");

            HashMap<String, Integer> cart = user.getCart();

            MyQuery maxIdQuery = new MyQuery(dbcon, "SELECT max(id) as maxId FROM sales");
            ResultSet rs = maxIdQuery.execute();
            int transactionId = 0;

            // set transaction ID by getting max sales ID
            if (rs.isBeforeFirst()){
                rs.first();
                transactionId = rs.getInt("maxId") + 1;
            }

            // make payment for all movies
            for(String movieId: cart.keySet()){
                // records sales & transaction
                PreparedStatement ps = dbcon.prepareStatement("CALL add_transaction(?, ?, ?, ?, ?)");
                ps.setInt(1, user.getId());
                ps.setString(2, movieId);
                ps.setDate(3, sqlDate);
                ps.setInt(4, cart.get(movieId));
                ps.setInt(5, transactionId);
                ps.executeUpdate();
                ps.close();
            }

            responseJsonObject.addProperty("transactionId", transactionId);
            maxIdQuery.close();
            dbcon.close();

        } catch (Exception e) {
            e.printStackTrace();
            responseJsonObject.addProperty("message", "Database error");
            responseJsonObject.addProperty("status", "fail");
        }
    }


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        JsonObject responseJsonObject = new JsonObject();
        if (this.isValidPayment(request, responseJsonObject)){
            responseJsonObject.addProperty("status", "success");
            this.doPayment(request, responseJsonObject);
        }
        else{
            responseJsonObject.addProperty("status", "fail");
        }

        response.getWriter().write(responseJsonObject.toString());
    }


    // not used but might have to later
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();
        JsonObject jsonObject = new JsonObject();

        out.write(jsonObject.toString());
        response.setStatus(200);
    }


}