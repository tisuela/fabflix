package utilities;

import org.jasypt.util.password.StrongPasswordEncryptor;
import utilities.MyQuery;

import java.sql.*;

public class VerifyPassword {
	private String name = null;
	private int id = -1;

	/*
	 * After you update the passwords in customers table,
	 *   you can use this program as an example to verify the password.
	 *   
	 * Verify the password is simple:
	 * success = new StrongPasswordEncryptor().checkPassword(password, encryptedPassword);
	 * 
	 * Note that you need to use the same StrongPasswordEncryptor when encrypting the passwords
	 * 
	 */

	public boolean verifyCredentials(String email, String password, String database) throws Exception {
		String loginUser = "mytestuser";
		String loginPasswd = "mypassword";
		String loginUrl = "jdbc:mysql://localhost:3306/moviedb";

		Class.forName("com.mysql.jdbc.Driver").newInstance();
		Connection dbcon = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);

		System.out.println(String.format("Querying database: %s", database));

		MyQuery query = new MyQuery(dbcon, String.format("SELECT * from %s", database));
		query.append("where email = ?", email);
		ResultSet rs = query.execute();

		boolean success = false;
		if (rs.next()) {
		    // get the encrypted password from the database
			String encryptedPassword = rs.getString("password");
			id = (database.equals("customers")) ? rs.getInt("id") : -1;
			name = email;
			
			// use the same encryptor to compare the user input password with encrypted password stored in DB
			success = new StrongPasswordEncryptor().checkPassword(password, encryptedPassword);
		}
		query.close();
		dbcon.close();
		System.out.println("verify " + email + " - " + password);

		return success;
	}

	public String getName() {
		return name;
	}

	public int getId() {
		return id;
	}
}
