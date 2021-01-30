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
 * <p> The "pseudo-compiler" &mdash; a rather misleading name, which is why
 * I've called it something that I think is more accurate.</p>
 * <p> The purpose of this class is to integrate the user's algorithm code
 * into the running application.  It does so with the following steps:
 * <ol>
 * <li>Stripping comments.
 * <li>Replacing any macros with the equivalent Java code.</li>
 * <li>Inserting the code into a basic class structure.</li>
 * <li>Compiling the completed Java class into a <code>.class</code> file,
 *  which is stored in the folder defined by
 * {@link GalantPreferences#OUTPUT_DIRECTORY}.</li>
 * <li>Loading the <code>.class</code> file into the program so that its
 * <code>runAlgorithm</code> method can be called.</li>
 * <li>Creating an <code>Algorithm</code> from the loaded class
 * for easy reference.</li>
 * </ol> </p>
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
     * the code that appears at the beginning and end of the algorithm, to
     * call on initialization and cleanup routines
     */
    private static final String REAL_ALGORITHM_HEAD = "initialize();";
    private static final String REAL_ALGORITHM_TAIL = "finishAlgorithm();";

    /**
     * The basic class structure into which the user's code can be inserted
     * so it can be compiled.
     * @formatter:off
     */
    private static final String CLASS_STRUCTURE =
        "package " + PACKAGE + ";" +
        "import java.util.*;" +
        "import java.awt.Point;" +
        "import edu.ncsu.csc.Galant.algorithm.Algorithm;" +
        "import edu.ncsu.csc.Galant.graph.component.Graph;" +
        "import edu.ncsu.csc.Galant.graph.component.Node;" +
        "import edu.ncsu.csc.Galant.graph.component.Edge;" +
        "import edu.ncsu.csc.Galant.graph.component.GraphElement;" +
        "import edu.ncsu.csc.Galant.graph.datastructure.NodeSet;" +
        "import edu.ncsu.csc.Galant.graph.datastructure.EdgeSet;" +
        "import edu.ncsu.csc.Galant.graph.datastructure.NodeList;" +
        "import edu.ncsu.csc.Galant.graph.datastructure.EdgeList;" +
        "import edu.ncsu.csc.Galant.graph.datastructure.NodeQueue;" +
        "import edu.ncsu.csc.Galant.graph.datastructure.EdgeQueue;" +
        "import edu.ncsu.csc.Galant.graph.datastructure.NodePriorityQueue;" +
        "import edu.ncsu.csc.Galant.graph.datastructure.EdgePriorityQueue;" +
        "import edu.ncsu.csc.Galant.algorithm.code.macro.Function;" +
        "import edu.ncsu.csc.Galant.algorithm.code.macro.Pair;" +
        "import edu.ncsu.csc.Galant.GalantException;" +
        "import edu.ncsu.csc.Galant.GraphDispatch;" +
        "import edu.ncsu.csc.Galant.algorithm.Terminate;" +
        IMPORTS_FIELD +
        "public class " + NAME_FIELD + " extends Algorithm" + "{" + CODE_FIELD + "}";

    /**
     * at the end of the algorithm code, various exceptions must be caught;
     * there is not a specific catch statement for each because it's not
     * known whether a given algorithm will include any function calls that
     * throw them; for example, an algorithm that does nothing to the display
     * may not even call a method that throws Terminate
     * that's why we need the "instanceof" tests
     */
  public static final String FINAL_EXCEPTION_HANDLING
    = "catch (Exception e) {"
    + " if (e instanceof Terminate) {"
    + "  System.out.println(\"Algorithm finished\");"
    + " }"
    + " else if (e instanceof GalantException) {"
    + "  GalantException ge = (GalantException) e;"
    + "  ge.report(\"\");"
    + "  try {ge.display();}"
    + "  catch (Terminate t) {" // in case user exits when exception is
                                // displayed
    + "   System.out.println(\"finished during exception display\");"
    + "  }"
    + " }"
    + " else {"
    + "  e.printStackTrace(System.out);displayException(e);"
    + " }"
    + "}";

    public static final String ALGORITHM_STRUCTURE
        = "public void run(){ try {" +
        ALGORITHM_HEAD + ALGORITHM_BODY + ALGORITHM_TAIL
        + "} "
        + FINAL_EXCEPTION_HANDLING
        + "}";

    /**
     * Converts the unmodified user algorithm code into a proper Java class,
     * as would be found in a .java file.
     */
    // protected so it can be accessed by tests
    protected static String toJavaClass(String algorithmName, String userCode)
        throws MalformedMacroException
    {
        // separate animator declared imports from main code; these are
        // assumed to be at the beginning of the program
        Matcher matcher = Pattern.compile("import.*;").matcher(userCode);
        int splitAt = 0;
        while ( matcher.find() )
            splitAt = matcher.end();
        // splitAt should be 1st char in 1st line not starting with "import"
        String imports = userCode.substring(0, splitAt);
        userCode = userCode.substring(splitAt);

        userCode = removeAllComments(userCode);

        // Rebuild the algorithm and add head or tail as needed; this
        // essentially expands the 'algorithm' macro
        int startOfAlgorithm = userCode.indexOf("algorithm");
        if ( startOfAlgorithm < 0 ) {
            throw new MalformedMacroException("Algorithm needs to be contained in 'algorithm { ... }'");
        }
        // everything up to the key word 'algorithm'
        StringBuilder sb
            = new StringBuilder(userCode.substring(0, startOfAlgorithm));

        // add the algorithm body as a run() method with calls on
        // initialization and cleanup methods
        sb.append(modifyAlgorithm(REAL_ALGORITHM_HEAD,
                                  REAL_ALGORITHM_TAIL,
                                  userCode));
        userCode = sb.toString();

        // apply macros
        for ( Macro macro : Macro.MACROS ) {
            userCode = macro.applyTo(userCode);
        }
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
        // Make sure the name is a valid Java identifier; use the file name
        // and replace all illegal characters with _'s
        int nameLength = algorithmName.length();
        StringBuilder nameBuilder = new StringBuilder(nameLength);
        for(int i = 0; i < algorithmName.codePointCount(0, nameLength); i++) {
            int c = algorithmName.codePointAt(i);
            if ( i == 0 ? Character.isJavaIdentifierStart(c)
                 : Character.isJavaIdentifierPart(c) )
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
        if ( diagnostics != null )
            throw new CompilationException(diagnostics);

        // Load
        return CompilerAndLoader.loadAlgorithm(qualifiedName);
    }

    /**
     * @returns a modified version of the algorithm structure with the
     * placeholders replaced by their real counterparts
     * @param head initialization code before the algorithm
     * @param tail cleanup code after algorithm
     * @param userCode user code within algorithm brackets
     */
    public static String modifyAlgorithm(String head, String tail, String userCode) throws MalformedMacroException {
        String modifiedBody;
        String modifiedAlgorithm;
        try{
            modifiedBody
                = getCodeBlock(userCode.substring(userCode.indexOf("algorithm"), userCode.length()));
        }
        catch (IOException e) {
            throw new MalformedMacroException("Something went wrong when processing algorithm block");
        }

        modifiedAlgorithm = ALGORITHM_STRUCTURE;
        modifiedAlgorithm = modifiedAlgorithm.replace(ALGORITHM_HEAD, head);
        modifiedAlgorithm = modifiedAlgorithm.replace(ALGORITHM_TAIL, tail);
        modifiedAlgorithm = modifiedAlgorithm.replace(ALGORITHM_BODY,
                                                      modifiedBody);
        return modifiedAlgorithm;
    }

    /**
     * Read and return code in between { and } in the given code
     * @param code code to be processed
     */
    private static String getCodeBlock(String code) throws IOException {
        /* Convert code string to a BufferReader */
        // http://www.coderanch.com/t/519147/java/java/ignore-remove-comments-java-file
        InputStream codeInput = new ByteArrayInputStream(code.getBytes());
        BufferedReader reader = new BufferedReader(new InputStreamReader(codeInput));
        StringBuilder sb = new StringBuilder();
        /* Increment counter when meet a {, decrement it when meet a }
         * Stop when counter hits 0 */
        int counter = 0;
        /* If false, means that the first { has not been read yet. This
         * circumstance will not stop the reader even though counter is 0;
         */
        boolean firstEncounter = false;
        int currentChar = reader.read();
        while ( currentChar != -1 ) {
            // not at end of stream yet
            if ( currentChar == '{' ) {
                counter++;
                firstEncounter = true;
            }
            else if ( currentChar == '}' ) {
                counter --;
            }

            if ( firstEncounter == true ) {
                sb.append((char)currentChar);
                if ( counter == 0 ) {
                    break;
                }
            }

            currentChar = reader.read();
        }
        // use substring to ignore the first pair of { and }. They are
        // mean for marking the start and end of algorithm {}
        return sb.toString().substring(1, sb.length() - 1);
    }

    public enum State {
        DEFAULT,                // not in comment, last char not '/'
        SLASH,                  // not in comment, last char is '/'
        SLASH_STAR,             // in C-style comment, last char not '*'
        STAR,                   // in C-style comment, last char is '*'
        SLASH_SLASH,            // in C++-style comment
        IN_STRING,              // inside a double-quoted string
        BACKSLASH               // inside a double-quoted string, escaped (\)
    }

    /**
     * @return code with all the comments removed; uses a simple
     * finite-state machine; preserves line numbering -- all line breaks
     * remain intact.
     *
     * @todo Use a StringBuffer to make this more efficient (?); probably
     * won't make a difference unless the algorithm has thousands of lines.
     */
    private static String removeAllComments(String code) {
        LogHelper.logDebug("-> removeAllComments, code =\n" + code);

        String withoutComments = "";
        State state = State.DEFAULT;

        for ( int i = 0; i < code.length(); i++ ) {
            char current = code.charAt(i);
            //LogHelper.logDebug("  current = " + current);
            if ( state == State.DEFAULT ) {
                //LogHelper.logDebug("    state = DEFAULT");
                if ( current == '/' ) state = State.SLASH;
                else {
                    if ( current == '"' ) state = State.IN_STRING;
                    withoutComments += current;
                }
            }
            else if ( state == State.SLASH ) {
                //LogHelper.logDebug("    state = SLASH");
                if ( current == '*' ) state = State.SLASH_STAR;
                else if ( current == '/' ) state = State.SLASH_SLASH;
                else {
                    state = State.DEFAULT;
                    withoutComments += '/';
                    withoutComments += current;
                }
            }
            else if ( state == State.SLASH_STAR ) {
                //LogHelper.logDebug("    state = SLASH_STAR");
                if ( current == '*' ) state = State.STAR;
                else if ( current == '\n' ) withoutComments += current;
                // do nothing otherwise -- in a comment
            }
            else if ( state == State.STAR ) {
                //LogHelper.logDebug("    state = STAR");
                if ( current == '/' ) state = State.DEFAULT;
                else {
                    state = State.SLASH_STAR;
                    if ( current == '\n' ) withoutComments += current;
                }
            }
            else if ( state == State.SLASH_SLASH ) {
                //LogHelper.logDebug("    state = SLASH_SLASH");
                if ( current == '\n' ) {
                    state = State.DEFAULT;
                    withoutComments += current;
                }
                // do nothing otherwise -- in a comment
            }
            else if ( state == State.IN_STRING ) {
                if ( current == '\\' ) state = State.BACKSLASH;
                if ( current == '"' ) state = state.DEFAULT;
                withoutComments += current;
            }
            else if ( state == State.BACKSLASH ) {
                state = State.IN_STRING;
                withoutComments += current;
            }
        }
        LogHelper.logDebug("<- removeAllComments, withoutComments =\n"
                           + withoutComments);
        return withoutComments;
    }
}

//  [Last modified: 2021 01 30 at 22:26:21 GMT]
