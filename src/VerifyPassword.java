import org.jasypt.util.password.StrongPasswordEncryptor;

import javax.annotation.Resource;
import javax.sql.DataSource;
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

	public boolean verifyCredentials(String email, String password) throws Exception {
		String loginUser = "mytestuser";
		String loginPasswd = "mypassword";
		String loginUrl = "jdbc:mysql://localhost:3306/moviedb";

		Class.forName("com.mysql.jdbc.Driver").newInstance();
		Connection dbcon = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);

		MyQuery query = new MyQuery(dbcon, "SELECT * from customers");
		query.append("where email = ?", email);
		ResultSet rs = query.execute();

		boolean success = false;
		if (rs.next()) {
		    // get the encrypted password from the database
			String encryptedPassword = rs.getString("password");
			id = rs.getInt("id");
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
