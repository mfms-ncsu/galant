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
			public void stateChanged(ChangeEvent arg0) {
				Double wgt = (Double) weight.getValue();
                // avoid changing a null weight to 0.0 (usually not intended)
                Double previousWeight = workingElement.getWeight();
                if ( previousWeight != null || wgt != 0.0 ) {
                  Graph g = dispatch.getWorkingGraph();
                  try {
                    workingElement.setWeight(wgt);
                  }
                  catch ( Exception e ) {
                    e.printStackTrace();
                  }
                  dispatch.pushToTextEditor();
                  dispatch.pushToGraphEditor();
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
			public void keyTyped(KeyEvent arg0) {}
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
		this.workingElement = ge;

		if (ge == null) {
			this.setVisible(false);
			return;
		} else {
			this.setVisible(true);
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

		Double wgt = ge.getWeight();
        /**
         * @todo use NaN here and a text field in place of the spinner; NaN
         * causes spinner to crash.
         */
        if ( wgt == null ) wgt = 0.0;
		weight.setValue(wgt);
	}
}



//  [Last modified: 2017 01 21 at 21:08:52 GMT]
