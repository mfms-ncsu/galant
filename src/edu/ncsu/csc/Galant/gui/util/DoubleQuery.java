/**
 * Captures a double in response to a user's input during algorithm
 * execution. Can also be used during editing.
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
        super(GraphWindow.getGraphFrame(), prompt);
    }

    protected void performAction(String answerText) 
        throws Terminate, GalantException {
        Double doubleAnswer = Double.parseDouble(answerText);
        GraphDispatch.getInstance().setDoubleAnswer(doubleAnswer);
    }
}

//  [Last modified: 2016 09 16 at 16:28:47 GMT]
