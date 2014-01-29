package hu.rycus86.bioid.util;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.text.NumberFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Helper class for various utility methods.
 * 
 * @author viktor.adam
 */
public class Util {
	
	/** Section header and footer in the output file for endings. */
	private static final String SECTION_ENDINGS		= "---------- ENDINGS  ----------";
	/** Section header and footer in the output file for branches. */
	private static final String SECTION_BRANCHES	= "---------- BRANCHES ----------";
	
	/** Returns true if the source window matches the given mask. */
	public static boolean matches(int[] src, int[] mask) {
		for(int idx = 0; idx < src.length; idx++) {
			if(mask[idx] == 0xAA) continue;
			if(src[idx] != mask[idx]) return false;
		}
		return true;
	}
	
	/** Rotates the given mask/kernel/window counter-clockwise. */
	public static int[] rotateCounterClockwise(int[] src) {
		return new int[] {
			src[2], src[5], src[8],
			src[1], src[4], src[7],
			src[0], src[3], src[6]
		};
	}
	
	/**
	 * Returns the legth of the branch starting at (x1, y1) going to (x2, y2).
	 * @param x1 The initial X coordinate
	 * @param y1 The initial Y coordinate
	 * @param x2 The next X coordinate
	 * @param y2 The next Y coordinate
	 * @param raster Raster object holding the image data
	 * @param limit Maximum number of steps to take
	 * @param initMask The initial mask/kernel that initiated this computation
	 */
	public static int branchLength(int x1, int y1, int x2, int y2, Raster raster, int limit, int[] initMask) {
		Set<Point> visited = new LinkedHashSet<>();
		visited.add(new Point(x1, y1));
		
		for(int dx = -1; dx <= 1; dx++) {
			for(int dy = -1; dy <= 1; dy++) {
				if(initMask[4 + dx + 3 * dy] == 0x00) { // skip zeros in initial mask
					visited.add(new Point(x1 + dx, y1 + dy));
				}
			}
		}
		
		int[] shx = new int[] { +0, +0, -1, +1, -1, -1, +1, +1 };
		int[] shy = new int[] { -1, +1, +0, +0, -1, +1, -1, +1};
		
		int[] window = new int[9];
		
		int moves = 0;
		
		boolean foundNext = false;
		
		do {
			visited.add(new Point(x2, y2));
			foundNext = false;
			
			if(x2 <= 0 || y2 <= 0) break;
			if(x2 + 1 >= raster.getWidth()) break;
			if(y2 + 1 >= raster.getHeight()) break;
			
			raster.getPixels(x2 - 1, y2 - 1, 3, 3, window);
			
			for(int i = 0; i < Math.min(shx.length, shy.length); i++) {
				int cx = shx[i];
				int cy = shy[i];
				int pos = 4 + cx + cy * 3;
				
				Point p = new Point(x2 + cx, y2 + cy);
				if(visited.contains(p)) continue;
				
				if(window[pos] == 0x00) {
					x1 = x2;
					y1 = y2;
					x2 = x2 + cx;
					y2 = y2 + cy;
					
					foundNext = true;
					moves++;
					break;
				}
			}
		} while(foundNext && moves < limit);
		
		return moves;
	}

	/** Copies the given image into an image of same type. */
	public static BufferedImage copyImage(BufferedImage original) {
		BufferedImage copy = new BufferedImage(original.getWidth(), original.getHeight(), original.getType());
		copy.setData(original.getData());
		return copy;
	}
	
	/** Copies the given image into a 1-byte gray image. */
	public static BufferedImage toByteImage(BufferedImage original) {
		BufferedImage copy = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		Graphics2D g = copy.createGraphics();
		g.drawImage(original, 0, 0, null);
		g.dispose();
		return copy;
	}
	
	/** Copies the given image into a 3-byte RGB image. */
	public static BufferedImage toRGBImage(BufferedImage original) {
		BufferedImage copy = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g = copy.createGraphics();
		g.drawImage(original, 0, 0, null);
		g.dispose();
		return copy;
	}
	
	/** Computes and describes the rate list for endings and branches. */
	public static String createRateListInfo(List<Point> endings, List<Point> branches) {
		Interval ix = new Interval();
		Interval iy = new Interval();
		
		for(Point pt : endings) {
			ix.process(pt.x);
			iy.process(pt.y);
		}
		
		for(Point pt : branches) {
			ix.process(pt.x);
			iy.process(pt.y);
		}
		
		double width  = ix.getLength();
		double height = iy.getLength();
		
		NumberFormat format = NumberFormat.getNumberInstance();
		format.setMinimumFractionDigits(5);
		format.setMaximumFractionDigits(5);
		
		StringBuilder builder = new StringBuilder();
		
		if(!endings.isEmpty()) {
			builder.append(System.getProperty("line.separator"));
			builder.append(SECTION_ENDINGS.replaceAll("(.*).", "$1>"));
			
			int index = 0;
			for(Point pt : endings) {
				if(index++ % 5 == 0) builder.append(System.getProperty("line.separator"));
				
				double xr = (pt.x - ix.getMinimum()) / width;
				double yr = (pt.y - iy.getMinimum()) / height;
				
				builder.append(format.format(xr)).append("x").append(format.format(yr)).append("e ");
			}
			
			builder.append(System.getProperty("line.separator"));
			builder.append(SECTION_ENDINGS.replaceAll(".(.*)", "<$1"));
		}
		
		if(!branches.isEmpty()) {
			builder.append(System.getProperty("line.separator"));
			builder.append(SECTION_BRANCHES.replaceAll("(.*).", "$1>"));
			
			int index = 0;
			for(Point pt : branches) {
				if(index++ % 5 == 0) builder.append(System.getProperty("line.separator"));
				
				double xr = (pt.x - ix.getMinimum()) / width;
				double yr = (pt.y - iy.getMinimum()) / height;
				
				builder.append(format.format(xr)).append("x").append(format.format(yr)).append("b ");
			}
			
			builder.append(System.getProperty("line.separator"));
			builder.append(SECTION_BRANCHES.replaceAll(".(.*)", "<$1"));
		}
		
		return builder.toString();
	}
	
}
