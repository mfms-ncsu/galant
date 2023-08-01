/**
 * java_exception_test.alg - Creates a deliberate Java exception, as
 * opposed to a Galant exception to see if popup window and termination
 * are handled correctly
 */
algorithm {
    beginStep();
    int [] A = new int[1];
    /**
     * @todo without the line below, or something else that moves the animation
     * forward, the Java exception results in an infinite loop; need
     * some way for the algorithm synchronizer to catch Java
     * exceptions before this happens.
     * The good news is that a popup and the command line report the
     * error correctly.
     */
    //    color(startNode(), GREEN);
    A[1] = 0;
    endStep();
}

//  [Last modified: 2021 02 06 at 16:25:43 GMT]
