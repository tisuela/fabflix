
/* Queries can be made and executed through this class
 * Replaces ExecuteQuery
 */

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

public class MyQuery extends BuildQuery{
    private Connection dbcon;

    // We use prepared statements for versatility and better performance
    private PreparedStatement statement = null;
    private ResultSet rs = null;

    public MyQuery(Connection dbcon){
        super();
        this.dbcon = dbcon;
    }

    public MyQuery(Connection dbcon, String query){
        super(query);
        this.dbcon = dbcon;
    }


    // builds statement query from query fragments (see BuildQuery)
    public void buildStatement() {
        try {
            this.statement = dbcon.prepareStatement(this.getQuery());
        } catch (Exception e){ e.printStackTrace();}
    }


    // fills in parameter values
    private void prepareStatement(){
        int parameterCount = 1;

        for(Map<String, String> valueInfo: this.getValues()){
            String type = valueInfo.get("type");
            String value = valueInfo.get("value");

            // check type
            try {
                if (type.equals("String")) {
                    this.statement.setString(parameterCount, value);
                    parameterCount++;
                }
                else if (type.equals("int")) {
                    this.statement.setInt(parameterCount, Integer.parseInt(value));
                    parameterCount++;
                }
            } catch (Exception e){ e.printStackTrace(); }
        }
    }


    // Executes statement
    public ResultSet execute(){
        try{
            this.buildStatement();
            this.prepareStatement();
            rs = this.statement.executeQuery();
        } catch (Exception e){ e.printStackTrace();}

        return rs;
    }


    // free resources
    public void close(){
        try{
            rs.close();
            statement.close();
        } catch (Exception e){ e.printStackTrace();}
    }

    public PreparedStatement getStatement() {
        return statement;
    }
}
