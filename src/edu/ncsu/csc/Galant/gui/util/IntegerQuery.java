/**
 * Captures an integer in response to a user's input during algorithm
 * execution. Can also be used during editing.
 */
package edu.ncsu.csc.Galant.gui.util;

import java.awt.Frame;

import edu.ncsu.csc.Galant.GraphDispatch;
import edu.ncsu.csc.Galant.GalantException;
import edu.ncsu.csc.Galant.logging.LogHelper;
import edu.ncsu.csc.Galant.algorithm.Terminate;
import edu.ncsu.csc.Galant.gui.window.GraphWindow;

public class IntegerQuery extends QueryDialog {

    String answerText = null;

    public IntegerQuery(String prompt) {
        super(GraphWindow.getGraphFrame(), prompt, false);
    }

    /** @see QueryDialog.java */
    public IntegerQuery(String prompt, boolean waitForResponse) {
        super(GraphWindow.getGraphFrame(), prompt, waitForResponse);
    }

    protected void performAction(String answerText) 
        throws Terminate, GalantException {
        Integer integerAnswer = Integer.parseInt(answerText);
        GraphDispatch.getInstance().setIntegerAnswer(integerAnswer);
    }
}

//  [Last modified: 2016 09 17 at 12:54:01 GMT]
