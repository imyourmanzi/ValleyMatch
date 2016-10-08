package valley_match_windows;

/*
 * Valley Match Services
 * Criterion
 * (c) 2016 Matthew R. Manzi
 * 
 * Class to hold the value of a specific criterion from comparison between
 * a MatcheeEntrant and an Entrant
 * @author matteomanzi
 * @version 1.0 --- Jan 25, 2016
 * @version 1.1 --- Jan 29, 2016 -- Changed name of criterionName to title, added
 * 									extra instance data for more object-oriented
 * 									style of data storage
 * 
 */
public class Criterion {

	// INSTANCE DATA \\
	private String title;
	private int id; // Column index
	private float score;
	
	// CONSTRUCTORS \\
	public Criterion() {
		this.title = null;
		this.id = -1;
		this.score = 0.0f;
	}
	
	public Criterion(String title, int id, float score) {
		this.title = title;
		this.id = id;
		this.score = score;
	}

	// GETTERS & SETTERS \\
	public String getTitle() {
		return title;
	}
	
	public int getId() {
		return id;
	}

	public float getScore() {
		return score;
	}
	
	// OTHER \\
	@Override
	public String toString() {
		return title + ": " + Float.toString(score);
	}
	
} // End class
