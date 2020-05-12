package edu.ncsu.csc.Galant.gui.window.panels;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.ncsu.csc.Galant.GraphDispatch;
import edu.ncsu.csc.Galant.GalantException;
import edu.ncsu.csc.Galant.algorithm.Terminate;
import edu.ncsu.csc.Galant.gui.util.ExceptionDialog;
import edu.ncsu.csc.Galant.graph.component.Graph;
import edu.ncsu.csc.Galant.graph.component.GraphElement;

/**
 * Panel for editing <code>Graph</code> components visually
 * @author Jason Cockrell, Ty Devries, Alex McCabe, Michael Owoc
 *
 */
public class ComponentEditPanel extends JPanel {

    /**
     * Important dimensions
     */
    private final int EDIT_PANEL_HEIGHT = 250;
    private final int EDIT_PANEL_WIDTH = 3000; // this is a maximum, should
                                               // be large
    private final int LABEL_FIELD_WIDTH = 12;  // number of columns
    private final int WEIGHT_FIELD_WIDTH = 50; // pixels

    /**
     * weights used for the spinner that allows user to set edge weights
     */
    private final double DEFAULT_WEIGHT = 0.0;
    private final double MINIMUM_WEIGHT = 0.0;
    private final double MAXIMUM_WEIGHT = Double.MAX_VALUE;
    private final double WEIGHT_INCREMENT = 1.0;

	private JButton apply;

	private ColorPanel cp;
	private JTextField label;
	private JSpinner weight;

	private GraphElement workingElement;
    /**
     * @todo This is a hack to avoid a null element when changing text inside
     * a spinner and neglecting to hit [return]; at some point, need to
     * investigate features of editor in JSpinner 
     */
	private GraphElement previousElement;

	GraphDispatch dispatch = GraphDispatch.getInstance();

	public ComponentEditPanel() {
		super();

		this.setMaximumSize(new Dimension(EDIT_PANEL_WIDTH, EDIT_PANEL_HEIGHT));
		this.setLayout(new FlowLayout());

		JLabel lLabel = new JLabel("Label: ");
		JLabel lWeight = new JLabel("Weight: ");
		JLabel lColor = new JLabel("Color: ");

		label = new JTextField(LABEL_FIELD_WIDTH);
		label.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent arg0) {}
			@Override
			public void keyReleased(KeyEvent arg0) {
				Graph g = dispatch.getWorkingGraph();
                try {
                    workingElement.setLabel(label.getText());
                }
                catch ( Exception e ) {
                    e.printStackTrace();
                }
				dispatch.pushToTextEditor();
				dispatch.pushToGraphEditor();
			}
			@Override
			public void keyTyped(KeyEvent arg0) {}
		});

		weight
            = new JSpinner(new SpinnerNumberModel( DEFAULT_WEIGHT,
                                                   MINIMUM_WEIGHT,
                                                   MAXIMUM_WEIGHT,
                                                   WEIGHT_INCREMENT ));
		JSpinner.NumberEditor editor
            = (JSpinner.NumberEditor) weight.getEditor();
		Dimension d = editor.getPreferredSize();
		d.width = WEIGHT_FIELD_WIDTH;
		editor.setPreferredSize(d);
		weight.addChangeListener(new ChangeListener() {
			@Override
            /**
             * @todo the weight spinner is the first to receive focus and its
             * default value is 0; for now, we avoid accidentally setting a 0
             * weight in a way that prevents intentional setting of weight to
             * 0; eventually need to avoid spinners - would also be better
             * for keyboard shortcuts
             */
			public void stateChanged(ChangeEvent arg0) {
				Double newWeight = (Double) weight.getValue();
                System.err.println("stateChanged, newWeight = " + newWeight
                                   + ", element = " + workingElement
                                   + ", previous = " + previousElement);
                // avoid changing a null weight to 0.0 (usually not intended)
                Double previousWeight = workingElement.getWeight();
                if ( previousWeight != null || newWeight != 0.0 ) {
                  Graph g = dispatch.getWorkingGraph();
                  try {
                    workingElement.setWeight(newWeight);
                  }
                  catch ( Exception e ) {
                    e.printStackTrace();
                  }
                  if ( ! newWeight.equals(previousWeight) ) { 
                    dispatch.pushToTextEditor();
                    dispatch.pushToGraphEditor();
                  }
                }
            }
          });

        // ensure that what the user types in the text field of the spinner
        // is handled properly
		editor.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent arg0) {}
			@Override
			public void keyReleased(KeyEvent arg0) {
				Graph g = dispatch.getWorkingGraph();
                String weightText = editor.getTextField().getText();
                if ( ! weightText.equals("") ) {
                    Double weightValue = null;
                    try {
                        weightValue = Double.parseDouble(weightText);
                    }
                    catch ( NumberFormatException e ) {
                        ExceptionDialog.displayExceptionInDialog(e);
                    }
                    try {
                        workingElement.setWeight(weightValue);
                    }
                    catch ( Terminate t ) {
                        // should not happen
                        t.printStackTrace();
                    }
                    catch ( Exception e ) {
                        e.printStackTrace();
                    }
                }
                else {
                    try {
                        workingElement.clearWeight();
                    }
                    catch ( Terminate t ) {
                        // should not happen
                        t.printStackTrace();
                    }
                }
                dispatch.pushToTextEditor();
				dispatch.pushToGraphEditor();
			}
			@Override
			public void keyTyped(KeyEvent arg0) { }
		});

		cp = new ColorPanel(workingElement);

		this.add(lLabel);
		this.add(label);
		this.add(lWeight);
		this.add(weight);
		this.add(lColor);
		this.add(cp);
	}

	public void setWorkingComponent(GraphElement ge) {
        System.err.println("-> setWorkingComponent, ge = " + ge);
		if (ge == null) {
			this.setVisible(false);
            this.workingElement = this.previousElement;
			return;
		} else {
			this.setVisible(true);
            this.workingElement = ge;
            System.err.println("    $*$ saving previous element " + previousElement);
            this.previousElement = ge;
            // make sure the weight change spinner always gets the first
            // focus so user can use up/down key to change the weights of
            // nodes or edges
            weight.requestFocusInWindow();
		}

		this.remove(cp);
		cp = new ColorPanel(ge);
		this.add(cp);
		this.validate();

		String text = ge.getLabel();
		if (text == null) {
			text = "";
		}
		label.setText(text);

		Double newWeight = ge.getWeight();
        /**
         * @todo use NaN here and a text field in place of the spinner; NaN
         * causes spinner to crash.
         */
        if ( newWeight == null ) newWeight = 0.0;
		weight.setValue(newWeight);
        System.err.println("<- setWorkingComponent: text, weight = "
                           + text + ", " + newWeight);
	}
}



//  [Last modified: 2020 05 12 at 18:58:16 GMT]
