package valley_match;

// IMPORTS \\

	// For interacting with MySQL database
import java.sql.*;
import java.util.Scanner;
	// For interacting with Excel Workbooks (xls files only)
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
	// For reading and writing data file streams
import java.io.*;
	// For sending emails to certain entrants
import javax.mail.MessagingException;
// For importing the Excel file into the program
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/* 
 * Valley Match Services
 * JDU: Java Database Updater
 * (c) 2016 Matthew R. Manzi
 * 
 * Class to update the MySQL database with all the information from the
 * Excel (xls) workbook containing all of the entrant data
 * @author matteomanzi
 * @version 1.0 --- Jan 25, 2016
 * @version 1.0.1 --- Jan 30, 2016 -- Refined the xlsFileChooser dialog box
 * @version 1.1 --- Feb 1, 2016 -- Added support for Cells of type NUMERIC
 * 								   compared to Cells of type STRING so that
 * 								   the program does not crash attempting to
 * 								   get the string value of a numeric cell
 * @version 2.0 --- Feb 5, 2016 -- Added full support for validating and
 * 								   handling access codes submitted by users
 * 								   and handling code exceptions, etc.
 * @version 2.0.1 --- Feb 5, 2016 -- Set database connection URL property
 * 									 useSSL to false so that there is no
 * 									 warning about using a server with an
 * 									 unverified certificate
 * @version 2.0.2 --- Feb 6, 2016 -- Revised email messages sent to entrants
 * 									 with access code errors
 * 
 */
public class ValleyMatchDatabaseUpdate {
	
