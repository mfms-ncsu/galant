/**
 * The purpose of this class is to set up and manage synchronization of display
 * and algorithm from the point of view of the "main program", treating the
 * algorithm execution as a worker thread. It is responsible for firing up the
 * algorithm and keeping track of the relationship between the display state and
 * the algorithm state. It is also responsible for signaling the algorithm to
 * terminate and then terminating the thread.
 *
 * @see edu.ncsu.csc.Galant.algorithm.AlgorithmSynchronizer for the worker
 *      thread end of the synchronization.
 */

package edu.ncsu.csc.Galant.algorithm;

import edu.ncsu.csc.Galant.GraphDispatch;
import edu.ncsu.csc.Galant.graph.component.Node;
import edu.ncsu.csc.Galant.gui.window.GraphWindow;
import edu.ncsu.csc.Galant.logging.LogHelper;

public class AlgorithmExecutor {

	/**
	 * the amount of time to wait between polling the algorithm, in milliseconds
	 */
	final int WAIT_TIME = 10;

	/**
	 * interval between console printouts during busy wait
	 */
	final int PRINT_INTERVAL = 500;

	/**
	 * amount of time to wait before concluding that the algorithm is in an infinite
	 * loop
	 */
	final int BUSY_WAIT_TIME_LIMIT = 10000;

	private Algorithm algorithm;
	private AlgorithmSynchronizer synchronizer;
	/**
	 * Needs to be public so that it can be interrupted (not clear that it helps)
	 */
	public Thread algorithmThread;
	private int algorithmState;
	private int displayState;

	/**
	 * true if an error or infinite loop occurred during execution
	 */
	public boolean infiniteLoop = false;

	/**
	 * true if a GalantException is thrown during algorithm execution
	 *
	 * @todo this is now redundant with equivalent flag in AlgorithmSynchronizer
	 */
	public boolean exceptionThrown = false;

	/**
	 * Makes a note of the algorithm and its synchronizer and creates a thread to
	 * run the algorithm
	 */
	public AlgorithmExecutor(Algorithm algorithm, AlgorithmSynchronizer synchronizer){
		this.algorithm = algorithm;
		this.synchronizer = synchronizer;
		this.algorithmThread = new Thread(algorithm);
		algorithmThread.setName("Execution thread");
		this.infiniteLoop = false;
		this.exceptionThrown = false;
	}

	/**
	 * Starts the algorithm thread and causes it to execute the first step.
	 */
	public void startAlgorithm(){
		GraphDispatch dispatch = GraphDispatch.getInstance();
		dispatch.setActiveQuery(null);
		algorithmState = displayState = 0;
		algorithmThread.start();

		incrementDisplayState();
	}

	/**
	 * Informs the algorithm that it should terminate and then terminates the
	 * thread.
	 */
	public synchronized void stopAlgorithm(){
		LogHelper.disable();
		LogHelper.enterMethod(getClass(), "stopAlgorithm");
		GraphDispatch dispatch = GraphDispatch.getInstance();
		dispatch.stopAlgorithm();
		synchronized (synchronizer){
			synchronizer.stop();
			synchronizer.notify();
		}
		LogHelper.logDebug("algorithm thread notified" + ", infiniteLoop = " + infiniteLoop + ", exceptionThrown = "
				+ synchronizer.exceptionThrown() + ", activeQuery = " + dispatch.getActiveQuery());
		try{
			if ( ! infiniteLoop && ! synchronizer.exceptionThrown() && dispatch.getActiveQuery() == null ){
				LogHelper.logDebug("stopAlgorithm(): about to join algorithm thread");
				algorithmThread.join();
				LogHelper.logDebug("stopAlgorithm(): joined algorithm thread");
			}
			LogHelper.logDebug("stopAlgorithm(): beyond (conditional) joining of algorithm thread");
		} catch (InterruptedException e){
			System.out.println("Synchronization problem in stopAlgorithm()");
		}
		if ( dispatch.getActiveQuery() != null ){
			dispatch.getActiveQuery().dispose();
		}
		algorithmState = displayState = 0;
		// made by 2021 Galant Team
		for (Node n : dispatch.getWorkingGraph().getNodes()){
			n.setpos = false;
		}
		LogHelper.exitMethod(getClass(), "stopAlgorithm");
		LogHelper.restoreState();
	}

	/**
	 * Needed so that graph elements can record their modifications based on current
	 * algorithm state.
	 */
	public int getAlgorithmState(){
		return algorithmState;
	}

	/**
	 * Needed for code that relies on knowing what the current display is showing
	 */
	public int getDisplayState(){
		return displayState;
	}

