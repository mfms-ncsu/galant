package edu.ncsu.csc.Galant.gui.editor;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.tools.Diagnostic;
import edu.ncsu.csc.Galant.GraphDispatch;
import edu.ncsu.csc.Galant.algorithm.Algorithm;
import edu.ncsu.csc.Galant.algorithm.code.CodeIntegrator;
import edu.ncsu.csc.Galant.algorithm.code.CompilationException;
import edu.ncsu.csc.Galant.algorithm.code.macro.MalformedMacroException;
import edu.ncsu.csc.Galant.logging.LogHelper;
import edu.ncsu.csc.Galant.GalantException;
import edu.ncsu.csc.Galant.algorithm.code.CompilerAndLoader;
import edu.ncsu.csc.Galant.GalantPreferences;

/**
 * See GAlgorithmEdiorPanel.java 
 */
public class GCompiledAlgorithmEditorPanel extends GEditorPanel {
	
	/** Should always be accessed through getter/setter */
	private Algorithm compiledAlgorithm;
	private RunButton runButton;
	public static final String PACKAGE = "edu.ncsu.csc.Galant.algorithm.code.compiled";

	/**
	 * Create a new edit session of an algorithm.
	 * @param gTabbedPane The parent tabbed pane, of which there is only ever one.
	 * @param filename The name of the file to be edited, which may be an unsaved file with a dummy name.
	 * @param content The text which constitutes the contet of the file to be edited. It is either the result of reading in the file, or the empty string.
	 */
	public GCompiledAlgorithmEditorPanel(GTabbedPane gTabbedPane, String filename, String content) {
		super(gTabbedPane, filename, content);
		add(new ButtonsPanel(), BorderLayout.SOUTH);
		
		syntaxHighlighter = new GAlgorithmSyntaxHighlighting(textPane);
		documentUpdated();
		setCompiledAlgorithm(null);

		// Need to manipulate qualified name so class file can be loaded
		// see CodeIntegrator.java 
		String qualifiedName = GalantPreferences.OUTPUT_DIRECTORY.get() + "/" + filename;

		setCompiledAlgorithm(CompilerAndLoader.loadAlgorithm(qualifiedName));
		GraphDispatch.getInstance().addChangeListener(this);
		
	}
	
	/**
	 * @return compiledAlgorithm.
	 */
	private Algorithm getCompiledAlgorithm()
		{
			return compiledAlgorithm;
		}

	/**
	 * Sets compiledAlgorithm to the given Algorithm.
	 * @param compiledAlgorithm the new compiledAlgorithm.
	 */
	private void setCompiledAlgorithm(Algorithm compiledAlgorithm)
		{
			this.compiledAlgorithm = compiledAlgorithm;
			runButton.setEnabled(compiledAlgorithm != null);
		}

	/**
	 * Called when the user presses the Run button.
	 * Not at all related to the run() method in Runnable.
	 */
	public void run() {
		GraphDispatch.getInstance().setAnimationMode(true);
		getCompiledAlgorithm().setGraph(GraphDispatch.getInstance().getWorkingGraph());
		getCompiledAlgorithm().run();
	}

	/**
	 * A convenient way to hold the Compile, Run, and C&R buttons.
	 */
	class ButtonsPanel extends JPanel {
		public ButtonsPanel() {
			setLayout(new GridLayout(1,3));
			add(runButton = new RunButton());
		}
	}
	
	class RunButton extends JButton implements ActionListener {
		public RunButton() { super("Run"); addActionListener(this); }
		@Override
		public void actionPerformed(ActionEvent arg0) {run();}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(GraphDispatch.ANIMATION_MODE)) {
			if ( (Boolean) evt.getNewValue() ) { //animation mode
				this.textPane.setEnabled(false);
			} else { //edit mode
				this.textPane.setEnabled(true);
			}
		}
	}

}

//  [Last modified: 2015 05 08 at 14:58:01 GMT]
