package valley_match_windows;

import java.util.ArrayList;
import java.util.Random;
import java.sql.*;
import org.apache.poi.ss.usermodel.Row;

/*
 * Valley Match Services
 * AccessCodeManager
 * (c) 2016 Matthew R. Manzi
 * 
 * Utility class to create access codes and manage them, including
 * checking for uniqueness and and comparing entrants access codes
 * to currently stored codes
 * @author matteomanzi
 * @version 1.0 --- Feb 4, 2016
 * 
 */
public class AccessCodeManager {

	
	// PUBLIC STATIC METHODS \\
	
	public static boolean batchGenerateUniqueAccessCodes(int num, Connection conn, String table) throws SQLException {
		String insertStr = "blank_string";
		String code;
		
		try {
			Statement stmnt = conn.createStatement();
			
			for (int i = 1; i <= num; i++) {
				insertStr = "INSERT INTO " + table + " (access_code) VALUES ("; 
				
				code = generateUniqueAccessCode();
				insertStr += ("\"" + code + "\")");
				stmnt.executeUpdate(insertStr);
			}
			
		} catch (SQLException se) {
			se.printStackTrace();
			System.out.println("Requested MySQL Insert Statement:\n\t" + insertStr);
			
			return false;
		}
		
		return true;
	}
	
	public static Validity checkForValidAccessCode(Row entrant, Connection conn, String table) {
		String enteredCode = entrant.getCell(1).getStringCellValue();
		boolean foundCode = false;
		
		try {
			Statement stmnt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
			ResultSet accessResults = stmnt.executeQuery("SELECT * FROM " + table);
			
			while (accessResults.next()) {
				
				if (accessResults.getString("access_code").equals(enteredCode) &&
						accessResults.getString("username").equals("null")) {
					accessResults.updateString("username", entrant.getCell(0).getStringCellValue());
					accessResults.updateRow();
					foundCode = true;
					
					return Validity.VALID;
				} else if (accessResults.getString("access_code").equals(enteredCode) &&
						accessResults.getString("username").equals(entrant.getCell(0).getStringCellValue())) {
					foundCode = true;
					
					return Validity.VALID;
				} else if (accessResults.getString("access_code").equals(enteredCode) &&
						!accessResults.getString("username").equals(entrant.getCell(0).getStringCellValue())) {
					foundCode = true;
					
					return Validity.REUSE_ERROR;
				}
				
			}
			
			if (!foundCode) {
				return Validity.NOT_FOUND_ERROR;
			}
			
		} catch (SQLException se) {
			se.printStackTrace();
			
			System.exit(34); // Exit with status code: 34 - Failed to Insert access code(s)
		}
		
		return Validity.UNKNOWN_ERROR;
	}
	
	public static int ensureAllCodesAreUnique(Connection conn, String table) throws SQLException {
		int codesRemoved = 0;
		ArrayList<String> codesList = new ArrayList<String>();
		ArrayList<Integer> idsList = new ArrayList<Integer>();
		
		Statement stmnt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
		ResultSet codes = stmnt.executeQuery("SELECT * FROM " + table);
		while (codes.next()) {
			codesList.add(codes.getString("access_code"));
			idsList.add(codes.getInt("id"));
		}
		codes.first();
		while (codes.next()) {
			
			for (int i = 0; i < codesList.size(); i++) {
				
				if (codes.getString("access_code").equals(codesList.get(i)) &&
						codes.getInt("id") != idsList.get(i)) {
					codes.deleteRow();
					codesRemoved++;
				}
				
			}
			
		}
		
		return codesRemoved;
	}
	
	
	// PRIVATE METHODS \\
	
	private static String generateUniqueAccessCode() {
		Random charGen = new Random();
		String code = "";
		
		for (int i = 0; i < 4; i++) {
			code += (char) (charGen.nextInt((90 - 65) + 1) + 65);			
		}
		
		for (int j = 0; j < 3; j++) {
			code += (char) (charGen.nextInt((57- 48) + 1) + 48);
		}
		
		for (int j = 0; j < 3; j++) {
			code += (char) (charGen.nextInt((122 - 97) + 1) + 97);
		}
		
		return code;
	}
	
}
