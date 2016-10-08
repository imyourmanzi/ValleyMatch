package valley_match_windows;

// IMPORTS \\
	// For accessing the SQL database
import java.sql.*;
import javax.sql.rowset.*;
	// For storing matchees
import java.util.ArrayList;
import java.util.Calendar;
// For writing to the backup file
import java.io.File;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
	// For prompting the admin
import java.util.Scanner;
	// For sending the emails
import javax.mail.MessagingException;

/*
 * Valley Match Services
 * JCA: Java Compatibility Appropriator
 * (c) 2016 Matthew R. Manzi
 * 
 * Main class to run the comparison and evaluation of all the entrants in the
 * Valley Match program
 * !!!WARNING: MAX NUMBER OF EMAILS THAT CAN BE SENT BEFORE THE SPAM BLOCKER IN
 * GMAIL FREAKS OUT IS 86-90 EMAILS!!!
 * @author matteomanzi
 * @version 1.0 --- Jan 28, 2016
 * @version 1.1 --- Jan 29, 2016 -- Changes naming scheme of some variables,
 * 									removed certain variables for efficiency,
 * 									updated method names and print statements,
 * 									added shouldBeSame values, updated criteria
 * 									evaluation loop
 * @version 1.1.1 --- Feb 1, 2016 -- Standardized naming convention of constants,
 * 									 added COLUMN_NAME[] to allow each column
 * 									 to be referenced by its name, and not an
 * 									 index which inherently means nothing to SQL,
 * 									 removed variable firstCritIndex and changed
 * 									 TOTAL_FIELDS to represent the number of
 * 									 fields that are used to determine a score of
 * 									 compatibility between two entrants
 * @version 1.2 --- Feb 2, 2016 -- Added a new step in the program where the data
 * 								   for all matchees, etc. is saved to a .csv file
 * 								   as a backup for all calculated information
 * @version 1.3 --- Feb 5, 2016 -- Relocated email methods and related variables
 * 								   to the class EmailClient so that it may be
 * 								   instantiated by other classes and used to send
 * 								   emails
 * @version 1.3.1 --- Feb 5, 2016 -- Set database connection URL property useSSL
 * 									 to false so that there is no warning about
 * 									 using a server with an unverified certificate
 * @version 1.4 --- Feb 9, 2016 -- Adapted to run on the main computer in B202 with
 * 								   a Windows 7 environment
 * @version 1.4.1 --- Feb 19, 2016 -- Added an extra email step telling entrants
 * 									  that they will receive another email with more
 * 									  exact match percentages (for Feb 19, 2016 only)
 * 
 */
public class ValleyMatchMain {
	
