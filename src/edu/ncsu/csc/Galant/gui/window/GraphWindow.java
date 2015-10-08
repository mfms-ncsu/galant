package edu.ncsu.csc.Galant.gui.window;

import java.util.Random;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.KeyListener;
import java.awt.KeyboardFocusManager;
import java.awt.KeyEventDispatcher;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.JOptionPane;

import edu.ncsu.csc.Galant.GraphDispatch;
import edu.ncsu.csc.Galant.graph.component.Edge;
import edu.ncsu.csc.Galant.graph.component.Graph;
import edu.ncsu.csc.Galant.graph.component.GraphState;
import edu.ncsu.csc.Galant.graph.component.Node;
import edu.ncsu.csc.Galant.gui.prefs.PreferencesPanel;
import edu.ncsu.csc.Galant.gui.util.WindowUtil;
import edu.ncsu.csc.Galant.gui.window.panels.ComponentEditPanel;
import edu.ncsu.csc.Galant.gui.window.panels.GraphPanel;
import edu.ncsu.csc.Galant.logging.LogHelper;
import edu.ncsu.csc.Galant.prefs.Preference;
import edu.ncsu.csc.Galant.GalantException;
import edu.ncsu.csc.Galant.gui.util.EdgeEditDialog;
import edu.ncsu.csc.Galant.gui.util.DeleteNodeDialog;
import edu.ncsu.csc.Galant.gui.editor.GTabbedPane; // for confirmation dialog
import edu.ncsu.csc.Galant.gui.editor.GEditorFrame; // for confirmation dialog

/**
 * Window for displaying the <code>Graph</code>, containing all necessary
 * window components, and reacting to mouse events.
 *
 * @author Jason Cockrell, Ty Devries, Alex McCabe, Michael Owoc
 *
 * $Id: GraphWindow.java 109 2015-04-30 17:02:27Z mfms $
 */
public class GraphWindow extends JPanel implements PropertyChangeListener, ComponentListener {
	
	public static final int DEFAULT_WIDTH = 600, DEFAULT_HEIGHT = 750;
    public static final int TOOLBAR_HEIGHT = 24;
    public static final int ANIMATION_BUTTON_SIZE = 40;
	   
	/** Refers to the singleton GraphDispatch to push global information */
	private final GraphDispatch dispatch;
	
	/** The main frame for the Visual Graph Editor **/
	private static JFrame frame;
	
	private static JMenu fileMenu; 
	private static JMenuBar menuBar;
	private static JToolBar toolBar;
	
	/** The Graph panel used to draw the active graph **/
	private static GraphPanel gp;
	
	/** A panel used to edit Graph element's properties **/ 
	public static ComponentEditPanel componentEditPanel;
	
	/** A panel used to navigate through an animation **/
	private static JPanel animationButtons;
	private final JButton stepForward;
	public JButton getStepForward(){
		return stepForward;
	}
	private final JButton stepBack;
  public JButton getStepBack(){
		return stepBack;
	}
    private final JButton done;
	
	private ButtonGroup modeGroup = new ButtonGroup();
	private JToggleButton select;
	private JToggleButton addNode;
	private JToggleButton addEdge;
	private JToggleButton deleteBtn;
	
	private ButtonGroup directedGroup = new ButtonGroup();
	private JToggleButton directedBtn;
	private JToggleButton undirectedBtn;
	
	private JToggleButton nodeLabels;
	private JToggleButton edgeLabels;
	private JToggleButton nodeWeights;
	private JToggleButton edgeWeights;
	
	private JToggleButton repositionBtn;

	// MPM: I would recommend that all the instance variables be grouped together, and the status label-modifying methods
	// go at the bottom of this class.
	// MPM: The status label has been a handy GUI debug tool, but I think it's really useful for ordinary users too.
	// Think about what would be good to display in this field, and then discuss it with the rest of your team.
	private JLabel statusLabel;
	
	
	/* Update the status message to display the graph state
	 * @param s the integer state to display as part of a message
	 */
	public void updateStatusLabel(int s){
		String t = "Display state is " + s;
		statusLabel.setText(t);
		//System.out.printf("\nUpdating status label to a number %d; caller is %s\n",s, Thread.currentThread().getName());
	}
	/*
	 * Want to display a custom message?  We've got you covered.  Lets the status message be anything desired
	 * @param a Character array to display as the status message
	 */
	public void updateStatusLabel(char [] a){
		statusLabel.setText(new String(a));
		//System.out.printf("\nUpdating status label to chara array: %s; caller is %s\n", new String(a), Thread.currentThread().getName());
	}
	
