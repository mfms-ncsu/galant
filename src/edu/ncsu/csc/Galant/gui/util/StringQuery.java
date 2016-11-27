/**
 * Captures text in response to a user's input during algorithm
 * execution. Can also be used during editing.
 */
package edu.ncsu.csc.Galant.gui.util;

import java.awt.Frame;

import edu.ncsu.csc.Galant.GraphDispatch;
import edu.ncsu.csc.Galant.GalantException;
import edu.ncsu.csc.Galant.logging.LogHelper;
import edu.ncsu.csc.Galant.algorithm.Terminate;
import edu.ncsu.csc.Galant.gui.window.GraphWindow;

public class StringQuery extends QueryDialog {

    String answerText = null;

    public StringQuery(String prompt) {
        super(GraphWindow.getGraphFrame(), prompt, false);
        GraphDispatch.getInstance().setStringAnswer(null);
    }

    /** @see QueryDialog.java */
    public StringQuery(String prompt, boolean waitForResponse) {
        super(GraphWindow.getGraphFrame(), prompt, waitForResponse);
        GraphDispatch.getInstance().setStringAnswer(null);
    }

    protected void performAction(String answerText)
        throws Terminate, GalantException {
        GraphDispatch.getInstance().setStringAnswer(answerText);
    }
}

//  [Last modified: 2016 11 27 at 18:59:28 GMT]
