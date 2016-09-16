package edu.ncsu.csc.Galant.gui.util;

import javax.swing.JOptionPane;
import javax.swing.JDialog;
import javax.swing.JTextField;
import java.beans.*; //property change stuff
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import edu.ncsu.csc.Galant.GraphDispatch;
import edu.ncsu.csc.Galant.GalantException;
import edu.ncsu.csc.Galant.algorithm.Terminate;
import edu.ncsu.csc.Galant.gui.window.GraphWindow;
import edu.ncsu.csc.Galant.graph.component.Graph;
import edu.ncsu.csc.Galant.graph.component.Node;
import edu.ncsu.csc.Galant.gui.window.panels.GraphPanel;
import edu.ncsu.csc.Galant.gui.window.panels.ComponentEditPanel;
import edu.ncsu.csc.Galant.logging.LogHelper;

/**
 * This dialog is displayed whenever a user needs to get some information via
 * a query. The result could be an integer, a string or boolean (y/n).
 */
public abstract class QueryDialog extends JDialog
    implements ActionListener,
               PropertyChangeListener {

    private static final int TEXT_FIELD_LENGTH = 10;

    /** text entered by user for the node */
    private String answerText = null;
    /** text field into which node id is typed */
    private JTextField answerTextField;

    private JOptionPane optionPane;

    private String enter = "Enter";
    private String cancel = "Cancel";
    
    private Frame frame;

    /** Creates the reusable dialog. */
    public QueryDialog(Frame frame, String prompt) {
        super(frame);
        setTitle("Query");
        LogHelper.enterConstructor(getClass());
        answerTextField = new JTextField(TEXT_FIELD_LENGTH);

        // Create an array of the text and components to be displayed
        Object[] displayComponents
            = {prompt, "", answerTextField};

        // Create an array specifying the number of dialog buttons
        // and their text.
        Object[] options = {enter, cancel};

        // Question is essentially whether or not to enter an edge, default
        // is yes.
        optionPane = new JOptionPane(displayComponents,
                                     JOptionPane.QUESTION_MESSAGE,
                                     JOptionPane.YES_NO_OPTION,
                                     null,
                                     options,
                                     enter);
        setContentPane(optionPane);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Ensure the text field always gets the first focus.
        addComponentListener(new ComponentAdapter() {
                public void componentShown(ComponentEvent ce) {
                    answerTextField.requestFocusInWindow();
                }
            });

        //Register an event handler that puts the text into the option pane.
        answerTextField.addActionListener(this);

        //Register an event handler that reacts to option pane state changes.
        optionPane.addPropertyChangeListener(this);

        this.pack();
        this.setLocationRelativeTo(frame);
        this.setVisible(true);

        LogHelper.exitConstructor(getClass());
    }

    /** action to be performed when source and target are identified;
     * specified by subclass */
    protected abstract void performAction(String answer)
        throws Terminate, GalantException;

    /** handles events for the text field. */
    public void actionPerformed(ActionEvent e) {
        optionPane.setValue(enter);
    }

    /** This method reacts to state changes in the option pane. */
    public void propertyChange(PropertyChangeEvent event) {
        LogHelper.enterMethod(getClass(), "propertyChange");
        String prop = event.getPropertyName();

        if ( isVisible()
             && (event.getSource() == optionPane)
             && (JOptionPane.VALUE_PROPERTY.equals(prop) ||
                 JOptionPane.INPUT_VALUE_PROPERTY.equals(prop)) ) {
            Object value = optionPane.getValue();

            if ( value == JOptionPane.UNINITIALIZED_VALUE ) {
                // ignore reset
                return;
            }

            // Reset the JOptionPane's value.  If you don't do this, then if
            // the user presses the same button next time, no property change
            // event will be fired.
            optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);

            if ( enter.equals(value) ) {
                answerText = answerTextField.getText();
                try {
                    performAction(answerText);
                    this.dispose();
                }
                catch (Exception e) {
                    JOptionPane.showMessageDialog(QueryDialog.this,
                                                  e.toString(),
                                                  "*** Error: Try Again ***",
                                                  JOptionPane.ERROR_MESSAGE);
                    answerTextField.selectAll();
                    answerText = null;
                    answerTextField.requestFocusInWindow();
                }
            } // enter button pushed
            else { //user closed dialog or clicked cancel
                this.dispose();
            }
        }
        LogHelper.exitMethod(getClass(), "propertyChange");
    }
}

//  [Last modified: 2016 09 16 at 00:02:45 GMT]
