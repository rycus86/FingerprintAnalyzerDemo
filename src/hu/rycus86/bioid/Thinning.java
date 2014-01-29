package hu.rycus86.bioid;

import hu.rycus86.bioid.util.ByteFlag;
import hu.rycus86.bioid.util.Util;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

/**
 * Helper class for executing a Thinning algorithm.
 * 
 * @see
 *   http://homepages.inf.ed.ac.uk/rbf/HIPR2/thin.htm
 *   http://homepages.inf.ed.ac.uk/rbf/HIPR2/hitmiss.htm
 * 
 * @author viktor.adam
 */
public class Thinning {
	
	/**
	 * Executes the operation.
	 * @param binarized A binarized image
	 * @param changed Flag to signal if execution changed the original image
	 * @return A gray 1-byte thinned image
	 */
	public static BufferedImage execute(BufferedImage binarized, ByteFlag changed) {
		changed.set(false);
		
		int width  = binarized.getWidth();
		int height = binarized.getHeight();
		
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		
		WritableRaster source = binarized.getRaster();
		WritableRaster target = image.getRaster();

		int[] tmask = thinning_kernel;
		int[] hmask = hit_and_miss_mask;
		
		for(int rotate = 0; rotate < 4; rotate++) {
			changed.or( apply(source, target, width, height, tmask) );
			tmask = Util.rotateCounterClockwise(tmask);
			
			source.setRect(target);
			
			changed.or( apply(source, target, width, height, hmask) );
			hmask = Util.rotateCounterClockwise(hmask);
			
			source.setRect(target);
		}
		
		return image;
	}

	/** Applies the given kernel on the source image data and modifies the target image data. */
	private static boolean apply(WritableRaster source, WritableRaster target, int width, int height, int[] kernel) {
		boolean changed = false;
		
		int[] pixel  = new int[1];
		int[] window = new int[9];
		
		for(int x = 1; x < width - 2; x++) {
			for(int y = 1; y < height - 2; y++) {
				source.getPixels(x - 1, y - 1, 3, 3, window);
				
				if(window[4] > 0x00) {
					pixel[0] = 0xFF;
				} else if(Util.matches(window, kernel)) {
					pixel[0] = 0xFF;
					changed = true;
				} else {
					pixel[0] = 0x00;
				}
				
				target.setPixel(x, y, pixel);
			}
		}
		
		return changed;
	}
	
	/** Mask/kernel for the hit-and-miss operation. */
	private static int[] hit_and_miss_mask = new int[] {
		// 0xAA -- don't care
		0xAA, 0xFF, 0xFF,
		0x00, 0x00, 0xFF,
		0x00, 0x00, 0xAA
	};
	
	/** Mask/kernel for the thinning operation. */
	private static int[] thinning_kernel = new int[] {
		// 0xAA -- don't care
		0xFF, 0xFF, 0xFF,
		0xAA, 0x00, 0xAA,
		0x00, 0x00, 0x00
	};
	
}
