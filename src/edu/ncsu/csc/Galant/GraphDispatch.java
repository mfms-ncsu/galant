package edu.ncsu.csc.Galant;

import java.awt.Point;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.swing.JDialog;

import edu.ncsu.csc.Galant.algorithm.Algorithm;
import edu.ncsu.csc.Galant.algorithm.AlgorithmExecutor;
import edu.ncsu.csc.Galant.algorithm.AlgorithmSynchronizer;
import edu.ncsu.csc.Galant.algorithm.Terminate;
import edu.ncsu.csc.Galant.graph.component.Graph;
import edu.ncsu.csc.Galant.graph.component.Node;
import edu.ncsu.csc.Galant.gui.window.GraphWindow;
import edu.ncsu.csc.Galant.logging.LogHelper;

/**
 * Dispatch for managing working graphs in the editor; also used for passing
 * information about window width and height to other classes as appropriate;
 * and for passing information about current mode (animation vs. editing)
 *
 * @author Jason Cockrell, Ty Devries, Alex McCabe, Michael Owoc; edited by
 *         Matthias Stallmann, Weijia Li, and Yuang Ni.
 *
 * @todo I'm puzzled by the need for getIntance() when there is only one
 *       instance at any given time. Seems like all information should be static
 *       and initialized appropriately when Galant execution begins. A more
 *       appropriate name for this class, given its current usage, would be
 *       Globals. The original intent may have been to have multiple instances
 *       for multiple communication channels between graphs, algorithms and
 *       editors.
 *
 *       Or, in the world of design patterns, this is a "singleton"; but could
 *       have been handled like LogHelper. In any case, clients deal with it
 *       differently; often the instance of GraphDispatch is an instance
 *       variable of the client class or a local variable in a longer method.
 */
public class GraphDispatch {

    private static GraphDispatch  instance;
    private Graph                 workingGraph;

    private Graph                 editGraph;
    /**
     * A unique identifier for a graph.
     *
     * @todo not clear to me what the purpose of UUID is; seems to be a way to
     *       uniquely identify a graph and tie together the working graph in the
     *       dispatch with its source text in GGraphEditorPanel and GTabbedPane.
     */
    private UUID                  graphSource;

    private int                   windowWidth;
    private int                   windowHeight;

    /** true if animating an algorithm instead of editing */
    private boolean               animationMode  = false;

    /**
     * true if editing a graph; false during parsing
     */
    private boolean               editMode       = false;

    /**
     * reference to controller of algorithm execution from the point of view of
     * the display, i.e., the object whose methods are called when user
     * starts/stops algorithm and steps forward or backward.
     */
    private AlgorithmExecutor     algorithmExecutor;

    /**
     * reference to the object whose methods are called when the algorithm
     * changes the state of the graph in response to user actions
     */
    private AlgorithmSynchronizer algorithmSynchronizer;

    /**
     * The current graph window, whether in edit or animation mode
     */
    private GraphWindow           graphWindow;

    /**
     * true during edit mode if several elements need to be changed
     * simultaneously; for example, when a node is deleted all incident edges
     * need to be deleted without a state change.
     */
    private boolean               atomic         = false;

    /**
     * Reference to an active query window during algorithm execution (so that
     * it can be properly closed and does not cause Galant to hang)
     */
    private JDialog               activeQuery;

    /**
     * text of response to latest interactive query with text input
     */
    private String                stringAnswer;

    /**
     * integer value of answer to latest interactive query for an integer
     */
    private Integer               integerAnswer;

    /**
     * double value of answer to latest interactive query for a double
     */
    private Double                doubleAnswer;

    /**
     * minimum distance from the edge of a window when fitting a graph to the
     * window
     */
    final static int              WINDOW_PADDING = 50;

    /**
     * Offset to account for the fact that (0,0) is not a visible part of the
     * window.
     */
    final static int              WINDOW_OFFSET  = 20;

    /**
     * getters and setters for the query answers
     */
    public void setActiveQuery ( final JDialog dialog ) {
        this.activeQuery = dialog;
    }

    public JDialog getActiveQuery () {
        return this.activeQuery;
    }

    public void setStringAnswer ( final String answer ) {
        this.stringAnswer = answer;
    }

