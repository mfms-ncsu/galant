/**
 * Captures a double in response to a user's input during algorithm
 * execution. Can also be used during editing.
 * @todo Seems wrong to have the "doubleAnswer" to be a property of a graph;
 * same issue in IntegerQuery and StringQuery
 */
package edu.ncsu.csc.Galant.gui.util;

import java.awt.Frame;

import edu.ncsu.csc.Galant.GraphDispatch;
import edu.ncsu.csc.Galant.GalantException;
import edu.ncsu.csc.Galant.logging.LogHelper;
import edu.ncsu.csc.Galant.algorithm.Terminate;
import edu.ncsu.csc.Galant.gui.window.GraphWindow;

public class DoubleQuery extends QueryDialog {

    String answerText = null;

    public DoubleQuery(String prompt) {
        super(GraphWindow.getGraphFrame(), prompt, false);
        GraphDispatch.getInstance().setDoubleAnswer(null);
    }

    /** @see QueryDialog.java */
    public DoubleQuery(String prompt, boolean waitForResponse) {
        super(GraphWindow.getGraphFrame(), prompt, waitForResponse);
        GraphDispatch.getInstance().setDoubleAnswer(null);
    }

    protected void performAction(String answerText)
        throws Terminate, GalantException {
        Double doubleAnswer = Double.parseDouble(answerText);
        GraphDispatch.getInstance().setDoubleAnswer(doubleAnswer);
    }
}

//  [Last modified: 2016 11 27 at 18:58:07 GMT]
