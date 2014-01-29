package hu.rycus86.bioid;

import hu.rycus86.bioid.util.Log;
import hu.rycus86.bioid.util.Point;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.LinkedList;
import java.util.List;

/**
 * Helper class for finding endings on a binarized image.
 * 
 * @author viktor.adam
 */
public class FindEndings {

	/**
	 * Searches for endings on the given image.
	 * @param binarized A binarized image
	 * @param g A graphics for an image to draw circles around found endings
	 * @return The list of coordinates for the found endings
	 */
	public static List<Point> execute(BufferedImage binarized, Graphics g) {
		List<Point> foundPoints = new LinkedList<>();
		
		WritableRaster source = binarized.getRaster();
		
		int[] window = new int[9];
		
		boolean match = false;
		
		for(int y = 1; y < binarized.getHeight() - 2; y++) {
			for(int x = 1; x < binarized.getWidth() - 2; x++) {
				
				source.getPixels(x - 1, y - 1, 3, 3, window);
				
				if(window[4] != 0x00) {
					match = false;
				} else {
					int darkPixels = 0;
					for(int px : window) {
						if(px == 0x00) darkPixels++;
					}
					
					if(darkPixels == 3) {
						if(window[0] == 0x00) darkPixels--;
						if(window[2] == 0x00) darkPixels--;
						if(window[6] == 0x00) darkPixels--;
						if(window[8] == 0x00) darkPixels--;
					}
					
					match = darkPixels == 2;
				}
				
				if(match) {
					g.drawOval(x - 2, y - 2, 5, 5);
					foundPoints.add(new Point(x, y));
				}
				
			}
		}
		
		Log.info("Found " + foundPoints.size() + " ending(s)");
		
		return foundPoints;
	}
	
}
