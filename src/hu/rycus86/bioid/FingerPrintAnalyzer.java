package hu.rycus86.bioid;

import hu.rycus86.bioid.util.ByteFlag;
import hu.rycus86.bioid.util.Log;
import hu.rycus86.bioid.util.Point;
import hu.rycus86.bioid.util.Serializer;
import hu.rycus86.bioid.util.Util;

import java.awt.AWTEvent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Main class for analyzing fingerprint images.
 * 
 * @author viktor.adam
 */
public class FingerPrintAnalyzer {
	
	/** The threshold used for binarization. */
	private static int BINARIZATION_THRESHOLD = 0x7F;
	
	/** The path of the image file. */
	private final String path;
	
	/** The original image. */
	private BufferedImage originalImage;
	/** The final image. */
	private BufferedImage processedImage;
	
	/** The list of coordinates for found endings. */
	private List<Point> endings;
	/** The list of coordinates for found branches. */
	private List<Point> branches;
	
	/** Constructor. */
	public FingerPrintAnalyzer(String path) {
		this.path = path;
	}
	
	/** Executes all operations on the original image and produces output. */
	private void execute(int binarizeThreshold) {
		try {
			originalImage = ImageIO.read(new File(path));
		} catch(Exception ex) {
			Log.error("Failed to read original image", ex);
		}
		
		if(originalImage == null) {
			return;
		}
		
		Log.info("Processing: " + path);
		
		BufferedImage binarized = Binarize.execute(originalImage, binarizeThreshold);
		BufferedImage thinned	= executeThinning(binarized);
		
		processedImage = Util.toRGBImage(thinned);
		produceTargetImage(thinned);
	}
	
	/** Executes the Thinning algorithm as many times as needed. */
	private BufferedImage executeThinning(BufferedImage image) {
		int runs = 0;
		
		ByteFlag changed = new ByteFlag(false);
		
		BufferedImage thinned = image;
		do {
			thinned = Thinning.execute(thinned, changed);
			runs++;
		} while(changed.get());

		Log.info("Ran thinning " + runs + " times");
		
		return thinned;
	}
	
	/** Produces the target image for presenting it to the user. */
	private void produceTargetImage(BufferedImage rawImage) {
		int width  = processedImage.getWidth();
		int height = processedImage.getHeight();
		
		Graphics2D graphics = processedImage.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
		
		Stroke defaultStroke = graphics.getStroke();
		Stroke dashedStroke  = new BasicStroke(1f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1f, new float[] { 3f, 3f }, 0f);
		
		graphics.setColor(Color.RED);
		endings = FindEndings.execute(rawImage, graphics);
		
		Set<Integer> processedAreas = new HashSet<>();
		processedAreas.add(0);
		
		for(Point pt : endings) {
			int area = pt.y / 50;
			if(processedAreas.contains(area)) continue;
			
			if(pt.x < width / 2) {
				graphics.setStroke(dashedStroke);
				graphics.drawLine(50, pt.y, pt.x - 5, pt.y);
				graphics.setStroke(defaultStroke);
				graphics.drawLine(40 - pt.y / 25, 30, 50, pt.y);
			
				processedAreas.add(area);
			}
		}
		
		String strEndings = endings.size() + " ending" + (endings.size() > 1 ? "s" : "");
		graphics.drawString(strEndings, 10, 30 - graphics.getFontMetrics().getDescent() - 2);
		
		graphics.setColor(Color.BLUE);
		branches = FindBranches.execute(rawImage, graphics);
		
		processedAreas.clear();
		processedAreas.add(height / 50);
		
		for(Point pt : branches) {
			int area = pt.y / 50;
			if(processedAreas.contains(area)) continue;
			
			if(pt.x > width / 2) {
				graphics.setStroke(dashedStroke);
				graphics.drawLine(pt.x + 5, pt.y, width - 50, pt.y);
				graphics.setStroke(defaultStroke);
				graphics.drawLine(width - 50, pt.y, width - 10 - pt.y / 25, height - 30);
			
				processedAreas.add(area);
			}
		}
		
		String strBranches = branches.size() + " branch" + (branches.size() > 1 ? "es" : "");
		graphics.drawString(strBranches, 
				width - graphics.getFontMetrics().stringWidth(strBranches) - 10, 
				height - 30 + graphics.getFontMetrics().getAscent() + 2);
	}
	
	/** Produces an output file from the results of the analyzation. */
	private void produceOutput(String outputName) {
		Serializer.write(path, outputName, endings, branches);
	}
	