	/*
	 * Same as above, but it takes a string object instead of a character array
	 * @param s String to display as status message
	 */
	public void updateStatusLabel(String s) {
		//System.out.printf("\n in the update status label function: %s; caller is %s \n",s, Thread.currentThread().getName());
		statusLabel.setText(s);
	}
	

	public String getStatusLabelAsAString(){
		return statusLabel.getText();
	}
	
	
	private GraphMode mode = null;
  
  private EdgeEditDialog edgeEditDialog;
  private DeleteNodeDialog deleteNodeDialog;  
	
	/**
	 * The Edit modes GraphWindow can assume. Used in the listener for the
	 * GraphPanel instance to update the Graph appropriately when edited visually
	 */
	public enum GraphMode {
		SELECT("select"),
		CREATE_NODE("newnode"),
		CREATE_EDGE("newedge"),
		DELETE("close");
		
		private ImageIcon icon;
		
		private GraphMode(String iconName)
			{
				java.net.URL imageURL = GraphWindow.class.getResource("images/" + iconName + "_24.png");
				icon = new ImageIcon(imageURL);
			}
		
		public ImageIcon getIcon()
			{
				return icon;
			}
	}
	
	/**
	 * Enumerates the types of displayed components the user can
	 * toggle on and off
	 */
	public enum GraphDisplays {
		NODE_LABELS("nodelabel"),
		EDGE_LABELS("edgelabel"),
		NODE_WEIGHTS("weightednode"),
		EDGE_WEIGHTS("weightededge");
		
		private ImageIcon icon;
		private boolean isShown;
		
		private GraphDisplays(String iconName)
			{
				java.net.URL imageURL = GraphWindow.class.getResource("images/" + iconName + "_24.png");
				icon = new ImageIcon(imageURL);
			}
		
		public ImageIcon getIcon()
			{
				return icon;
			}
		
		public boolean isShown()
			{
				return isShown;
			}
		public void setShown(boolean shown)
			{
				isShown = shown;
				Preference.PREFERENCES_NODE.putBoolean(name(), isShown);
			}
	}
	
	/**
	 * Enumerates the directedness properties of a Graph
	 */
	public enum Directedness {
		DIRECTED("directed"),
		UNDIRECTED("undirected");
		
		private ImageIcon icon;
		
		private Directedness(String iconName)
			{
				java.net.URL imageURL = GraphWindow.class.getResource("images/" + iconName + "_24.png");
				icon = new ImageIcon(imageURL);
			}
		
		public ImageIcon getIcon()
			{
				return icon;
			}
	}
	
	public static JFrame getGraphFrame()
		{
			return frame;
		}
	
	public static GraphPanel getGraphPanel()
	{
		return gp;
	}
	
