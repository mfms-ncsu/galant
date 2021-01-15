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
  public static final String VERSION = "v6.1.2";
  public static void main(String[] args) {
    ExceptionDialog.setDialogExceptionHandlerAsDefault();
    SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          Timer.parsingTime = new Timer("parsing");
          Timer.drawingTime = new Timer("drawing");
          GraphDispatch graphDispatch = GraphDispatch.getInstance();
          GalantPreferences.initPrefs();
          GraphWindow graphWindow = new GraphWindow(graphDispatch);
          graphDispatch.setGraphWindow(graphWindow);
          GEditorFrame editorFrame = new GEditorFrame();
          GraphWindow.getGraphFrame().addWindowListener(editorFrame);
          WindowUtil.linkWindows();
          GraphDispatch.getInstance().pushToGraphEditor();
          GraphDispatch.getInstance().setEditMode(true);
        }
      });
  }
}

//  [Last modified: 2021 01 15 at 21:44:19 GMT]
