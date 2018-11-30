package edu.ncsu.csc.Galant.gui.editor;

import java.beans.PropertyChangeEvent;
import java.util.UUID;

import edu.ncsu.csc.Galant.GraphDispatch;
import edu.ncsu.csc.Galant.GalantException;
import edu.ncsu.csc.Galant.gui.util.ExceptionDialog;
import edu.ncsu.csc.Galant.graph.component.Graph;
import edu.ncsu.csc.Galant.graph.parser.GraphMLParser;
import edu.ncsu.csc.Galant.logging.LogHelper;

/**
 * Each instance of GGraphEditorPanel corresponds to a particular
 * edit session of a particular graph file, or unsaved graph.
 * The panel displays in the GTabbedPane.
 *
 * Compare to GAlgorithmEditPanel.
 *
 * @author Jason Cockrell
 */
public class GGraphEditorPanel extends GEditorPanel {

  private final UUID uuid = UUID.randomUUID();
    private Graph myGraph;

  /**
   * Create a new edit session of a graph.
   * @param gTabbedPane The parent tabbed pane, of which there is only ever one.
   * @param filename The name of the file to be edited, which may be an unsaved file with
   *a dummy name.
   * @param content The text which constitutes the content of the file to be edited. It is
   * either the result of reading in the file, or the empty string.
   */
  public GGraphEditorPanel(GTabbedPane gTabbedPane, String filename, String content) {
    super(gTabbedPane, filename, content);
    GraphDispatch dispatch = GraphDispatch.getInstance();
    LogHelper.disable();
    LogHelper.enterConstructor( getClass() );
    dispatch.addChangeListener(this);

    try {
      if ( ! content.equals("") ) {
        GraphMLParser parser = new GraphMLParser(content);
        myGraph = parser.getGraph();
      } else {
          myGraph = new Graph();
      }
    }
    catch ( GalantException e ) {
      e.report("");
      e.displayStatic();
    }
    catch ( Exception e ) {
      System.out.println( e.getMessage() );
      ExceptionDialog.displayExceptionInDialog(e);
    }

    dispatch.setWorkingGraph(myGraph, uuid);
    syntaxHighlighter = new GGraphSyntaxHighlighting(textPane);
    documentUpdated();
    LogHelper.exitConstructor( getClass() );
    LogHelper.restoreState();
  }

  /**
   * Special handling of dirty bit is required for graphs; otherwise it gets
   * set after GraphML parsing or animation execution
   */
  @Override
  public void setDirty(Boolean dirty) {
    LogHelper.disable();
    LogHelper.enterMethod(getClass(), "setDirty");
    if ( ! dirty ) super.setDirty(dirty);
    else {
      LogHelper.logDebug(" dirty is true");
      if ( GraphDispatch.getInstance().isEditMode() ) {
        super.setDirty(dirty);
        LogHelper.logDebug(" set dirty flag");
      }
    }
    LogHelper.exitMethod(getClass(), "setDirty");
    LogHelper.restoreState();
  }
  
  /**
   * Handle property changes to the graph, which occur when
   * the user modifies a graph through the visual editor (toolbar).
   */
  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    LogHelper.enable();
    LogHelper.enterMethod(getClass(), "propertyChange");
    if ( evt.getPropertyName().equals(GraphDispatch.ANIMATION_MODE) ) {
      if ( (Boolean) evt.getNewValue() ) {
        // entering animation mode
        LogHelper.logDebug(" to animation mode; disable text panel");
        this.textPane.setEnabled(false);
      }
      else {
        // back to edit mode
        this.textPane.setEnabled(true);
        LogHelper.logDebug(" to edit mode ...");
        if ( GraphDispatch.getInstance().getGraphSource().equals(uuid) ) {
          LogHelper.logDebug("  the right graph, updating");
          textPane.setText( GraphDispatch.getInstance().getWorkingGraph().xmlString() );
        }
      }
    } // end, in animation mode
    else {
      LogHelper.logDebug(" nothing to do with animation ...");
      if ( GraphDispatch.getInstance().getGraphSource().equals(uuid) ) {
        LogHelper.logDebug("  doing a text update in active panel");
        textPane.setText( GraphDispatch.getInstance().getWorkingGraph().xmlString() );
        GraphDispatch.getInstance().setEditMode(false);
      }
    } // end, not animation mode
    LogHelper.exitMethod(getClass(), "propertyChange");
    LogHelper.restoreState();
  }

  public UUID getUUID() {
    return uuid;
  }

}

// [Last modified: 2018 11 30 at 16:09:23 GMT]
