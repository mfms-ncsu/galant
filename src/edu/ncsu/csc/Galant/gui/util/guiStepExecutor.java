package edu.ncsu.csc.Galant.gui.util;

import java.util.List;

import javax.swing.SwingWorker;

import edu.ncsu.csc.Galant.GraphDispatch;
import edu.ncsu.csc.Galant.gui.window.GraphWindow;
import edu.ncsu.csc.Galant.gui.window.panels.GraphPanel;
import edu.ncsu.csc.Galant.logging.LogHelper;

/**
 * 
 * @author Kai Presler-Marshall
 * The guiStepExecutor uses synchronization in Swing to deliver a superior multithreaded experience.
 * Rather than doing an infinite loop that keeps checking "is the algorithm step done yet" naively on the only GUI thread, which offers easy potential for Galant to hang,
 * perhaps indefinitely, if the user writes a bad algorithm, or requiring the threading code to interfere with the gui in ways that badly violate the MVC paradigm,
 * this offers a better way.
 * This way of doing threading actually creates two more threads (in addition to the 'main' thread)--consequently there is a bit more overhead in terms of a performance
 * Behavior on a single-core system is unknown but might result in the GUI hanging if the algorithm hits an infinite loop.
 * The doInBackground() function is the one that is executed when the Executor object is created and started; this function will then run to completion,
 * intermittently publishing values (runtime elapsed) to the process() function; when doInBackground() finishes, then the done() function is executed.
 *   
 *
 */
public class guiStepExecutor extends SwingWorker<Void,Long>{

	// a reference to both the GraphPanel and GraphDisaptch is necessary
	GraphPanel gp;
	GraphDispatch dispatch;
    
	/**
	 * Only constructor needed for the guiStepExecutor; better than having to manually set the graphPanel later when we can instead be guaranteed that the caller will always have one
	 * @param gp GraphPanel object that a reference is kept to
	 */
	public guiStepExecutor(GraphPanel gp){
		this.gp = gp;
    this.dispatch = GraphDispatch.getInstance();
	}
	
	/** This is the function that gets executed when the user clicks on the nextStep button or hits the right arrow key.  It's what's actually executed by the incrementDisplayState function
	 It is responsible for making sure that the GUI is updated properly (& doesn't hang) during execution
	 @param none
	 @return none
    */
    @Override
	protected Void doInBackground(){
		if(gp.getDisplayState() < dispatch.getWorkingGraph().getGraphState().getState()){
			//increment the graphState array position
			//update the display state but don't run anything
		}
		else if(true){ // go run a step of the algorithm if we're moving forward but not lagging behind
			
			gp.resumeAlgorithmExecution(); // Start the algorithm up again
			long time = System.currentTimeMillis(); // Assuming that the previous call will return fast enough that we can consider this the start time

			long sleepPeriod = 15L;
			int count = 0;
					
			while ( ! dispatch.getWorkingGraph().getGraphState().getStepComplete()
                    && ! dispatch.getAlgorithmComplete()) { // while algorithm isn't finished and a step isn't "ready", keep checking
				try{
					Thread.sleep(sleepPeriod); // well after waiting for a bit, that is
				}
				catch (InterruptedException e){
					
				}
				count++;
				long elapsed = (System.currentTimeMillis() - time);
				// If we've gone over 5 seconds, update the time taken every fifth time (~5*15 ms = 75 ms ~= 1/13 of a second)
				if ((elapsed > 5000L) && (count % 5 == 0)) publish(new Long(elapsed));
			}
			
			
		}
		LogHelper.enterMethod(getClass(),"incrementDisplayState");
        LogHelper.logDebug("" + gp.getState());
		gp.setState(gp.getState()+1);
		LogHelper.exitMethod(getClass(),"incrementDisplayState");
		return null;
	}
	
	/**
	 This 'done' function is called when the doInBackground function returns.  It is run on the GUI thread and is responsible for updating the graph panel
	 Takes no parameters and returns nothing; all it does (and needs to do) is the repainting done internally
	*/
	public void done(){
		dispatch.getGraphWindow().updateStatusLabel(gp.getDisplayState());
		dispatch.getGraphWindow().getGraphFrame().repaint();
	}
	
	/**
	 This function handles warning the user if execution has taken a long time.
	 It will update the status label to alert the user of the time elapsed if the single step execution time goes over the 5-second mark
	 This function runs on the GUI thread
	 @param timeList A List of Long objects representing the times at which the doInBackground function published the time taken to execute.  Each time the display is updated, the last one is pulled from the list, converted to a long, and displayed on the status bar
	 
	 */
    @Override
	public void process(List<Long> timeList){
		int numUpdates = timeList.size();
		Long lastElapsedTime = timeList.get(numUpdates - 1);
		String message = String.format("Warning: execution has taken %2.2f seconds", lastElapsedTime.longValue()/1000.0);
		dispatch.getGraphWindow().updateStatusLabel(message);
	}
	
	
}

//  [Last modified: 2015 07 11 at 00:40:42 GMT]
