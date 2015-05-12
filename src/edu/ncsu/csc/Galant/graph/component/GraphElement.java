package edu.ncsu.csc.Galant.graph.component;

/**
 * Abstract class containing graph element manipulation methods
 * @author Jason Cockrell, Ty Devries, Alex McCabe, Michael Owoc
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

//  [Last modified: 2014 05 26 at 15:19:37 GMT]
