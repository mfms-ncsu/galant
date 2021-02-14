/**
 * The purpose of this class is to synchronize the state of the algorithm
 * with the display. Methods that change the state of the graph during
 * algorithm execution interact with AlgorithmExecutor to ensure that
 * those changes are reflected in the display when appropriate. The
 * synchronization needs to happen only when the display catches up with
 * the algorithm, i.e., not when the display shows a state that has already
 * been executed.
 *
 * The sequence of events surrounding a synchronization is ...
 *
 * - user steps forward leading to an incrementDisplayState() in the thread
 *   manager (AlgorithmExecutor)
 *
 * - if the current display state is the same as the algorithm state, the
 *   algorithm wakes up and executes, doing a startStep(), i.e., setting
 *   stepFinished false and checking for termination
 *
 * - when the current algorithm step is done, the algorithm calls
 *   pauseExecution() to wake up the AlgorithmExecuter (main thread)
 *
 * - this synchronizer waits to be woken up again, i.e., when the user steps
 *    forward beyond the current algorithm state
 */

package edu.ncsu.csc.Galant.algorithm;

import edu.ncsu.csc.Galant.GraphDispatch;
import edu.ncsu.csc.Galant.algorithm.AlgorithmExecutor;
import edu.ncsu.csc.Galant.logging.LogHelper;

public class AlgorithmSynchronizer {

    /** true if done with current algorithm step */
    protected boolean stepFinished = false;
    /** true if algorithm has reached the end of execution; may still be
     * animating */
    protected boolean algorithmFinished = false;
    /** true if user has ended animation; set via the Terminate exception */
    protected boolean terminated = false;
    /** true if current state is "locked" -- changes in graph state continue to
     * take place until algorithm has an explicit endStep(); a lock is
     * initiated by a beginStep() */
    protected boolean locked = false;
    /** true if there was an exception thrown during the current step */
    protected boolean exceptionThrown = false;

    /**
     * Signals the algorithm that it needs to stop running. The signal is
     * heeded at the beginning of the next step.
     */
    public synchronized void stop() {
        terminated = true;
     }

    public synchronized boolean stopped() {
        return terminated;
    }

    /**
     * The algorithm signals that it has reached the end of execution on its own.
     */
    public synchronized void finishAlgorithm() {
        synchronized( this ) {
            algorithmFinished = true;
        }
    }

    public synchronized boolean algorithmFinished() {
        return algorithmFinished;
    }

    public void lock() { locked = true; }
    public void unlock() { locked = false; }
    public boolean isLocked() { return locked; }

    public synchronized void reportExceptionThrown() {
        exceptionThrown = true;
    }

    public synchronized boolean exceptionThrown() {
        return exceptionThrown;
    }

    /**
     * Signals the beginning of a step: the algorithm calls startStep() and
     * the main thread checks, in a busy wait loop, to see whether the
     * current step is finished.
     *
     * startStep() is also used by the algorithm to take appropriate action when
     * termination is called for; if yes then throws an exception to
     * effectively do a 'long jump' to the end of the run() method of the
     * compiled algorithm;
     * @see edu.ncsu.csc.Galant.algorithm.code.CodeIntegrator
     */
    public synchronized void startStep() throws Terminate {
        if ( terminated )
            throw new Terminate();
        if ( locked ) {
            locked = false;
            pauseExecution();
        }
        if ( terminated )
            throw new Terminate();
        stepFinished = false;
    }

    public synchronized boolean stepFinished() {
        return stepFinished;
    }

    public synchronized void finishStep() {
        stepFinished = true;
    }

    /**
     * Called at the end of each algorithm step; yields control back to the
     * main thread
     */
    public synchronized void pauseExecution() throws Terminate {
        LogHelper.disable();
        LogHelper.logDebug("-> pauseExecution, locked = " + locked);
        if ( terminated )
            throw new Terminate();
        AlgorithmExecutor executor
            = GraphDispatch.getInstance().getAlgorithmExecutor();
        stepFinished = true;
        synchronized( this ) {
            try {
                if ( ! locked ) {
                    this.wait();
                }
            }
            catch ( InterruptedException e ) {
                System.out.println("interruption in pauseExecution");
            }
        }
        if ( terminated )
            throw new Terminate();
        LogHelper.logDebug("<- pauseExecution, locked = " + locked);
        LogHelper.restoreState();
    }
}

//  [Last modified: 2021 01 30 at 22:21:41 GMT]