    public String getStringAnswer () {
        return this.stringAnswer;
    }

    public void setIntegerAnswer ( final Integer answer ) {
        this.integerAnswer = answer;
    }

    public Integer getIntegerAnswer () {
        return this.integerAnswer;
    }

    public void setDoubleAnswer ( final Double answer ) {
        this.doubleAnswer = answer;
    }

    public Double getDoubleAnswer () {
        return this.doubleAnswer;
    }

    /**
     * true if the algorithm moves nodes; an algorithm should set this if it
     * does not want the user to move nodes during execution.
     */
    private boolean                            algorithmMovesNodes = false;

    public static final String                 ANIMATION_MODE      = "animationMode";
    public static final String                 GRAPH_UPDATE        = "graphUpdate";
    public static final String                 TEXT_UPDATE         = "textUpdate";

    public static final String                 ADD_NODE            = "addNode";
    public static final String                 ADD_EDGE            = "addEdge";
    public static final String                 DELETE_COMPONENT    = "deleteComponent";

    private final List<PropertyChangeListener> listener            = new ArrayList<PropertyChangeListener>();

    private GraphDispatch () {
        LogHelper.enterConstructor( getClass() );
        LogHelper.exitConstructor( getClass() );
    }

    /**
     * @return the (unique) instance of a GraphDispatch; this method allows
     *         various parts of the code to communicate with each other
     *         indirectly; for example, an instance of the Graph class does not
     *         have to be associated with a GraphDispatch instance upon creation
     *         in order for it to interact with the display, animation, etc.
     */
    public static GraphDispatch getInstance () {
        if ( instance == null ) {
            instance = new GraphDispatch();
        }
        return instance;
    }

    public Graph getWorkingGraph () {
        if ( workingGraph == null ) {
            workingGraph = new Graph();
            workingGraph.graphWindow = graphWindow;
        }
        return workingGraph;
    }

    public void setWorkingGraph ( final Graph g, final UUID u ) {
        this.workingGraph = g;
        this.graphSource = u;
        notifyListeners( GRAPH_UPDATE, null, null );
    }

    public UUID getGraphSource () {
        return graphSource;
    }

    public void setGraphSource ( final UUID graphSource ) {
        this.graphSource = graphSource;
    }

    public boolean isAnimationMode () {
        return this.animationMode;
    }

    public boolean isEditMode () {
        return this.editMode;
    }

    public boolean isAtomic () {
        return this.atomic;
    }

    public void setAtomic ( final boolean atomic ) {
        this.atomic = atomic;
    }

    /**
     * @param mode
     *            true when user is editing the working graph, false otherwise
     */
    public void setEditMode ( final boolean mode ) {
        LogHelper.enterMethod( getClass(), "setEditMode " + mode );
        this.editMode = mode;
        LogHelper.exitMethod( getClass(), "setEditMode" );
    }

    /**
     * Does everything that's required to initiate execution of the algorithm
     */
    public void startAnimation ( final Algorithm algorithm ) {
        this.animationMode = true;
        this.editMode = false;
        // save the current working graph so that changes made by algorithm
        // can be undone easily
        this.editGraph = this.workingGraph;
        this.algorithmSynchronizer = new AlgorithmSynchronizer();
        this.algorithmExecutor = new AlgorithmExecutor( algorithm, this.algorithmSynchronizer );
        this.graphWindow.updateStatusLabel( "Starting animation" );
        // start the animation with a clean copy of the edit graph, a copy
        // without the edit states
        this.workingGraph = this.editGraph.copyCurrentState( this.editGraph );
        algorithm.setGraph( this.workingGraph );
        this.algorithmExecutor.startAlgorithm();
        notifyListeners( ANIMATION_MODE, !this.animationMode, this.animationMode );
    }

    /**
     * undoes effect of animation by returning to the edit graph, but preserving
     * any node position changes during algorithm execution
     */
    public void stopAlgorithm () {

        final Graph algorithmGraph = this.workingGraph;

        if ( this.algorithmMovesNodes() ) {
            this.workingGraph = this.editGraph;
            this.workingGraph.setNodePositions( algorithmGraph );
        }
        initializeVirtualWindow();
        this.animationMode = false;
        this.editMode = true;
        this.graphWindow.updateStatusLabel( "Animation stopped" );
        notifyListeners( ANIMATION_MODE, !this.animationMode, this.animationMode );
    }

