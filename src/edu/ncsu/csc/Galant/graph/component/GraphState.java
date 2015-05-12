package edu.ncsu.csc.Galant.graph.component;

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
	private int state;

    /**
     * non-zero if in the middle of a step, i.e., between beginStep() and
     * endStep()
     */
	private int locked = 0;

	private boolean directed;
	
	public GraphState() {
		state = 1;
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
		if(locked==0) this.state++;
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
