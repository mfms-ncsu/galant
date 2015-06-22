package edu.ncsu.csc.Galant.gui.editor;

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.prefs.Preferences;

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
 * Pane stored as a tab in the text editor window.
 * @author Michael Owoc
 *
 */
public class GTabbedPane extends JTabbedPane implements ChangeListener {
	
	private static final long serialVersionUID = 170081847L;
	
	public static final String emptyAlgorithmFilename = "unsaved_alg." + AlgorithmOrGraph.Algorithm.getDefaultFileExtension();
	public static final String emptyGraphFilename = "unsaved_graph." + AlgorithmOrGraph.Graph.getDefaultFileExtension();
	public static final String newAlgorithm = "Create new algorithm";
	public static final String newGraph = "Create new graph";
	public static final String CLOSE_TAB = "Close tab";
	public static final String CONFIRM = "Confirm";
	public static final String confirmClose = "File has unsaved changes. Really close tab?";
	public static final String YES = "Yes";
	public static final String NO = "No";
	
	public static enum AlgorithmOrGraph {
		// Note: there shouldn't be any overlap between extensions for algorithms and extensions for graphs,
		// because that would make the results of typeForFileName(String) not be well-defined.
		Algorithm("alg", "txt"), Graph("graphml");
		
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
		
		addEmptyTabs();
		addChangeListener(this);
	}
	
	private void addEmptyTabs() {
		boolean hasAlgorithm = false;
		boolean hasGraph = false;
		for(GEditorPanel gep : editorPanels) {
			if(gep instanceof GAlgorithmEditorPanel) hasAlgorithm = true;
			if(gep instanceof GGraphEditorPanel) hasGraph = true;
		}
		if(!hasAlgorithm) addEmptyAlgorithm();
		if(!hasGraph) addEmptyGraph();
	}
	
	public void addEmptyAlgorithm(){addEditorTab(emptyAlgorithmFilename, null, "", AlgorithmOrGraph.Algorithm).setDirty(true);}
	public void addEmptyGraph(){addEditorTab(emptyGraphFilename, null, "", AlgorithmOrGraph.Graph).setDirty(true);}
	
	public GEditorPanel addEditorTab(String filename, String filepath, String content, AlgorithmOrGraph type) {
		return addEditorTab(filename, filepath, content, type, true);
	}
	public GEditorPanel addEditorTab(String filename, String filepath, String content, AlgorithmOrGraph type, boolean serialize) {
		String fullyQualifiedName = (filepath != null) ? filepath + "/" + filename : null;
		GEditorPanel panel;
		if(type == AlgorithmOrGraph.Graph)
			panel = new GGraphEditorPanel(this, filename, content);
		else if(type == AlgorithmOrGraph.Algorithm)
			panel = new GAlgorithmEditorPanel(this, filename, content);
		else return null;
		
		if(filepath != null) panel.setFilePath(filepath);
		TabRenderer tbr = new TabRenderer(filename, panel);
		
		if(getSelectedPanel() != null 
			&& getSelectedPanel().getText().equals("") && getSelectedPanel().getFilePath() == null
			&& ((getSelectedPanel() instanceof GAlgorithmEditorPanel && type == AlgorithmOrGraph.Algorithm)
			|| (getSelectedPanel() instanceof GGraphEditorPanel && type == AlgorithmOrGraph.Graph))) {

			insertTab(filename, null, panel, fullyQualifiedName, getSelectedIndex());
			setSelectedIndex(getSelectedIndex()-1);
			setTabComponentAt(getSelectedIndex(), tbr);
			removeEditorTab((JPanel) getComponentAt(getSelectedIndex()+1));
			
		} else {
			setSelectedIndex(0);
			insertTab(filename, null, panel, fullyQualifiedName, (getTabCount() == 0 ? 0 : getTabCount()-2));
			setTabComponentAt((getTabCount() == 2 ? 0 : getTabCount()-3), tbr);
			setSelectedIndex(getTabCount() == 2 ? 0 : getTabCount()-3);
		}
		
		panel.setTabRenderer(tbr);
		editorPanels.add(panel);
		if(serialize) serializeState();
		return panel;
	}
	
	/**
	 * Attempt to reopen any previously edited files.
	 * Should only be called upon initialization.
	 */
	protected void restoreLastState() {
		Integer ii = Accessors.INT_ACCESSOR.get("numberOfFiles");
		if(ii == null)
			ii = 0;
		int jj = 0;
		
		File file;
		while(jj < ii) {
			file = new File(Preference.PREFERENCES_NODE.get("editSession" + ++jj, ""));
			open(file);
		}
	}
	
	/**
	 * Record the current files open in the editor. 
	 * Should be called any time the number or nature of tabs changes.
	 */
	protected void serializeState() {
		int ii = 0;
		
		for(GEditorPanel gep : editorPanels)  {
			if(gep.getFilePath() != null && gep.getFilePath().length() > 0) {
				Preference.PREFERENCES_NODE.put("editSession" + ++ii, gep.getFilePath());
			}
		}
		
		Accessors.INT_ACCESSOR.put("numberOfFiles", ii);
	}
	
