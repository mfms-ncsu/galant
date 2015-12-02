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
     * Needed so that graph elements can record their modifications based on
     * current algorithm state.
     */
    public int getAlgorithmState() { return algorithmState; }

    /**
     * Called whenever user interaction requests a step forward
     */
    public synchronized void incrementDisplayState() {
        System.out.printf("-> incrementDisplayState\n");
        if ( displayState >= algorithmState) {
            System.out.printf(" algorithm needs to wake up: displayState = %d," 
                              + " algorithmState = %d\n",
                              displayState, algorithmState);
						
            // wake up the algorithmThread, have it do something	
            synchronized ( synchronizer ) {
                algorithm.notify();							
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
            algorithmState++;
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

//  [Last modified: 2015 12 02 at 13:30:28 GMT]
