/**
 * Captures text in response to a user's input during algorithm execution.
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
        super(GraphWindow.getGraphFrame(), prompt);
    }

    protected void performAction(String answerText) 
        throws Terminate, GalantException {
        GraphDispatch.getInstance().setStringAnswer(answerText);
    }
}

//  [Last modified: 2016 09 16 at 15:45:37 GMT]
