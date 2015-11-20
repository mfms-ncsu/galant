/**
 * The purpose of this class is to synchronize the state of the algorithm
 * with the display. Methods that change the state of the graph during
 * algorithm execution interact with AlgorithmStateManager to ensure that
 * those changes are reflected in the display when appropriate. The
 * synchronization needs to happen only when the display catches up with
 * the algorithm, i.e., not when the display shows a state that has already
 * been executed.
 *
 * The name AlgorithmStateManager is a bit misleading. The algorithmState is
 * actually maintained by an AlgorithmThreadManager. The
 * incrementAlgorithmState method does not increment anything; it merely
 * waits for the thread manager to do the appropiate updates. The sequence of
 * events surrounding an incrementAlgorithmState is ...
 *
 * - user steps forward leading to an incrementDisplayState in the thread
 *   manager
 *
 * - if the current display state is the same as the algorithm state, the
 *   algorithm wakes up and executes until it reaches the next
 *   incrementAlgorithmState call
 *
 * - the thread manager increments the algorithm state while the state
 *   manager waits to be woken up again
 */

public class AlgorithmSynchronizer {

    protected boolean stepFinished = false;
    protected boolean algorithmFinished = false;
    protected boolean terminated = false;

    /**
     * Signals the algorithm that it needs to stop running. The signal is
     * heeded at the beginning of the next step.
     */
    public synchronized void stop() {
        System.out.println("-> stop");
        terminated = true;
    }
    
    /**
     * The algorithm signals that it has reached the end of execution on its own.
     * @todo not clear that this has to be synchronized
     */
    public synchronized void finishAlgorithm() {
        synchronized( this ) {
            algorithmFinished = true;
        }
    }

    public synchronized boolean algorithmFinished() {
        return algorithmFinished;
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
        stepFinished = false;
    }

    /**
     * Signals the end of a step, prompting the main thread to stop waiting.
     */
    public synchronized void finishStep() {
        stepFinished = true;
    }

    public synchronized boolean stepFinished() {
        return stepFinished;
    }

    /**
     * Called at the end of each algorithm step; yields control back to the
     * main thread
     */
    public synchronized void incrementAlgorithmState() {
        System.out.printf("-> incrementAlgorithmState\n");

        synchronized( this ) {
            try {
                this.wait();
            }
            catch(InterruptedException e){
                System.out.printf("Error occured while trying to wait");
                e.printStackTrace(System.out);
            }
        }
        System.out.printf("<- incrementAlgorithmState\n");
    }
}

//  [Last modified: 2015 11 20 at 22:25:29 GMT]
