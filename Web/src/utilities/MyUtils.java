package utilities;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

// for commonly used functions
public class MyUtils {

    public static boolean notEmpty(String s){
        return (s != null && !s.equals(""));
    }


    // gets Database connection using pooling (defined in context.xml)
    public static Connection getConnection() throws NamingException, SQLException {
        Context initCtx = new InitialContext();

        Context envCtx = (Context) initCtx.lookup("java:comp/env");
        if (envCtx == null)
            System.out.println("getConnection: environment context is NULL");

        // Look up our data source
        DataSource ds = (DataSource) envCtx.lookup("jdbc/moviedb");

        // the following commented lines are direct connections without pooling
        //Class.forName("org.gjt.mm.mysql.Driver");
        //Class.forName("com.mysql.jdbc.Driver").newInstance();
        //Connection dbcon = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);

        if (ds == null)
            System.out.println("getConnection: datasource is null.");

        Connection dbcon = ds.getConnection();
        if (dbcon == null)
            System.out.println("getConnection: dbcon is null.");

        return dbcon;
    }
}
