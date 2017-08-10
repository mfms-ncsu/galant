package edu.ncsu.csc.Galant;

import javax.swing.SwingUtilities;
import java.awt.Dimension;

import edu.ncsu.csc.Galant.gui.editor.GEditorFrame;
import edu.ncsu.csc.Galant.gui.util.ExceptionDialog;
import edu.ncsu.csc.Galant.gui.util.WindowUtil;
import edu.ncsu.csc.Galant.gui.window.GraphWindow;
import edu.ncsu.csc.Galant.Timer;

/**
 * Class from which Galant runs.
 * @author Michael Owoc, Jason Cockrell, Alex McCabe, Ty Devries
 */
public class Galant {
  public static final String VERSION = "v6.0.2";
  public static void main(String[] args) {
    ExceptionDialog.setDialogExceptionHandlerAsDefault();
    SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          Timer.parsingTime = new Timer("parsing");
          Timer.drawingTime = new Timer("drawing");
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

//  [Last modified: 2017 07 25 at 19:21:14 GMT]
