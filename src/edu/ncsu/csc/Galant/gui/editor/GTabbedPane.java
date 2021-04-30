package edu.ncsu.csc.Galant.gui.editor;

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.ncsu.csc.Galant.GraphDispatch;
import edu.ncsu.csc.Galant.GalantException;
import edu.ncsu.csc.Galant.logging.LogHelper;
import edu.ncsu.csc.Galant.graph.component.Graph;
import edu.ncsu.csc.Galant.graph.parser.GraphMLParser;
import edu.ncsu.csc.Galant.gui.util.ExceptionDialog;
import edu.ncsu.csc.Galant.prefs.Accessors;
import edu.ncsu.csc.Galant.prefs.Preference;

/**
 * This class handles everything related to the tabs at the top of the text
 * window. A tabbed panel is created when a file is opened or when the tab
 * with an empty algorithm/graph icon is selected. A panel is removed by
 * clicking it's x icon -- mouseClicked() method. A panel is selected via the
 * stateChanged method. If there are no other panels, there will
 * always be one for an empty graph.
 *
 * @todo The dialog for closing a dirty tab should also be invoked when
 * quitting Galant. It works with the menu but not with Command/Alt-Q,
 * at least not on a Mac.
 *
 * @author Michael Owoc, radically simplified by Matthias Stallmann
 */
public class GTabbedPane extends JTabbedPane implements ChangeListener {

  public static final String EMPTY_ALGORITHM_FILE_NAME
    = "untitled." + AlgorithmOrGraph.Algorithm.getDefaultFileExtension();
  public static final String EMPTY_ALGORITHM_CONTENT
    = "algorithm {\n}";
  public static final String EMPTY_GRAPH_FILE_NAME
    = "untitled." + AlgorithmOrGraph.Graph.getDefaultFileExtension();
  public static final String EMPTY_GRAPH_CONTENT
    = "<graphml><graph></graph></graphml>";

  public final GraphDispatch dispatch = GraphDispatch.getInstance();

  /**
   * The icons for creating empty algorithms and graphs, respectively, will
   * be in indexes 0 and 1; the panel for an empty graph, if one exists
   * will always be at index 2; an empty algorithm will be at 2 or 3,
   * depending on whether there is an empty graph.
   */

  /**
   * The following indexes are functions rather than constants so that
   * their calculation can be changed, e.g., to put creation tabs on the
   * right instead of the left.
   */
  private int algorithmCreationIndex() { return 0; }
  private int graphCreationIndex() { return 1; }
  /**
   * Index at which to insert any new tab, i.e., just to the right of
   * the creation tabs
   */
  private int newTabPosition(AlgorithmOrGraph type) {
    return 2;
  }
  /**
   * true if an empty algorithm tab exists; ensures there is only one at a
   * time; set in serializeState(), where all tabs are examined
   */
  private boolean emptyAlgorithmExists = false;
  /**
   * true if an empty graph tab exists; ensures there is only one at a
   * time; set in serializeState(), where all tabs are examined
   */
  private boolean emptyGraphExists = false;

  public static final String newAlgorithm = "Create new algorithm";
  public static final String newGraph = "Create new graph";
  public static final String CLOSE_TAB = "Close tab";
  public static final String CONFIRM = "Confirm";
  public static final String confirmClose = "File has unsaved changes. Really close tab?";
  public static final String YES = "Yes";
  public static final String NO = "No";

  public static enum AlgorithmOrGraph {
    CompiledAlgorithm("class"), Algorithm("alg"), Graph("graphml");

    private static final List<String> ALL_FILE_EXTS = new ArrayList<String>();
    static
    {
      for ( AlgorithmOrGraph type : values() )
        ALL_FILE_EXTS.addAll( type.getFileExtensions() );
    }
    public static List<String> getAllFileExtensions()
    {
      return ALL_FILE_EXTS;
    }
    public static AlgorithmOrGraph typeForFileName(String filename)
    {
      for ( AlgorithmOrGraph type : values() )
        for ( String extension : type.getFileExtensions() )
          if ( filename.endsWith("." + extension) )
            return type;
      return null;
    }

