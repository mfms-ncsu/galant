package edu.ncsu.csc.Galant.algorithm.code.macro;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Manager class for Macros
 * @author Jason Cockrell, Ty Devries, Alex McCabe, Michael Owoc
 *
 */
public class MacroUtil
	{
		/** Pairs of strings that surround a section of code that is considered as a whole. */
		public static final List<Pair<Character, Character>> DEFAULT_BLOCK_DELIMITERS =
			new ArrayList<Pair<Character, Character>>();
		static
			{
				DEFAULT_BLOCK_DELIMITERS.add(new Pair<Character, Character>('(', ')'));
				DEFAULT_BLOCK_DELIMITERS.add(new Pair<Character, Character>('[', ']'));
				DEFAULT_BLOCK_DELIMITERS.add(new Pair<Character, Character>('{', '}'));
				// DEFAULT_BLOCK_DELIMITERS.add(new Pair<Character, Character>('<', '>'));
				// may be issues with stand-alone < and >
			}

		private MacroUtil()
			{}

		public static class NestedRegexResult
			{
				private String match;
				private Map<String, SortedSet<Integer>> foundIndices;
				private String extra;

				public NestedRegexResult(String match, Map<String, SortedSet<Integer>> foundIndices, String extra)
					{
						this.match = match;
						this.foundIndices = foundIndices;
						this.extra = extra;
					}
				/** The part of the match that is actually between the outer delimiters. */
				public String getMatch()
					{
						return match;
					}
				/** The indices of each string searched for. */
				public Map<String, SortedSet<Integer>> getFoundIndices()
					{
						return foundIndices;
					}
				/** The part of the match after the closing delimiter. */
				public String getExtra()
					{
						return extra;
					}
			}
		/**
		 * <p>
		 * Should be used when:
		 * </p>
		 * <ul>
		 * <li>You want to find a pair of {@linkplain #DEFAULT_BLOCK_DELIMITERS block delimiters}, and you need to account for
		 * nested blocks.</li>
		 * <li>You want to find something only at the top level: e.g., a comma-separated list that can include array
		 * initializations, like "a, b, new int[]{1, 2, 3}, d", where you want to find only the commas outside the {}s.</li>
		 * </ul>
		 * <p>
		 * <code>prefix</code> and <code>suffix</code> are regexes that are used to find a potentially valid block. The
		 * returned regex will match anything starting with prefix and ending with suffix;
		 * {@link #evaluateNestedRegexMatch(List, String, String, String, String...)} should be used to determine the true
		 * match, which may or may not be one you're looking for.
		 * </p>
		 * @see #evaluateNestedRegexMatch(List, String, String, String, String...)
		 */
		public static String nestedRegex(String prefix, String suffix)
			{
				return prefix + "(?s)(.*)" + suffix;
			}
		/**
		 * <p>
		 * Evaluate the match of a pattern returned by {@link #nestedRegex(String, String)}. <code>openDelim</code> and
		 * <code>closeDelim</code> are the pair of delimiters that contain the match; this method determines whether or not
		 * they're matching delimiters, and truncates the match to the first instance of <code>closeDelim</code>.
		 * </p>
		 * <p>
		 * All of the parameters of this method take literal strings, not regexes.
		 * </p>
		 * @param match the string that matched between the prefix and suffix; the {@linkplain MatchResult#group(int) capturing
		 *        group} between those in the prefix and those in the suffix.
		 * @param toFind a list of strings to find in the top level of the match
		 * @see #nestedRegex(String, String)
		 */
		public static NestedRegexResult evaluateNestedRegexMatch(List<Pair<Character, Character>> blockDelimiters,
			String openDelim, String closeDelim, String match, String... toFind) throws MalformedMacroException
			{
				// escape delimiters
				List<Pair<String, String>> escapedDelimiters = new ArrayList<Pair<String, String>>(blockDelimiters.size());
				for(Pair<Character, Character> delimiterPair : blockDelimiters)
					escapedDelimiters.add(new Pair<String, String>(escapeChar(delimiterPair.getElement1()),
						escapeChar(delimiterPair.getElement2())));

				// make regex to match anything that doesn't contain any delimiters
				StringBuilder noDelimitersRegexBuilder = new StringBuilder("[^");
				for(Pair<String, String> delimiterPair : escapedDelimiters)
					noDelimitersRegexBuilder.append(delimiterPair.getElement1() + delimiterPair.getElement2());
				String noDelimitersRegex = noDelimitersRegexBuilder.append("]*").toString();

				// find char that's neither a delimiter nor in toFind, to use as a temporary replacement
				char replacement = 0;
				for(boolean done = false; !done; replacement++)
					{
						for(Pair<Character, Character> delimiterPair : blockDelimiters)
							if(replacement == delimiterPair.getElement1() || replacement == delimiterPair.getElement2())
								continue;
						for(String stringToFind : toFind)
							if(stringToFind.contains("" + replacement))
								continue;
						done = true;
					}
				replacement--;

				StringBuilder topLevel = new StringBuilder(match);
				boolean foundNested;
				do
					{
						foundNested = false;
						for(Pair<String, String> escapedDelimPair : escapedDelimiters)
							{
								Pattern innerBlock =
									Pattern.compile(escapedDelimPair.getElement1() + noDelimitersRegex +
										escapedDelimPair.getElement2());
								// remove nested delimiters and anything from toFind inside them
								Matcher matcher;
								while((matcher = innerBlock.matcher(topLevel)).find())
									{
										foundNested = true;
										topLevel.setCharAt(matcher.start(), replacement);
										topLevel.setCharAt(matcher.end() - 1, replacement);
										for(String stringToFind : toFind)
											{
												int foundIndex;
												while((foundIndex = matcher.group().indexOf(stringToFind)) != -1)
													topLevel.setCharAt(matcher.start() + foundIndex, replacement);
											}
									}
							}
					}while(foundNested);

				// check if there's a closer ending delimiter
				boolean extra = false;
				int end = topLevel.indexOf(closeDelim);
				if(end == -1)
					end = topLevel.length();
				else
					extra = true;
				String topLevelToEnd = topLevel.substring(0, end);

				// check if the ending delimiter actually matches a different opening delimiter
				if(topLevelToEnd.indexOf(openDelim) != -1)
					throw new MalformedMacroException("Missing " + closeDelim);

				// find top-level toFind locations
				Map<String, SortedSet<Integer>> foundIndices = new HashMap<String, SortedSet<Integer>>();
				for(String stringToFind : toFind)
					{
						SortedSet<Integer> indexSet = new TreeSet<Integer>();
						foundIndices.put(stringToFind, indexSet);
						int nextIndex = 0;
						while((nextIndex = topLevelToEnd.indexOf(stringToFind, nextIndex + 1)) != -1)
							indexSet.add(nextIndex);
					}
				return new NestedRegexResult(match.substring(0, end), foundIndices, (extra ? match.substring(end +
					closeDelim.length()) +
					closeDelim : ""));
			}
		/**
		 * Calls {@link #evaluateNestedRegexMatch(List, String, String, String, String...)} with the
		 * {@link #DEFAULT_BLOCK_DELIMITERS}.
		 */
		public static NestedRegexResult evaluateNestedRegexMatch(String openDelim, String closeDelim, String match,
			String... toFind) throws MalformedMacroException
			{
				return evaluateNestedRegexMatch(DEFAULT_BLOCK_DELIMITERS, openDelim, closeDelim, match, toFind);
			}
		private static String escapeChar(char ch)
			{
				return (Character.isLetter(ch) ? "" : "\\") + ch;
			}

		public enum IdentifierPartLocation
			{
				BEFORE(true, false), AFTER(false, true), BOTH(true, true);

				private boolean before, after;
				private int numLocations;

				private IdentifierPartLocation(boolean before, boolean after)
					{
						this.before = before;
						this.after = after;
						numLocations = 0 + (before ? 1 : 0) + (after ? 1 : 0);
					}

				public Integer[] relevantCodePoints(String code, MatchResult match)
					{
						Integer[] codePoints = new Integer[numLocations];
						if(before)
							codePoints[0] = match.start() == 0 ? null : code.codePointBefore(match.start() - 1);
						int afterIndex = before ? 1 : 0;
						if(after)
							codePoints[afterIndex] = match.end() == code.length() ? null : code.codePointAt(match.end());
						return codePoints;
					}
			}
		/**
		 * Determines whether or not the given match is part of a Java identifier in the given code (in which case it may not
		 * actually be a proper match of the macro). That is, determines whether or not the code points before and/or after the
		 * given match are parts of Java identifiers.
		 * @param location where to look for Java identifier parts.
		 * @param code the code in which the given match was found.
		 * @param match a potential match.
		 * @return whether or not one or more Java identifier parts were found.
		 */
		public static boolean isPartOfIdentifier(IdentifierPartLocation location, String code, MatchResult match)
			{
				for(Integer codePoint : location.relevantCodePoints(code, match))
					if(codePoint != null && Character.isJavaIdentifierPart(codePoint))
						return true;
				return false;
			}

		/**
		 * Replaces named group references of the form "${<i>name</i>}" in a replacement string with their equivalent
		 * Java-6-compatible group-index references of the form "$<i>i</i>".
		 * @param replacementString a replacement string containing named group references.
		 * @param names the names used in the string, in the order of their groups (e.g., the first name is the first group,
		 *        $1).
		 */
		public static String replaceNames(String replacementString, String... names)
			{
				for(int i = 0; i < names.length; i++)
					replacementString = replacementString.replace("${" + names[i] + "}", "$" + (i + 1));
				return replacementString;
			}
		/**
		 * Makes single spaces match any amount of {@linkplain Macro#WHITESPACE whitespace}, and double spaces match at least
		 * one, unless they're preceded by a backslash.
		 */
		public static String replaceWhitespace(String regex)
			{
				return regex.replaceAll("([^\\\\])  ", "$1(?:" + Matcher.quoteReplacement(Macro.WHITESPACE) + "+)")
					.replaceAll("([^\\\\]) ", "$1(?:" + Matcher.quoteReplacement(Macro.WHITESPACE) + "*)");
			}
	}