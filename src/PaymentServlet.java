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


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {



        JsonObject responseJsonObject = new JsonObject();
        if (this.isValidPayment(request, responseJsonObject)){
            responseJsonObject.addProperty("status", "success");
        }
        else{
            responseJsonObject.addProperty("status", "fail");
        }


        response.getWriter().write(responseJsonObject.toString());
    }
}