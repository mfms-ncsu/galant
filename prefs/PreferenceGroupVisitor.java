package edu.ncsu.csc.Galant.prefs;

/**
 * Abstract class for iteration through preference groups.
 * @author tydevries
 */
public interface PreferenceGroupVisitor
	{
		public void visit(PreferenceGroup group);
	}
