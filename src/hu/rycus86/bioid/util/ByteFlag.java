package hu.rycus86.bioid.util;

/**
 * Helper class storing a single boolean flag.
 * 
 * @author viktor.adam
 */
public class ByteFlag {

	/** The actual value. */
	private boolean value;
	
	/** Constructor. */
	public ByteFlag(boolean initialValue) {
		this.value = initialValue;
	}
	
	/** Returns the current value. */
	public boolean get() { 
		return value; 
	}
	
	/** Sets the current value. */
	public void set(boolean value) {
		this.value = value;
	}
	
	/** Executes a logical OR operation with the given parameter. */
	public void or(boolean operand) {
		this.value |= operand;
	}
	
}
