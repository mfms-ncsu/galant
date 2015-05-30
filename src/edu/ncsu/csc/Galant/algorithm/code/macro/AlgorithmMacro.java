package edu.ncsu.csc.Galant.algorithm.code.macro;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AlgorithmMacro extends Macro {

	private static volatile AlgorithmMacro instance;
	
	public AlgorithmMacro() {
		super(Pattern.compile("algorithm"));
	}

	/* src: http://en.wikipedia.org/wiki/Singleton_pattern */
	public static AlgorithmMacro getInstance() {
		if ( instance == null ) {
			synchronized (AlgorithmMacro.class) {
                if (instance == null) {
                    instance = new AlgorithmMacro();
                }
            }
		}

		return instance;
	}

	@Override
	protected final String modify(String code, MatchResult match) throws MalformedMacroException
	{	
		return "public void run() ";

	}

	@Override
	public boolean getIncludedInAlgorithm() {
		return false;
	}
	
} 