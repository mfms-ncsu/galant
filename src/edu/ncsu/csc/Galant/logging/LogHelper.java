package edu.ncsu.csc.Galant.logging;

/**
 * Debug class for monitoring GALANT's actions
 * @author Michael Owoc, modified by Matthias Stallmann
 */
public class LogHelper {

    private static final String INDENT_STRING = "..";
	private static LogHelper logHelper = null;
	private static boolean loggingEnabled = true;
    /**
     * Logging related to the graph panel, i.e., mouse actions and drawing,
     * clutter the loggin unnecessarily unless they are specifically
     * desired. - mfms
     */
    private static boolean guiLoggingEnabled = false;
    private static boolean savedState = loggingEnabled;
	
	private static int spaces = 0;
	public static LogHelper getInstance() {
		if (logHelper == null) {
			logHelper = new LogHelper();
		}
		
		return logHelper;
	}
	
	private LogHelper() {
	}

    public static void setEnabled( boolean enabled ) {
        savedState = loggingEnabled;
        loggingEnabled = enabled;
    }

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
        loggingEnabled = savedState;
    }
	
	public static void logDebug(String msg) {
		int lineCounter = 0;
		if (loggingEnabled) {
			// Split the msg/source code with new line,
			// then increment line counter
			for (String line: msg.split("\n")){
// 				lineCounter++;
//          		System.out.println("Line " + lineCounter + ": " + line);
         		System.out.println(line);
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
		spaces++;
		
		if (loggingEnabled)
			System.out.println(spaceString() + "-> " + cls.getName() + "." + methodName);
	}
	
	public static void exitMethod(Class<?> cls, String methodName) {
		if (loggingEnabled)
			System.out.println(spaceString() + "<- " + cls.getName() + "." + methodName);
		
		spaces--;
	}
	
	public static void guiLogDebug(String msg) {
		int lineCounter = 0;
		if ( guiLoggingEnabled ) {
			// Split the msg/source code with new line,
			// then increment line counter
			for (String line: msg.split("\n")){
				lineCounter++;
         		System.out.println("Line " + lineCounter + ": " + line);
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
		spaces++;
		
		if ( guiLoggingEnabled )
			System.out.println(spaceString() + "-> " + cls.getName() + "." + methodName);
	}
	
	public static void guiExitMethod(Class<?> cls, String methodName) {
		if ( guiLoggingEnabled )
			System.out.println(spaceString() + "<- " + cls.getName() + "." + methodName);
		
		spaces--;
	}
	
    public static void beginIndent() {
        spaces++;
    }

    public static void endIndent() {
        spaces--;
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

//  [Last modified: 2015 12 07 at 20:53:46 GMT]
