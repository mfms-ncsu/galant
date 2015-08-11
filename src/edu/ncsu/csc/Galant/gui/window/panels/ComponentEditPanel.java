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

				g.getGraphState().setLocked(true);
				workingElement.setLabel(label.getText());
				g.getGraphState().setLocked(false);

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
		Component editor = weight.getEditor();
		Dimension d = editor.getPreferredSize();
		d.width = WEIGHT_FIELD_WIDTH;
		editor.setPreferredSize(d);
		weight.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				Double wgt = (Double) weight.getValue();
				
				Graph g = dispatch.getWorkingGraph();

				g.getGraphState().setLocked(true);
				workingElement.setWeight(wgt);
				g.getGraphState().setLocked(false);

				dispatch.pushToTextEditor();
				dispatch.pushToGraphEditor();
			}
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
      //make sure the weight change spinner always gets the first focus
      //so user can use up/down key to change the weights of nodes or edges
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

		double wgt = ge.getWeight();
		weight.setValue(wgt);
	}
}



//  [Last modified: 2014 07 08 at 19:13:44 GMT]
