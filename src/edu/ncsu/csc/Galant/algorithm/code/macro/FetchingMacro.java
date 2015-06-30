package edu.ncsu.csc.Galant.algorithm.code.macro;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import edu.ncsu.csc.Galant.algorithm.code.macro.MacroUtil.IdentifierPartLocation;
import edu.ncsu.csc.Galant.algorithm.code.macro.MacroUtil.NestedRegexResult;

/**
 * <p>
 * A macro of the form: <br />
 * <i>name</i> <i>variable</i><code>;</code>
 * </p>
 * <p>
 * Fetch and return Node/Edges on the graph. Declare the variable as global 
 * and initiliza them inside of algorithm{}.
 * </p>
 */
public abstract class FetchingMacro extends Macro
	{	
		String initializationPrefix = "";
		String name = "";

		/**
		 * Creates a new <code>FetchingMacro</code>. <code>name</code> speclify the name. 
		 * <code>intilizationPrefix</code> defines the expected variable type. e.g: for macro
		 * that gets the number of nodes on the graph, we use "int" as our <code>intilizationPrefix</code>.
		 */
		public FetchingMacro(String name, String initializationPrefix)
		{	
				
				super(name + "(.*)(\\;)");
				this.initializationPrefix = initializationPrefix;
				this.name = name;
		}

		@Override
		public String toString()
		{
				return super.toString();
		}

		@Override
		public String getName() {
			return name;
		}

		/**
		 * This method returns the variable name as a string. e.g: for macro that gets the number of nodes
		 * on the graph, this method would return <code>t</code> if input were <code>numOfNodes t;</code>.
		 * @param code the code of this macro apply to 
		 * @throws MalformedMacroException if input is not in the correct pattern or there is no <code>;</code> at
		 * the end of the syntax.
		 */
		public String getVariableName(String code) throws MalformedMacroException
		{
			Matcher matcher = getPattern().matcher(code); 
				if(matcher.find()) {
					String originalExpression = matcher.group(0);	
					System.out.println("for testing ->" + originalExpression + "<--");
					matcher = Pattern.compile("(\\s)(.*)(\\;)").matcher(originalExpression);
					try{
						matcher.find();
						return matcher.group(0).replace(";", "");
					} catch(Exception e) {
						throw new MalformedMacroException("Wrong macro sytax. Seperate variable with white space");
					}
			}
			return null;
		}

		@Override
		protected final String modify(String code, MatchResult match) throws MalformedMacroException
		{	
			return initializationPrefix + getVariableName(code);	
		}
	}
