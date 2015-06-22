package edu.ncsu.csc.Galant.graph.component;

import edu.ncsu.csc.Galant.gui.window.GraphWindow;

/**
 * 
 * Stores the current state of the graph for iterating through the animation. Each state
 * corresponds with the next step in the animation.
 * 
 * @author Michael Owoc
 *
 */
/**
 * 
 * Stores the current state of the graph for iterating through the animation. Each state
 * corresponds with the next step in the animation.
 * 
 * @author Michael Owoc
 *
 */
public class GraphState {
	
	private Graph g;
	
	public Graph getGraph(){
		return g;
	}
	public Boolean setGraph(Graph g){
		this.g = g;
		return true;
	}

	public static final int GRAPH_START_STATE = 1;

	
	private int state = 0;
	static public int iComplete = 0;

	Thread t;
	
	

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
		iComplete++;
		Thread myThread = Thread.currentThread();
		// modify the status message to say tha tinitialization is complete
		//System.out.printf("Initialization complete; suspending thread [%s]", myThread.getName());
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
	
	public boolean synchronizedWait(){
		
		synchronized(this){
			try{
				try{
					throw new IllegalArgumentException();
				}
				catch (IllegalArgumentException e){
					e.printStackTrace(System.out);
				}
				

				if(!this.initilizationIncomplete()){
					System.out.printf("In the synchronizedWait function; about to suspend execution\n");
					GraphWindow.getGraphPanel().incrementDisplayState();
					
					this.wait();
					
				}
			}
			catch (InterruptedException e){
				e.printStackTrace(System.out);
			}
		}
		
		return true ? true : false;
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
