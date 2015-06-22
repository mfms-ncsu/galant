package edu.ncsu.csc.Galant.test;

import static org.junit.Assert.*;
import org.junit.Test;
import edu.ncsu.csc.Galant.algorithm.code.CodeIntegrator;
import edu.ncsu.csc.Galant.algorithm.code.macro.MalformedMacroException;

public class MacroTest extends CodeIntegrator
	{
		private static final String TEST_NAME = "Name";

		private static String getExpectedCode(String imports, String name, String whitespace, String funcType,
			String paramType, String code, String returnType, boolean assign, String args)
			{
				//@formatter:off
				return "package " + PACKAGE + ";" +
								"import java.util.LinkedList;" +
								"import java.util.Queue;" +
								"import edu.ncsu.csc.Galant.algorithm.Algorithm;" +
								"import edu.ncsu.csc.Galant.graph.component.Graph;" +
								"import edu.ncsu.csc.Galant.graph.component.Node;" +
								"import edu.ncsu.csc.Galant.graph.component.Edge;" +
								"import edu.ncsu.csc.Galant.algorithm.code.macro.Function;" +
								"import edu.ncsu.csc.Galant.algorithm.code.macro.Pair;" +
								imports +
								"public class " + TEST_NAME + " extends Algorithm" +
									"{" +
										"@Override " +
										"public void run()" +
											"{final Pair<" + funcType + ", Void> " + name + " = new Pair<" +
								funcType + ", Void>(null, null); " + funcType + " $" + name + " = new " + funcType +
								"(){public " + returnType + " invoke(" + paramType + " $args)" + whitespace + "{" +
								code + "}}; " + name + ".setElement1($" + name + "); " +
								(assign ? returnType + " var = " : "") + name + ".getElement1().invoke(" + args + ");" +
								String.format("%n}}");
				//@formatter:on
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
		public void testFunctionMacroNoParamsNoReturn() throws Exception
			{
				String userCode = "function a() { System.out.println('a'); } a();";
				String funcType = "Function<Void, Void>";
				String name = "a";
				String returnType = "Void";
				String paramType = "Void";
				String whitespace = " ";
				String code = " System.out.println('a'); return null;";
				doAssert(getExpectedCode("", name, whitespace, funcType, paramType, code, returnType, false, "null"), userCode);
			}

		@Test
		public void testFunctionMacro1ParamNoReturn() throws Exception
			{
				String userCode = "function b(int i) { System.out.println(i); } b(10);";
				doAssert(getExpectedCode("", "b", " ", "Function<Integer, Void>", "Integer",
					"int i = $args; System.out.println(i); return null;", "Void", false, "10"), userCode);
			}

		@Test
		public void testFunctionMacro2ParamsNoReturn() throws Exception
			{
				String userCode = "function c(int i, int j) { System.out.println(i + j); } c(4, 13);";
				doAssert(getExpectedCode("", "c", " ", "Function<Pair<Integer, Integer>, Void>", "Pair<Integer, Integer>",
					"int i = $args.getElement1();int j = $args.getElement2(); System.out.println(i + j); return null;",
					"Void", false, "new Pair<Integer, Integer>(4, 13)"), userCode);
			}

		@Test
		public void testFunctionMacro3ParamsNoReturn() throws Exception
			{
				String userCode = "function d(int i, int j, int k) { System.out.println(i + j + k); } d(11, 11, 11);";
				doAssert(getExpectedCode("", "d", " ", "Function<Pair<Integer, Pair<Integer, Integer>>, Void>",
					"Pair<Integer, Pair<Integer, Integer>>",
					"int i = $args.getElement1();int j = $args.getElement2().getElement1();"
						+ "int k = $args.getElement2().getElement2(); System.out.println(i + j + k); return null;", "Void",
					false, "new Pair<Integer, Pair<Integer, Integer>>(11, new Pair<Integer, Integer>(11, 11))"), userCode);
			}

		@Test
		public void testFunctionMacroNoParamsWithReturn() throws Exception
			{
				String userCode = "function double e() { return 2.718; } Double var = e();";
				doAssert(getExpectedCode("", "e", " ", "Function<Void, Double>", "Void", " return 2.718; ", "Double", true,
					"null"), userCode);
			}
	}
