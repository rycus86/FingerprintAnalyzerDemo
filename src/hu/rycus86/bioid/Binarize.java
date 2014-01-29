package hu.rycus86.bioid;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

/**
 * Helper class for binarizing RGB images.
 * 
 * @author viktor.adam
 */
public class Binarize {
	
	/** Executes the binarization of the original RGB image with the given threshold. */
	public static BufferedImage execute(BufferedImage original, int threshold) {
		BufferedImage image = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		
		WritableRaster source = original.getRaster();
		WritableRaster target = image.getRaster();
		
		int[] pixel = new int[3];
		
		for(int x = 0; x < original.getWidth(); x++) {
			for(int y = 0; y < original.getHeight(); y++) {
				source.getPixel(x, y, pixel);
				
				int max = Math.max(pixel[0], Math.max(pixel[1], pixel[2]));
				
				pixel[0] = max > threshold ? 0xFF : 0x00;
				target.setPixel(x, y, pixel);
			}
		}
		
		return image;
	}
	
}
