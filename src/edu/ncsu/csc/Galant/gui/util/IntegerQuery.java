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
        GraphDispatch.getInstance().setIntegerAnswer(null);
    }

    /** @see QueryDialog.java */
    public IntegerQuery(String prompt, boolean waitForResponse) {
        super(GraphWindow.getGraphFrame(), prompt, waitForResponse);
        GraphDispatch.getInstance().setIntegerAnswer(null);
    }

    protected void performAction(String answerText)
        throws Terminate, GalantException {
        Integer integerAnswer = Integer.parseInt(answerText);
        GraphDispatch.getInstance().setIntegerAnswer(integerAnswer);
    }
}

//  [Last modified: 2016 11 27 at 18:58:39 GMT]
