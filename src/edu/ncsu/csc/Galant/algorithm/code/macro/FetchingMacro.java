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
 * Fetch and return Node/Edges on the graph. Declare the variable as global 
 * and initiliza them inside of algorithm{}.
 */
public abstract class FetchingMacro extends Macro
	{	
		String initializationPrefix = "";

		public FetchingMacro(String regex, String initializationPrefix)
		{	
				super(regex);
				this.initializationPrefix = initializationPrefix;
		}

		public String toString()
		{
				return super.toString();
		}

		public String getVariableName(String code) throws MalformedMacroException
		{
			Matcher matcher = getPattern().matcher(code); 
				if(matcher.find()) {
					String originalExpression = matcher.group(0);
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
