/**
 * Used to jump to the end of the run method -- not a real exception
 */
package edu.ncsu.csc.Galant.algorithm;

import edu.ncsu.csc.Galant.logging.LogHelper;

public class Terminate extends Exception {
    public Terminate() {
        LogHelper.logDebug("Terminate thrown");
    }
}

//  [Last modified: 2016 12 13 at 20:33:54 GMT]