	public static void main(String[] args) {

		// ALL VARIABLES \\
		
			// Database Constants
		final String DATABASE = "valley_match_data";
		final String STUDENT_TABLE = "ValleyMatchStudents";
//		final String STUDENT_TABLE = "ValleyMatchRobotics";	// LOCAL TEST INFO
		final String ACCESS_CODES_TABLE = "ValleyMatchAccessCodes";
		final int TOTAL_FIELDS = 40;	// This is the total number of fields that should be in the workbook,
										// and it should not have a 'Timestamp' or 'I agree...' column. Also
										// need to delete first row with column headers.
		final int GRADE_SEEK_IND = 4;	// Index of the column that holds the grade_seeking value for an entrant
			// File Chooser
		JFileChooser xlsFileChooser;
		FileFilter filter;
		int openDialogResult;
			// XLS Workbook
		Workbook masterWorkbook;
		Sheet masterList;
		Connection dbConnection;
		Statement dbStatement;
			// Iterating Workbook
		String[] entrantValues;
		int columnIndex;
		Validity hasValidCode;
		EmailClient ec;
			// Updating Database
		String insertEntrantStr = "";
		Scanner input = new Scanner(System.in);
			// Reporting Current Database Condition
		String selectAllStr = "";
		int rowCount;
		ResultSet selectResults;
		String entrantName;
		
		
		// BEGIN EXECUTION OF PROGRAM \\
		
		// Graphically let the admin choose a file to open and import into the database
		// *Note: only '.xls' files may be chosen as indicated by the XLSFilter class*
		xlsFileChooser = new JFileChooser();
		filter = new XLSFilter();
		xlsFileChooser.removeChoosableFileFilter(xlsFileChooser.getAcceptAllFileFilter());	// Update 1.0.1 -- removes "all files" filter
		xlsFileChooser.setFileFilter(filter);												// Update 1.0.1 -- sets XLSFilter as default filter
		openDialogResult = xlsFileChooser.showOpenDialog(null);
		
		if (openDialogResult == JFileChooser.APPROVE_OPTION) {
			
			try {
				// Create the workbook object with the Excel document that contains all entrant information and
				// get the first sheet
				masterWorkbook = new HSSFWorkbook(new FileInputStream(xlsFileChooser.getSelectedFile()));
				masterList = masterWorkbook.getSheetAt(0);
				
				try {
					// Login to the local MySQL database for Valley Match and create a statement to use for inserting entrants
					dbConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + DATABASE + "?useSSL=false", "myuser", "xxxx");
					dbStatement = dbConnection.createStatement();
					
					// Iterate through the spreadsheet to get data from each cell to place the values into a spot
					// in the entrantValues array, which will be used to pass data into insertEntrantStr for inserting
					// each entrant's values into the database
					entrantValues = new String[TOTAL_FIELDS];
					for (Row entrant: masterList) {
						// Reset InsertEntrantStr
						insertEntrantStr = "INSERT INTO " + STUDENT_TABLE
								+ " (username, access_code_used, name, grade, grade_seeking, gender,"																																// Eligibility criteria
								+ " spontaneous, `sensitive`, `reliable`, passive, artistic, quick_tempered, intellectual, adventurous, reserved, passionate, outgoing, shy,"														// Section 1
								+ " sports, watch_movies, attend_parties, meet_new_people, pop_music, rock_alt_music, country_music, classical_music, maintain_physical_fitness, `read`, attend_school_events,"						// Section 2
								+ " with_family, text_frequently, moral_compass, personal_space, well_in_school, involved_at_school, friendly, nervous_about_speaking, concerts_music_festivals, kind_to_others, plan_for_future)"	// Section 3
								+ " VALUES (";
						
						hasValidCode = AccessCodeManager.checkForValidAccessCode(entrant, dbConnection, ACCESS_CODES_TABLE);
						
						if (hasValidCode == Validity.VALID) {
							
							for (Cell entrantData: entrant) {
								columnIndex = entrantData.getColumnIndex();
//								System.out.println("Index: " + columnIndex);
								
//								System.out.println("Type: String-" + (entrantData.getCellType() == Cell.CELL_TYPE_STRING));
//								System.out.println("      Number-" + (entrantData.getCellType() == Cell.CELL_TYPE_NUMERIC));
								if (entrantData.getCellType() == Cell.CELL_TYPE_STRING) {
									entrantValues[columnIndex] = "\"" + entrantData.getStringCellValue() + "\"";
								} else if (entrantData.getCellType() == Cell.CELL_TYPE_NUMERIC) {
									
									if (entrantData.getColumnIndex() == GRADE_SEEK_IND) {
										entrantValues[columnIndex] = "\"" + (Integer.toString((int) entrantData.getNumericCellValue())) + "\"";
									} else {
										entrantValues[columnIndex] = Integer.toString((int) entrantData.getNumericCellValue());
									}
									
								}
								
//								System.out.println("Value: " + entrantValues[columnIndex] + "\n");
//								entrantValues[columnIndex].replaceAll("'", "''");
								insertEntrantStr += (columnIndex == TOTAL_FIELDS - 1) ? (entrantValues[columnIndex] + ")") : (entrantValues[columnIndex] + ", ");
							}

							// Ask to continue inserting the most recent entrant's data into the database
//							System.out.println("Executing:\n\t" + insertEntrantStr);
//							System.out.print("Press Enter to add entrant...");
//							input.nextLine();
							
							dbStatement.executeUpdate(insertEntrantStr);
//							System.out.println("Executed: " + insertEntrantStr);
						} else if (hasValidCode == Validity.NOT_FOUND_ERROR) {
							// Send email to person notifying them that their access code not found
							ec = new EmailClient(entrant.getCell(0).getStringCellValue());
							ec.sendCustomEmail("Invalid Access Code", "Dear Entrant,\n\nIt appears as though your access code was not found in our database."
									+ "  We appologize for any inconvenience.\n\n\nIf you have any further questions you may reply to this email or talk"
									+ " to a representative of the GVHS Robotics Team.\n\nThank you for your support.  Happy Matching!");
							ec.close();
							System.out.println("Sent an email to " + entrant.getCell(0).getStringCellValue() + " about " + Validity.NOT_FOUND_ERROR);
						} else if (hasValidCode == Validity.REUSE_ERROR) {
							// Send email to person notifying them that their access code was already used
							ec = new EmailClient(entrant.getCell(0).getStringCellValue());
							ec.sendCustomEmail("Invalid Access Code", "Dear Entrant,\n\nIt appears as though your access code was found in our database but"
									+ " has already been used.  We appologize for any inconvenience.\n\n\nIf you have any further questions you may"
									+ " reply to this email or talk to a representative of the"
									+ " GVHS Robotics Team.\n\nThank you for your support.  Happy Matching!");
							ec.close();
							System.out.println("Sent an email to " + entrant.getCell(0).getStringCellValue() + " about " + Validity.REUSE_ERROR);
						} else {
							// Send email to person notifying them that there was a problem with their access code
							ec = new EmailClient(entrant.getCell(0).getStringCellValue());
							ec.sendCustomEmail("Invalid Access Code", "Dear Entrant,\n\nSomething went wrong on our end.  Unfortunately, your information"
									+ " has not been added to our database.  We appologize for your inconvience.\n\n\nPlease reply to this email or see"
									+ " a representative of the GVHS Robotics Team to obtain a new access code.  After you receive your new access code"
									+ " you will be able to update the access code on your original Google Form and resubmit your information to our"
									+ " database.\n\nThank you for your support.  Happy Matching!");
							ec.close();
							System.out.println("Sent an email to " + entrant.getCell(0).getStringCellValue() + " about " + Validity.UNKNOWN_ERROR);
						}
						
					} // End loop
					input.close();
					
					// Print out all names in the table and a row count to ensure
					// that all rows were created
					System.out.println("Name:");
					selectAllStr = "SELECT name FROM " + STUDENT_TABLE;
					rowCount = 0;
					selectResults = dbStatement.executeQuery(selectAllStr);
					while (selectResults.next()) {
						
						entrantName = selectResults.getString("name");
						System.out.println(entrantName);
						++rowCount;
						
					}
					System.out.println("\nTotal Rows: " + rowCount);
					
				} catch (SQLException se) {
					se.printStackTrace();
					System.out.println("Requested MySQL Insert Statement:\n\t" + insertEntrantStr);
					System.out.println("Requested MySQL Select Statement:\n\t" + selectAllStr);
				} catch (MessagingException me) {
					me.printStackTrace();
				} // End try for database connection
				
			} catch (FileNotFoundException fnfe) {
				fnfe.printStackTrace();
				System.out.println("File not found");
			} catch (IOException ioe) {
				ioe.printStackTrace();
			} // End try for file chooser
			
		} // End if

	} // End main()

} // End class
