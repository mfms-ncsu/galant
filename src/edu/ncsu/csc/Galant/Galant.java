package edu.ncsu.csc.Galant;

import javax.swing.SwingUtilities;

import edu.ncsu.csc.Galant.algorithm.Algorithm;
import edu.ncsu.csc.Galant.gui.editor.GEditorFrame;
import edu.ncsu.csc.Galant.gui.util.ExceptionDialog;
import edu.ncsu.csc.Galant.gui.util.WindowUtil;
import edu.ncsu.csc.Galant.gui.window.GraphWindow;

/**
 * Class from which Galant runs.
 * @author Michael Owoc, Jason Cockrell, Alex McCabe, Ty Devries
 */
public class Galant {

	public static void main(String[] args) {
		System.out.printf("In Galant main\n");
		ExceptionDialog.setDialogExceptionHandlerAsDefault();
		SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run(){
				GraphDispatch gd = GraphDispatch.getInstance();
				GalantPreferences.initPrefs();
				GraphWindow g = new GraphWindow(gd);
				gd.setGraphWindow(g);
				g.updateStatusLabel(gd.getWorkingGraph().getGraphState().getState());
				new GEditorFrame();
				GraphWindow.getGraphFrame().addWindowListener(GEditorFrame.getSingleton());
				WindowUtil.linkWindows();
				GraphDispatch.getInstance().pushToGraphEditor();
				}
		}
		);
		System.out.printf("Galant has finished running\n");
	}
	
	
}

//  [Last modified: 2015 04 30 at 15:18:43 GMT]
