package edu.ncsu.csc.Galant.algorithm.code;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Locale;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import edu.ncsu.csc.Galant.GalantPreferences;
import edu.ncsu.csc.Galant.algorithm.Algorithm;
import edu.ncsu.csc.Galant.gui.util.ExceptionDialog;
import edu.ncsu.csc.Galant.GalantException;

/**
 * Dynamic code compilation class
 * @see http://www.accordess.com/wpblog/an-overview-of-java-compilation-api-jsr-199/
 */
public class CompilerAndLoader
	{
		/**
		 * Does the required object initialization and compilation.
		 * @param qualifiedName The qualified name of the class to compile.
		 * @param sourceCode The source code to compile.
		 * @return if there were compilation errors: a <code>DiagnosticCollector</code> containing <code>Diagnostic</code>s
		 *         detailing the errors; otherwise, <code>null</code>.
		 */
		public static DiagnosticCollector<JavaFileObject> compile(String qualifiedName, String sourceCode)
			{
				// Create output directory
				File outputDir = GalantPreferences.OUTPUT_DIRECTORY.get();

				if(!outputDir.exists())
					outputDir.mkdirs();

				/* Creating dynamic java source code file object */
				SimpleJavaFileObject fileObject = new DynamicJavaSourceCodeObject(qualifiedName, sourceCode);
				JavaFileObject javaFileObjects[] = new JavaFileObject[]{fileObject};

				/* Instantiating the java compiler */
				JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

				/**
				 * Retrieving the standard file manager from compiler object, which is used to provide basic building block for
				 * customizing how a compiler reads and writes to files. The same file manager can be reopened for another
				 * compiler task. Thus we reduce the overhead of scanning through file system and jar files each time
				 */
				StandardJavaFileManager stdFileManager = compiler.getStandardFileManager(null, Locale.getDefault(), null);

				/*
				 * Prepare a list of compilation units (java source code file objects) to input to
				 * compilation task
				 */
				Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(javaFileObjects);

				/* Prepare any compilation options to be used during compilation */
				// In this example, we are asking the compiler to place the output files under bin
				// folder.
				String[] compileOptions = new String[]{"-d", outputDir.getPath()};
				Iterable<String> compilationOptionss = Arrays.asList(compileOptions);

				/* Create a diagnostic controller, which holds the compilation problems */
				DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();

				/*
				 * Create a compilation task from compiler by passing in the required input objects
				 * prepared above
				 */
				CompilationTask compilerTask =
					compiler.getTask(null, stdFileManager, diagnostics, compilationOptionss, null, compilationUnits);

				// Perform the compilation by calling the call method on compilerTask object.
				boolean status = compilerTask.call();
				
				// Want to delete class files on exit.

				// The class file was deleted after each compilation.
				// removeClassFiles(outputDir);

				if(!status)
					{// If compilation error occurs
						return diagnostics;
					}
				try
					{
						stdFileManager.close();// Close the file manager
					}
				catch(IOException e)
					{
						ExceptionDialog.displayExceptionInDialog(e);
					}
				return null;
			}

		/**
		 * Loads the class with the given name as an <code>Algorithm</code>, and returns an instance of it.
		 * @param qualifiedName the qualified name of the class to load.
		 * @return an <code>Algorithm</code> object representing the desired algorithm.
		 */
		public static Algorithm loadAlgorithm(String qualifiedName)
			{
				try
					{
						ClassLoader cl =
							new URLClassLoader(new URL[]{GalantPreferences.OUTPUT_DIRECTORY.get().toURI().toURL()});
						return cl.loadClass(qualifiedName).asSubclass(Algorithm.class).newInstance();
					}
				catch(Exception e)
					{	
						ExceptionDialog.displayExceptionInDialog(e);
					}
                return null;
			}
		private static void removeClassFiles(File directory)
			{
				for(File file : directory.listFiles())
					{
						if(file.isDirectory())
							removeClassFiles(file);
						if(file.isFile() && file.getName().endsWith(".class"))
							file.deleteOnExit();
					}
			}
	}

/**
 * Creates a dynamic source code file object This is an example of how we can prepare a dynamic java source code for
 * compilation. This class reads the java code from a string and prepares a JavaFileObject
 */
class DynamicJavaSourceCodeObject extends SimpleJavaFileObject
	{
		private String qualifiedName;
		private String sourceCode;

		/**
		 * Converts the name to an URI, as that is the format expected by JavaFileObject
		 * @param name fully qualified name given to the class file
		 * @param code the source code string
		 */
		protected DynamicJavaSourceCodeObject(String name, String code)
			{
				super(URI.create("string:///" + name.replaceAll("\\.", "/") + Kind.SOURCE.extension), Kind.SOURCE);
				this.qualifiedName = name;
				this.sourceCode = code;
			}

		@Override
		public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException
			{
				return sourceCode;
			}

		public String getQualifiedName()
			{
				return qualifiedName;
			}

		public void setQualifiedName(String qualifiedName)
			{
				this.qualifiedName = qualifiedName;
			}

		public String getSourceCode()
			{
				return sourceCode;
			}

		public void setSourceCode(String sourceCode)
			{
				this.sourceCode = sourceCode;
			}
	}

//  [Last modified: 2015 12 11 at 15:49:35 GMT]
