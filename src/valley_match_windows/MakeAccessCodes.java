package valley_match_windows;

import java.sql.*;

/*
 * Valley Match Services
 * MakeAccessCodes
 * (c) 2016 Matthew R. Manzi
 * 
 * Class to make the access codes with use of the
 * methods from AccessCodeManager
 * @author matteomanzi
 * @version 1.0 --- Feb 5, 2016
 * @version 1.0.1 --- Feb 5, 2016 -- Set database connection URL property useSSL
 * 									 to false so that there is no warning about
 * 									 using a server with an unverified certificate
 * @version 1.2 --- Feb 9, 2016 -- Adapted to run on the main computer in B202 with
 * 								   a Windows 7 environment
 * 
 */
public class MakeAccessCodes {

	public static void main(String[] args) {
		
		final String DATABASE = "valley_match_data";
		final String ACCESS_CODES_TABLE = "ValleyMatchAccessCodes";
		final int CODES = 1000;
		int droppedCodes = 0;
		
		try {
			Connection dbConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + DATABASE + "?useSSL=false", "valleymatch", "xxxx");
			
			System.out.println("Codes Requested:\t" + CODES);
			
			if (AccessCodeManager.batchGenerateUniqueAccessCodes(CODES, dbConnection, ACCESS_CODES_TABLE)) {				
				droppedCodes = AccessCodeManager.ensureAllCodesAreUnique(dbConnection, ACCESS_CODES_TABLE);
				System.out.println("Codes Generated:\t" + CODES);
				System.out.println("Codes Dropped:  \t" + droppedCodes);
			}
			
		} catch (SQLException se) {
			se.printStackTrace();
		}
		
	}

}
