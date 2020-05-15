import org.jasypt.util.password.PasswordEncryptor;
import org.jasypt.util.password.StrongPasswordEncryptor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class UpdateSecurePassword {

    /*
     * 
     * This program updates your existing moviedb customers or employees table to change the
     * plain text passwords to encrypted passwords.
     * 
     * You should only run this program **once**, because this program uses the
     * existing passwords as real passwords, then replace them. If you run it more
     * than once, it will treat the encrypted passwords as real passwords and
     * generate wrong values.
     * 
     */
    public static void main(String[] args) throws Exception {

        boolean iWantToEncrypt = true;


        // if iWantToEncrypt is true, this will be bypassed
        // WARNING, DOUBLE ENCRYPTING PASSWORDS WILL LEAD TO BAD THINGS
        // REMEMBER TO TURN FLAG BACK TO FALSE AFTER RUNNING IT
        if (! iWantToEncrypt){
            System.out.println("Safety measures activated: please turn them off to enable encryption");

            return;
        }

        String database = "customers";
        String primaryKey = (database.equals("customers")) ? "id" : "email";

        String loginUser = "mytestuser";
        String loginPasswd = "mypassword";
        String loginUrl = "jdbc:mysql://localhost:3306/moviedb";

        Class.forName("com.mysql.jdbc.Driver").newInstance();
        Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
        Statement statement = connection.createStatement();

        // change the "database" table password column from VARCHAR(20) to VARCHAR(128)
        String alterQuery = String.format("ALTER TABLE %s MODIFY COLUMN password VARCHAR(128)", database);
        int alterResult = statement.executeUpdate(alterQuery);
        System.out.println("altering " + database + " table schema completed, " + alterResult + " rows affected");

        // get the ID and password for each entry
        String query = String.format("SELECT %1$s, password from %2$s", primaryKey, database);

        ResultSet rs = statement.executeQuery(query);

        // we use the StrongPasswordEncryptor from jasypt library (Java Simplified Encryption) 
        //  it internally use SHA-256 algorithm and 10,000 iterations to calculate the encrypted password
        PasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();

        ArrayList<String> updateQueryList = new ArrayList<>();

        System.out.println("encrypting password (this might take a while)");
        String updateQueryTemplate = "UPDATE %1$s SET password='%2$s' WHERE %3$s='%4$s'";
        while (rs.next()) {
            // get the ID and plain text password from current table
            String id = rs.getString(primaryKey);
            String password = rs.getString("password");
            
            // encrypt the password using StrongPasswordEncryptor
            String encryptedPassword = passwordEncryptor.encryptPassword(password);

            // generate the update query
            updateQueryList.add(String.format(updateQueryTemplate, database, encryptedPassword, primaryKey, id));
        }
        rs.close();

        // execute the update queries to update the password
        System.out.println("updating password");
        int count = 0;
        for (String updateQuery : updateQueryList) {
            System.out.println("update query: " + updateQuery);
            int updateResult = statement.executeUpdate(updateQuery);
            count += updateResult;
        }
        System.out.println("updating password completed, " + count + " rows affected");

        statement.close();
        connection.close();

        System.out.println("finished");

    }

}
