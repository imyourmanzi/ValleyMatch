package valley_match;

/*
 * Valley Match Services
 * Validity
 * (c) 2016 Matthew R. Manzi
 * 
 * Enum to describe the outcome of an entrant using
 * an access_code so that information may be shared
 * with said entrant in his/her specific situation
 * @author matteomanzi
 * @version 1.0 --- Feb 5, 2016
 * 
 */
public enum Validity {
	VALID(0),
	REUSE_ERROR(1),
	NOT_FOUND_ERROR(2),
	UNKNOWN_ERROR(99);
	
	private int value;
	
	private Validity(int value) {
		this.value = value;
	}
	
	public int getValue() {
		return value;
	}
	
}
