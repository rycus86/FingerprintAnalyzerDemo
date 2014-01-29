package hu.rycus86.bioid.util;

/**
 * Helper class representing an integer-valued point with (x, y) coordinates.
 * 
 * @author viktor.adam
 */
public class Point {
	
	/** The X coordinate. */
	public int x;
	
	/** The Y coordinate. */
	public int y;
	
	/** Constructor. */
	public Point(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	/* @see java.lang.Object#equals(java.lang.Object) */
	@Override public boolean equals(Object obj) {
		if(obj instanceof Point) {
			return ((Point) obj).x == this.x && ((Point) obj).y == this.y;
		}
		return super.equals(obj);
	}
	
	/* @see java.lang.Object#toString() */
	@Override public String toString() {
		return "P(" + x + ", " + y + ")";
	}
	
	/* @see java.lang.Object#hashCode() */
	@Override public int hashCode() {
		return (x << 16) + y;
	}
	
}