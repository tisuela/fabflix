
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


// Helper class for executing queries
public class ExecuteQuery {
    private Connection dbcon;

    // We use prepared statements for versatility and better performance
    private PreparedStatement statement = null;
    private String query;
    private ResultSet rs = null;

    public ExecuteQuery(Connection dbcon){
        this.dbcon = dbcon;
    }

    public ExecuteQuery(Connection dbcon, String query){
        this(dbcon);
        setQuery(query);
    }

    public ExecuteQuery(Connection dbcon, BuildQuery query){
        this(dbcon, query.getQuery());
    }


    // Executes statement
    public ResultSet execute(){
        try{
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

    // When setting query, set the statement
    public void setQuery(String query) {
        this.query = query;
        setStatement(query);
    }

    public void setStatement(String query) {
        try {
            this.statement = dbcon.prepareStatement(query);
        } catch (Exception e){ e.printStackTrace();}
    }



}
