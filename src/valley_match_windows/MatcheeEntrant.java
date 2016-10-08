package valley_match_windows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

/*
 * Valley Match Services
 * MatcheeEntrant
 * (c) 2016 Matthew R. Manzi
 * 
 * Class to compile all of the entrants that one (matchee) entrant was
 * compared against; this is that matchee's object
 * @author matteomanzi
 * @version 1.0 --- Jan 25, 2016
 * @version 1.1 --- Jan 29, 2016 -- Changes naming scheme of variables and methods,
 * 									added extra instance data for more object-
 * 									oriented style of data storage, and removed
 * 									all irrelevant setter methods
 * 
 */
public class MatcheeEntrant {

	// INSTANCE DATA \\
	private String name;
	private String email;
	private int id;
	private String gradeSeeking;
	private ArrayList<Entrant> matchedEntrants;
	
	// CONSTRUCTORS \\
	public MatcheeEntrant() {
		this.name = null;
		this.email = null;
		this.id = -1;
		this.gradeSeeking = null;
		this.matchedEntrants = new ArrayList<Entrant>();
	}
	
	public MatcheeEntrant(String name, String email, int id) {
		this.name = name;
		this.email = email;
		this.id = id;
		this.gradeSeeking = null;
		this.matchedEntrants = new ArrayList<Entrant>();
	}
	
	// GETTERS & SETTERS \\
	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}
	
	public int getId() {
		return id;
	}

	public String getGradeSeeking() {
		return gradeSeeking;
	}

	public void setGradeSeeking(String gradeSeeking) {
		this.gradeSeeking = gradeSeeking;
	}

	// UPDATE MATCHED ENTRANTS \\
	public void addMatchedEntrant(Entrant matchedEntrant) {
		matchedEntrants.add(matchedEntrant);
	}
	
	// GET MATCHES \\
	public ArrayList<Entrant> getTopThree(String gender) {
		ArrayList<Entrant> topThree = new ArrayList<Entrant>();
		
		Collections.sort(matchedEntrants, new Comparator<Entrant>() {

			@Override
			public int compare(Entrant o1, Entrant o2) {
				int result = Float.compare(o2.getFinalScore(), o1.getFinalScore());
				
				if (result == 0) {
					Random rand = new Random();
					result = Float.compare(rand.nextFloat(), rand.nextFloat());
				}
				
				return result;
			}
			
		});
		
		int count = 1;
		for (Entrant entrant: matchedEntrants) {
		
			if (count == 4) {
				break;
			} else if (entrant.getGender().equalsIgnoreCase(gender)) {
				topThree.add(entrant);
				++count;
			}
			
		}
		
		return topThree;
	}
	
	// OTHER \\
	@Override
	public String toString() {
		return id + name + email;
	}
	
} // End class
