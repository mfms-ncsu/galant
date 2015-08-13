package edu.ncsu.csc.Galant.algorithm;

/**
 * 
 * @author Kai Presler-Marshall
 *
 * The AlgorithmExecutor class represents the (single) instance of the AlgorithmExecutor object that is used to handle synchronization and threading
 * It keeps track of
 * -> the algorithm state (furthest step to have been "executed"
 * -> display state (what does the display graph look like now)
 * -> whether or not a step of the algorithm is complete (and hence not executing) thus ready to be displayed
 * -> whether or not the algorithm as a whole is finished executing (used to update the status message bar)
 * 
 * An incomplete process is in the midst to transfer all of the synchronization/threading code to this file
 */
public class AlgorithmExecutor{
	
	
	/* These are all reasonable things to keep track of here
	   displayState and algorithmState are naturally kept in other places here, but having a second copy stored here makes it easy to get access to (and set)
	   all values necessary for threatening and deciding whether or not to run a step and when to update the status label */
	protected int algorithmState;
	protected int displayState;
	protected boolean stepComplete;
	protected boolean algorithmComplete;
	
	
	
	public int algorithmState(){
		return this.algorithmState;
	}
	
	public int getDisplayState(){
		return this.displayState;
	}
	
	public void incrementDisplayState(){
		this.displayState++;
	}
	
	public void incrementAlgorithmState(){
		this.algorithmState++;
	}
	
	public void decrementDisplayState(){
		--this.displayState;
	}
	
	public void setStepComplete(){
		this.stepComplete = true;
	}
	
	public void setStepIncomplete(){
		this.stepComplete = false;
	}
	
	public boolean getStepComplete(){
		return this.stepComplete ? true : false;
	}
	
	public void setAlgorithmComplete(boolean complete){
		this.algorithmComplete = complete;
	}
	
	public boolean getAlgorithmComplete(){
		return this.algorithmComplete;
	}
	
	
	
	
}
