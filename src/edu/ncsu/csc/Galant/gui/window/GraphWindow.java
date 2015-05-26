package edu.ncsu.csc.Galant.gui.window;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import edu.ncsu.csc.Galant.GraphDispatch;
import edu.ncsu.csc.Galant.graph.component.Edge;
import edu.ncsu.csc.Galant.graph.component.Graph;
import edu.ncsu.csc.Galant.graph.component.GraphState;
import edu.ncsu.csc.Galant.graph.component.Node;
import edu.ncsu.csc.Galant.graph.component.NodeState;
import edu.ncsu.csc.Galant.gui.prefs.PreferencesPanel;
import edu.ncsu.csc.Galant.gui.util.WindowUtil;
import edu.ncsu.csc.Galant.gui.window.panels.ComponentEditPanel;
import edu.ncsu.csc.Galant.gui.window.panels.GraphPanel;
import edu.ncsu.csc.Galant.logging.LogHelper;
import edu.ncsu.csc.Galant.prefs.Preference;
import edu.ncsu.csc.Galant.GalantException;

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
	private static ComponentEditPanel componentEditPanel;
	
	/** A panel used to navigate through an animation **/
	private static JPanel animationButtons;
	private final JButton stepForward;
	private final JButton stepBack;
	
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
	
	private GraphMode mode = null;
	
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
		_dispatch.addChangeListener(this);
		
		// Create the panel that renders the active Graph
		gp = new GraphPanel(dispatch);

        // add keyboard shortcuts (left and right arrows, enter)
        gp.addKeyListener(new AnimationKeyListener());
        gp.setFocusable(true);
        gp.requestFocusInWindow();

		// Add a listener to handle visual editing of the Graph
		gp.addMouseMotionListener( new MouseMotionListener() {
                @Override
                    public void mouseDragged(MouseEvent arg0) {
                    // If you start dragging, set dragging mode so you don't
                    // perform any other operations on the Node after
                    // releasing it
                    //@todo sometimes graph panel can't recognize some nodes
                    //if ( ! dispatch.isAnimationMode() ) {
                        Node sel = gp.getSelectedNode();
                        if (sel != null) {
                            gp.setDragging(true);
                            gp.setEdgeTracker(null);
                            if ( ! dispatch.isAnimationMode() ) {
                                sel.setFixedPosition( arg0.getPoint() );
                            } else {
                                NodeState currentState = null;
                                try {
                                    currentState = sel.getLatestValidState(gp.getDisplayState());
                                } catch ( GalantException e ) {
                                    //@todo need to figure out how to handle this exception 
                                }
                                currentState.setPosition( arg0.getPoint() );
                            }
                        }
                    //}
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
                    //if ( ! dispatch.isAnimationMode() ) {
                        Point location = e.getPoint();
                        LogHelper.logDebug( "CLICK, location = " + location );
					
                        prevNode = gp.getSelectedNode();
                        Node n = gp.selectTopClickedNode(location);
                    //}
                }

                @Override
                    public void mouseReleased(MouseEvent arg0) {
                    //if ( ! dispatch.isAnimationMode() ) {
                        // not in animation mode
                        Point location = arg0.getPoint();
                        LogHelper.logDebug("RELEASE");
                        LogHelper.logDebug(mode.toString());
				
                        if ( gp.isDragging() ) {
                            LogHelper.logDebug( "End of drag: location = " + location );
                            // Finished dragging a node, now reset the
                            // GraphPanel's tracking info
                            gp.setDragging(false);
                            gp.setSelectedNode(null);
                            gp.setPrevSelectedNode(null);
                            gp.setEdgeTracker(null);
					
                            // Unselected the node, so hide the edit panel
                            componentEditPanel.setWorkingComponent(null);
					
                            dispatch.pushToTextEditor(); 
							
                        } // dragging
                        else {
                            // release after click
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
                                LogHelper.logDebug("CREATE NODE");
							
                                // add a new default node to the working
                                // graph at this position
				
                                Graph g = dispatch.getWorkingGraph();
                                Node n = g.addInitialNode();
                                LogHelper.logDebug( " addInitial: node = " + n );
                                n.setFixedPosition(location);
                                LogHelper.logDebug( " setFixedPosition: node = " + n );
							
                                // select the new node
                                Node nNew = gp.selectTopClickedNode(location);
                                LogHelper.logDebug( " select: node = " + n );

                                componentEditPanel.setWorkingComponent(nNew);
                                LogHelper.logDebug( " setWorking: node = " + n );
                                
                                dispatch.pushToTextEditor(); 
							
                            } // create node
                            else if ( mode == GraphMode.CREATE_EDGE
                                      && clickNode != null && prevNode != null) {
                                // Create an edge if you've selected two nodes
                                LogHelper.logDebug("CREATE EDGE");
							
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
                        
                    //} // not in animation mode
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
		
		stepForward = new JButton(new ImageIcon(GraphWindow.class.getResource("images/stepforward_24.png")));
		stepBack = new JButton(new ImageIcon(GraphWindow.class.getResource("images/stepback_24.png")));
		
		componentEditPanel = new ComponentEditPanel();
		componentEditPanel.setVisible(false);
		
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		this.add(initToolbar());
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
		LogHelper.enterMethod(getClass(), "changeMode");
		
		this.mode = mode;
		gp.setEdgeTracker(null);
				
		switch(mode)
			{
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
		
		LogHelper.exitMethod(getClass(), "changeMode");
	}
	
	/**
	 * Sets the graph to display as Directed or Undirected
	 * @param mode
	 */
	private void changeDirectedness(Directedness mode) {
		LogHelper.enterMethod(getClass(), "changeDirectedness " + mode);
		
		switch(mode)
			{
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
		
		LogHelper.exitMethod(getClass(), "changeDirectedNess");
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
				toolBar.setVisible(false);
			} else {
				animationButtons.setVisible(false);
				toolBar.setVisible(true);
			}
			
		// Update the graph directedness flag to the new working graph's flag
		} else if (evt.getPropertyName().equals(GraphDispatch.GRAPH_UPDATE)) {
			if (dispatch.getWorkingGraph().isDirected()) {
				changeDirectedness(Directedness.DIRECTED);
			} else {
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
		LogHelper.enterMethod(GraphWindow.class, "initMenu");
		
		menuBar = new JMenuBar();
		
		fileMenu = new JMenu("File");
		fileMenu.add(PreferencesPanel.SHOW_PREFS_DIALOG);
		fileMenu.add(WindowUtil.EXPORT_ACTION);
		fileMenu.addSeparator();
		fileMenu.add(WindowUtil.QUIT_ACTION);
		
		menuBar.add(fileMenu);


		LogHelper.exitMethod(GraphWindow.class, "initMenu");
		return menuBar;
	}
	
	/**
	 * Creates toolbar components
	 * @return
	 */
	private JToolBar initToolbar() {
		LogHelper.enterMethod(getClass(), "initToolbar");
		
		toolBar = new JToolBar(SwingConstants.HORIZONTAL);
		toolBar.setFloatable(false);
		toolBar.setMaximumSize(new Dimension(DEFAULT_WIDTH, TOOLBAR_HEIGHT));
		toolBar.setMinimumSize(new Dimension(DEFAULT_WIDTH, TOOLBAR_HEIGHT));
		toolBar.setAlignmentY(0);
		
		select = createButton(GraphMode.SELECT);
		addNode = createButton(GraphMode.CREATE_NODE);
		addEdge = createButton(GraphMode.CREATE_EDGE);
		deleteBtn = createButton(GraphMode.DELETE);
		changeMode(GraphMode.SELECT);
		
		toolBar.addSeparator();
		
		undirectedBtn = createButton(Directedness.UNDIRECTED);
		directedBtn = createButton(Directedness.DIRECTED);
		changeDirectedness(Directedness.UNDIRECTED);
		
		toolBar.addSeparator();
		
		nodeLabels = createButton(GraphDisplays.NODE_LABELS);
		edgeLabels = createButton(GraphDisplays.EDGE_LABELS);
		nodeWeights = createButton(GraphDisplays.NODE_WEIGHTS);
		edgeWeights = createButton(GraphDisplays.EDGE_WEIGHTS);
		
		toolBar.addSeparator();
		
		java.net.URL imageURL = GraphWindow.class.getResource("images/autoposition_24.png");
		repositionBtn = new JToggleButton(new ImageIcon(imageURL));
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
		

		LogHelper.exitMethod(this.getClass(), "initToolbar");
		return toolBar;
	}
	
	/**
	 * Creates a GraphMode button. The icon URL associated with each is retrieved from the enum
	 * @param mode The Edit mode to create a button for
	 * @return the appropriate JToggleButton
	 */
	private JToggleButton createButton(final GraphMode mode)
		{
			JToggleButton button = new JToggleButton();
			button.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e)
					{
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
			public void actionPerformed(ActionEvent e)
				{
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
			public void actionPerformed(ActionEvent e)
				{
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
	
    static class AnimationKeyListener extends KeyAdapter {
        static final long DELAY_TIME = 17;

        /**
         * Intent is to delay for about 1/60 second = roughly 17 milliseconds
         * to allow the display to catch up to repeated keystrokes when an
         * arrow key is held down. Ultimately this should be handled by an
         * invocation of the Timer class.
         */
        void delay() {
            long startTime = System.currentTimeMillis();
            long currentTime = System.currentTimeMillis();
            long endOfDelay = startTime + DELAY_TIME;
            while ( currentTime < endOfDelay ) {
                currentTime = System.currentTimeMillis();
            }
        }

        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_LEFT) {
				gp.decrementDisplayState();
            }
            if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
				gp.incrementDisplayState();
            }
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                // delete row method (when "delete" is pressed)
            }
            delay();
            frame.repaint();
        }
    }

	/**
	 * Initialize the animation panel controls
	 * Steps through an Algorithm or exits the mode
	 * @return
	 */
	private JPanel initAnimationPanel() {
		LogHelper.enterMethod(getClass(), "initAnimationPanel");
		animationButtons = new JPanel();
		
		// Move the display state in GraphPanel back one 
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
				gp.incrementDisplayState();
				
				stepForward.setEnabled(gp.hasNextState());
				stepBack.setEnabled(gp.hasPreviousState());
				
				frame.repaint();
			}
		});
		
		// Exit the animation and change back to Edit mode
		JButton done = new JButton(new ImageIcon(GraphWindow.class.getResource("images/close_24.png")));
		done.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispatch.setAnimationMode(false);
			}
		});
		//animationButtons.add(new JButton(new ImageIcon(GraphWindow.class.getResource("images/stepbeginning_24.png"))));
		animationButtons.add(stepBack);
		animationButtons.add(stepForward);
		//animationButtons.add(new JButton(new ImageIcon(GraphWindow.class.getResource("images/stepEnd_24.png"))));
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

        // add keyboard shortcuts (left and right arrows, enter)
        animationButtons.addKeyListener(new AnimationKeyListener());
        animationButtons.setFocusable(true);
        animationButtons.requestFocusInWindow();

		LogHelper.exitMethod(getClass(), "initAnimationPanel");
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

//  [Last modified: 2015 04 30 at 00:24:27 GMT]
