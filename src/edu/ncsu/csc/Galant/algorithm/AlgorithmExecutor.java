package edu.ncsu.csc.Galant.algorithm;

/**
 * 
 * @author Kai
 *
 * The AlgorithmExecutor class represents the (single) instance of the AlgorithmExecutor object that is used to handle synchronization and threading
 * It keeps track of
 * -> the algorithm state (furthest step to have been "executed"
 * -> display state (what does the display graph look like now)
 * -> whether or not a step of the algorithm is complete (and hence not executing) thus ready to be displayed
 * -> whether or not the algorithm as a whole is finished executing (used to udate the status message bar)
 */
public class AlgorithmExecutor{

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
	
	public void setAlgorithmComplete(){
		this.algorithmComplete = true;
	}
	
	public boolean getAlgorithmComplete(){
		return this.algorithmComplete;
	}
	
	
	
}