    private List<String> fileExtensions;
    private AlgorithmOrGraph(String ... fileExtensions)
    {
      this.fileExtensions = Arrays.asList(fileExtensions);
    }
    public List<String> getFileExtensions()
    {
      return fileExtensions;
    }
    public String getDefaultFileExtension()
    {
      return fileExtensions.get(0);
    }
  };

  private final ImageIcon closeTabIcon =
    new ImageIcon( getClass().getResource("images/close_14s.png") );
  private final ImageIcon newAlgorithmIcon =
    new ImageIcon( getClass().getResource("images/newalgorithm_24.png") );
  private final ImageIcon newGraphIcon =
    new ImageIcon( getClass().getResource("images/newgraph_24.png") );

  private final ArrayList<GEditorPanel> editorPanels = new ArrayList<GEditorPanel>();

  public GTabbedPane() {
    super();
    addTab(null, newAlgorithmIcon, null, newAlgorithm);
    addTab(null, newGraphIcon,     null, newGraph);

    restoreLastState();

    addTabIfNeeded();
    addChangeListener(this);
  }

  /**
   * Adds a tab (for an empty graph) if and only if the only remaining tabs
   * are the icons for creating an empty graph or algorithm.
   */
  private void addTabIfNeeded() {
    if ( getTabCount() <= 2 ) {
      addEmptyGraph();
    }
  }

  /**
   * Adds a tab for an empty algorithm.
   * Only one empty graph at a time is allowed.
   */
  public void addEmptyAlgorithm() {
    if ( ! emptyAlgorithmExists ) {
      addEditorTab(EMPTY_ALGORITHM_FILE_NAME,
                   null,
                   EMPTY_ALGORITHM_CONTENT,
                   AlgorithmOrGraph.Algorithm);
    }
  }

  /**
   * Adds a tab for an empty graph; the pane contains wrapper GraphML text
   * to avoid a NullPointerException when the GraphMLParser tries to parse
   * it tab is created or an algorithm is fired.
   * Only one empty alogorithm at a time.
   */
  public void addEmptyGraph() {
    if ( ! emptyGraphExists ) {
      addEditorTab(EMPTY_GRAPH_FILE_NAME,
                   null,
                   EMPTY_GRAPH_CONTENT,
                   AlgorithmOrGraph.Graph);
    }
  }

  /**
   * Adds a new editor tab and the associated panel and returns the latter.
   * @param filename the name of the file whose contents are displayed
   * (this is shown on the tab)
   * @param filepath the path to the directory containing the file
   * @param content the text contained in the file
   * @param type whether this is an algorithm or a graph
   */
  public GEditorPanel addEditorTab(String filename, String filepath, String content,
                                   AlgorithmOrGraph type) {
    String fullyQualifiedName = (filepath != null) ? filepath + "/" + filename : null;
    GEditorPanel panel;
    GraphDispatch dispatch = GraphDispatch.getInstance();
    if ( type == AlgorithmOrGraph.Graph )
      panel = new GGraphEditorPanel(this, filename, content);
    else if ( type == AlgorithmOrGraph.Algorithm )
      panel = new GAlgorithmEditorPanel(this, filename, content);
// else if(type == AlgorithmOrGraph.CompiledAlgorithm)
// panel = new GCompiledAlgorithmEditorPanel(this, filename, content);
    else return null;

    if ( filepath != null ) panel.setFilePath(filepath);
    TabRenderer tbr = new TabRenderer(filename, panel);
    insertTab( filename, null, panel, fullyQualifiedName,
               newTabPosition(type) );
    setTabComponentAt(newTabPosition(type), tbr);
    setSelectedIndex( newTabPosition(type) );

    panel.setTabRenderer(tbr);
    editorPanels.add(panel);
    serializeState();
    return panel;
  }

  /**
   * Attempt to reopen any previously edited files.
   * Should only be called upon initialization.
   */
  protected void restoreLastState() {
    Integer numberOfFiles = Accessors.INT_ACCESSOR.get("numberOfFiles");
    if ( numberOfFiles == null )
      numberOfFiles = 0;
    int tabIndex = 0;

    File file;
    while ( tabIndex < numberOfFiles ) {
      tabIndex++;
      file = new File( Preference.PREFERENCES_NODE.get("editSession" + tabIndex, "") );
      open(file);
    }
  }

