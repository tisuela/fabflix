import com.google.gson.JsonObject;

import javax.annotation.Resource;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
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
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    private boolean notEmpty(String s){
        return (s != null && !s.equals(""));
    }

    // Checks if payment is valid and puts info into json object
    private boolean isValidPayment(HttpServletRequest request, JsonObject responseJsonObject){
        try {
            // get values
            String firstName = request.getParameter("firstName");
            String lastName = request.getParameter("lastName");
            String creditCardNumber = request.getParameter("creditCardNumber");
            String expirationDate = request.getParameter("expirationDate");

            Connection dbcon = dataSource.getConnection();

            String equalsStr = "%1$s = \"%2$s\"";

            BuildQuery cardQuery = new BuildQuery();
            cardQuery.setSelectStr("*");
            cardQuery.addFromTables("creditcards");
            cardQuery.addWhereConditions(equalsStr, "creditcards.id", creditCardNumber);
            cardQuery.addWhereConditions(equalsStr, "creditcards.expiration", expirationDate);

            System.out.println("Payment query = " + cardQuery.getQuery());
            System.out.println("Payment query where = " + String.format(equalsStr, creditCardNumber, expirationDate));

            ExecuteQuery cardExecute = new ExecuteQuery(dbcon, cardQuery);

            ResultSet cardSet = cardExecute.execute();

            // Check if there is no matching card
            if (!cardSet.isBeforeFirst() || !notEmpty(creditCardNumber) || !notEmpty(expirationDate)){
                responseJsonObject.addProperty("message", "Invalid Payment Information");
                return false;
            }

            responseJsonObject.addProperty("message","Payment is Valid");

            dbcon.close();
            cardExecute.close();
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


            Connection dbcon = dataSource.getConnection();

            Date date = new Date();
            java.sql.Date sqlDate = new java.sql.Date(date.getTime());

            User user = (User)request.getSession().getAttribute("user");

            HashMap<String, Integer> cart = user.getCart();

            String latestSaleIdQuery = "SELECT * FROM sales ORDER BY sales.id DESC LIMIT 3";
            ExecuteQuery latestSaleIdExecute = new ExecuteQuery(dbcon, latestSaleIdQuery);
            ResultSet latestSales = latestSaleIdExecute.execute();
            int latestId = 0;
            if (latestSales.isBeforeFirst()){
                latestSales.first();
                latestId = latestSales.getInt("sales.id");

            }

            // id to be set for this transaction; all the sales made here found via this
            String transactionId = String.valueOf(latestId + 1);

            for(String movieId: cart.keySet()){
                PreparedStatement ps = dbcon.prepareStatement("INSERT INTO sales (customerId, movieId, saleDate, quantity, transactionId) VALUES (?, ?, ?, ?, ?)");
                ps.setInt(1, user.getId());
                ps.setString(2, movieId);
                ps.setDate(3, sqlDate);
                ps.setInt(4, cart.get(movieId));
                ps.setString(5, transactionId);
                ps.executeUpdate();
                ps.close();
            }

            responseJsonObject.addProperty("transactionId", transactionId);
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