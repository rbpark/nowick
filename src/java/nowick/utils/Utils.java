package nowick.utils;

import java.io.File;
import java.util.Collection;
import java.util.Random;

/**
 * A util helper class full of static methods that are commonly used.
 */
public class Utils {
	public static final Random RANDOM = new Random();

	/**
	 * Private constructor.
	 */
	private Utils() {
	}

	/**
	 * Equivalent to Object.equals except that it handles nulls. If a and b are
	 * both null, true is returned.
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static boolean equals(Object a, Object b) {
		if (a == null || b == null) {
			return a == b;
		}

		return a.equals(b);
	}

	/**
	 * Print the message and then exit with the given exit code
	 * 
	 * @param message
	 *            The message to print
	 * @param exitCode
	 *            The exit code
	 */
	public static void croak(String message, int exitCode) {
		System.err.println(message);
		System.exit(exitCode);
	}

	public static File createTempDir() {
		return createTempDir(new File(System.getProperty("java.io.tmpdir")));
	}

	public static File createTempDir(File parent) {
		File temp = new File(parent,
				Integer.toString(Math.abs(RANDOM.nextInt()) % 100000000));
		temp.delete();
		temp.mkdir();
		temp.deleteOnExit();
		return temp;
	}

	public static String flattenToString(Collection<?> collection, String delimiter) {
		StringBuffer buffer = new StringBuffer();
		for (Object obj : collection) {
			buffer.append(obj.toString());
			buffer.append(',');
		}

		if (buffer.length() > 0) {
			buffer.setLength(buffer.length() - 1);
		}
		return buffer.toString();
	}
	
	public static <T> T nonNull(T t) {
		if (t == null) {
			throw new IllegalArgumentException("Null value not allowed.");
		} else {
			return t;
		}
	}
}