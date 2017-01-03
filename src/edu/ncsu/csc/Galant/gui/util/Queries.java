/**
 * This class will evntually become the clearinghouse for all types of
 * queries. For now, only a Boolean query is offered
 */

package edu.ncsu.csc.Galant.gui.util;

import javax.swing.JOptionPane;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import edu.ncsu.csc.Galant.gui.window.GraphWindow;
import edu.ncsu.csc.Galant.GraphDispatch;

public class Queries {
    public static Boolean booleanQuery(String question,
                                       String yesAnswerText,
                                       String noAnswerText) {
        GraphDispatch dispatch = GraphDispatch.getInstance();
        String [] options = {yesAnswerText, noAnswerText};
        int response
            = JOptionPane.showOptionDialog(GraphWindow.getGraphFrame(),
                                           question,
                                           "Boolean query",
                                           JOptionPane.YES_NO_OPTION,
                                           JOptionPane.QUESTION_MESSAGE,
                                           null,
                                           options,
                                           options[0]
                                           );
        return (response == 0);
    }

}

//  [Last modified: 2016 12 19 at 17:32:48 GMT]
