package edu.ncsu.csc.Galant.graph.component;

import edu.ncsu.csc.Galant.algorithm.line_up_nodes_alg;
import edu.ncsu.csc.Galant.gui.window.GraphWindow;
/**
 * 
 * Stores the current state of the graph for iterating through the animation. Each state
 * corresponds with the next step in the animation.
 * 
 * @author Michael Owoc
 *
 */
public class GraphState {
	

	public static final int GRAPH_START_STATE = 1;

	
	private int state = 0;
	static public int iComplete = 0;
	//line_up_nodes_alg l = new line_up_nodes_alg();
	
	

    /**
     * non-zero if in the middle of a step, i.e., between beginStep() and
     * endStep()
     */
	private int locked = 0;

	private boolean directed;
	
	public GraphState() {
		state = 1;
	}
	static public void setInitializationComplete(){
		iComplete++;
	}
	static public void setInitializationIncomplete(){
		iComplete = 0;
	}
	static public boolean initilizationIncomplete(){
		return iComplete == 0;
	}
	
	
	/**
	 * @return true if the graph is directed, false otherwise
	 */
	public boolean isDirected() {
		return directed;
	}

	/**
	 * @param directed true if setting the graph to directed, false if undirected
	 */
	public void setDirected(boolean directed) {
		this.directed = directed;
	}
	
	public int getState() {
		return state;
	}

	public void setState(int state) {
		if(locked==0) this.state = state;
	}
	
	public void incrementState() {
		System.out.printf("Incrementing the graph state");
		try{
			throw new ArrayIndexOutOfBoundsException();
		}
		catch (Exception e){
			e.printStackTrace(System.out);
		}
		if(locked==0) {
			this.state++;
			
		}
	}
	
	
	public void setLocked(boolean lock) {
		if (lock) {
			this.locked++;
		} else {
			if (this.locked > 0) {
				this.locked--;
			}
		}
		
	}
	
	public boolean isLocked() {
		return this.locked>0;
	}
	
	public void resetLocks() {
		this.locked = 0;
		
	}
	
}

//  [Last modified: 2014 03 14 at 14:26:02 GMT]
