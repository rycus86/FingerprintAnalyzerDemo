package hu.rycus86.bioid.util;

/**
 * Helper class to easily compute an intervals both end values.
 * 
 * @author viktor.adam
 */
public class Interval {
	
	/** Has any number processed yet? */
	private boolean initialized = false;

	/** The intervals minimum value. */
	private int min;
	/** The intervals maximum value. */
	private int max;
	
	/** Processes the given number: checks the interval limits. */
	public void process(int number) {
		if(initialized) {
			min = Math.min(min, number);
			max = Math.max(max, number);
		} else {
			min = number;
			max = number;
			initialized = true;
		}
	}
	
	/** Returns the current minimum value. */
	public int getMinimum() { return min; }
	/** Returns the current maximum value. */
	public int getMaximum() { return max; }
	
	/** Returns the length of the interval. */
	public int getLength() { return max - min; }
	
}