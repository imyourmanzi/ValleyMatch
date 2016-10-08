package valley_match;

import java.sql.*;

/*
 * Valley Match Services
 * TestSelect
 * (c) 2016 Matthew R. Manzi
 * 
 * Class to select all of the records from
 * the table to ensure that all records do
 * exist
 * @author matteomanzi
 * @version 1.0 --- Feb 2, 2016
 * @version 1.0.1 --- Feb 5, 2016 -- Set database connection URL property useSSL
 * 									 to false so that there is no warning about
 * 									 using a server with an unverified certificate
 * 
 */
public class TestSelect {

	public static void main(String[] args) {
		
		try {
			Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/valley_match_data?useSSL=false", "myuser", "xxxx");
			Statement stmnt = conn.createStatement();
			
			System.out.println("Columns:");
			ResultSet results = stmnt.executeQuery("SELECT * FROM ValleyMatchStudents");
			while (results.next()) {
				
				for (int i = 1; i <= 38; i++) {
					System.out.print(results.getString(i) + "\t");
				}
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		

	}

}
