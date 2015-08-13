package edu.ncsu.csc.Galant;

import javax.swing.SwingUtilities;

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
		ExceptionDialog.setDialogExceptionHandlerAsDefault();
		SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run(){
				GraphDispatch gd = GraphDispatch.getInstance();
				GalantPreferences.initPrefs();
				GraphWindow g = new GraphWindow(gd);
				gd.setGraphWindow(g);
				g.updateStatusLabel("No algorithm running".toCharArray());
				GEditorFrame gef = new GEditorFrame();
				GraphWindow.getGraphFrame().addWindowListener(gef);
				WindowUtil.linkWindows();
				GraphDispatch.getInstance().pushToGraphEditor();
            }
		});
	}
	
}

//  [Last modified: 2015 07 03 at 14:42:20 GMT]
