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
 * <i>name</i><code>(</code><i>param1</i><code>, </code><i>param2</i><code>,</code> <i>...</i> <code>)</code>
 * </p>
 * <p>
 * Matches immediately following Java identifier parts are ignored: for example, if the name is "macro", then
 * "a_macro(&hellip;)" won't be counted as a match.
 * </p>
 * <p>
 * Subclasses of this class should override {@link #modifyMatch(String, MatchResult, String[], String, String)} rather than
 * {@link #modify(String, MatchResult)}, since this class needs to include some functionality in the latter.
 * </p>
 */
public abstract class ParameterizedMacro extends Macro
	{
		private static final int NAME_GROUP = 1;
		/** Note that this comes after any groups in the name, so this should be added to the count of such groups. */
		private static final int MATCH_GROUP = 2;

		private Pattern namePattern;
		private String name = null;
		private int minParams, maxParams;
		private boolean includeCodeBlock;

		/* If this field is true, then this macro will be included in algorithm{} no matter where it was initially set to be. 
		   This can be helpful when you want to create something will call getNodes().
		*/    

		/**
		 * Creates a new <code>ParameterizedMacro</code>. <code>minParams</code> and <code>maxParams</code> determine the
		 * number of parameters allowed; if the actual number of parameters is less than <code>minParams</code> or greater than
		 * <code>maxParams</code>, a {@link MalformedMacroException} will be thrown. If <code>maxParams</code> is negative or
		 * <code>minParams > maxParams</code>, any number of parameters will be accepted.
		 * @param name the name of the macro (can be a regex).
		 * @param minParams the minimum number of parameters the macro should take.
		 * @param maxParams the maximum number of parameters the macro should take.
		 */
		public ParameterizedMacro(String name, int minParams, int maxParams, boolean includeCodeBlock)
			{
				super(MacroUtil.nestedRegex(MacroUtil.replaceWhitespace("(" + name + ") \\("), (includeCodeBlock ? "\\}" : "\\)")));
				this.name = name;
				this.namePattern = Pattern.compile(name);
				this.minParams = minParams;
				this.maxParams = maxParams;
				this.includeCodeBlock = includeCodeBlock;
			}
		public ParameterizedMacro(String name, int numParams, boolean includeCodeBlock)
			{
				this(name, numParams, numParams, includeCodeBlock);
			}
		/** Defaults to not including code block */
		public ParameterizedMacro(String name, int numParams)
			{
				this(name, numParams, false);
			}
		/** Can have any number of params */
		public ParameterizedMacro(String name, boolean includeCodeBlock)
			{
				this(name, -1, includeCodeBlock);
			}

		@Override

		protected String includeInAlgorithm() {
			return null;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String toString()
			{
				return name == null ? namePattern.toString() : name;
			}

		@Override
		protected final String modify(String code, MatchResult match) throws MalformedMacroException
			{
				if(MacroUtil.isPartOfIdentifier(IdentifierPartLocation.BEFORE, code, match))
					return null;
				name = match.group(NAME_GROUP);
				Matcher nameMatcher = namePattern.matcher(name);
				nameMatcher.matches();
				MatchResult nameMatch = nameMatcher.toMatchResult();

				NestedRegexResult params =
					MacroUtil.evaluateNestedRegexMatch("(", ")", match.group(nameMatch.groupCount() + MATCH_GROUP), ",");

				NestedRegexResult block = null;
				String whitespace = null;
				if(includeCodeBlock)
					{
						String afterParams = params.getExtra();
						if(afterParams.endsWith(")"))
							afterParams = afterParams.substring(0, afterParams.length() - 1);
						Matcher betweenParamsAndBlock = Pattern.compile("(\\s*)\\{").matcher(afterParams);
						if(!betweenParamsAndBlock.lookingAt())
							throw new MalformedMacroException(toString() + ": curly braces required.");
						block =
							MacroUtil.evaluateNestedRegexMatch("{", "}", afterParams.substring(betweenParamsAndBlock.end()));
						whitespace = betweenParamsAndBlock.group(1);
					}

				SortedSet<Integer> commaIndices = params.getFoundIndices().get(",");

				int numParams = commaIndices.size() + 1;
				if(numParams == 1 && params.getMatch().trim().equals(""))
					numParams = 0;

				if(maxParams >= 0 && minParams <= maxParams)
					{
						// check number of parameters
						if(numParams < minParams || numParams > maxParams)
							throw new MalformedMacroException("Illegal number of parameters for macro " + toString() +
								". Expected: " + minParams + (maxParams == minParams ? "" : " to " + maxParams) +
								"; actual: " + numParams);
					}

				List<Integer> paramBounds = new ArrayList<Integer>();
				paramBounds.add(-1);
				paramBounds.addAll(commaIndices);
				paramBounds.add(params.getMatch().length());

				// build arg array
				String[] args = new String[numParams];
				for(int i = 0; i < numParams; i++)
					args[i] = params.getMatch().substring(paramBounds.get(i) + 1, paramBounds.get(i + 1)).trim();

				if(block == null)
					return modifyMatch(code, nameMatch, args, null, null) + Matcher.quoteReplacement(params.getExtra());
				return modifyMatch(code, nameMatch, args, whitespace, block.getMatch()) +
					Matcher.quoteReplacement(block.getExtra());
			}

		/**
		 * See {@link Macro#modify(String, MatchResult)}. The difference is that instead of being given a
		 * <code>MatchResult</code>, the implementation is given an array of arguments.
		 * @param code the user code on which this macro is being applied.
		 * @param nameMatch the result of matching the pattern passed in as the name to the part of the overall match that
		 *        matched that pattern. The main reason for this is access to any capturing groups in the name.
		 * @param args the arguments passed to this macro.
		 * @param whitespace the whitespace between the parameters and the code block (to keep the same number of lines), or
		 *        <code>null</code> if the code block is not included.
		 * @param block the code in the code block, or <code>null</code> if the code block is not included.
		 */
		protected abstract String modifyMatch(String code, MatchResult nameMatch, String[] args, String whitespace,
			String block);
	}

//  [Last modified: 2015 06 30 at 15:23:12 GMT]