  /**
   * Records the current files open in the editor.
   * Should be called any time the number or nature of tabs changes.
   * As a side effect, makes a note of the existence of empty graphs or
   * empty algorithms.
   */
  protected void serializeState() {
    LogHelper.enterMethod(getClass(), "serializeState");
    int numberOfFiles = 0;

    emptyAlgorithmExists = false;
    emptyGraphExists = false;
    for ( GEditorPanel gep : editorPanels ) {
      LogHelper.logDebug( "serializeState, filePath = " + gep.getFilePath()
                          + ", fileName = " + gep.getFileName() );
      String filePath = gep.getFilePath();
      if ( filePath != null && filePath.length() > 0 ) {
        if ( filePath.equals(EMPTY_ALGORITHM_FILE_NAME) )
          emptyAlgorithmExists = true;
        if ( filePath.equals(EMPTY_GRAPH_FILE_NAME) )
          emptyGraphExists = true;
        numberOfFiles++;
        Preference.PREFERENCES_NODE.put( "editSession" + numberOfFiles,
                                         gep.getFilePath() );
      } else {
        String fileName = gep.getFileName();
        if ( fileName.equals(EMPTY_ALGORITHM_FILE_NAME) )
          emptyAlgorithmExists = true;
        if ( fileName.equals(EMPTY_GRAPH_FILE_NAME) )
          emptyGraphExists = true;
      }
    }

    Accessors.INT_ACCESSOR.put("numberOfFiles", numberOfFiles);
    LogHelper.exitMethod(getClass(),
                         "serializeState, numberOfFiles = " + numberOfFiles);
  }

  /**
   * Removes the tab and panel at the given index
   */
  public void removeEditorTab(int index) {
    JPanel panel = (JPanel) getComponentAt(index);
    removeEditorTab(index, panel);
  }

  /**
   * Removes the tab associated with the given panel and the panel itself
   */
  public void removeEditorTab(JPanel panel) {
    int index = indexOfComponent(panel);
    removeEditorTab(index, panel);
  }

  /**
   * Removes the tab at the given index and the given panel; assumes that
   * index is the index of the tab associated with the panel
   */
  private void removeEditorTab(int index, JPanel panel) {
    remove(index);
    editorPanels.remove(panel);
    serializeState();
  }

  /**
   * @return the selected panel, cast as GEditorPanel
   */
  public GEditorPanel getSelectedPanel() {
    if ( getSelectedComponent() == null ) return null;
    return (GEditorPanel) getSelectedComponent();
  }

  /**
   * Determines what a tab will look like and how it will react to the
   * mouse.
   */
  class TabRenderer extends JPanel implements MouseListener {
    private JPanel panel;
    private JLabel title;
    private String filename;

    /**
     * Creates the tab with a "close" icon on the left and the file name
     * on the right.
     */
    public TabRenderer(String _filename, JPanel _panel) {
      super();
      filename = _filename;
      panel = _panel;

      setOpaque(false);
      setLayout( new BorderLayout() );

      title = new JLabel();
      JLabel icon = new JLabel(closeTabIcon);
      icon.setToolTipText(CLOSE_TAB);

      icon.addMouseListener(this);

      add(title, BorderLayout.WEST);
      add(icon,  BorderLayout.CENTER);

      updateLabel(filename, false);
    }

    /**
     * Puts the file name on the tab -- with a * to the left if it has
     * been modified.
     */
    public void updateLabel(String filename, boolean _isDirty) {
      if ( _isDirty ) title.setText("*" + filename + "  ");
      else title.setText(" " + filename + "  ");
    }

    /**
     * @return the filename associated with this tab
     */
    public String getFilename() { return filename; }