	/**
	 * Create a new GraphWindow and all of its associated components
	 * @param _dispatch The GraphDispatch object used to share data between Galant windows
	 */
	public GraphWindow(GraphDispatch _dispatch) {
		LogHelper.enterConstructor(getClass());
		
		this.dispatch = _dispatch;
		// Register this object as a change listener. Allows GraphDispatch notifications to be pushed to this object
		_dispatch.getWorkingGraph().graphWindow = this;
		_dispatch.addChangeListener(this);
		
		// Create the panel that renders the active Graph
		gp = new GraphPanel(dispatch, this);

		// Add a listener to handle visual editing of the Graph
		gp.addMouseMotionListener( new MouseMotionListener() {
                @Override
                    public void mouseDragged(MouseEvent arg0) {
                    // If you start dragging, set dragging mode so you don't
                    // perform any other operations on the Node after
                    // releasing it
                    Node sel = gp.getSelectedNode();
                    if (sel != null) {
                        gp.setDragging(true);
                        gp.setEdgeTracker(null);
                        if ( !dispatch.isAnimationMode() || !dispatch.algorithmMovesNodes()) {
                            sel.setFixedPosition( arg0.getPoint() );
                        }
                    }
                    frame.repaint();
                }

                @Override
                    public void mouseMoved(MouseEvent arg0) {
                    // If you have the source selected in edge creation,
                    // follow the mouse with an edge
                    if ( ! dispatch.isAnimationMode() ) {
                        if (mode == GraphMode.CREATE_EDGE) {
                            gp.setEdgeTracker(arg0.getPoint());
                            frame.repaint();
                        }
                    }
                }
            }); // addMouseMotionListener
		
		// Add a listener to handle visual editing of the Graph
		gp.addMouseListener( new MouseAdapter() {
                Node prevNode;
			
                @Override
                    public void mousePressed(MouseEvent e) {
                        Point location = e.getPoint();
                        LogHelper.guiLogDebug( "CLICK, location = " + location );
					
                        prevNode = gp.getSelectedNode();
                        Node n = gp.selectTopClickedNode(location);
                }

                @Override
                    public void mouseReleased(MouseEvent arg0) {       
                        Point location = arg0.getPoint();
                        LogHelper.guiLogDebug("RELEASE");
                        LogHelper.guiLogDebug(mode.toString());
				
                        if ( gp.isDragging() ) {
                            LogHelper.guiLogDebug( "End of drag: location = " + location );
                            // Finished dragging a node, now reset the
                            // GraphPanel's tracking info
                            gp.setDragging(false);
                            gp.setSelectedNode(null);
                            gp.setPrevSelectedNode(null);
                            gp.setEdgeTracker(null);
					
                            // Unselected the node, so hide the edit panel
                            componentEditPanel.setWorkingComponent(null);
					
                            dispatch.pushToTextEditor(); 
							
                        } // only allow "dragging" in animation mode
                        else if ( ! dispatch.isAnimationMode() ){
                            // release after click
                            // not in animation mode
                            Node clickNode = gp.getSelectedNode();
                            Edge clickEdge = null;
                            if (clickNode == null) {
                                prevNode = null;
                                clickEdge = gp.selectTopClickedEdge(location);
                            }
				
                            // Perform the Edit action associated with the
                            // currently selected mode
                            if (clickNode != null) { 
                                componentEditPanel.setWorkingComponent(clickNode);
                            }
                            else {
                                componentEditPanel.setWorkingComponent(clickEdge);
                            }
						
                            if ( mode == GraphMode.CREATE_NODE
                                 && clickNode == null ) {
                                // Create a new node if you haven't clicked on
                                // any existing nodes
                                LogHelper.guiLogDebug("CREATE NODE");
							
                                // add a new default node to the working
                                // graph at this position
				
                                Graph g = dispatch.getWorkingGraph();
                                Node n = g.addInitialNode(location.x, location.y);
							
                                // select the new node
                                Node nNew = gp.selectTopClickedNode(location);
                                LogHelper.guiLogDebug( " select: node = " + n );

                                componentEditPanel.setWorkingComponent(nNew);
                                LogHelper.guiLogDebug( " setWorking: node = " + n );
                                
                                dispatch.pushToTextEditor(); 
							
                            } // create node
                            else if ( mode == GraphMode.CREATE_EDGE
                                      && clickNode != null && prevNode != null) {
                                // Create an edge if you've selected two nodes
                                LogHelper.guiLogDebug("CREATE EDGE");
							
                                // add a new edge between the clicked nodes
                                Graph g = dispatch.getWorkingGraph();
                                Edge e = g.addInitialEdge(prevNode, clickNode);
							
                                // select the new edge and clear the edge
                                // trackers
                                gp.setSelectedNode(null);
                                gp.setSelectedEdge(e);
                                gp.setEdgeTracker(null);
							
                                componentEditPanel.setWorkingComponent(e);
							
                                if (repositionBtn.isSelected())
                                   g.smartReposition();
                                 
                                dispatch.pushToTextEditor(); 
							
                            } // create edge
                            else if (mode == GraphMode.DELETE) {
                                // Delete a component if you clicked on one
                                // Priority goes to nodes
                                if (clickNode != null) {
                                    Graph g = dispatch.getWorkingGraph();
                                    g.removeNode(clickNode);
                                    gp.setSelectedNode(null);
								
                                    if (repositionBtn.isSelected())
                                        g.smartReposition();
								
                                } // node deletion
                                else if (clickEdge != null) {
                                    // If you didn't click on a node, look
                                    // for an edge
                                    Graph g = dispatch.getWorkingGraph();
                                    g.removeEdge(clickEdge);
                                    gp.setSelectedEdge(null);
								
                                    if (repositionBtn.isSelected())
                                        g.smartReposition();
								
                                } // edge deletion
							
                                componentEditPanel.setWorkingComponent(null);
							
                                dispatch.pushToTextEditor(); 
							
                            } // delete mode
                        } // not dragging
                    frame.repaint();
                }
            }
            ); // addMouseListener
		
		frame = new JFrame("Galant: Graph Editor");
		
		// The Main Galant class adds a listener that handles close operations to check if any dirty edit sessions exist
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		
		frame.setJMenuBar(initMenu());
		
		frame.add(this);
		
		frame.addComponentListener(this);
		
		// Create the buttons to navigate the graph state: forward, back, and stop
		stepForward = new JButton(new ImageIcon(GraphWindow.class.getResource("images/stepforward_24.png")));
		stepForward.setToolTipText("Step Forward\n[->]");
    stepBack = new JButton(new ImageIcon(GraphWindow.class.getResource("images/stepback_24.png")));
		stepBack.setToolTipText("Step Backward\n[<-]");
    done = new JButton(new ImageIcon(GraphWindow.class.getResource("images/close_24.png")));
		done.setToolTipText("Exit Animation\n[Esc]");
    
		componentEditPanel = new ComponentEditPanel();
		componentEditPanel.setVisible(false);

		statusLabel = new JLabel("No status");
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		this.add(initToolbar());
		this.add(statusLabel);
		this.add(gp);
		this.add(initAnimationPanel());
		this.add(componentEditPanel);

		frame.setName("graph_window");
		WindowUtil.preserveWindowBounds(frame, 0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT);
		frame.setVisible(true);
		
		dispatch.setWindowSize(gp.getHeight(), gp.getWidth());
		
		LogHelper.exitConstructor(getClass());
	} // constructor
	