    public AlgorithmExecutor getAlgorithmExecutor () {
        return algorithmExecutor;
    }

    public void setAlgorithmExecutor ( final AlgorithmExecutor algorithmExecutor ) {
        this.algorithmExecutor = algorithmExecutor;
    }

    public AlgorithmSynchronizer getAlgorithmSynchronizer () {
        return algorithmSynchronizer;
    }

    public void setAlgorithmSynchronizer ( final AlgorithmSynchronizer algorithmSynchronizer ) {
        this.algorithmSynchronizer = algorithmSynchronizer;
    }

    /**
     * @return the current display state or the current edit state, depending on
     *         whether an animation is running or not
     */
    public int getDisplayState () {
        int returnState = 0;
        if ( animationMode ) {
            returnState = algorithmExecutor.getDisplayState();
        }
        else {
            returnState = workingGraph.getEditState();
        }
        return returnState;
    }

    /**
     * @return the current algorithm state or 0 if not in animation mode; used
     *         when the context does not know whether or not algorithm is
     *         running
     */
    public int getAlgorithmState () {
        if ( animationMode ) {
            return algorithmExecutor.getAlgorithmState();
        }
        return 0;
    }

    /**
     * Does what the name suggests
     *
     * @return true if a new step/state occurs (not currently used)
     */
    public boolean startStepIfAnimationOrIncrementEditState () throws Terminate {
        if ( animationMode && !algorithmSynchronizer.isLocked() ) {
            this.algorithmSynchronizer.startStep();
            return true;
        }
        if ( !animationMode && !atomic ) {
            this.workingGraph.incrementEffectiveEditState();
            return true;
        }
        return false;
    }

    /**
     * Differs from startStepIfRunning() in that it ignores a lock; this is
     * needed when an algorithm happens to be in a locked state when a dialog is
     * initiated.
     */
    public void initStepIfRunning () throws Terminate {
        if ( animationMode ) {
            algorithmSynchronizer.startStep();
        }
    }

    public void pauseExecutionIfRunning () throws Terminate {
        if ( animationMode ) {
            algorithmSynchronizer.pauseExecution();
        }
    }

    /**
     * Locks the current algorithm state if algorithm is running
     */
    public void lockIfRunning () {
        if ( animationMode ) {
            algorithmSynchronizer.lock();
        }
    }

    /**
     * Unlocks the current algorithm state if algorithm is running
     */
    public void unlockIfRunning () {
        if ( animationMode ) {
            algorithmSynchronizer.unlock();
        }
    }

    public boolean algorithmMovesNodes () {
        return this.algorithmMovesNodes;
    }

    /**
     * sets an indicator that the algorithm will move nodes during execution and
     * disables user movement of nodes
     */
    public void setAlgorithmMovesNodes ( final boolean algorithmMovesNodes ) {
        this.algorithmMovesNodes = algorithmMovesNodes;
    }

    public void pushToGraphEditor () {
        notifyListeners( GRAPH_UPDATE, null, null );
    }

    /**
     * Notifies the text panel corresponding to the graph window that a change
     * has occurred; the text is updated to reflect the current state of the
     * graph
     *
     * @todo This should be rethought. There is no need to reflect every change
     *       in the graph window immediately in the corresponding text window. A
     *       better way would be to postpone updates to the text window until
     *       the user opts to take action, e.g., by a saving the file as is
     *       currently done from the text window to push updates to the graph
     *       window. To make this work, there would have to be a dirty bit for
     *       the graph panel/window so that user is asked whether to really exit
     *       when there are unsaved graph window changes.
     */
    public void pushToTextEditor () {
        LogHelper.disable();
        LogHelper.enterMethod( getClass(), "pushToTextEditor" );
        notifyListeners( TEXT_UPDATE, null, null );
        LogHelper.exitMethod( getClass(), "pushToTextEditor" );
        LogHelper.restoreState();
    }

