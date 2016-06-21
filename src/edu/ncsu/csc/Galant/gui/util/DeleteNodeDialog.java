package edu.ncsu.csc.Galant.gui.util;

import javax.swing.JOptionPane;
import javax.swing.JDialog;
import javax.swing.JTextField;
import java.beans.*; //property change stuff
import java.awt.*;
import java.awt.event.*;
import edu.ncsu.csc.Galant.GraphDispatch;
import edu.ncsu.csc.Galant.GalantException;
import edu.ncsu.csc.Galant.gui.window.GraphWindow;
import edu.ncsu.csc.Galant.graph.component.Edge;
import edu.ncsu.csc.Galant.graph.component.Graph;
import edu.ncsu.csc.Galant.graph.component.Node;
import edu.ncsu.csc.Galant.gui.window.panels.GraphPanel;
import edu.ncsu.csc.Galant.gui.window.panels.ComponentEditPanel;

/**
 * this dialog displayed when user delete node with "delete + n" 
 */
public class DeleteNodeDialog extends JDialog
                   implements ActionListener,
                              PropertyChangeListener {
    private String nodeText = null;
    private JTextField nodeTextField;

    private JOptionPane optionPane;

    private String btnString1 = "Enter";
    private String btnString2 = "Cancel";
    
    /** Refers to the singleton GraphDispatch to push global information */
	  private final GraphDispatch dispatch;
    
    private Frame frame;

    /** Creates the reusable dialog. */
    public DeleteNodeDialog(Frame frame, GraphDispatch _dispatch) {
        super(frame, true);
        this.dispatch = _dispatch;
		    // Register this object as a change listener. Allows GraphDispatch notifications to be pushed to this object
		    _dispatch.addChangeListener(this);
        setTitle("Delete Node");

        nodeTextField = new JTextField(10);

        //Create an array of the text and components to be displayed
        Object[] array = {"Node ID: ", nodeTextField};

        //Create an array specifying the number of dialog buttons
        //and their text.
        Object[] options = {btnString1, btnString2};

        //Create the JOptionPane.
        optionPane = new JOptionPane(array,
                                    JOptionPane.QUESTION_MESSAGE,
                                    JOptionPane.YES_NO_OPTION,
                                    null,
                                    options,
                                    options[0]);

        //Make this dialog display it.
        setContentPane(optionPane);

        //Handle window closing correctly.
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent we) {
                /*
                 * Instead of directly closing the window,
                 * we're going to change the JOptionPane's
                 * value property.
                 */
                    optionPane.setValue(new Integer(
                                        JOptionPane.CLOSED_OPTION));
            }
        });

        //Ensure the text field always gets the first focus.
        addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent ce) {
                nodeTextField.requestFocusInWindow();
            }
        });

        //Register an event handler that puts the text into the option pane.
        nodeTextField.addActionListener(this);
        
        //Register an event handler that reacts to option pane state changes.
        optionPane.addPropertyChangeListener(this);
    }

    /** This method handles events for the text field. */
    public void actionPerformed(ActionEvent e) {
        optionPane.setValue(btnString1);
    }

    /** This method reacts to state changes in the option pane. */
    public void propertyChange(PropertyChangeEvent e) {
        String prop = e.getPropertyName();

        if (isVisible()
         && (e.getSource() == optionPane)
         && (JOptionPane.VALUE_PROPERTY.equals(prop) ||
             JOptionPane.INPUT_VALUE_PROPERTY.equals(prop))) {
            Object value = optionPane.getValue();

            if (value == JOptionPane.UNINITIALIZED_VALUE) {
                //ignore reset
                return;
            }

            //Reset the JOptionPane's value.
            //If you don't do this, then if the user
            //presses the same button next time, no
            //property change event will be fired.
            optionPane.setValue(
                    JOptionPane.UNINITIALIZED_VALUE);

            if (btnString1.equals(value)) {
                nodeText = nodeTextField.getText();
                boolean isInt = true;
                int nodeId = 0;
                try {
                    nodeId = Integer.parseInt(nodeText);
                } catch (NumberFormatException ex) {
                    isInt = false;
                }
                Graph g = dispatch.getWorkingGraph();
                Node n = null;
                try {
                    n = g.getNodeById(nodeId);
                }
                catch (GalantException ge) {
                    ge.display();
                }
                if (isInt && n != null && !n.isDeleted()) {
                    //delete specified node
                    g.removeNode(n);                    
                    dispatch.pushToTextEditor(); 
                    //we're done; clear and dismiss the dialog
                    clearAndHide();
                } else {
                    //text was invalid
                    nodeTextField.selectAll();
                    JOptionPane.showMessageDialog(
                                    DeleteNodeDialog.this,
                                    "Sorry, Node " + nodeText + " Doesn't Exist.",
                                    "Try again",
                                    JOptionPane.ERROR_MESSAGE);
                    nodeText = null;
                    nodeTextField.requestFocusInWindow();
                }
            } else { //user closed dialog or clicked cancel
                nodeText = null;
                clearAndHide();
            }
        }
    }

    /** This method clears the dialog and hides it. */
    public void clearAndHide() {
        nodeTextField.setText(null);
        setVisible(false);
    }
}

//  [Last modified: 2016 06 21 at 17:07:58 GMT]
