package edu.ncsu.csc.Galant.algorithm.code;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Scanner;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import edu.ncsu.csc.Galant.GalantPreferences;
import edu.ncsu.csc.Galant.algorithm.Algorithm;
import edu.ncsu.csc.Galant.algorithm.code.macro.Macro;
import edu.ncsu.csc.Galant.algorithm.code.macro.MalformedMacroException;
import edu.ncsu.csc.Galant.logging.LogHelper;
import edu.ncsu.csc.Galant.GalantException;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * <p>
 * The "pseudo-compiler" &mdash; a rather misleading name, which is why I've called it something
 * that I think is more accurate.
 * </p>
 * <p>
 * The purpose of this class is to integrate the user's algorithm code into the running application.
 * It does so with the following steps:
 * <ol>
 * <li>Replacing any macros with the equivalent Java code.</li>
 * <li>Inserting the code into a basic class structure.</li>
 * <li>Compiling the completed Java class into a .class file, which is stored in the folder defined
 * by {@link GalantPreferences#OUTPUT_DIRECTORY}.</li>
 * <li>Loading the .class file into the program so that its <code>runAlgorithm</code> method can be
 * called.</li>
 * <li>Creating an <code>Algorithm</code> from the loaded class for easy reference.
 * </ol>
 * </p>
 */
public class CodeIntegrator	{
    public static final String METHOD_NAME = "runAlgorithm";

    public static final String PACKAGE = "edu.ncsu.csc.Galant.algorithm.code.compiled";

    /**
     * @todo These fields are initialized to meaningless values that will
     * be replaced by others later. Should be a better way.
     */
    private static final String IMPORTS_FIELD = "{Imports}";
    private static final String NAME_FIELD = "{Algorithm Name}";
    private static final String CODE_FIELD = "{User Code}";
    private static final String ALGORITHM_HEAD = "{Algorithm Head}";
    private static final String ALGORITHM_TAIL = "{Algorithm Tail}";
    private static final String ALGORITHM_BODY = "{Algorithm Body}";

    /**
     * Here is the real code that appears before and after the algorithm.
     */
    private static final String REAL_ALGORITHM_HEAD = "initialize();";
    private static final String REAL_ALGORITHM_TAIL = "finishAlgorithm();";

    // The basic class structure into which the user's code can be inserted so it can be
    // compiled.
    //@formatter:off
    private static final String CLASS_STRUCTURE =
        "package " + PACKAGE + ";" +
        "import java.util.*;" +
        "import edu.ncsu.csc.Galant.algorithm.Algorithm;" +
        "import edu.ncsu.csc.Galant.graph.component.Graph;" +
        "import edu.ncsu.csc.Galant.graph.component.Node;" +
        "import edu.ncsu.csc.Galant.graph.component.Edge;" +
        "import edu.ncsu.csc.Galant.algorithm.code.macro.Function;" +
        "import edu.ncsu.csc.Galant.algorithm.code.macro.Pair;" +
        "import edu.ncsu.csc.Galant.GalantException;" +
        "import edu.ncsu.csc.Galant.GraphDispatch;" +
        "import edu.ncsu.csc.Galant.algorithm.Terminate;" +
        IMPORTS_FIELD +
        "public class " + NAME_FIELD + " extends Algorithm" + "{" + CODE_FIELD + "}";

    public static final String ALGORITHM_STRUCTURE
        = "public void run(){ try {" +
        ALGORITHM_HEAD + ALGORITHM_BODY + ALGORITHM_TAIL
        + "}" + "catch (Exception e)"
        + " { if ( e instanceof Terminate ) { System.out.println(\"Terminate\"); } "
        + " else if ( e instanceof GalantException )"
        + " { GalantException ge = (GalantException) e;"
        + " ge.report(\"\"); ge.display(); }"
        + " else {e.printStackTrace(System.out);displayException(e);}}}" ;

    /**
     * Converts the unmodified user algorithm code into a proper Java class, as would be found
     * in a .java file.
     */
    // protected so it can be accessed by tests
    protected static String toJavaClass(String algorithmName, String userCode) throws MalformedMacroException
    {
        // separate imports from main code
        // find lines starting with "import"
        Matcher matcher = Pattern.compile("import.*;").matcher(userCode);
        int splitAt = 0;
        while(matcher.find())
            splitAt = matcher.end();
        // splitAt should be 1st char in 1st line not starting with "import"
        String imports = userCode.substring(0, splitAt);
        userCode = userCode.substring(splitAt);

        userCode = removeAllComments(userCode);

        // Rebuild the algorithm and add head or tail as needed

        if ( userCode.indexOf("algorithm") < 0 ) {
            throw new MalformedMacroException("Algorithm needs to be contained in 'algorithm { ... }'");
        }
        StringBuilder sb
            = new StringBuilder( userCode.substring(0, userCode.indexOf("algorithm")));
        sb.append(modifyAlgorithm( REAL_ALGORITHM_HEAD,
                                   REAL_ALGORITHM_TAIL,
                                   userCode ) );
        userCode = sb.toString();

        // apply macros
        for(Macro macro : Macro.MACROS) {
            userCode = macro.applyTo(userCode); }
        // apply generated macros, removing each one so if the code is recompiled,
        // you don't end up with incorrect/duplicate macros
        while(!Macro.GENERATED_MACROS.isEmpty())
            userCode = Macro.GENERATED_MACROS.remove(0).applyTo(userCode);

        // insert into class structure
        return CLASS_STRUCTURE.replace(NAME_FIELD, algorithmName).replace(CODE_FIELD,
                                                                          userCode).replace(IMPORTS_FIELD, imports);
    }

    /**
     * Integrates the given code into the program as a class with the given name.
     * @param algorithmName the name of the algorithm to be integrated.
     * @param userCode the code of the algorithm to be integrated.
     * @return an <code>Algorithm</code> object representing the algorithm.
     * @throws CompilationException if compiler errors occur.
     * @throws MalformedMacroException if there are errors in macro usage.
     */
    public static Algorithm integrateCode(String algorithmName, String userCode)
        throws CompilationException, MalformedMacroException, GalantException
    {
        // Make sure the name is a valid Java identifier
        StringBuilder nameBuilder = new StringBuilder(algorithmName.length());
        for(int i = 0; i < algorithmName.codePointCount(0, algorithmName.length()); i++)
            {
                int c = algorithmName.codePointAt(i);
                if(i == 0 ? Character.isJavaIdentifierStart(c) : Character
                   .isJavaIdentifierPart(c))
                    nameBuilder.appendCodePoint(c);
                else
                    nameBuilder.appendCodePoint('_');
            }
        String className = nameBuilder.toString();
        String qualifiedName = PACKAGE + "." + className;

        // Replace macros and insert into class structure
        String sourceCode = toJavaClass(className, userCode);
				
        // Display source code after macro processing
        LogHelper.showSourceCode(sourceCode);

        // Compile
        DiagnosticCollector<JavaFileObject> diagnostics =
            CompilerAndLoader.compile(qualifiedName, sourceCode);
        if(diagnostics != null)
            throw new CompilationException(diagnostics);
					
        // Load
        return CompilerAndLoader.loadAlgorithm(qualifiedName);
    }

    /**
     * Modify Algorithm as needed 
     * @param head code block to be appended before the substantial algorithm part
     * @param tail code block to be appended after the substantial algorithm part
     * @param userCode user code containing algorithm{}
     */
    public static String modifyAlgorithm(String head, String tail, String userCode) throws MalformedMacroException {
        String modifiedBody ;

        try{
            modifiedBody = getCodeBlock(userCode.substring(userCode.indexOf("algorithm"), userCode.length()));
        } catch (IOException e) {
            throw new MalformedMacroException() ;
        }

        try {
            return ALGORITHM_STRUCTURE.replace(ALGORITHM_HEAD, head).replace(ALGORITHM_TAIL, tail).replace(ALGORITHM_BODY, modifiedBody);				
        } catch (NullPointerException e) {
            return ALGORITHM_STRUCTURE.replace(ALGORITHM_HEAD, "").replace(ALGORITHM_TAIL, "").replace(ALGORITHM_BODY, modifiedBody);
        }
    }

    /**
     * Read and return code in between { and } in the given code
     * @param code code to be proceessed 
     */
    private static String getCodeBlock(String code) throws IOException {
        /* Convert code string to a BufferReader */
        // http://www.coderanch.com/t/519147/java/java/ignore-remove-comments-java-file
        InputStream codeInput = new ByteArrayInputStream(code.getBytes());
        BufferedReader reader = new BufferedReader(new InputStreamReader(codeInput));
			
        StringBuilder sb = new StringBuilder();
			
        /* Increment counter when meet a {, decrement it when meet a } 
         * Stop when counter hit 0;
         */
        int counter = 0;
			
        /* If false, means that the first { has not been read yet. This circumstance will not stop the 
         * reader even though counter is 0;
         */
        boolean firstEncounter = false;
        boolean startScan = false;
			
        int char1 = reader.read();
        if (char1 != -1) {
            do {
                if(char1 == '{') {
                    counter ++;
                    firstEncounter = true;
                } else if(char1 == '}'){
                    counter --;
                }

                if(firstEncounter == true) {
                    sb.append((char)char1);
                    if(counter == 0) {
                        break;
                    }
                }

                char1 = reader.read();
					
            } while (char1 != -1);
            // use substring to ignore the first pair of { and }. They are mean for marking the start and end of algorithm {}	
        }	return sb.toString().substring(1, sb.length() - 1);
    }

    public enum State {
        DEFAULT,            // not in comment, last char not '/'
            SLASH,          // not in comment, last char is '/'
            SLASH_STAR,     // in C-style comment, last char not '*'
            STAR,           // in C-style comment, last char is '*'
            SLASH_SLASH,    // in C++-style comment
            IN_STRING       // inside a double-quoted string (escapes not
                            // recognized for now)
            }

    /** 
     * @return code with all the comments removed; uses a simple
     * finite-state machine; preserves line numbering -- all line breaks
     * remain intact.
     */
    private static String removeAllComments(String code) {
        LogHelper.logDebug("-> removeAllComments, code =\n" + code);

        String withoutComments = "";
        State state = State.DEFAULT;

        for ( int i = 0; i < code.length(); i++ ) {
            char current = code.charAt(i);
            LogHelper.logDebug("  current = " + current);
            if ( state == State.DEFAULT ) {
                LogHelper.logDebug("    state = DEFAULT");
                if ( current == '/' ) state = State.SLASH;
                else {
                    if ( current == '"' ) state = State.IN_STRING; 
                    withoutComments += current;
                }
            }
            else if ( state == State.SLASH ) {
                LogHelper.logDebug("    state = SLASH");
                if ( current == '*' ) state = State.SLASH_STAR;
                else if ( current == '/' ) state = State.SLASH_SLASH;
                else {
                    state = State.DEFAULT;
                    withoutComments += '/';
                    withoutComments += current;
                }
            }
            else if ( state == State.SLASH_STAR ) {
                LogHelper.logDebug("    state = SLASH_STAR");
                if ( current == '*' ) state = State.STAR;
                else if ( current == '\n' ) withoutComments += current;
                // do nothing otherwise -- in a comment
            }
            else if ( state == State.STAR ) {
                LogHelper.logDebug("    state = STAR");
                if ( current == '/' ) state = State.DEFAULT;
                else {
                    state = State.SLASH_STAR;
                    if ( current == '\n' ) withoutComments += current;
                }
            }
            else if ( state == State.SLASH_SLASH ) {
                LogHelper.logDebug("    state = SLASH_SLASH");
                if ( current == '\n' ) {
                    state = State.DEFAULT;
                    withoutComments += current;
                }
                // do nothing otherwise -- in a comment
            }
            else if ( state == State.IN_STRING ) {
                if ( current == '"' ) state = state.DEFAULT;
                withoutComments += current;
            }
        }
        LogHelper.logDebug("<- removeAllComments, withoutComments =\n"
                           + withoutComments);
        return withoutComments;
    }
}

//  [Last modified: 2015 12 30 at 17:34:58 GMT]
