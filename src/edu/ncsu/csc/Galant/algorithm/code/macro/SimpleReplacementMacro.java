package edu.ncsu.csc.Galant.algorithm.code.macro;

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A <code>Macro</code> that just replaces each match with a replacement string. The replacement
 * string is as described in {@link Matcher#appendReplacement(StringBuffer, String)}.
 */
public class SimpleReplacementMacro extends Macro
	{
		private String replacement;

		/**
		 * @param replacement the replacement string to use, as described in
		 *        {@link Matcher#appendReplacement(StringBuffer, String)}.
		 */
		public SimpleReplacementMacro(Pattern pattern, String replacement)
			{
				super(pattern);
				this.replacement = replacement;
			}
		/** @see #SimpleReplacementMacro(Pattern, String) */
		public SimpleReplacementMacro(String pattern, String replacement)
			{
				super(pattern);
				this.replacement = replacement;
			}

		@Override
		protected String modify(String code, MatchResult match)
			{
				return replacement;
			}

		@Override
		public boolean getIncludedInAlgorithm() {
			return false;
		}	
	}
