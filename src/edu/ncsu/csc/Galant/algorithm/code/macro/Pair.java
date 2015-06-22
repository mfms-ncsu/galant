package edu.ncsu.csc.Galant.algorithm.code.macro;

/**
 * A type-safe, fixed-length pair of two objects; pretty self-explanatory. Can be expanded to larger
 * numbers of elements by having one or both of the elements be another pair.
 */
public class Pair<E1, E2>
	{
		private E1 element1;
		private E2 element2;

		public Pair(E1 element1, E2 element2)
			{
				setElement1(element1);
				setElement2(element2);
			}

		public E1 getElement1()
			{
				return element1;
			}

		public void setElement1(E1 element1)
			{
				this.element1 = element1;
			}

		public E2 getElement2()
			{
				return element2;
			}

		public void setElement2(E2 element2)
			{
				this.element2 = element2;
			}

		/**
		 * Returns a string of the form: <br />
		 * <code>(</code><i>element1</i><code>, </code><i>element2</i><code>)</code>
		 */
		@Override
		public String toString()
			{
				return "(" + getElement1() + ", " + getElement2() + ")";
			}
	}
