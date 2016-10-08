package valley_match_windows;

import java.text.DecimalFormat;
import java.util.ArrayList;

/*
 * Valley Match Services
 * Entrant
 * (c) 2016 Matthew R. Manzi
 * 
 * Class to hold the data of a match to an entrant (with another entrant)
 * and the breakdown for how they match; in other words, one entrant will
 * have many instances of this class in an array, one of how well they match
 * all of the other entrant in the database
 * @author matteomanzi
 * @version 1.0 --- Jan 25, 2016
 * @version 1.1 --- Jan 28, 2016 -- Added entrantGender instance data,
 * 									updated constructors, and added relevant
 * 									getter and setter
 * @version 1.2 --- Jan 29, 2016 -- Changes naming scheme of variables and methods,
 * 									added extra instance data for more object-
 * 									oriented style of data storage, and removed
 * 									all setter methods
 * @version 1.2.1 --- Feb 19, 2016 -- Last minute fix to the algorithm to determine
 * 									  final score percentage that better represents
 * 									  the percentage of match with another entrant
 * 
 */
public class Entrant {

	// INSTANCE DATA \\
	private final float TOTAL_POINTS = 488.0f;
	private final float FLOOR = 10.0f;
	private final float CAP = 82.0f;
	private String name;
	private String email;
	private int id;
	private String gender;
	private String grade;
	private ArrayList<Criterion> criteria;
	private float finalScore;
	private final DecimalFormat DF = new DecimalFormat("0.##");
	
	// CONSTRUCTORS \\
	public Entrant() {
		this.name = null;
		this.email = null;
		this.id = -1;
		this.gender = null;
		this.grade = null;
		this.criteria = new ArrayList<Criterion>();
		this.finalScore = 0.0f;
	}
	
	public Entrant(String name, String email, int id, String gender, String grade) {
		this.name = name;
		this.email = email;
		this.id = id;
		this.gender = gender;
		this.grade = grade;
		this.criteria = new ArrayList<Criterion>();
		this.finalScore = 0.0f;
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

	public String getGender() {
		return gender;
	}

	public String getGrade() {
		return grade;
	}

	// UPDATE CRITERIA \\
	public void addCriterion(String title, int id, float score) {
		criteria.add(new Criterion(title, id, score));
	}
	
	// GET SCORES \\
	public float getFinalScore() {
		
		float tempScore = 0.0f;
		
		for (Criterion criterion: criteria) {
			tempScore += criterion.getScore();
		}
		
		finalScore = tempScore;
		
		return finalScore;
	}
	
	public float getFinalScorePercentage() {
		
		float percent = (getFinalScore() / TOTAL_POINTS) * 100.0f;
		percent -= FLOOR;
		percent /= CAP - FLOOR;
		percent *= 100.0f;
		
		return percent;
	}
	
	// OTHER \\
	@Override
	public String toString() {
		return name + " - " + DF.format(getFinalScorePercentage()) + "%";
	}
	
} // End class
