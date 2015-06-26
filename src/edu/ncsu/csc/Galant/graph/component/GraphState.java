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
		return String.format("Graph state: %d; initialization is: %s; graph is: %s; graph is: %s\n",state, initializationComplete>0 ? "complete" : "incomplete", directed ? "directed" : "undirected", locked!=0 ? "locked" : "unlocked");
	}
	
	
	private Graph graph;

	public static final int GRAPH_START_STATE = 1;

	private int state = 0;
	
	// MPM: Why is this static? It seems like it should be an instance variable that goes along with the graph state.
	static public int initializationComplete = 0;

    /**
     * non-zero if in the middle of a step, i.e., between beginStep() and
     * endStep()
     */
	private int locked = 0;

	private boolean directed;
	
	public GraphState() {
		state = 1;
	}
	
	public void setInitializationComplete(){
		initializationComplete++;
		Thread a = Thread.currentThread();
		// modify the status message to say that initialization is complete
		//System.out.printf("Initialization complete; suspending thread [%s]", a.getName());
		//graphWindow doesn't exist
		//this.graph.graphWindow.updateStatusLabel("Initialization complete".toCharArray());
		synchronized(this){
			try{
				this.wait();
			}
			catch(InterruptedException e){
				e.printStackTrace(System.out);
			}
		}
	}
	
	
	static public void setInitializationIncomplete(){
		initializationComplete = 0;
	}
	
	static public boolean initializationComplete(){
		return initializationComplete != 0;
	}
	
	static public boolean initializationIncomplete(){
		return initializationComplete == 0;
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
		System.out.printf("Incrementing the graph state\n");
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
	
	public boolean pauseExecution(){
		
		synchronized(this){
			try{
				
				if(!this.initializationIncomplete()  && (locked==0)){ /* Wait only if initialization is actually complete; otherwise program hangs while setting up graph when launching (when the addNodeState and addEdgeState function calls are also made) */
					GraphDispatch.getInstance().getGraphWindow().getStepForward().setEnabled(true);
					/* System.out.printf("In the synchronizedWait function; about to suspend execution\n");
					
					GraphDispatch.getInstance().getGraphWindow().updateStatusLabel("Algorithm runner thread has completed a step; state is now " + state + ".");
					System.out.println("Graph state: " + toString());
					^^Debug info */
					
					
					// Make sure to remember to update the graph here */
					this.getGraph().graphWindow.getGraphPanel().incrementDisplayState();
					/*
					int a = GraphDispatch.getInstance().getGraphWindow().getGraphPanel().getDisplayState();
					GraphDispatch.getInstance().getGraphWindow().updateStatusLabel(a);*/
					GraphDispatch.getInstance().getGraphWindow().repaintFrame(); // I don't know that repainting the frame is required but it does this regularly at other points too
					this.wait(); // Suspend algorithm execution until notified to complete another step
					
				}
			}
			catch (InterruptedException e){
				e.printStackTrace(System.out);
			}
		}
		return this != null ? true : false;
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
	
	public Graph getGraph(){
		return graph;
	}
	public boolean setGraph(Graph g){
		if(this.graph == g) return false;
		this.graph = g;
		return true;
	}
	
	
}

//  [Last modified: 2014 03 14 at 14:26:02 GMT]
