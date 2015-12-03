/**
 * The purpose of this class is to set up and manage synchronization of
 * display and algorithm from the point of view of the "main program",
 * treating the algorithm execution as a worker thread. It is responsible for
 * firing up the algorithm and keeping track of the relationship between the
 * display state and the algorithm state. It is also responsible for
 * signaling the algorithm to terminate and then terminating the thread.
 * @see edu.ncsu.csc.Galant.algorithm.AlgorithmSynchronizer for the worker
 * thread end of the synchronization.
 */

package edu.ncsu.csc.Galant.algorithm;

import java.lang.Thread;
import edu.ncsu.csc.Galant.algorithm.Algorithm;
import edu.ncsu.csc.Galant.logging.LogHelper;

public class AlgorithmExecutor {

    /** the amount of time to wait between polling the algorithm */
    final int WAIT_TIME = 15;

    private Algorithm algorithm;
    private AlgorithmSynchronizer synchronizer;
    private Thread algorithmThread;
    private int algorithmState = 0;
    private int displayState = 0;

    /**
     * Makes a note of the algorithm and its synchronizer and creates a
     * thread to run the algorithm
     */
    public AlgorithmExecutor(Algorithm algorithm,
                             AlgorithmSynchronizer synchronizer) {
        LogHelper.logDebug("=> AlgorithmExecutor: algorithm = " + algorithm
                           + ", synchronizer = " + synchronizer);
        this.algorithm = algorithm;
        this.synchronizer = synchronizer;
        this.algorithmThread = new Thread(algorithm);
		algorithmThread.setName("Execution thread");
    }

    /**
     * Starts the algorithm thread and causes it to execute the first step.
     * @todo not clear if we want the first step to execute
     */
    public void startAlgorithm() {
        algorithmThread.start();
        incrementDisplayState();
    }

    /**
     * Informs the algorithm that it should terminate and then terminates the
     * thread.
     */
    public synchronized void stopAlgorithm() {
        synchronized ( synchronizer ) {
            synchronizer.stop();
            synchronizer.notify();
        }
        try {
            algorithmThread.join();
        }
        catch (InterruptedException e) {}
    }


    /**
     * Needs to be called from the algorithm at some point after control is
     * yielded to it and before it pauses execution.
     */
    public void incrementAlgorithmState() {
        algorithmState++;
    }

    /**
     * Needed so that graph elements can record their modifications based on
     * current algorithm state.
     */
    public int getAlgorithmState() { return algorithmState; }

    /**
     * Needed for code that relies on knowing what the current display is showing
     */
    public int getDisplayState() { return displayState; }

    /**
     * Called whenever user interaction requests a step forward; the
     * algorithm is then responsible for calling incrementAlgorithmState() to
     * put the algorithm in sync with the display 
     */
    public synchronized void incrementDisplayState() {
        System.out.printf("-> incrementDisplayState %s\n", synchronizer);
        if ( displayState >= algorithmState) {
            System.out.printf(" algorithm needs to wake up: displayState = %d," 
                              + " algorithmState = %d\n",
                              displayState, algorithmState);
						
            // wake up the algorithmThread, have it do something	
            synchronized ( synchronizer ) {
                synchronizer.notify();							
            }

            // checking until it's accomplished the task
            System.out.println(" algorithm is running");
            do {
                try {
                    Thread.sleep(WAIT_TIME);
                    System.out.print(".");
                } catch (InterruptedException e) {
                    System.out.printf("Error occured while trying to wait");
                    e.printStackTrace(System.out);
                }
            } while ( ! synchronizer.stepFinished() );
            System.out.println();
        }
        else {
            System.out.printf(" algorithm is ahead, displayState = %d,"
                              + " algorithmState = %d\n",
                              displayState, algorithmState);
        }
        displayState++;
        System.out.printf("<- incrementDisplayState\n");
    }

    /**
     * Called when user requests a step back
     */
    public void decrementDisplayState() {
        if ( displayState >= 0 ) displayState--;
        System.out.printf("-> decrementDisplayState: displayState = %d,"
                          + " algorithmState = %d\n",
                          displayState, algorithmState);
        System.out.printf("<- decrementDisplayState\n");
    }

    /**
     * True if it's possible to step forward
     */
    public boolean hasNextState() {
        if ( algorithmState > displayState ) return true;
		if ( ! synchronizer.algorithmFinished() ) return true;
        return false;
	}

    /**
     * true if it's possible to step back
     * @todo not clear whether the lower bound is 1 or 0
     */
    public boolean hasPreviousState() {
        return (displayState > 1);
    }

    public void printStates() {
        System.out.printf("displayState = %d, algorithmState = %d\n",
                          displayState, algorithmState);
    }
}

//  [Last modified: 2015 12 03 at 02:23:05 GMT]
