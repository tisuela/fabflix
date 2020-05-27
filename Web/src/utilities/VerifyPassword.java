package utilities;

import org.jasypt.util.password.StrongPasswordEncryptor;
import utilities.MyQuery;

import javax.naming.Context;
import javax.naming.InitialContext;
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

	public boolean verifyCredentials(String email, String password, String table) throws Exception {

		Connection dbcon = MyUtils.getConnection();

		System.out.println(String.format("Querying table: %s", table));

		MyQuery query = new MyQuery(dbcon, String.format("SELECT * from %s", table));
		query.append("where email = ?", email);
		ResultSet rs = query.execute();

		boolean success = false;
		if (rs.next()) {
		    // get the encrypted password from the database
			String encryptedPassword = rs.getString("password");
			id = (table.equals("customers")) ? rs.getInt("id") : -1;
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
