package hu.rycus86.bioid;

import hu.rycus86.bioid.util.Log;
import hu.rycus86.bioid.util.Point;
import hu.rycus86.bioid.util.Util;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.LinkedList;
import java.util.List;

/**
 * Helper class for finding branches on a binarized image.
 * 
 * @author viktor.adam
 */
public class FindBranches {

	/**
	 * Searches for branches on the given image.
	 * @param binarized A binarized image
	 * @param g A graphics for an image to draw circles around found branches
	 * @return The list of coordinates for the found branches
	 */
	public static List<Point> execute(BufferedImage binarized, Graphics g) {
		List<Point> foundPoints = new LinkedList<>();
		
		WritableRaster source = binarized.getRaster();
		
		int[] kernel = new int[9];

		for(int y = 1; y < binarized.getHeight() - 2; y++) {
			for(int x = 1; x < binarized.getWidth() - 2; x++) {
				
				source.getPixels(x - 1, y - 1, 3, 3, kernel);
				
				int[] m = branch_mask;
				boolean match = Util.matches(kernel, m);
				if(!match) {
					for(int rotate = 0; rotate < 3 && !match; rotate++) {
						m = Util.rotateCounterClockwise(m);
						match = Util.matches(kernel, m);
					}
				}
				
				if(match) {
					for(int idx = 0; idx < 9 && match; idx++) {
						if(idx == 4) continue;
						if(m[idx] == 0x00) {
							int dx = (idx % 3) - 1;
							int dy = (idx / 3) - 1;
							
							int maxlen = 10;
							int len = Util.branchLength(x, y, x + dx, y + dy, source, maxlen, m);
							if(len < maxlen) {
								match = false;
							}
						}
					}
				}
				
				if(match) {
					g.drawOval(x - 2, y - 2, 5, 5);
					foundPoints.add(new Point(x, y));
				}
				
			}
		}
		
		Log.info("Found " + foundPoints.size() + " branch(es)");
		
		return foundPoints;
	}
	
	/** A mask pattern for branch start detection. */
	private static int[] branch_mask = new int[] {
		// 0xAA -- don't care
		0xFF, 0x00, 0xFF,
		0x00, 0x00, 0x00,
		0xAA, 0xAA, 0xAA
	};
	
}
