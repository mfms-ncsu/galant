package edu.ncsu.csc.Galant.algorithm.code.macro;

/**
 * Custom exception thrown for malformed Macros
 * @author Jason Cockrell, Ty Devries, Alex McCabe, Michael Owoc
 *
 */
public class MalformedMacroException extends Exception
	{
		/** Creates a new <code>MalformedMacroException</code>. */
		public MalformedMacroException()
			{}

		/** Creates a new <code>MalformedMacroException</code>. */
		public MalformedMacroException(String message)
			{
				super(message);
			}

		/** Creates a new <code>MalformedMacroException</code>. */
		public MalformedMacroException(Throwable cause)
			{
				super(cause);
			}

		/** Creates a new <code>MalformedMacroException</code>. */
		public MalformedMacroException(String message, Throwable cause)
			{
				super(message, cause);
			}
	}
