package edu.ncsu.csc.Galant.algorithm.code.macro;
/**
 * Represents a simple function that can be later called with a parameter of type P to get a result
 * of type R and that doesn't throw any checked exceptions.
 * @param <P> The type of the parameter.
 * @param <R> The type of the result.
 */
public interface Function<P, R>
	{
		/**
		 * Invokes this function.
		 * @param param the function's parameter.
		 * @return a value of type R.
		 */
		public R invoke(P param);
	}
