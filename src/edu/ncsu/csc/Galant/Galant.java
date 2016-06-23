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

    public static final String VERSION = "v5.2.1";
	public static void main(String[] args) {
		ExceptionDialog.setDialogExceptionHandlerAsDefault();
		SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run(){
				GraphDispatch gd = GraphDispatch.getInstance();
				GalantPreferences.initPrefs();
				GraphWindow g = new GraphWindow(gd);
				gd.setGraphWindow(g);
				g.updateStatusLabel("No algorithm running");
				GEditorFrame gef = new GEditorFrame();
				GraphWindow.getGraphFrame().addWindowListener(gef);
				WindowUtil.linkWindows();
				GraphDispatch.getInstance().pushToGraphEditor();
            }
		});
	}
	
}

//  [Last modified: 2016 06 23 at 19:47:35 GMT]
