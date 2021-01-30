/**
 * Used to jump to the end of the run method -- not a real exception
 *
 * @todo lots of methods that change attributes of nodes and edges throw
 * Terminate; this is necessary when an animation is running, but forces
 * awkward catching of Terminate in methods invoked only during editing
 */
package edu.ncsu.csc.Galant.algorithm;

import edu.ncsu.csc.Galant.logging.LogHelper;

public class Terminate extends Exception {
    public Terminate() {
        LogHelper.logDebug("Terminate thrown");
    }
}

//  [Last modified: 2021 01 30 at 22:23:14 GMT]
