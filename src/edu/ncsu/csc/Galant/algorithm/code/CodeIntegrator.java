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

/**
 * @todo Exception handling needs work, but the bigger issue is creation of a
 * separate method that runs the algorithm along with an
 * "algorithm { <body> }"
 * syntax.
 * This would mitigate the weirdness of function declarations and global
 * variables.
 *
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
public class CodeIntegrator
	{
		public static final String METHOD_NAME = "runAlgorithm";

		public static final String PACKAGE = "edu.ncsu.csc.Galant.algorithm.code.compiled";

		// fields to represent variables in the class structure
		private static final String IMPORTS_FIELD = "{Imports}", NAME_FIELD = "{Algorithm Name}",
						CODE_FIELD = "{User Code}";

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
			IMPORTS_FIELD +
			"public class " + NAME_FIELD + " extends Algorithm" +

            /* @todo The following code needs to be put in the run method
				during macro replacement
						"{ try { <body of algorithm> }"
                                      + " catch (Exception e)"
                                      + " { if ( e instanceof GalantException )"
                                      + " {GalantException ge = (GalantException) e;"
                                      + " ge.report(\"\"); ge.display(); }"
                                      + " else e.printStackTrace(System.out);}%n }",
				"}";
            */

				"{" + CODE_FIELD + "}";
				
		//@formatter:on

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

				// Skip all the comment line then build the new source code  
				// Initilize a new String
				StringBuilder sb = new StringBuilder();

				Scanner scanner = new Scanner(userCode);
				while(scanner.hasNextLine()) {
					String line = scanner.nextLine();				
					Matcher cStyleMatcher = Pattern.compile("(.*)\\/\\*\\*(.*)").matcher(line);
					Matcher singleAsteriskMatcher = Pattern.compile("(.*)\\/\\*(.*)").matcher(line);
					Matcher doubleSlashMatcher = Pattern.compile("(.*)\\/\\/(\\s|.*)").matcher(line);


					if( cStyleMatcher.find() ) {
							sb.append(cStyleMatcher.group(1));

							Matcher behindCStyleMatcher = Pattern.compile("(\\*\\/)(.*)").matcher(cStyleMatcher.group(2));

							if( behindCStyleMatcher.find()) {
								// single line c style if falls in here

								sb.append(behindCStyleMatcher.group(2));
							} else {
								// multiple line c style comment if falls in here 
								// infinte loop keep reading until hit "*/""
								while(scanner.hasNextLine()) {
									line = scanner.nextLine();	
									behindCStyleMatcher = Pattern.compile("(\\*\\/)(.*)").matcher(line);

									if(behindCStyleMatcher.find()) {
										break;
									}
								}
								
								sb.append(behindCStyleMatcher.group(2));
							}
					} else if( singleAsteriskMatcher.find()) {
						sb.append(singleAsteriskMatcher.group(1));
						Matcher behindSingleAsterisk = Pattern.compile("(\\*\\/)(.*)").matcher(singleAsteriskMatcher.group(2));

						if( behindSingleAsterisk.find()) {
								// single-line single-asterisk if falls in here
								sb.append(behindSingleAsterisk.group(2));
						} else {
							while(scanner.hasNextLine()) {
								line = scanner.nextLine();
								behindSingleAsterisk = Pattern.compile("(\\*\\/)(.*)").matcher(line);

								if(behindSingleAsterisk.find()) {
									break;
								}
							}

							sb.append( behindSingleAsterisk.group(2));
						}	
					} else if( doubleSlashMatcher.find()) {
						// read everything after //
						sb.append(doubleSlashMatcher.group(1));
					} else if( line!= null && line.length()>0 ){
						sb.append(line + "\n");
					}
				}
				
				// Reassign userCode with comment line removed
				userCode = sb.toString();

				// apply macros
				for(Macro macro : Macro.MACROS) {
					System.out.println(macro.toString());
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
                LogHelper.setEnabled( true );
				LogHelper.logDebug(sourceCode);
                LogHelper.restoreState();

				// Compile
				DiagnosticCollector<JavaFileObject> diagnostics =
					CompilerAndLoader.compile(qualifiedName, sourceCode);
				if(diagnostics != null)
					throw new CompilationException(diagnostics);
					
				// Load
				return CompilerAndLoader.loadAlgorithm(qualifiedName);
			}
	}

//  [Last modified: 2015 06 30 at 15:57:34 GMT]