	/** Presents the original and the processed image for the user on a graphical user interface. */
	private void presentResults() {
		SwingUtilities.invokeLater(new Runnable() {
			/* @see java.lang.Runnable#run() */
			@Override public void run() {
				BufferedImage image = processedImage;
				
				double ratio = (image.getWidth() * 2.0) / image.getHeight();
				
				GraphicsDevice gDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
				GraphicsConfiguration gConfiguration = gDevice.getDefaultConfiguration();
				int maxHeight = gConfiguration.getBounds().height;
				
				Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(gConfiguration);
				int height = (int) Math.min(image.getHeight() * 1.05f, maxHeight - screenInsets.top - screenInsets.bottom);
				int width  = (int) Math.round(height * ratio);
				
				if(width > gConfiguration.getBounds().width - screenInsets.left - screenInsets.right) {
					width  = gConfiguration.getBounds().width - screenInsets.left - screenInsets.right;
					height = (int) Math.round(width / ratio);
				}
				
				final JFrame frame = new JFrame("Fingerprint analyzer [MQNK3C]");
				frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				frame.setSize(width, height);
				frame.setLocationByPlatform(true);
				
				try {
					Image icon = ImageIO.read(getClass().getResource("/icon.png"));
					frame.setIconImage(icon);
				} catch(Exception ex) {
					Log.error("Failed to load icon image", ex);
				}
				
				JComponent panel = new JComponent() {
					private static final long serialVersionUID = 1L;

					/* @see javax.swing.JComponent#paintComponent(java.awt.Graphics) */
					@Override protected void paintComponent(Graphics g) {
						int imgWidth = getWidth() / 2;
						
						Graphics2D g2d = (Graphics2D) g;
						g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
						g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
						
						g2d.drawImage(originalImage, 0, 0, imgWidth, getHeight(), this);
						g2d.drawImage(processedImage, imgWidth, 0, imgWidth, getHeight(), this);
						
						g2d.setColor(Color.BLACK);
						g2d.setFont(g2d.getFont().deriveFont(Font.BOLD));
						
						g2d.drawString("Original image", 10, g2d.getFontMetrics().getHeight());
						
						int strWidth = g2d.getFontMetrics().stringWidth("Processed image");
						g2d.drawString("Processed image", getWidth() - strWidth - 10, g2d.getFontMetrics().getHeight());
						
						String prefix = "Image file path:";
						strWidth = Math.max(g2d.getFontMetrics().stringWidth(path), g2d.getFontMetrics().stringWidth(path));
						
						g2d.drawString(prefix, 
								imgWidth - strWidth, 
								getHeight() - g2d.getFontMetrics().getDescent() - g2d.getFontMetrics().getHeight() - 2);
						
						g2d.setFont(g2d.getFont().deriveFont(Font.PLAIN));
						g2d.drawString(path, 
								imgWidth - strWidth, 
								getHeight() - g2d.getFontMetrics().getDescent() - 2);
					}
				};
				frame.add(panel);
				
				frame.setVisible(true);
				
				Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
					/* @see java.awt.event.AWTEventListener#eventDispatched(java.awt.AWTEvent) */
					@Override public void eventDispatched(AWTEvent event) {
						if(!frame.isActive()) return;
						
						if(event instanceof KeyEvent) {
							KeyEvent kev = (KeyEvent) event;
							if(kev.getID() == KeyEvent.KEY_RELEASED && kev.getKeyCode() == KeyEvent.VK_ESCAPE) {
								frame.dispose();
							}
						}
					}
				}, KeyEvent.KEY_EVENT_MASK);
			}
		});
	}

	/** Main entry point for the application. */
	public static void main(String[] args) {
		if(args != null && args.length > 0) {
			String argument = args[0];
			if(argument != null && argument.matches("[0-9]+")) {
				// treat the first parameter as binarization threshold
				BINARIZATION_THRESHOLD = Integer.parseInt(argument);
				// shift arguments by one
				args = Arrays.copyOfRange(args, 1, args.length);
			}
		}
		
		String[] paths = args != null && args.length > 0 ? args : new String[0];
		
		if(paths.length == 0) {
			// default images
			paths = new String[] { "sample/fingerprint.png", "sample/fingerprint2.jpg" };
		}
		
		int processors = Runtime.getRuntime().availableProcessors();
		ExecutorService executor = Executors.newFixedThreadPool(processors);
		
		for(final String path : paths) {
			executor.execute(new Runnable() {
				/* @see java.lang.Runnable#run() */
				@Override public void run() {
					String name = path;
					if(name.contains("/")) name = name.substring(name.lastIndexOf('/') + 1);
					if(name.contains(".")) name = name.substring(0, name.lastIndexOf('.'));
					
					Log.init(name);
					
					FingerPrintAnalyzer analyzer = new FingerPrintAnalyzer(path);
					analyzer.execute(BINARIZATION_THRESHOLD);
					analyzer.produceOutput(name);
					analyzer.presentResults();
				}
			});
		}
		
		executor.shutdown();
	}
		
}
