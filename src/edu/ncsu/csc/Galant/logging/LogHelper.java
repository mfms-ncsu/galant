package edu.ncsu.csc.Galant.logging;

import java.util.Stack;

/**
 * Debug class for monitoring GALANT's actions
 * @author Michael Owoc, modified by Matthias Stallmann
 */
public class LogHelper {

    private static final String INDENT_STRING = "..";
	private static LogHelper logHelper = null;
	private static boolean loggingEnabled = false;

  /**
   * a stack is used in order to selectively enable/disable debug printing in
   * a sequence of nested method calls.
   */
  private static Stack<Boolean> savedStates = new Stack<Boolean>();

  /**
   * Logging related to the graph panel, i.e., mouse actions and drawing,
   * clutter the loggin unnecessarily unless they are specifically
   * desired. - mfms
   */
    private static boolean guiLoggingEnabled = false;

  /**
   * degree of indentation, i.e., the number of occurrences of INDENT_STRING
   */
	private static int spaces = 0;

    public static void setEnabled( boolean enabled ) {
      savedStates.push(loggingEnabled);
        loggingEnabled = enabled;
    }

    public static void enable() { setEnabled(true); }
    public static void disable() { setEnabled(false); }

    public static boolean isEnabled() {
        return loggingEnabled;
    }

    /**
     * Restores the state that existed before the last call to setEnabled,
     * whether enabled or disabled. This allows for local logging and is, in
     * fact, necessary for compiler output in the current implementation.
     * - mfms
     */
    public static void restoreState() {
      if ( savedStates.isEmpty() ) {
        loggingEnabled = false;
      }
      else {
        loggingEnabled = savedStates.pop();
      }
    }

	public static void logDebug(String msg) {
		if (loggingEnabled) {
			for (String line: msg.split("\n")){
         		System.out.println(spaceString() + line);
      		}
		}
	}

	public static void enterConstructor(Class<?> cls) {
		spaces++;
		if (loggingEnabled)
			System.out.println(spaceString() + "=> " + cls.getName() + "()");
	}
	public static void exitConstructor(Class<?> cls) {
		if (loggingEnabled)
			System.out.println(spaceString() + "<= " + cls.getName() + "()");
		spaces--;
	}

	public static void enterMethod(Class<?> cls, String methodName) {
		if (loggingEnabled) {
            spaces++;
			System.out.println(spaceString() + "-> " + cls.getName() + "." + methodName);
        }
	}

	public static void exitMethod(Class<?> cls, String methodName) {
		if (loggingEnabled) {
			System.out.println(spaceString() + "<- " + cls.getName() + "." + methodName);
            spaces--;
        }
	}

	public static void guiLogDebug(String msg) {
		if ( guiLoggingEnabled ) {
			for (String line: msg.split("\n")){
         		System.out.println(spaceString() + line);
      		}
		}
	}

	public static void guiEnterConstructor(Class<?> cls) {
		spaces++;
		if ( guiLoggingEnabled )
			System.out.println(spaceString() + "=> " + cls.getName() + "()");
	}

	public static void guiExitConstructor(Class<?> cls) {
		if ( guiLoggingEnabled )
			System.out.println(spaceString() + "<= " + cls.getName() + "()");
		spaces--;
	}

	public static void guiEnterMethod(Class<?> cls, String methodName) {
		if ( guiLoggingEnabled ) {
            spaces++;
			System.out.println(spaceString() + "-> " + cls.getName() + "." + methodName);
        }
	}

	public static void guiExitMethod(Class<?> cls, String methodName) {
		if ( guiLoggingEnabled ) {
			System.out.println(spaceString() + "<- " + cls.getName() + "." + methodName);
            spaces--;
        }
	}

    public static void beginIndent() {
        spaces++;
    }

    public static void endIndent() {
        spaces--;
    }

    public static void showSourceCode(String code) {
        int lineCounter = 0;
        for (String line: code.split("\n")) {
            lineCounter++;
            System.out.println("" + lineCounter + ": " + line);
        }
    }

	private static String spaceString() {
		String spaceStr = "";
		if (spaces > 0) {
			int spc = 0;
			while (spc < spaces) {
				spaceStr = spaceStr + INDENT_STRING;
				spc++;
			}
		}
		return spaceStr;
	}
}

//  [Last modified: 2017 01 10 at 14:50:35 GMT]
