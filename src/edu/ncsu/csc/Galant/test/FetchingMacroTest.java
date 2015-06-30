package edu.ncsu.csc.Galant.test;

import static org.junit.Assert.*;
import org.junit.Test;
import edu.ncsu.csc.Galant.algorithm.code.CodeIntegrator;
import edu.ncsu.csc.Galant.algorithm.code.macro.MalformedMacroException;

public class FetchingMacroTest extends CodeIntegrator {
	private static final String TEST_NAME = "Name";

	private static final String PACKAGE = "edu.ncsu.csc.Galant.algorithm.code.compiled";

	private static String getExpectedCode(String imports, String expectedType, String translatedExpression,String variableName)
		{
			return "package " + PACKAGE + ";\n" +
							"import java.util.*;\n" +
							"import edu.ncsu.csc.Galant.algorithm.Algorithm;\n" +
							"import edu.ncsu.csc.Galant.graph.component.Graph;\n" +
							"import edu.ncsu.csc.Galant.graph.component.Node;\n" +
							"import edu.ncsu.csc.Galant.graph.component.Edge;\n" +
							"import edu.ncsu.csc.Galant.algorithm.code.macro.Function;\n" +
							"import edu.ncsu.csc.Galant.algorithm.code.macro.Pair;\n" +
							"import edu.ncsu.csc.Galant.GalantException;\n" + 
							imports +
							"public class " + TEST_NAME + " extends Algorithm{\n" 
							+ expectedType + " " + variableName + ";"
							+ "public void run() { " 
							+ variableName + "=" + translatedExpression
							+ "\n}\n" 
							+ "}" ;
		}

	private static void doAssert(String expectedCode, String userCode)
		{
			try
				{
					assertEquals(expectedCode, toJavaClass(TEST_NAME, userCode));
				}
			catch(MalformedMacroException e)
				{
					fail(e.getMessage());
				}
	}

	@Test
	public void testNumOfNodes() throws Exception
		{
			 doAssert(getExpectedCode("", "int", " getNodes().size();", "currentNumberOfNodes"), 
					"numOfNodes currentNumberOfNodes;algorithm{}" );
					
			// System.out.println(toJavaClass(TEST_NAME, "numOfNodes currentNumberOfNodes; algorithm{}")); 
		}
	
	@Test
	public void testNodesList() throws Exception
		{
			 doAssert(getExpectedCode("", "Node[]", " new Node[ getNodes().size() ];", "currentNodesList"), 
					"nodesList currentNodesList;algorithm{}" );
					
			// System.out.println(toJavaClass(TEST_NAME, "numOfNodes currentNumberOfNodes; algorithm{}")); 
		}
	@Test
	public void testCommentLine() throws Exception
	{	
		
		doAssert(getExpectedCode("", "Node[]", " new Node[ getNodes().size() ];", "currentNodesList"), 
						"nodesList currentNodesList;algorithm{\n"
						+ "/** This is a test. \n"
						+ "* test \n"
						+ "*/ \n"
						+ "// test \n"
						+ "/* test */"
						+ "\n}"); 			
	}
}