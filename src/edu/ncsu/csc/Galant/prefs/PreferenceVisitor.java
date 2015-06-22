package edu.ncsu.csc.Galant.prefs;

/**
 * Represents something that visits a series of {@link Preference}s and does something with each
 * one.
 */
public interface PreferenceVisitor
	{
		/** Visits the given {@link Preference}, performing some action on it. */
		public void visit(Preference<?> preference);
	}