	public void removeEditorTab(JPanel panel) {
		removeTabAt(indexOfComponent(panel)); 
    	remove(panel);
    	editorPanels.remove(panel);
    	serializeState();
	}
	
	public GEditorPanel getSelectedPanel() {
		if (getSelectedComponent() == null) return null;
//		return (GEditorPanel) ((JScrollPane)getSelectedComponent()).getViewport().getView();
		return (GEditorPanel) getSelectedComponent();
	}
	
	class TabRenderer extends JPanel implements MouseListener { 
		private JPanel panel;
		private JLabel title;
		
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
		
		public void updateLabel(String _filename, boolean _isDirty) {
			if(_isDirty) title.setText("*" + _filename + "  ");
			else title.setText(" " + _filename + "  ");
		}
		
		@Override
		public void mouseClicked(MouseEvent arg0) {
			AlgorithmOrGraph typeSelected = null;
			boolean foundGraph = false;
			boolean foundAlg = false;
			boolean foundSelected = false;
			if(getTabCount() < 4) return;
			if(panel != null){
				GEditorPanel gaep = (GEditorPanel) panel;
				if(gaep instanceof GGraphEditorPanel) typeSelected = AlgorithmOrGraph.Graph;
				if(gaep instanceof GAlgorithmEditorPanel) typeSelected = AlgorithmOrGraph.Algorithm;
				for(GEditorPanel gep : editorPanels) {
					if(gep instanceof GAlgorithmEditorPanel) {
						if(!foundAlg) foundAlg = true;
						else if(typeSelected == AlgorithmOrGraph.Algorithm) foundSelected = true;
					}
					if(gep instanceof GGraphEditorPanel) {
						if(!foundGraph) foundGraph = true;
						else if(typeSelected == AlgorithmOrGraph.Graph) foundSelected = true;
					}
				}
				if(!foundGraph || !foundAlg || !foundSelected) return;
				if(gaep.getDirty() && gaep.getText().length() > 0)
					if(JOptionPane.showOptionDialog(getParent().getParent(), confirmClose, CONFIRM, 
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new Object[] {YES, NO},NO) != 0) return;
			}
	    	if(getSelectedIndex() == indexOfComponent(panel)) {
	    		if(getSelectedIndex() > 0) setSelectedIndex(getSelectedIndex()-1);
	    		else setSelectedIndex(getSelectedIndex()+1);
	    	}
	    	GTabbedPane.this.removeEditorTab(panel);
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

	@Override
	public void stateChanged(ChangeEvent arg0) {
		int selectionIndex = getModel().getSelectedIndex();
		if(selectionIndex == getTabCount()-2) {	addEmptyAlgorithm(); }
		else if(selectionIndex == getTabCount()-1) { addEmptyGraph() ;}
		
		GEditorPanel gp = getSelectedPanel();
		if (gp != null && GGraphEditorPanel.class.isInstance(gp)) {
			GGraphEditorPanel gep = (GGraphEditorPanel) gp;
			try {
                String panelText = gep.getText();
                if ( ! panelText.equals( "" ) ) {
                    GraphMLParser parser = new GraphMLParser( panelText );
                    GraphDispatch.getInstance().setWorkingGraph(parser.getGraph(), gep.getUUID());
                }
                else {
                    GraphDispatch.getInstance().setWorkingGraph(new Graph(), gep.getUUID());
                }
			} catch (Exception e) {
				GraphDispatch.getInstance().setWorkingGraph(new Graph(), gep.getUUID());
			}
		}
	}
	
	private void open(File file) {
        if( file.getName().endsWith( ".alg" )
            || file.getName().endsWith( ".txt" )
            || file.getName().endsWith( ".graphml" )) {
       	 Scanner scanner = null;
       	  try {
       		 GTabbedPane.AlgorithmOrGraph type;
       		 if(file.getName().endsWith(".alg") || file.getName().endsWith(".txt")) type = GTabbedPane.AlgorithmOrGraph.Algorithm;
       		 else type = GTabbedPane.AlgorithmOrGraph.Graph;
       		 
        	 scanner = new Scanner(file);
        	 scanner.useDelimiter("\\A");
        	 addEditorTab(file.getName(), file.getPath(), scanner.next(), type, false);

       	  } catch(Exception e) { ExceptionDialog.displayExceptionInDialog(e); }
       	  	finally { if(scanner != null) scanner.close(); }
        }
	}
	
	public void setFontSize(Integer size) {
		for(GEditorPanel gep : editorPanels) 
			gep.setFontSize(size);
	}
	
	public void setTabSize(Integer size) {
		for(GEditorPanel gep : editorPanels) 
			gep.setTabSize(size);
	}
	
	public boolean isDirty() {
		for(GEditorPanel gep : editorPanels) if(gep.isDirty) return true;
		return false;
	}
}

//  [Last modified: 2015 05 02 at 12:34:33 GMT]
