package edu.ncsu.csc.Galant.graph.component;

import edu.ncsu.csc.Galant.GalantException;

/**
 * Abstract class containing graph element manipulation methods
 * @author Jason Cockrell, Ty Devries, Alex McCabe, Michael Owoc,
 *         completely rewritten by Matthias Stallmann
 */
public abstract class GraphElement {

	public abstract double getWeight();	
	public abstract double getWeight(int state);
	public abstract void setWeight(double weight);
    public abstract boolean hasWeight();
    public abstract boolean hasWeight(int state);
    public abstract void clearWeight();
	
	public abstract String getLabel();	
	public abstract String getLabel(int state);
	public abstract void setLabel(String label);
    public abstract boolean hasLabel();
    public abstract boolean hasLabel(int state);
    public abstract void clearLabel();
	
	public abstract String getColor();	
	public abstract String getColor(int state);
	public abstract void setColor(String color);
	
}

//  [Last modified: 2015 10 02 at 00:55:15 GMT]
