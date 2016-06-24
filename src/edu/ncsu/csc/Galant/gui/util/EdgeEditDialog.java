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
import edu.ncsu.csc.Galant.gui.window.GraphWindow;
import edu.ncsu.csc.Galant.gui.window.GraphWindow.GraphMode;
import edu.ncsu.csc.Galant.graph.component.Edge;
import edu.ncsu.csc.Galant.graph.component.Graph;
import edu.ncsu.csc.Galant.graph.component.Node;
import edu.ncsu.csc.Galant.gui.window.panels.GraphPanel;
import edu.ncsu.csc.Galant.gui.window.panels.ComponentEditPanel;

/**
 * this dialog displayed when user create new edge with "ctrl + e" 
 * or delete edge with "delete + e"
 */
public class EdgeEditDialog extends JDialog
    implements ActionListener,
               PropertyChangeListener {

    private static final int TEXT_FIELD_LENGTH = 10;
    private String point1Text = null;
    private String point2Text = null;
    private JTextField point1TextField;
    private JTextField point2TextField;

    private JOptionPane optionPane;

    private String enter = "Enter";
    private String cancel = "Cancel";
    
    /** Refers to the singleton GraphDispatch to push global information */
	  private final GraphDispatch dispatch;
    
    private Frame frame;
    
    private GraphMode mode;

    /** Creates the reusable dialog. */
    public EdgeEditDialog(Frame frame, GraphDispatch _dispatch, GraphMode mode) {
        super(frame, true);
        this.dispatch = _dispatch;
        this.mode = mode;
		    // Register this object as a change listener. Allows GraphDispatch notifications to be pushed to this object
		    _dispatch.addChangeListener(this);
        
        if (mode == GraphMode.CREATE_EDGE) {
            setTitle("Create New Edge");
        } else if (mode == GraphMode.DELETE) {
            setTitle("Delete Edge");
        }
        point1TextField = new JTextField(TEXT_FIELD_LENGTH);
        point2TextField = new JTextField(TEXT_FIELD_LENGTH);

        //Create an array of the text and components to be displayed
        Object[] displayComponents
            = {"End Point 1", point1TextField, "End Point 2", point2TextField};

        //Create an array specifying the number of dialog buttons
        //and their text.
        Object[] options = {enter, cancel};

        //Create the JOptionPane.
        optionPane = new JOptionPane(displayComponents,
                                    JOptionPane.QUESTION_MESSAGE,
                                    JOptionPane.YES_NO_OPTION,
                                    null,
                                    options,
                                    enter);

        //Make this dialog display it.
        setContentPane(optionPane);

        //Handle window closing correctly.
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent we) {
                    // Instead of directly closing the window,
                    // we're going to change the JOptionPane's
                    // value property.
                    optionPane.setValue(new Integer(JOptionPane.CLOSED_OPTION));
                }
            });

        //Ensure the text field always gets the first focus.
        addComponentListener(new ComponentAdapter() {
                public void componentShown(ComponentEvent ce) {
                    point1TextField.requestFocusInWindow();
                }
            });

        //Register an event handler that puts the text into the option pane.
        point1TextField.addActionListener(this);
        point2TextField.addActionListener(this);

        //Register an event handler that reacts to option pane state changes.
        optionPane.addPropertyChangeListener(this);
    }

    /** This method handles events for the text field. */
    public void actionPerformed(ActionEvent e) {
        optionPane.setValue(enter);
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
                // ignore reset
                return;
            }

            // Reset the JOptionPane's value.  If you don't do this, then if
            // the user presses the same button next time, no property change
            // event will be fired.
            optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);

            if (enter.equals(value)) {
                point1Text = point1TextField.getText();
                point2Text = point2TextField.getText();
                boolean isInt = true;
                int point1 = 0;
                int point2 = 0;
                try {
                    point1 = Integer.parseInt(point1Text);
                    point2 = Integer.parseInt(point2Text);
                } catch (NumberFormatException ex) {
                    isInt = false;
                }
                Graph g = dispatch.getWorkingGraph();
                int numNodes = g.nodeIds();
                Node n1 = null;
                Node n2 = null;
                try {
                    n1 = g.getNodeById(point1);
                    n2 = g.getNodeById(point2);
                }
                catch (GalantException galantException) {
                    galantException.display();
                }
                if ( isInt && n1 != null && n2 != null ) {
                    if (mode == GraphMode.CREATE_EDGE) {
                        //create a new edge from point1 to point2
                        Edge edge = g.addInitialEdge(n1, n2);
                        // select the new edge and clear the edge
                        // trackers
                        GraphPanel gp = GraphWindow.getGraphPanel();
                        gp.setSelectedNode(null);
                        gp.setSelectedEdge(edge);
                        GraphWindow.componentEditPanel.setWorkingComponent(edge);
                        gp.setEdgeTracker(null);
                    }
                    if (mode == GraphMode.DELETE) {
                        //delete the edge between those node
                        ArrayList<Edge> edges = new ArrayList<Edge>();
                        if (n1.equals(n2)) {
                            for (Edge e1 : n1.getEdges()) {
                                if (e1.getSourceNode().equals(e1.getTargetNode())) {
                                    edges.add(e1);
                                }
                            }
                        }
                        else {
                            for (Edge e1 : n1.getEdges()) {
                                for (Edge e2 : n2.getEdges()) {
                                    if (e1.getId() == e2.getId()) {
                                        edges.add(e2);
                                    }
                                }
                            }
                        }
                        for (Edge ed : edges) {
                            g.removeEdge(ed);
                        }
                        GraphWindow.componentEditPanel.setWorkingComponent(null);
                    }
                    dispatch.pushToTextEditor(); 
                    //we're done; clear and dismiss the dialog
                    clearAndHide();
                }
                else {
                    //text was invalid
                    point1TextField.selectAll();
                    point1TextField.selectAll();
                    JOptionPane.showMessageDialog(EdgeEditDialog.this,
                                                  "Sorry,"
                                                  + " one of the endpoints"
                                                  + " does not exist",
                                                  "Try again",
                                                  JOptionPane.ERROR_MESSAGE);
                    point1Text = null;
                    point2Text = null;
                    point1TextField.requestFocusInWindow();
                }
            }
            else { //user closed dialog or clicked cancel
                point1Text = null;
                point2Text = null;
                clearAndHide();
            }
        }
    }

    /** This method clears the dialog and hides it. */
    public void clearAndHide() {
        point1TextField.setText(null);
        point2TextField.setText(null);
        setVisible(false);
    }
}

//  [Last modified: 2016 06 23 at 22:18:47 GMT]
