package edu.ncsu.csc.Galant.graph.component;

import edu.ncsu.csc.Galant.GraphDispatch;

/**
 * 
 * Stores the current state of the graph for iterating through the animation. Each state
 * corresponds with the next step in the animation.
 * 
 *  
 * @author Michael Owoc
 *
 */
public class GraphState {
	
	public String toString(){
		return String.format("Graph state: %d; initialization is: %s; graph is: %s; graph is: %s\n",state, initializationComplete>0 ? "complete" : "incomplete", directed ? "directed" : "undirected", locked ? "locked" : "unlocked");
	}
	
	
	private Graph graph;
	
	private boolean stepComplete = false;
	
	public void setStepComplete(boolean stepComplete){
		this.stepComplete = stepComplete;
	}
	
	public boolean getStepComplete(){
		return stepComplete;
	}

	public static final int GRAPH_START_STATE = 0;

	private int state;
	
	static private int initializationComplete = 0;

    /**
     * non-zero if in the middle of a step, i.e., between beginStep() and
     * endStep()
     */
	private boolean locked;

	private boolean directed;
	
	public GraphState() {
		state = GRAPH_START_STATE;
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
		if(locked==false) this.state = state;
	}
	
	public void incrementState() {
		if(locked==false) {
			this.state++;
		}
	}
	
	public boolean pauseExecution(){	
		this.setStepComplete(true);
		synchronized(this){
			try{
				if ( !locked ) {
                    // Suspend algorithm execution until notified to complete
                    // another step
					this.wait();
				}
			}
			catch (InterruptedException e){
				e.printStackTrace(System.out);
			}
		}
		return this != null ? true : false;
	}
	
	
	public void setLocked(boolean lock) {
		this.locked = lock;
		
	}
	
	public boolean isLocked() {
		return locked;
	}
	
	public void resetLocks() {
		this.locked = false;
		
	}	
	public Graph getGraph(){
		return graph;
	}
	public boolean setGraph(Graph g){
		if(this.graph == g) return false;
		this.graph = g;
		return true;
	}
	
	
}

//  [Last modified: 2015 08 07 at 13:43:01 GMT]