	private void showStates(){
		String message = "display state = " + this.displayState + "  algorithm state = " + this.algorithmState;
		GraphDispatch.getInstance().getGraphWindow().updateStatusLabel(message);
	}

	/**
	 * Called whenever user interaction requests a step forward. If the display
	 * state and algorithm state are the same, the requested step will also cause a
	 * the algorithm to take a step.
	 */
	public synchronized void incrementDisplayState(){
		LogHelper.disable();
		LogHelper.logDebug("-> incrementDisplayState display = " + displayState + " algorithm = " + algorithmState);
		// initialization, but should end up being replaced by
		// something before being displayed
		String message = "default (should not happen)";
		GraphDispatch dispatch = GraphDispatch.getInstance();
		if ( displayState == algorithmState && ! synchronizer.algorithmFinished() && ! synchronizer.stopped()
				&& ! synchronizer.exceptionThrown() && dispatch.getActiveQuery() == null ){
			displayState++;
			algorithmState++;
			this.showStates();

			// wake up the algorithmThread, have it do something
			synchronized (synchronizer){
				synchronizer.notify();
			}
			int timeInBusyWait = 0;
			do{
				try{
					Thread.sleep(WAIT_TIME);
					timeInBusyWait += WAIT_TIME;
					if ( timeInBusyWait % PRINT_INTERVAL == 0 ){
						message = "waiting " + (timeInBusyWait / (double) 1000) + " seconds of "
								+ BUSY_WAIT_TIME_LIMIT / ((double) 1000);
						GraphWindow window = dispatch.getGraphWindow();
						/**
						 * @todo despite the synchronization, the status label fails to get updated here
						 */
						synchronized (window){
							window.updateStatusLabel(message);
						}
						System.out.println(message);
					}
				} catch (InterruptedException e){
					message = "Terminated because of exception";
					System.out.printf(message);
					dispatch.getGraphWindow().updateStatusLabel(message);
					e.printStackTrace(System.out);
				}
			} while( ! synchronizer.stepFinished() && ! synchronizer.stopped()
			/**
			 * there two types of exception thrown here: synchronizer when an exception is
			 * displayed in a popup this whenever a GalantException is thrown during
			 * execution not clear that both are needed, but the synchronizer one is a
			 * backstop
			 */
					&& ! synchronizer.exceptionThrown() && ! this.exceptionThrown
					&& timeInBusyWait < BUSY_WAIT_TIME_LIMIT );
			if ( timeInBusyWait >= BUSY_WAIT_TIME_LIMIT ){
				message = "Busy wait time limit exceeded";
				System.out.println(message);
				dispatch.getGraphWindow().updateStatusLabel(message);
				infiniteLoop = true;
			}
		} else if ( displayState < algorithmState ){
			displayState++;
			this.showStates();
		}
		if ( infiniteLoop || synchronizer.exceptionThrown() ){
			// need to let window know that algorithm was terminated due
			// to unusual circumstances so that appropriate message will
			// appear on the status bar
			dispatch.getGraphWindow().performDone();
		}

		// made by 2021 Galant Team
		// if the algorithm is moving nodes, the nodes shall bounce back after
		// forward, backward, scaling, or cancel algorithm.
		if ( dispatch.algorithmMovesNodes() ){
			for (Node n : dispatch.getWorkingGraph().getNodes()){
				n.setpos = false;
			}
		}

		LogHelper.logDebug("<- incrementDisplayState display = " + displayState + " algorithm = " + algorithmState);
		LogHelper.restoreState();

	}

	/**
	 * Called when user requests a step back
	 */
	public void decrementDisplayState(){
		GraphDispatch dispatch = GraphDispatch.getInstance();
		if ( displayState > 0 ){
			displayState--;
			this.showStates();
		}
		// made by 2021 Galant Team
		// if the algorithm is moving nodes, the nodes shall bounce back after
		// forward, backward, scaling, or cancel algorithm.
		if ( dispatch.algorithmMovesNodes() ){
			for (Node n : dispatch.getWorkingGraph().getNodes()){
				n.setpos = false;
			}
		}
	}

	/**
	 * True if it's possible to step forward
	 */
	public boolean hasNextState(){
		if ( algorithmState > displayState ){
			return true;
		}
		if ( ! synchronizer.algorithmFinished() ){
			return true;
		}
		return false;
	}

	/**
	 * true if it's possible to step back
	 */
	public boolean hasPreviousState(){
		return (displayState > 1);
	}

	public void printStates(){
		System.out.printf("displayState = %d, algorithmState = %d\n", displayState, algorithmState);
	}
}

// [Last modified: 2021 02 08 at 18:01:49 GMT]
