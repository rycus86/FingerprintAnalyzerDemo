package hu.rycus86.bioid.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Helper class for serializing fingerprint analyzation results.
 * 
 * @author viktor.adam
 */
public class Serializer {
	
	/** Key for execution (serialization) date in the output file. */
	private static final String KEY_RUN_DATE 	= "DATE  ";
	/** Key for source image path in the output file. */
	private static final String KEY_SRC_PATH 	= "SOURCE";
	/** Key for ending positions on the source image in the output file. */
	private static final String KEY_ENDING		= "ENDING";
	/** Key for branch positions on the source image in the output file. */
	private static final String KEY_BRANCH		= "BRANCH";
	
	/**
	 * Writes out the results of an analyzation to a given file.
	 * @param sourcePath The path of the source image
	 * @param outputName A base name for the output file
	 * @param endings The list of endings found during the analyzation
	 * @param branches The list of branches found during the analyzation
	 */
	public static void write(String sourcePath, String outputName, List<Point> endings, List<Point> branches) {
		File outputDirectory = new File("output");
		outputDirectory.mkdir();
		
		String source	= new File(sourcePath).getAbsolutePath();
		String filename = outputName + ".dat";
		
		// SimpleDateFormat is not thread-safe so it is instantiated here every time
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd. HH:mm:ss ZZ");
		
		File outputFile = new File(outputDirectory, filename);
		
		try ( BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile)) ) {
			writer.append(KEY_RUN_DATE).append(": ");
			writer.append(dateFormat.format(new Date()));
			writer.newLine();
			
			writer.append(KEY_SRC_PATH).append(": ");
			writer.append(source);
			writer.newLine();
			
			if(!endings.isEmpty()) writer.newLine();
			
			for(Point pt : endings) {
				writer.append(KEY_ENDING).append(": ");
				writer.append(pt.x + " ; " + pt.y);
				writer.newLine();
			}
			
			if(!branches.isEmpty()) writer.newLine();
			
			for(Point pt : branches) {
				writer.append(KEY_BRANCH).append(": ");
				writer.append(pt.x + " ; " + pt.y);
				writer.newLine();
			}
			
			writer.append( Util.createRateListInfo(endings, branches) );
		} catch(Exception ex) {
			Log.error("Failed to write analyzation results to file", ex);
		}
		
		Log.info("Output file written: " + outputFile.getPath());
	}
	
}
