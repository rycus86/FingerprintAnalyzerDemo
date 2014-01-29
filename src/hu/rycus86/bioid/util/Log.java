package hu.rycus86.bioid.util;

/**
 * Helper class for logging.
 * 
 * @author viktor.adam
 */
public class Log {

	/** The name of the logger on the current thread. */
	private static final ThreadLocal<String> name = new ThreadLocal<String>() {
		/* @see java.lang.ThreadLocal#initialValue() */
		@Override protected String initialValue() { return "anonymous"; }
	};
	
	/** Initializes the logger with the given name. */
	public static void init(String name) {
		Log.name.set(name);
	}
	
	/** Prints an info message on the system console. */
	public static void info(String message) {
		info(message, null);
	}
	
	/** Prints an info message and (optionally) an exception on the system console. */
	public static void info(String message, Throwable th) {
		System.out.println(Log.name.get() + ": " + message);
		if(th != null) th.printStackTrace(System.out);
	}
	
	/** Prints an error message and (optionally) an exception on the system console. */
	public static void error(String message, Throwable th) {
		System.err.println(Log.name.get() + ": " + message);
		if(th != null) th.printStackTrace(System.err);
	}
	
}