    /**
     * If the mouse is clicked on (the close icon of?) this tab, closes
     * the panel (unless file has been modified and user answers "no")
     * and does housekeeping.
     */
    @Override
    public void mouseClicked(MouseEvent arg0) {
      AlgorithmOrGraph typeSelected = null;
      boolean foundGraph = false;
      boolean foundAlg = false;
      boolean foundSelected = false;
      if ( panel != null ) {
        GEditorPanel thisEditorPanel = (GEditorPanel) panel;
        if ( thisEditorPanel instanceof GGraphEditorPanel )
          typeSelected = AlgorithmOrGraph.Graph;
        if ( thisEditorPanel instanceof GAlgorithmEditorPanel )
          typeSelected = AlgorithmOrGraph.Algorithm;
        if ( thisEditorPanel.getDirty()
             && thisEditorPanel.getText().length() > 0 )
          if ( JOptionPane.showOptionDialog(getParent().getParent(),
                                            confirmClose, CONFIRM,
                                            JOptionPane.YES_NO_OPTION,
                                            JOptionPane.QUESTION_MESSAGE,
                                            null,
                                            new Object[] { YES, NO }, NO) != 0 )
            return;
      }
      GTabbedPane.this.removeEditorTab(panel);
    }
    @Override
    public void mouseEntered(MouseEvent arg0) { }
    @Override
    public void mouseExited(MouseEvent arg0) { }
    @Override
    public void mousePressed(MouseEvent arg0) { }
    @Override
    public void mouseReleased(MouseEvent arg0) { }
  }

  /**
   * The following is called whenever a tab is clicked.
   * if index is 0 or 1, creates an empty algorithm or graph, respectively
   */
  @Override
  public void stateChanged(ChangeEvent arg0) {

    int selectionIndex = getModel().getSelectedIndex();
    if ( selectionIndex == algorithmCreationIndex() )
      addEmptyAlgorithm();
    else if ( selectionIndex == graphCreationIndex() )
      addEmptyGraph();

    GEditorPanel graphEditPanel = getSelectedPanel();
    if ( graphEditPanel != null
         && GGraphEditorPanel.class.isInstance(graphEditPanel) ) {
      GGraphEditorPanel geditorPanel = (GGraphEditorPanel) graphEditPanel;
        // at this point you want to setWorkingGraph(gEditorPanel.getGraph())
        // and not do the stuff below
      try {
        String panelText = geditorPanel.getText();
        if ( ! panelText.equals("") ) {
          GraphMLParser parser = new GraphMLParser(panelText);
          GraphDispatch.getInstance().setWorkingGraph(
             parser.getGraph(), geditorPanel.getUUID() );
        } else {
          GraphDispatch.getInstance().setWorkingGraph( new Graph(),
                                                       geditorPanel.getUUID() );
        }
        
        // when switching tabs, intialize vwin
        dispatch.initializeVirtualWindow();
      }
      catch ( GalantException e ) {
        e.report("");
        e.displayStatic();
      }
      catch ( Exception e ) {
        System.out.println( e.getMessage() );
        ExceptionDialog.displayExceptionInDialog(e);
      }
    }
  }

  /**
   * Creates a new tab for the given file. Called when a file is opened via
   * the file menu.
   */
  private void open(File file) {
    if ( file.getName().endsWith(".alg")
         || file.getName().endsWith(".txt")
         || file.getName().endsWith(".graphml") ) {
      Scanner scanner = null;
      try {
        GTabbedPane.AlgorithmOrGraph type;
        if ( file.getName().endsWith(".alg")
             || file.getName().endsWith(".txt") )
          type = GTabbedPane.AlgorithmOrGraph.Algorithm;
        else type = GTabbedPane.AlgorithmOrGraph.Graph;

        scanner = new Scanner(file);
        scanner.useDelimiter("\\A");
        addEditorTab(file.getName(), file.getPath(), scanner.next(), type);

      } catch ( Exception e ) { ExceptionDialog.displayExceptionInDialog(e); }
      finally { if ( scanner != null ) scanner.close(); }
    }
  }

  public void setFontSize(Integer size) {
    for ( GEditorPanel geditorPanel : editorPanels )
      geditorPanel.setFontSize(size);
  }

  public void setTabSize(Integer size) {
    for ( GEditorPanel geditorPanel : editorPanels )
      geditorPanel.setTabSize(size);
  }

  public boolean isDirty() {
    for ( GEditorPanel geditorPanel : editorPanels ) {
      if ( geditorPanel.isDirty ) return true;
    }
    return false;
  }
}

// [Last modified: 2021 01 31 at 14:40:45 GMT]
