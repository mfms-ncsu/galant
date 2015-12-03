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
	
	public String toString() {
		return String.format("Graph state: graph is: %s\n",
                             directed ? "directed" : "undirected");
	}
	
	
	private Graph graph;
	private static GraphDispatch dispatch = GraphDispatch.getInstance();
	private boolean directed;
	
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
		return dispatch.getAlgorithmState();
	}

	public void incrementStateIfRunning() {
		if ( dispatch.isAnimationMode()
             && ! dispatch.getAlgorithmSynchronizer().isLocked() ) {
            dispatch.getAlgorithmExecutor().incrementAlgorithmState();
		}
	}
	
	public void pauseExecutionIfRunning() {
		if ( dispatch.isAnimationMode() )
            dispatch.getAlgorithmSynchronizer().pauseExecution();
	}

    /**
     * Locks the current algorithm state if algorithm is running
     */
    public void lockIfRunning() {
        if ( dispatch.isAnimationMode() )
            dispatch.getAlgorithmSynchronizer().lock();       
    }

    /**
     * Unlocks the current algorithm state if algorithm is running
     */
    public void unlockIfRunning() {
        if ( dispatch.isAnimationMode() )
            dispatch.getAlgorithmSynchronizer().unlock();       
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

//  [Last modified: 2015 12 03 at 16:39:09 GMT]
