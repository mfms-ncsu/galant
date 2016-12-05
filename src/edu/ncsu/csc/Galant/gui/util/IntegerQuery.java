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
        initDialog();
    }

    /** @see QueryDialog.java */
    public IntegerQuery(String prompt, boolean waitForResponse) {
        super(GraphWindow.getGraphFrame(), prompt, waitForResponse);
        initDialog();
    }

    private void initDialog() {
        GraphDispatch dispatch = GraphDispatch.getInstance();
        dispatch.setIntegerAnswer(null);
        dispatch.setActiveQuery(this);
    }

    protected void performAction(String answerText)
        throws Terminate, GalantException {
        Integer integerAnswer = Integer.parseInt(answerText);
        GraphDispatch.getInstance().setIntegerAnswer(integerAnswer);
        GraphDispatch.getInstance().setActiveQuery(null);
    }
}

//  [Last modified: 2016 12 05 at 17:03:12 GMT]
