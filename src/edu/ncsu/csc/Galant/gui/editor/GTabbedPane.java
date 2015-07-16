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
 * stateChanged method. 
 *
 * @todo The dialog for closing a dirty tab should also be invoked when
 * quitting Galant. This class is not the place for it.
 *
 * @author Michael Owoc
 */
public class GTabbedPane extends JTabbedPane implements ChangeListener {
	
    //	private static final long serialVersionUID = 170081847L;
	
	public static final String EMPTY_ALGORITHM_FILE_NAME
        = "untitled." + AlgorithmOrGraph.Algorithm.getDefaultFileExtension();
	public static final String EMPTY_ALGORITHM_CONTENT
        = "algorithm {\n}";
	public static final String EMPTY_GRAPH_FILE_NAME
        = "untitled." + AlgorithmOrGraph.Graph.getDefaultFileExtension();
	public static final String EMPTY_GRAPH_CONTENT
        = "<graphml><graph></graph></graphml>";

    // a lot of what follows will not be needed if we're willing to have
    // multiple empty graphs and algorithms; probably the easiest solution
    // and there's no compelling reason to do otherwise

    /**
     * The icons for creating empty algorithms and graphs, respectively, will
     * be in indexes 0 and 1; the panel for an empty graph, if one exists
     * will always be at index 2; an empty algorithm will be at 2 or 3,
     * depending on whether there is an empty graph.
     */
    private boolean emptyAlgorithmExists = false;
    private boolean emptyGraphExists = false;

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

    private int numberOfEmptyPanels() {
        int count = 0;
        if ( emptyAlgorithmExists ) count++;
        if ( emptyGraphExists ) count++;
        return count;
    }