	public static void main(String[] args) {
		
		// ALL VARIABLES \\
		
			// Database and Data Evaluation Constants
		final String DATABASE_NAME = "valley_match_data";
		final String STUDENT_TABLE_NAME = "ValleyMatchStudents";
		final int TOTAL_FIELDS = 34;	// The total number of fields used to compare the two entrants
		final String[] COLUMN_NAME = {"spontaneous", "sensitive", "reliable", "passive", "artistic", "quick_tempered", "intellectual",		// Section 1
									  "adventurous", "reserved", "passionate", "outgoing", "shy",											// Section 1
									  "sports", "watch_movies", "attend_parties", "meet_new_people", "pop_music", "rock_alt_music",			// Section 2
									  "country_music", "classical_music", "maintain_physical_fitness", "read", "attend_school_events",		// Section 2
									  "with_family", "text_frequently", "moral_compass", "personal_space", "well_in_school",				// Section 3
									  "involved_at_school", "friendly", "nervous_about_speaking", "concerts_music_festivals",				// Section 3
									  "kind_to_others", "plan_for_future"};																	// Section 3
		final boolean[] SHOULD_BE_SAME = {false, true, true, false, true,  false, false, false, false, false, false, false, false,	// Section 1
										true,  true, true, false, true,  true,  true,  true,  true,  false, true,					// Section 2
										true,  true, true, true,  true,  true,  false, false, false, true, false};					// Section 3
		final float[] WEIGHT = {15, 15, 10, 16, 12, 16, 16, 16, 15, 16, 13, 16, 16,			// Section 1
								13, 14, 19, 17, 11, 11, 11, 11, 15, 14, 19,					// Section 2
								19, 19, 19, 15, 13, 11, 12, 12, 12, 18, 15};				// Section 3
			// Temporary Storage of Matchees, Entrants, and Certain Data
		ArrayList<MatcheeEntrant> allMatchees = new ArrayList<MatcheeEntrant>();
		MatcheeEntrant crntMatchee;
		Entrant crntMatched;
		int crntMatchedId;
		String crntMatchedGrade;
			// Lists of Entrants
		CachedRowSet matcheeList;
		CachedRowSet matchedList;
			// Values for Determining Compatibility
		boolean eligible;
		float currentScore;
			// Backup File
		File backupFile;
		OutputStream writer;
		final String fileHeaders = "matchee_id,matchee_name,matchee_email,matchee_grade_seeking,"								// Matchee
								 + "matched_id,matched_name,matched_email,matched_gender,matched_grade,matched_final_score,"	// Matched
							 	 + "criterion_title,criterion_id,criterion_score\n";											// Criterion
			// Email Variables
		Scanner input = new Scanner(System.in);
		boolean sendEmails;
		int sentCount = 0;
		EmailClient ec;
		
		
		// BEGIN EXECUTION OF PROGRAM \\
		
		// COMPARE ALL ENTRANTS \\
		matcheeList = getAllDatabaseContentsForDatabaseAndTable(DATABASE_NAME, STUDENT_TABLE_NAME);
		try {
			
			// SET UP FILE WRITER & FILE HEADERS \\
			backupFile = new File("C:\\Robotics2016\\Java Code\\ValleyMatch\\backup\\Results" + (new SimpleDateFormat("yyyyMMdd-HHmm").format(Calendar.getInstance().getTime())) + ".bak.csv");
			writer = new FileOutputStream(backupFile);
			writer.write(fileHeaders.getBytes());
			
			matchedList = matcheeList.createCopy();
			
			while (matcheeList.next()) {
				
				crntMatchee = new MatcheeEntrant(
						matcheeList.getString("name"),
						matcheeList.getString("username"),
						matcheeList.getInt("id"));
				crntMatchee.setGradeSeeking(matcheeList.getString("grade_seeking").replaceAll(", ", "_"));
				
				while (matchedList.next()) {
					
					crntMatchedId = matchedList.getInt("id");
					crntMatchedGrade = matchedList.getString("grade");
					eligible = determineCompatibilityForEligibilityCriteria(crntMatchee.getGradeSeeking(), crntMatchedGrade);
					
					if ((crntMatchee.getId() != crntMatchedId) && eligible) {
						
						crntMatched = new Entrant(
								matchedList.getString("name"),
								matchedList.getString("username"),
								crntMatchedId,
								matchedList.getString("gender"),
								crntMatchedGrade);
						
						for (int i = 0; i < TOTAL_FIELDS ; i++) {
							
							if (i < 23) {
								
								currentScore = determineCompatibilityForCriterion(
										matcheeList.getInt(COLUMN_NAME[i]), matchedList.getInt(COLUMN_NAME[i]),
										SHOULD_BE_SAME[i], WEIGHT[i]);
								
							} else {

								currentScore = determineCompatibilityForCriterion(
										matcheeList.getString(COLUMN_NAME[i]), matchedList.getString(COLUMN_NAME[i]),
										SHOULD_BE_SAME[i], WEIGHT[i]);
								
							}
							
							crntMatched.addCriterion(COLUMN_NAME[i], i, currentScore);
							
							writer.write(Integer.toString(crntMatchee.getId()).getBytes());
							writer.write(',');
							writer.write(crntMatchee.getName().getBytes());
							writer.write(',');
							writer.write(crntMatchee.getEmail().getBytes());
							writer.write(',');
							writer.write(crntMatchee.getGradeSeeking().getBytes());
							writer.write(',');
							writer.write(Integer.toString(crntMatchedId).getBytes());
							writer.write(',');
							writer.write(crntMatched.getName().getBytes());
							writer.write(',');
							writer.write(crntMatched.getEmail().getBytes());
							writer.write(',');
							writer.write(crntMatched.getGender().getBytes());
							writer.write(',');
							writer.write(crntMatched.getGrade().getBytes());
							writer.write(',');
							writer.write(Float.toString(crntMatched.getFinalScorePercentage()).getBytes());
							writer.write(',');
							writer.write(COLUMN_NAME[i].getBytes());
							writer.write(',');
							writer.write(Integer.toString(i).getBytes());
							writer.write(',');
							writer.write(Float.toString(currentScore).getBytes());
							writer.write('\n');
							
						} // End for()
						
						crntMatchee.addMatchedEntrant(crntMatched);
					} else {
//						System.out.println("Matchee " + crntMatchee.getName() + " got an ineligible entrant id-" + crntMatchedId);
					} // End if (eligible)
					
				} // End while (matchedList.next())
				
				matchedList.beforeFirst();
				allMatchees.add(crntMatchee);
			} // End while(matchedList.next())
			
		} catch (SQLException se) {
			se.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			System.out.println("There was an error creating the file stream");
			System.exit(63); // Exit with status code: 63 - No file
		} // End try
		
		
		// WAIT TO SEND OUT EMAILS \\
		System.out.print("All entrants have been compared.\nWould you like to send out emails (y/n)? ");
		sendEmails = input.next().equalsIgnoreCase("y");
		input.close();
		
		
		// SEND OUT EMAILS TO ALL ENTRANTS \\
		if (sendEmails) {
			ec = new EmailClient();
			
			if (ec.setUpEmailClient()) {

				try {
				
					for (MatcheeEntrant matchee: allMatchees) {
						ec.setTo(matchee.getEmail());
						
//							ec.sendCustomEmail("Your UPDATED Results",
//									"Valley Matchers,\n\nAfter reviewing the results from our first run, "
//									+ "we have decided to resend your scores to you, this time with more "
//									+ "exact match percentages.  You will receive your new matches "
//									+ "shortly.\n\nThanks for your support.  Happy Matching!");
						if (ec.sendEmailWithTopEntrantsTo(matchee.getName(), matchee.getTopThree("male"), matchee.getTopThree("female"))) {
							System.out.println("Sent " + matchee.getName() + "'s email. Total sent is " + ++sentCount + " email(s)");
						} else {
							System.out.println("Matchee " + matchee.getName() + "'s email was not sent");
						}
						
					}
					
					ec.close();
				} catch (MessagingException me) {
					me.printStackTrace();
				}
				
			}
			
		} else {
			System.out.println("You have ended the program, all entrant data"
					+ " within the java runtime has been lost but a 'Results[date].bak.csv'"
					+ " file was created.");
		} // End if (sendEmails)
		
	} // End main()
	
	
	// HELPER METHODS \\
	private static CachedRowSet getAllDatabaseContentsForDatabaseAndTable(String database, String table) {
		ResultSet results;
		
		try {
			CachedRowSet rowset = RowSetProvider.newFactory().createCachedRowSet();
			
			Connection dbConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + database + "?useSSL=false", "valleymatch", "xxxx");
			Statement statement = dbConn.createStatement();
			results = statement.executeQuery("SELECT * FROM " + table);
			
			rowset.populate(results);
			
			return rowset;
		} catch (SQLException se) {
			se.printStackTrace();
		}
		