    private void notifyListeners ( final String property, final Object oldValue, final Object newValue ) {
        for ( final PropertyChangeListener name : listener ) {
            name.propertyChange( new PropertyChangeEvent( this, property, oldValue, newValue ) );
        }
    }

    public void addChangeListener ( final PropertyChangeListener newListener ) {
        listener.add( newListener );
    }

    public int getWindowWidth () {
        return windowWidth;
    }

    public void setWindowWidth ( final int windowWidth ) {
        this.windowWidth = windowWidth;
    }

    public int getWindowHeight () {
        return windowHeight;
    }

    public void setWindowHeight ( final int windowHeight ) {
        this.windowHeight = windowHeight;
    }

    public void setWindowSize ( final int height, final int width ) {
        LogHelper.enterMethod( getClass(), "setWindowSize()" );
        this.windowHeight = height;
        this.windowWidth = width;
        LogHelper.exitMethod( getClass(), "setWindowSize()" );
    }

    public GraphWindow getGraphWindow () {
        return graphWindow;
    }

    public void setGraphWindow ( final GraphWindow graphWindow ) {
        this.graphWindow = graphWindow;
    }

    // represents the virtual window boundary that contains all nodes
    // and is scaled to the actual window boundary
    // public double virtualWidth = 800;
    // public double virtualHeight = 500;
    // public double virtualX = 0;
    // public double virtualY = 0;
    public Rectangle virtualWindow = new Rectangle( 0, 0, 800, 500 );

    public void initializeVirtualWindow () {

        int minimalX = Integer.MAX_VALUE;
        int minimalY = Integer.MAX_VALUE;
        int maximalX = 0;
        int maximalY = 0;

        // iterate through all nodes
        final Graph g = getWorkingGraph();
        if ( g != null ) {
            for ( final Node v : g.getAllNodes() ) {

                if ( v.getX() < minimalX ) {
                    minimalX = v.getX();
                }
                if ( v.getY() < minimalY ) {
                    minimalY = v.getY();
                }
                if ( v.getX() > maximalX ) {
                    maximalX = v.getX();
                }
                if ( v.getY() > maximalY ) {
                    maximalY = v.getY();
                }
            }
        }

        virtualWindow.x = minimalX;
        virtualWindow.y = minimalY;
        virtualWindow.width = maximalX - minimalX;
        virtualWindow.height = maximalY - minimalY;

    }

    public Point ViewTransform ( final Point p ) {
        // do not transform layered graph
        if ( workingGraph.isLayered() ) {
            return p;
        }

        final double width = getWindowWidth();
        final double height = getWindowHeight();

        // convert from logical position to virtual window viewport coordinates
        final double vPointX = ( p.x - virtualWindow.x ) / (double) virtualWindow.width;
        final double vPointY = ( p.y - virtualWindow.y ) / (double) virtualWindow.height;

        // convert from virtual viewport (0,1) to display coordinates
        return new Point(
                /*
                 * these values correspond to outerNodeMargin and windowoff
                 * TODO: change these magic numbers into constants
                 */
                (int) ( WINDOW_PADDING + vPointX * ( width - 2 * WINDOW_PADDING ) ), (int) ( WINDOW_PADDING
                        + WINDOW_OFFSET + vPointY * ( height - 2 * ( WINDOW_PADDING + WINDOW_OFFSET ) ) ) );
    }

    public Point InvViewTransform ( final Point p ) {
        // do not transform layered graph interactions
        if ( workingGraph.isLayered() ) {
            return p;
        }

        final double width = getWindowWidth();
        final double height = getWindowHeight();

        // convert from display coordinates to virtual viewport (0,1)
        final double vPointX = ( p.x - WINDOW_PADDING ) / ( width - 2 * WINDOW_PADDING );
        final double vPointY = ( p.y - WINDOW_PADDING - WINDOW_OFFSET )
                / ( height - 2 * ( WINDOW_PADDING + WINDOW_OFFSET ) );

        // convert from virtual viewport to logical position
        return new Point( (int) ( virtualWindow.x + vPointX * virtualWindow.width ),
                (int) ( virtualWindow.y + vPointY * virtualWindow.height ) );
    }
}

// modified by 2021 Galant Team