    private int emptyGraphIndex() { return 2; }
    private int emptyAlgorithmIndex() { return emptyGraphExists ? 3 : 2; }

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
				for(AlgorithmOrGraph type : values())
					ALL_FILE_EXTS.addAll(type.getFileExtensions());
			}
		public static List<String> getAllFileExtensions()
			{
				return ALL_FILE_EXTS;
			}
		public static AlgorithmOrGraph typeForFileName(String filename)
			{
				for(AlgorithmOrGraph type : values())
					for(String extension : type.getFileExtensions())
						if(filename.endsWith("." + extension))
							return type;
				return null;
			}
		
		private List<String> fileExtensions;
		private AlgorithmOrGraph(String... fileExtensions)
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
	
	private final ImageIcon closeTabIcon = new ImageIcon(getClass().getResource("images/close_14s.png"));
	private final ImageIcon newAlgorithmIcon = new ImageIcon(getClass().getResource("images/newalgorithm_24.png"));
	private final ImageIcon newGraphIcon = new ImageIcon(getClass().getResource("images/newgraph_24.png"));
	
	private final ArrayList<GEditorPanel> editorPanels = new ArrayList<GEditorPanel>();
		
	public GTabbedPane() {
		super();
		addTab(null, newAlgorithmIcon, null, newAlgorithm);
		addTab(null, newGraphIcon, null, newGraph);
	    
		restoreLastState();
		
		addTabIfNeeded();
		addChangeListener(this);
	}
	
    /**
     * Adds a tab (for an empty graph) if the only remaining tabs are the
     * icons for creating an empty graph or algorithm.
     */
	private void addTabIfNeeded() {
        if ( getTabCount() <= 2 ) {
            addEmptyGraph();
        }
	}
	
    /**
     * Adds a tab for an empty algorithm.
     */
	public void addEmptyAlgorithm() {
        addEditorTab( EMPTY_ALGORITHM_FILE_NAME, 
                      null,
                      EMPTY_ALGORITHM_CONTENT, 
                      AlgorithmOrGraph.Algorithm ).setDirty(true);
        emptyAlgorithmExists = true;
        System.out.println( "Added empty algorithm, selectedIndex = " + getSelectedIndex() );
    }

	/**
     * Adds a tab for an empty graph; the pane contains wrapper GraphML text
	 * to avoid a NullPointerException when the GraphMLParser tries to parse
	 * it tab is created or an algorithm is fired.
     */
	public void addEmptyGraph() {
        addEditorTab( EMPTY_GRAPH_FILE_NAME,
                      null,
                      EMPTY_GRAPH_CONTENT,
                      AlgorithmOrGraph.Graph ).setDirty(true);
        emptyGraphExists = true;
        System.out.println( "Added empty graph, selectedIndex = " + getSelectedIndex() );
    }
	
    /**
     * Adds a new editor tab and the associated panel and returns the latter.
     * @param filename the name of the file whose contents are displayed
     * (this is shown on the tab)
     * @param filepath the path to the directory containing the file
     * @param content the text contained in the file
     * @param type whether this is an algorithm or a graph
     */
	public GEditorPanel addEditorTab( String filename, String filepath, String content,
                                      AlgorithmOrGraph type ) {
		String fullyQualifiedName = (filepath != null) ? filepath + "/" + filename : null;
		GEditorPanel panel;
		if(type == AlgorithmOrGraph.Graph)
			panel = new GGraphEditorPanel(this, filename, content);
		else if(type == AlgorithmOrGraph.Algorithm)
			panel = new GAlgorithmEditorPanel(this, filename, content);
		else if(type == AlgorithmOrGraph.CompiledAlgorithm)
			panel = new GCompiledAlgorithmEditorPanel(this, filename, content);
		else return null;
		
		if(filepath != null) panel.setFilePath(filepath);
		TabRenderer tbr = new TabRenderer(filename, panel);
		
//         if ( emptyAlgorithmExists && type == AlgorithmOrGraph.Algorithm ) {
//             System.out.println( "Removing empty algorithm at index " + emptyAlgorithmIndex() );
//             removeEditorTab( emptyAlgorithmIndex() );
//             emptyAlgorithmExists = false;
//         }
//         else if ( emptyGraphExists && type == AlgorithmOrGraph.Graph ) {
//             System.out.println( "Removing empty graph at index " + emptyGraphIndex() );
//             removeEditorTab( emptyGraphIndex() );
//             emptyGraphExists = false;
//         }

        System.out.println( "Adding new tab: file = " + filename
                           + ", emptyGraph = " +  emptyGraphExists
                           + ", emptyAlg = " + emptyAlgorithmExists
                            + ", #empty = " + numberOfEmptyPanels() );
        insertTab(filename, null, panel, fullyQualifiedName, newTabPosition(type));
        setTabComponentAt(newTabPosition(type), tbr);
        setSelectedIndex(newTabPosition(type));
		
		panel.setTabRenderer(tbr);
		editorPanels.add(panel);
		serializeState();
        System.out.println( "Done adding new tab: " + "emptyGraph = " +  emptyGraphExists
                            + ", emptyAlg = " + emptyAlgorithmExists
                            + ", #empty = " + numberOfEmptyPanels()
                            + ", selectIndex = " + getSelectedIndex()
                            );
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
			file = new File(Preference.PREFERENCES_NODE.get("editSession" + tabIndex, ""));
			open( file );
		}
	}
	
	/**
	 * Records the current files open in the editor.
	 * Should be called any time the number or nature of tabs changes.
	 */
	protected void serializeState() {
		int numberOfFiles = 0;
		
		for ( GEditorPanel gep : editorPanels ) {
			if ( gep.getFilePath() != null && gep.getFilePath().length() > 0 ) {
                numberOfFiles++;
				Preference.PREFERENCES_NODE.put("editSession" + numberOfFiles, gep.getFilePath());
			}
		}
		
		Accessors.INT_ACCESSOR.put("numberOfFiles", numberOfFiles);
	}
	
    /**
     * Updates information about existence of empty graphs and algorithms on
     * removal of a tab
     */
    public void updateEmptiesOnRemoval( int removedIndex ) {
        if ( removedIndex == emptyGraphIndex() ) emptyGraphExists = false;
        else if ( removedIndex == emptyAlgorithmIndex() ) emptyAlgorithmExists = false;
    }

    /**
     * Removes the tab and panel at the given index
     */
	public void removeEditorTab(int index) {
        System.out.println( "removeEditorTab: index = " + index );
        JPanel panel = (JPanel) getComponentAt(index);
 		remove(index); 
    	editorPanels.remove(panel);
    	serializeState();
        updateEmptiesOnRemoval( index );
	}

    /**
     * Removes the tab associated with the given panel and the panel itself
     */
	public void removeEditorTab(JPanel panel) {
        int index = indexOfComponent(panel);
        System.out.println( "removeEditorTab(panel): index = " + index );
		remove(index); 
    	editorPanels.remove(panel);
    	serializeState();
        updateEmptiesOnRemoval( index );
	}

    /**
     * @return the selected panel, cast as GEditorPanel
     */
	public GEditorPanel getSelectedPanel() {
		if (getSelectedComponent() == null) return null;
		return (GEditorPanel) getSelectedComponent();
	}
	
    /**
     * Determines what a tab will look like and how it will react to the
     * mouse.
     */
	class TabRenderer extends JPanel implements MouseListener { 
		private JPanel panel;
		private JLabel title;
		
        /**
         * Creates the tab with a "close" icon on the left and the file name
         * on the right.
         */
		public TabRenderer(String _filename, JPanel _panel) {
			super();
			panel = _panel;
			
			setOpaque(false);
			setLayout(new BorderLayout());
			
			title = new JLabel();
			JLabel icon = new JLabel(closeTabIcon);
			icon.setToolTipText(CLOSE_TAB);
			
			icon.addMouseListener(this);
			
			add(title, BorderLayout.WEST);
			add(icon, BorderLayout.CENTER);
			
			updateLabel(_filename, false);
		}
		
        /**
         * Puts the file name on the tab -- with a * to the left if it has
         * been modified.
         */
		public void updateLabel(String _filename, boolean _isDirty) {
			if(_isDirty) title.setText("*" + _filename + "  ");
			else title.setText(" " + _filename + "  ");
		}
		
        /**
         * If the mouse is clicked on (the close icon of?) this tab, closes
         * the panel (unless file has been modified and user answers "no")
         * and do housekeeping.
         *
         * @todo this appears to be where at least one algorithm and one
         * graph panel is kept open. The relevant code is a mess.
         */
		@Override
		public void mouseClicked(MouseEvent arg0) {
			AlgorithmOrGraph typeSelected = null;
			boolean foundGraph = false;
			boolean foundAlg = false;
			boolean foundSelected = false;
// 			if(getTabCount() < 4) return;
			if(panel != null){
				GEditorPanel thisEditorPanel = (GEditorPanel) panel;
				if ( thisEditorPanel instanceof GGraphEditorPanel )
                    typeSelected = AlgorithmOrGraph.Graph;
				if ( thisEditorPanel instanceof GAlgorithmEditorPanel )
                    typeSelected = AlgorithmOrGraph.Algorithm;
				if ( thisEditorPanel instanceof GCompiledAlgorithmEditorPanel )
                    typeSelected = AlgorithmOrGraph.CompiledAlgorithm;

				if ( thisEditorPanel.getDirty() && thisEditorPanel.getText().length() > 0 )
					if ( JOptionPane.showOptionDialog( getParent().getParent(), confirmClose, CONFIRM, 
                                                       JOptionPane.YES_NO_OPTION,
                                                       JOptionPane.QUESTION_MESSAGE,
                                                       null, new Object[] {YES, NO}, NO ) != 0 )
                        return;
			}
	    	GTabbedPane.this.removeEditorTab(panel);

            // need at least one non-empty panel (deprecated)
            // addTabIfNeeded();
		}
		@Override
		public void mouseEntered(MouseEvent arg0) {}
		@Override
		public void mouseExited(MouseEvent arg0) {}
		@Override
		public void mousePressed(MouseEvent arg0) {}
		@Override
		public void mouseReleased(MouseEvent arg0) {}
	}

    /**
     * The following is called whenever a tab is clicked.
     * if index is 0 or 1, creates an empty algorithm or graph, respectively
     */
	@Override
	public void stateChanged( ChangeEvent arg0 ) {
		int selectionIndex = getModel().getSelectedIndex();
        System.out.printf( "stateChanged, index = %d\n", selectionIndex );
		if ( selectionIndex == algorithmCreationIndex() )
            addEmptyAlgorithm();
		else if ( selectionIndex == graphCreationIndex() )
            addEmptyGraph();
		
        System.out.printf( "stateChanged: selected index after creation = %d\n", getSelectedIndex() );
		GEditorPanel graphEditPanel = getSelectedPanel();
		if ( graphEditPanel != null
             && GGraphEditorPanel.class.isInstance(graphEditPanel) ) {
			GGraphEditorPanel geditorPanel = (GGraphEditorPanel) graphEditPanel;
			try {
                String panelText = geditorPanel.getText();
                if ( ! panelText.equals( "" ) ) {
                    GraphMLParser parser = new GraphMLParser( panelText );
                    GraphDispatch.getInstance().setWorkingGraph(parser.getGraph(), geditorPanel.getUUID());
                }
                else {
                    GraphDispatch.getInstance().setWorkingGraph(new Graph(), geditorPanel.getUUID());
                }
			} catch (Exception e) {
				GraphDispatch.getInstance().setWorkingGraph(new Graph(), geditorPanel.getUUID());
			}
		}
	}

    /**
     * Creates a new tab for the given file. Called when a file is opened via
     * the file menu.
     */
	private void open( File file ) {
        if ( file.getName().endsWith( ".alg" )
             || file.getName().endsWith( ".txt" )
             || file.getName().endsWith( ".graphml" ) ) {
            Scanner scanner = null;
            try {
                GTabbedPane.AlgorithmOrGraph type;
                if ( file.getName().endsWith(".alg")
                     || file.getName().endsWith(".txt") )
                    type = GTabbedPane.AlgorithmOrGraph.Algorithm;
                else type = GTabbedPane.AlgorithmOrGraph.Graph;
       		 
                scanner = new Scanner(file);
                scanner.useDelimiter("\\A");
                addEditorTab( file.getName(), file.getPath(), scanner.next(), type );

            } catch (Exception e) { ExceptionDialog.displayExceptionInDialog(e); }
       	  	finally { if (scanner != null) scanner.close(); }
        }
	}
	
	public void setFontSize(Integer size) {
		for(GEditorPanel geditorPanel : editorPanels) 
			geditorPanel.setFontSize(size);
	}
	
	public void setTabSize(Integer size) {
		for(GEditorPanel geditorPanel : editorPanels) 
			geditorPanel.setTabSize(size);
	}
	
	public boolean isDirty() {
		for(GEditorPanel geditorPanel : editorPanels) if(geditorPanel.isDirty) return true;
		return false;
	}
}

//  [Last modified: 2015 07 16 at 00:28:32 GMT]
