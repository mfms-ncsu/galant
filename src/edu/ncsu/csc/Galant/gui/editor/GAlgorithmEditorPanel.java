package edu.ncsu.csc.Galant.gui.editor;

import java.util.Locale;
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
import edu.ncsu.csc.Galant.gui.util.ExceptionDialog;
import edu.ncsu.csc.Galant.graph.component.Graph;
import edu.ncsu.csc.Galant.gui.window.panels.GraphPanel;
import edu.ncsu.csc.Galant.algorithm.AlgorithmExecutor;
import edu.ncsu.csc.Galant.algorithm.AlgorithmSynchronizer;

/**
 * Each instance of GAlgorithmEditorPanel corresponds to a particular
 * edit session of a particular algorithm file, or unsaved algorithm.
 * The panel displays in the GTabbedPane and provides all the graphical interface
 * for the algorithm edit session, including the Compile and Run buttons.
 * 
 * Compare to GGraphEditorPanel.
 * 
 * @author Jason Cockrell
 */
public class GAlgorithmEditorPanel extends GEditorPanel {

    /**
     * maximum number of lines to display in popup in case of compiler
     * errors
     * @todo these seem to run together in a single line, so making this
     * anything other than 1 doesn't make sense
     */
    private static final int MAX_LINES_IN_ERROR_DISPLAY = 1;

	/** Should always be accessed through getter/setter */
	private Algorithm compiledAlgorithm;
	private RunButton runButton;

	/**
	 * Create a new edit session of an algorithm.
	 * @param gTabbedPane The parent tabbed pane, of which there is only ever one.
	 * @param filename The name of the file to be edited, which may be an unsaved file with a dummy name.
	 * @param content The text which constitutes the contet of the file to be edited. It is either the result of reading in the file, or the empty string.
	 */
	public GAlgorithmEditorPanel(GTabbedPane gTabbedPane, String filename, String content) {
		super(gTabbedPane, filename, content);
		add(new ButtonsPanel(), BorderLayout.SOUTH);
		syntaxHighlighter = new GAlgorithmSyntaxHighlighting(textPane);
		documentUpdated();
		setCompiledAlgorithm(null);
		
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
	 * Called when the user presses the Compile button.
	 * @return Whether the algorithm compiled into an executable correctly.
	 */
	public boolean compile() 
    {
		LogHelper.enterMethod(getClass(), "compile");
		try{
			setCompiledAlgorithm(CodeIntegrator.integrateCode(fileName, textPane.getText()));
			LogHelper.exitMethod(getClass(), "compile");
			return true;
		}
		catch(CompilationException e){
			setCompiledAlgorithm(null);
			LogHelper.exitMethod(getClass(), "compile [CompilationException]");
            LogHelper.setEnabled( true );
            /**
             * @todo there's no graceful way to display error messages in a
             * popup window, especially given the fact that the line breaks
             * are ignored
             */
            // String forDisplay = "";
            // int displayLineCount = 0;
			for(Diagnostic<?> diagnostic : e.getDiagnostics().getDiagnostics()) {
				long line =  diagnostic.getLineNumber();
                String message = diagnostic.getMessage(null);
				LogHelper.logDebug("Error, line " + line + ": " + message);
                // if ( displayLineCount < MAX_LINES_IN_ERROR_DISPLAY ) {
                //     forDisplay += "line " + line + ": " + message + "\n";
                //     displayLineCount++;
                // }
            }
            // forDisplay += "check console\n";
            ExceptionDialog.displayExceptionInDialog(e, "check console for error messages");
            LogHelper.restoreState();
			return false;
		}
		catch(MalformedMacroException e){
				setCompiledAlgorithm(null);
                ExceptionDialog.displayExceptionInDialog(e, e.getMessage());
                LogHelper.setEnabled( true );
				LogHelper.exitMethod(getClass(), "compile [MalformedMacroException]");
				LogHelper.logDebug(e.getMessage());
                LogHelper.restoreState();
				return false;
		}
        catch ( GalantException e ) {
            e.report( "Galant compiler error" );
            e.displayStatic();
            return false;
        }
	}

	/**
	 * Called when the user presses the Run button.
	 * Not at all related to the run() method in Runnable.
	 */
	public void run() {
        System.out.println("-> GAlgorithmEditorPanel.run()");
        GraphDispatch dispatch = GraphDispatch.getInstance();
		dispatch.setAnimationMode(true);

		Algorithm algorithm = getCompiledAlgorithm();
        Graph theGraph = dispatch.getWorkingGraph();
		algorithm.setGraph(theGraph);

        AlgorithmSynchronizer algorithmSynchronizer = new AlgorithmSynchronizer();
        AlgorithmExecutor algorithmExecutor
            = new AlgorithmExecutor(algorithm, algorithmSynchronizer);
        dispatch.setAlgorithmSynchronizer(algorithmSynchronizer);
        dispatch.setAlgorithmExecutor(algorithmExecutor);
        dispatch.getGraphWindow().updateStatusLabel();
        algorithmExecutor.startAlgorithm();
        dispatch.getGraphWindow().updateStatusLabel();
        System.out.println("<- GAlgorithmEditorPanel.run()");
	}

	/**
	 * Invokes compile, and if an executable is produced, invokes run.
	 */
	public void compileAndRun()
    {
        try {
            if( compile() )
                run();
        }
        catch ( Exception e ) {
            e.printStackTrace( System.out );
            ExceptionDialog.displayExceptionInDialog(e, e.getMessage());
       }
	}

	/**
	 * A convenient way to hold the Compile, Run, and C&R buttons.
	 */
	class ButtonsPanel extends JPanel {
		public ButtonsPanel() {
			setLayout(new GridLayout(1,3));
			add(new CompileButton());
			add(runButton = new RunButton());
			add(new CompileAndRunButton());
		}
	}
	
	class CompileButton extends JButton implements ActionListener {
		public CompileButton() { super("Compile"); addActionListener(this); }
		@Override
		public void actionPerformed(ActionEvent arg0)
        {
            compile();
        }
	}
	
	class RunButton extends JButton implements ActionListener {
		public RunButton() { super("Run"); addActionListener(this); }
		@Override
		public void actionPerformed(ActionEvent arg0) {run();}
	}
	
	class CompileAndRunButton extends JButton implements ActionListener {
		public CompileAndRunButton() { super("Compile and Run"); addActionListener(this); }
		@Override
		public void actionPerformed(ActionEvent arg0) {compileAndRun();}
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

//  [Last modified: 2016 12 13 at 19:36:58 GMT]