		return null;
	}
	
	@SuppressWarnings("unused")
	private static boolean determineCompatibilityForEligibilityCriteria(
			String matcheeGenderSeeking, String matchedGender,
			String matcheeGradeSeeking, String matchedGrade) {
		
		if (matcheeGenderSeeking.contains(matchedGender) &&
				matcheeGradeSeeking.contains(matchedGrade)) {
			return true;
		}
		
		return false;
	}
	
	private static boolean determineCompatibilityForEligibilityCriteria(
			String matcheeGradeSeeking, String matchedGrade) {
		
		if (matcheeGradeSeeking.contains(matchedGrade)) {
			return true;
		}
		
		return false;
	}
	
	private static float determineCompatibilityForCriterion(
			String matcheeValue, String matchedValue,
			boolean shouldBeSame, float weight) {
		float score = 0.0f;
		
		if (matcheeValue.equals(matchedValue) && shouldBeSame) {
			score = 1.0f;
		} else if (!matcheeValue.equals(matchedValue) && shouldBeSame) {
			score = 1.0f;
		} else {
			score = 0.3f;
		}
		
		score *= weight;
		
		return score;
	}
	
	private static float determineCompatibilityForCriterion(
			int matcheeValue, int matchedValue,
			boolean shouldBeSame, float weight) {
		float score = 0.0f;
		
		int diff = (int) Math.abs(matcheeValue - matchedValue);
		
		if (shouldBeSame) {
			diff = 5 - (diff + 1);
		}
		
		switch (diff) {
		
		case 0:
			score = 0.2f;
			break;
		case 1:
			score = 0.4f;
			break;
		case 2:
			score = 0.6f;
			break;
		case 3:
			score = 0.8f;
			break;
		case 4:
			score = 1.0f;
			break;
		default:
			score = 0.0f;
			break;
		}
		
		score *= weight;
		
		return score;
	}
	
} // End class
