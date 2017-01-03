package edu.ncsu.csc.Galant;

import javax.swing.SwingUtilities;
import java.awt.Dimension;

import edu.ncsu.csc.Galant.gui.editor.GEditorFrame;
import edu.ncsu.csc.Galant.gui.util.ExceptionDialog;
import edu.ncsu.csc.Galant.gui.util.WindowUtil;
import edu.ncsu.csc.Galant.gui.window.GraphWindow;

/**
 * Class from which Galant runs.
 * @author Michael Owoc, Jason Cockrell, Alex McCabe, Ty Devries
 */
public class Galant {
    public static final String VERSION = "v5.3";
    public static final Dimension MINIMUM_WINDOW_DIMENSION
        = new Dimension(500, 500);
	public static void main(String[] args) {
		ExceptionDialog.setDialogExceptionHandlerAsDefault();
		SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run(){
				GraphDispatch gd = GraphDispatch.getInstance();
				GalantPreferences.initPrefs();
				GraphWindow g = new GraphWindow(gd);
                g.setMinimumSize(MINIMUM_WINDOW_DIMENSION);
				gd.setGraphWindow(g);
				g.updateStatusLabel("No algorithm running");
				GEditorFrame gef = new GEditorFrame();
                gef.setMinimumSize(MINIMUM_WINDOW_DIMENSION);
				GraphWindow.getGraphFrame().addWindowListener(gef);
				WindowUtil.linkWindows();
				GraphDispatch.getInstance().pushToGraphEditor();
            }
		});
	}
}

//  [Last modified: 2017 01 03 at 17:08:53 GMT]