	/**
	 * Change the Graph edit mode
	 * @param mode The new active Edit mode
	 */
	private void changeMode(GraphMode mode) {
		LogHelper.guiEnterMethod(getClass(), "changeMode");
		
		this.mode = mode;
		gp.setEdgeTracker(null);
				
		switch(mode) {
				case SELECT:{
					select.setSelected(true);
					break;
				}
				case CREATE_EDGE:{
					addEdge.setSelected(true);
					gp.setSelectedNode(null);
					break;
				}
				case CREATE_NODE:{
					addNode.setSelected(true);
					break;
				}
				case DELETE:{
					deleteBtn.setSelected(true);
				}
			}
		
		frame.repaint();
		
		LogHelper.guiExitMethod(getClass(), "changeMode");
	}
	
	/**
	 * Sets the graph to display as Directed or Undirected
	 * @param mode
	 */
	private void changeDirectedness(Directedness mode) {
		LogHelper.guiEnterMethod(getClass(), "changeDirectedness " + mode);
		
		switch(mode){
				case DIRECTED:{
					directedBtn.setSelected(true);
					dispatch.getWorkingGraph().setDirected(true);
					frame.repaint();
					break;
				}
				case UNDIRECTED:{
					undirectedBtn.setSelected(true);
					dispatch.getWorkingGraph().setDirected(false);
					frame.repaint();
					break;
				}
			}
		
		frame.repaint();
		
		LogHelper.guiExitMethod(getClass(), "changeDirectedNess");
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// Update display to animation mode or edit mode based on the flag
		if (evt.getPropertyName().equals(GraphDispatch.ANIMATION_MODE)) {
			if ( (Boolean) evt.getNewValue() ) {
				componentEditPanel.setWorkingComponent(null);
				gp.setDisplayState(GraphState.GRAPH_START_STATE);
				stepForward.setEnabled(true);
				stepBack.setEnabled(false);
				animationButtons.setVisible(true);
                animationButtons.setFocusable(true);
                animationButtons.requestFocusInWindow();

        directedBtn.setVisible(false);
        undirectedBtn.setVisible(false);
				select.setVisible(false);
        addNode.setVisible(false);
        addEdge.setVisible(false);
        deleteBtn.setVisible(false);
        repositionBtn.setVisible(false);
			} else {
				animationButtons.setVisible(false);
        directedBtn.setVisible(true);
        undirectedBtn.setVisible(true);
				select.setVisible(true);
        addNode.setVisible(true);
        addEdge.setVisible(true);
        deleteBtn.setVisible(true);
        repositionBtn.setVisible(true);
			}
			
		// Update the graph directedness flag to the new working graph's flag
		} 
		else if (evt.getPropertyName().equals(GraphDispatch.GRAPH_UPDATE)) {
			if (dispatch.getWorkingGraph().isDirected()) {
				changeDirectedness(Directedness.DIRECTED);
			}
			else {
				changeDirectedness(Directedness.UNDIRECTED);
			}
		}
		
		frame.repaint();
		
	}
	
	/**
	 * Creates menu bar components
	 * @return menuBar the menu bar
	 */
	private static JMenuBar initMenu() {
		LogHelper.guiEnterMethod(GraphWindow.class, "initMenu");
		
		menuBar = new JMenuBar();
		
		fileMenu = new JMenu("File");
		fileMenu.add(PreferencesPanel.SHOW_PREFS_DIALOG);
		fileMenu.add(WindowUtil.EXPORT_ACTION);
		fileMenu.addSeparator();
		fileMenu.add(WindowUtil.QUIT_ACTION);
		
		menuBar.add(fileMenu);


		LogHelper.guiExitMethod(GraphWindow.class, "initMenu");
		return menuBar;
	}
	
	/**
	 * Creates toolbar components
	 * @return
	 */
	private JToolBar initToolbar() {
		LogHelper.guiEnterMethod(getClass(), "initToolbar");
		
		toolBar = new JToolBar(SwingConstants.HORIZONTAL);
		toolBar.setFloatable(false);
		toolBar.setMaximumSize(new Dimension(DEFAULT_WIDTH, TOOLBAR_HEIGHT));
		toolBar.setMinimumSize(new Dimension(DEFAULT_WIDTH, TOOLBAR_HEIGHT));
		toolBar.setAlignmentY(0);
		
		select = createButton(GraphMode.SELECT);
    select.setFocusable(false);
    select.setToolTipText("Select");
		addNode = createButton(GraphMode.CREATE_NODE);
    addNode.setFocusable(false);
		addNode.setToolTipText("Create Node");
    addEdge = createButton(GraphMode.CREATE_EDGE);
    addEdge.setFocusable(false);
		addEdge.setToolTipText("Create Edge");
    deleteBtn = createButton(GraphMode.DELETE);
    deleteBtn.setFocusable(false);
		deleteBtn.setToolTipText("Delete");
    changeMode(GraphMode.SELECT);		
    
		toolBar.addSeparator();
		
		undirectedBtn = createButton(Directedness.UNDIRECTED);
		undirectedBtn.setToolTipText("Undirected");
    undirectedBtn.setFocusable(false);
    directedBtn = createButton(Directedness.DIRECTED);
		directedBtn.setToolTipText("Directed");
    directedBtn.setFocusable(false);
    changeDirectedness(Directedness.UNDIRECTED);
		
		toolBar.addSeparator();
		
		nodeLabels = createButton(GraphDisplays.NODE_LABELS);
    nodeLabels.setFocusable(false);
		nodeLabels.setToolTipText("Display Node Labels");
    edgeLabels = createButton(GraphDisplays.EDGE_LABELS);
    edgeLabels.setFocusable(false);
		edgeLabels.setToolTipText("Display Edge Labels");
    nodeWeights = createButton(GraphDisplays.NODE_WEIGHTS);
    nodeWeights.setFocusable(false);
		nodeWeights.setToolTipText("Display Node Weights");
    edgeWeights = createButton(GraphDisplays.EDGE_WEIGHTS);
    edgeWeights.setFocusable(false);
		edgeWeights.setToolTipText("Display Edge Weights");
		toolBar.addSeparator();
		
		java.net.URL imageURL = GraphWindow.class.getResource("images/autoposition_24.png");
		repositionBtn = new JToggleButton(new ImageIcon(imageURL));
    repositionBtn.setFocusable(false);
		repositionBtn.setToolTipText("Intelligent Rearrange");
    repositionBtn.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				if (repositionBtn.isSelected()) {
					dispatch.getWorkingGraph().smartReposition();
					dispatch.pushToTextEditor(); 
				}
			}
		});
		toolBar.add(repositionBtn);
		

		LogHelper.guiExitMethod(this.getClass(), "initToolbar");
		return toolBar;
	}
	
	/**
	 * Creates a GraphMode button. The icon URL associated with each is retrieved from the enum
	 * @param mode The Edit mode to create a button for
	 * @return the appropriate JToggleButton
	 */
	private JToggleButton createButton(final GraphMode mode){
			JToggleButton button = new JToggleButton();
			button.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e){
						changeMode(mode);
					}
			});
			button.setIcon(mode.getIcon());
			modeGroup.add(button);
			toolBar.add(button);
			return button;
		}
	
	/**
	 * Creates a Directedness toggle button. The icon URL associated with each is retrieved from the enum
	 * @param mode Directed of Undirected
	 * @return the appropriate JToggleButton
	 */
	private JToggleButton createButton(final Directedness mode)
	{
		JToggleButton button = new JToggleButton();
		button.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
					changeDirectedness(mode);
					dispatch.pushToTextEditor(); 
				}
		});
		button.setIcon(mode.getIcon());
		directedGroup.add(button);
		toolBar.add(button);
		return button;
	}
	
	/**
	 * Creates a toggle button for displaying and hiding various properties
	 * @param displayType The type of property to hide
	 * @return the appropriate JToggleButton
	 */
	private static JToggleButton createButton(final GraphDisplays displayType) {
		final JToggleButton button = new JToggleButton();
		button.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
					displayType.setShown(button.isSelected());
					frame.repaint(); 
				}
		});
		button.setIcon(displayType.getIcon());
		toolBar.add(button);
		displayType.setShown(Preference.PREFERENCES_NODE.getBoolean(displayType.name(), true));
		button.setSelected(displayType.isShown());
		return button;
	}

	/**
	 * Initialize the animation panel controls
	 * Steps through an Algorithm or exits the mode
	 * @return
	 */
	private JPanel initAnimationPanel() {
		LogHelper.guiEnterMethod(getClass(), "initAnimationPanel");
		animationButtons = new JPanel();
		
		stepBack.setEnabled(false);
		stepBack.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				gp.decrementDisplayState();
				
				stepForward.setEnabled(gp.hasNextState());
				stepBack.setEnabled(gp.hasPreviousState());
				
				frame.repaint();
			}
		});
    		
		// Move the display state in GraphPanel forward one
		stepForward.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				updateStatusLabel("Algorithm execution in progress");
				gp.incrementDisplayState();
				
				stepForward.setEnabled(gp.hasNextState());
				stepBack.setEnabled(gp.hasPreviousState());
				
				frame.repaint();
			}
		});
		
		// Exit the animation and change back to Edit mode
		done.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispatch.setAnimationMode(false);
			}
		});
    
		animationButtons.add(stepBack);
		animationButtons.add(stepForward);
		animationButtons.add(done);
		animationButtons.setMaximumSize(new Dimension(DEFAULT_WIDTH, ANIMATION_BUTTON_SIZE));
		animationButtons.setMinimumSize(new Dimension(DEFAULT_WIDTH, ANIMATION_BUTTON_SIZE));
		
		// make animation panel initially invisible
		frame.addWindowListener(new WindowAdapter(){
			@Override
			public void windowOpened(WindowEvent e)
				{
					animationButtons.setVisible(false);
				}
		});

  // add keyboard shortcuts    
  KeyboardFocusManager keyManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
  keyManager.addKeyEventDispatcher(new KeyEventDispatcher() {
    /** 
     * Intent is to delay for about 1/60 second = roughly 17 milliseconds
     * to allow the display to catch up to repeated keystrokes when an
     * arrow key is held down. 
     */
    static final long DELAY_TIME = 17;
    boolean ctrlPressed = false;
    boolean deletePressed = false;
    boolean shiftPressed = false;
    @Override
    /**
     * This method is called by the current KeyboardFocusManager requesting
     * that this KeyEventDispatcher dispatch the specified event on its
     * behalf.  This KeyEventDispatcher is free to retarget the event,
     * consume it, dispatch it itself, or make other changes. This capability
     * is typically used to deliver KeyEvents to Components other than the
     * focus owner. This can be useful when navigating children of
     * non-focusable Windows in an accessible environment, for example. Note
     * that if a KeyEventDispatcher dispatches the KeyEvent itself, it must
     * use redispatchEvent to prevent the current KeyboardFocusManager from
     * recursively requesting that this KeyEventDispatcher dispatch the event
     * again.
     * 
     * If an implementation of this method returns false, then the KeyEvent
     * is passed to the next KeyEventDispatcher in the chain, ending with the
     * current KeyboardFocusManager. If an implementation returns true, the
     * KeyEvent is assumed to have been dispatched (although this need not be
     * the case), and the current KeyboardFocusManager will take no further
     * action with regard to the KeyEvent.  In such a case,
     * KeyboardFocusManager.dispatchEvent should return true as well. If an
     * implementation consumes the KeyEvent, but returns false, the consumed
     * event will still be passed to the next KeyEventDispatcher in the
     * chain. It is important for developers to check whether the KeyEvent
     * has been consumed before dispatching it to a target. By default, the
     * current KeyboardFocusManager will not dispatch a consumed KeyEvent.
     *
     * @param e the KeyEvent to dispatch
     * @return true if the KeyboardFocusManager should take no further action with regard to the KeyEvent; false otherwise
     */
    public boolean dispatchKeyEvent(KeyEvent e) {
      //"left" step backward when in animation mode
      if(dispatch.isAnimationMode() && e.getID()==KeyEvent.KEY_PRESSED && e.getKeyCode()==KeyEvent.VK_LEFT){
        synchronized (this) { //Handle delay
            gp.decrementDisplayState();
            stepForward.setEnabled(gp.hasNextState());
				    stepBack.setEnabled(gp.hasPreviousState());
            try {
              Thread.sleep(DELAY_TIME);
            } catch (InterruptedException f) {
              //Handle exception
            }
            frame.repaint();
        }
        return true;
      }
      //"right" step forward when in animation mode
      if(dispatch.isAnimationMode() && e.getID()==KeyEvent.KEY_PRESSED && e.getKeyCode()==KeyEvent.VK_RIGHT){
        synchronized (this) { //Handle delay
            gp.incrementDisplayState();
            stepForward.setEnabled(gp.hasNextState());
				    stepBack.setEnabled(gp.hasPreviousState());
            try {
              Thread.sleep(DELAY_TIME);
            } catch (InterruptedException f) {
              //Handle exception
            }
            frame.repaint();
        }
        return true;
      }
      // "Esc" leave animation mode when in animation mode
      if(dispatch.isAnimationMode() && e.getID()==KeyEvent.KEY_PRESSED
                                    && e.getKeyCode()==KeyEvent.VK_ESCAPE){
        dispatch.setAnimationMode(false);
        return true;
      }
      // "Ctrl" pressed
      if(e.getID()==KeyEvent.KEY_PRESSED && e.getKeyCode()==KeyEvent.VK_CONTROL){
          ctrlPressed = true;
        return true;
      }
      // "Ctrl" released
      if(e.getID()==KeyEvent.KEY_RELEASED && e.getKeyCode()==KeyEvent.VK_CONTROL){
        ctrlPressed = false;
        return true;
      }
      // "Shift" pressed
      if(e.getID()==KeyEvent.KEY_PRESSED && e.getKeyCode()==KeyEvent.VK_SHIFT){
          shiftPressed = true;
        return true;
      }
      // "Shift" released
      if(e.getID()==KeyEvent.KEY_RELEASED && e.getKeyCode()==KeyEvent.VK_SHIFT){
        shiftPressed = false;
        return true;
      }
      // "Delete" pressed
      if(!dispatch.isAnimationMode() && e.getID()==KeyEvent.KEY_PRESSED
                                     && e.getKeyCode()==KeyEvent.VK_DELETE){
        deletePressed = true;
        return true;
      }
      // "Delete" released
      if(e.getID()==KeyEvent.KEY_RELEASED && e.getKeyCode()==KeyEvent.VK_DELETE){
        deletePressed = false;
        return true;
      }
      // "E" pressed
      if(!dispatch.isAnimationMode() && e.getID()==KeyEvent.KEY_PRESSED
                                     && e.getKeyCode()==KeyEvent.VK_E){
        synchronized(this){
          // "ctrl" already pressed, create new edge
          if(ctrlPressed){
            LogHelper.guiLogDebug("CREATE EDGE");
						//prompt user for the id of two nodes	
            edgeEditDialog = new EdgeEditDialog(frame, dispatch, GraphMode.CREATE_EDGE);
            edgeEditDialog.pack();
            edgeEditDialog.setLocationRelativeTo(frame);
            edgeEditDialog.setVisible(true);
            dispatch.pushToTextEditor();
          } //Create new edge
          //delete edge
          if(deletePressed){
            LogHelper.guiLogDebug("DELETE EDGE");
						//prompt user for the id of two nodes	
            edgeEditDialog = new EdgeEditDialog(frame, dispatch, GraphMode.DELETE);
            edgeEditDialog.pack();
            edgeEditDialog.setLocationRelativeTo(frame);
            edgeEditDialog.setVisible(true);
            dispatch.pushToTextEditor();
          }//delete edge
        }
        return true;
      }
      // "N" pressed
      if(!dispatch.isAnimationMode() && e.getID()==KeyEvent.KEY_PRESSED
                                     && e.getKeyCode()==KeyEvent.VK_N){
        synchronized(this){
          // "ctrl" already pressed, create new node
          if(ctrlPressed){
            LogHelper.guiLogDebug("CREATE NODE");
							
            // add a new default node to the working
            Graph g = dispatch.getWorkingGraph();
            // choose a random position to place new node
            Point p = Node.genRandomPosition();
            Node n = g.addInitialNode(p.x, p.y);
							
            // select the new node
            Node nNew = gp.selectTopClickedNode(p);
            componentEditPanel.setWorkingComponent(nNew);
            LogHelper.guiLogDebug( " select: node = " + n );

            componentEditPanel.setWorkingComponent(nNew);
            LogHelper.guiLogDebug( " setWorking: node = " + n );
            
            dispatch.pushToTextEditor();
          } //Create new node
          //delete already pressed, delete node
          if (deletePressed) {
            LogHelper.guiLogDebug("DELETE NODE");	
            //hide edit panel
            componentEditPanel.setWorkingComponent(null);
            deleteNodeDialog = new DeleteNodeDialog(frame, dispatch);
            deleteNodeDialog.pack();
            deleteNodeDialog.setLocationRelativeTo(frame);
            deleteNodeDialog.setVisible(true);
            dispatch.pushToTextEditor();
          }//delete node
        }
        return true;
      }
      //"ctrl + i" intelligent rearrange
      if(!dispatch.isAnimationMode() && e.getID()==KeyEvent.KEY_PRESSED
        && e.getKeyCode()==KeyEvent.VK_I && ctrlPressed ){
        synchronized(this){
          if (!repositionBtn.isSelected()) {
            repositionBtn.setSelected(true);
            dispatch.getWorkingGraph().smartReposition();
					  dispatch.pushToTextEditor();
          } else {
            repositionBtn.setSelected(false);
            //todo - undo smartReposition
          }
        }
        return true;
      }
      // "ctrl+d" switch between directed and undirected
      if(!dispatch.isAnimationMode() && e.getID()==KeyEvent.KEY_PRESSED
        && e.getKeyCode()==KeyEvent.VK_D && ctrlPressed){
        synchronized(this){
          if (!dispatch.getWorkingGraph().isDirected()) {
				    changeDirectedness(Directedness.DIRECTED);
			    } else {
				    changeDirectedness(Directedness.UNDIRECTED);
			    }
        }
        return true;
      }
      // "ctrl+l" display node labels "ctrl+L" display edge labels
      if(e.getID()==KeyEvent.KEY_PRESSED && e.getKeyCode()==KeyEvent.VK_L && ctrlPressed){
        synchronized(this){
          if (shiftPressed) {
            GraphDisplays.EDGE_LABELS.setShown(!edgeLabels.isSelected());
            edgeLabels.setSelected(!edgeLabels.isSelected());
          } else {
            GraphDisplays.NODE_LABELS.setShown(!nodeLabels.isSelected());
            nodeLabels.setSelected(!nodeLabels.isSelected());
          }
          frame.repaint(); 
        }
        return true;
      }
      // "ctrl+w" display node weights "ctrl+W" display edge weights
      if(e.getID()==KeyEvent.KEY_PRESSED && e.getKeyCode()==KeyEvent.VK_W && ctrlPressed){
        synchronized(this){
          if (shiftPressed) {
            GraphDisplays.EDGE_WEIGHTS.setShown(!edgeWeights.isSelected());
            edgeWeights.setSelected(!edgeWeights.isSelected());
          } else {
            GraphDisplays.NODE_WEIGHTS.setShown(!nodeWeights.isSelected());
            nodeWeights.setSelected(!nodeWeights.isSelected());
          }
          frame.repaint(); 
        }
        return true;
      }
      return false;
    }
  });

		LogHelper.guiExitMethod(getClass(), "initAnimationPanel");
		return animationButtons;
	}

	@Override
	public void componentHidden(ComponentEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentMoved(ComponentEvent arg0) {
		// TODO Auto-generated method stub
		
	}

    @Override
	public void componentResized(ComponentEvent e){
        dispatch.setWindowSize(gp.getHeight(), gp.getWidth());
        frame.repaint();
    }


	@Override
	public void componentShown(ComponentEvent arg0) {
		// TODO Auto-generated method stub
	}
}

//  [Last modified: 2015 09 11 at 19:39:17 GMT]
