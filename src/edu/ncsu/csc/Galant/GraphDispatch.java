package edu.ncsu.csc.Galant;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.swing.JDialog;

import edu.ncsu.csc.Galant.graph.component.Graph;
import edu.ncsu.csc.Galant.gui.window.GraphWindow;
import edu.ncsu.csc.Galant.logging.LogHelper;
import edu.ncsu.csc.Galant.algorithm.AlgorithmExecutor;
import edu.ncsu.csc.Galant.algorithm.AlgorithmSynchronizer;
import edu.ncsu.csc.Galant.algorithm.Terminate;

/**
 * Dispatch for managing working graphs in the editor; also used for passing
 * information about window width and height to other classes as appropriate;
 * and for passing information about current mode (animation vs. editing)
 *
 * @author Jason Cockrell, Ty Devries, Alex McCabe, Michael Owoc; edited by
 * Matthias Stallmann, Weijia Li, and Yuang Ni.
 *
 * @todo I'm puzzled by the need for getIntance() when there is only one
 * instance at any given time. Seems like all information should be static
 * and initialized appropriately when Galant execution begins. A more
 * appropriate name for this class, given its current usage, would be
 * Globals. The original intent may have been to have multiple instances for
 * multiple communication channels between graphs, algorithms and editors.
 */
public class GraphDispatch {

	private static GraphDispatch instance;
	private Graph workingGraph;
    /**
     * A unique identifier for a graph.
     * @todo not clear to me what the purpose is
     */
	private UUID graphSource;

	private int windowWidth;
	private int windowHeight;

    /** true if animating an algorithm instead of editing */
	private boolean animationMode = false;

    /**
     * reference to controller of algorithm execution from the point of view
     * of the display, i.e., the object whose methods are called when user
     * starts/stops algorithm and steps forward or backward.
     */
    private AlgorithmExecutor algorithmExecutor;

    /**
     * reference to the object whose methods are called when the algorithm
     * changes the state of the graph in response to user actions
     */
    private AlgorithmSynchronizer algorithmSynchronizer;

    /**
     * The current graph window, whether in edit or animation mode
     */
	private GraphWindow graphWindow;

    /**
     * Reference to an active query window during algorithm execution (so
     * that it can be properly closed and does not cause Galant to hang)
     */
    private JDialog activeQuery;

    /**
     * text of response to latest interactive query with text input
     */
    private String stringAnswer;

    /**
     * integer value of answer to latest interactive query for an integer
     */
    private Integer integerAnswer;

    /**
     * double value of answer to latest interactive query for a double
     */
    private Double doubleAnswer;

    /**
     * getters and setters for the query answers
     */
    public void setActiveQuery(JDialog dialog) { this.activeQuery = dialog; }
    public JDialog getActiveQuery() { return this.activeQuery; }
    public void setStringAnswer(String answer) { this.stringAnswer = answer; }
    public String getStringAnswer() { return this.stringAnswer; }
    public void setIntegerAnswer(Integer answer) { this.integerAnswer = answer; }
    public Integer getIntegerAnswer() { return this.integerAnswer; }
    public void setDoubleAnswer(Double answer) { this.doubleAnswer = answer; }
    public Double getDoubleAnswer() { return this.doubleAnswer; }

    /**
     * true if the algorithm moves nodes; an algorithm should set this if it
     * does not want the user to move nodes during execution.
     */
    private boolean algorithmMovesNodes = false;

	public static final String ANIMATION_MODE = "animationMode";
	public static final String GRAPH_UPDATE = "graphUpdate";
	public static final String TEXT_UPDATE = "textUpdate";

	public static final String ADD_NODE = "addNode";
	public static final String ADD_EDGE = "addEdge";
	public static final String DELETE_COMPONENT = "deleteComponent";

	private List<PropertyChangeListener> listener = new ArrayList<PropertyChangeListener>();

	private GraphDispatch() {
		LogHelper.enterConstructor(getClass());
		LogHelper.exitConstructor(getClass());
	}

    /**
     * @return the (unique) instance of a GraphDispatch; this method allows
     * various parts of the code to communicate with each other indirectly;
     * for example, an instance of the Graph class does not have to be
     * associated with a GraphDispatch instance upon creation in order for it
     * to interact with the display, animation, etc.
     *
     * @todo this is a nonstandard use of getInstance(), which, in other
     * contexts, always returns a new instance; this should probably have
     * another name, such as getDispatch(), but that would require global
     * changes.
     */
	public static GraphDispatch getInstance() {
		if (instance == null) {
			instance = new GraphDispatch();
		}
		return instance;
	}

	public Graph getWorkingGraph() {
		if (workingGraph == null) {
			workingGraph = new Graph();
			workingGraph.graphWindow = graphWindow;
		}
		return workingGraph;
	}

	public void setWorkingGraph(Graph g, UUID u) {
		this.workingGraph = g;
		this.graphSource = u;
		notifyListeners(GRAPH_UPDATE, null, null);
	}

	public UUID getGraphSource() {
		return graphSource;
	}

	public void setGraphSource(UUID graphSource) {
		this.graphSource = graphSource;
	}

	public boolean isAnimationMode() {
		return this.animationMode;
	}

	public void setAnimationMode(boolean mode) {
		Boolean old = this.animationMode;
		this.animationMode = mode;
        // if at the end of an animation, need to reset the graph
        if ( ! mode && old ) {
            this.workingGraph.reset();
        }
		notifyListeners(ANIMATION_MODE, old, this.animationMode);
	}

    public AlgorithmExecutor getAlgorithmExecutor() {
        return algorithmExecutor;
    }

    public void setAlgorithmExecutor(AlgorithmExecutor algorithmExecutor) {
        this.algorithmExecutor = algorithmExecutor;
    }

    public AlgorithmSynchronizer getAlgorithmSynchronizer() {
        return algorithmSynchronizer;
    }
    public void setAlgorithmSynchronizer(AlgorithmSynchronizer algorithmSynchronizer) {
        this.algorithmSynchronizer = algorithmSynchronizer;
    }

    /**
     * @return the current display state or 0 if not in animation mode; used
     * when the context does not know whether or not algorithm is running
     */
    public int getDisplayState() {
        if ( animationMode ) return algorithmExecutor.getDisplayState();
        return 0;
    }

    /**
     * @return the current algorithm state or 0 if not in animation mode; used
     * when the context does not know whether or not algorithm is running
     */
    public int getAlgorithmState() {
        if ( animationMode ) return algorithmExecutor.getAlgorithmState();
        return 0;
    }

	public void startStepIfRunning() throws Terminate {
		if ( animationMode
             && ! algorithmSynchronizer.isLocked()
             ) {
            algorithmSynchronizer.startStep();
		}
	}

    /**
     * Differs from startStepIfRunning() in that it ignores a lock; this is
     * needed when an algorithm happens to be in a locked state when a dialog
     * is initiated.
     */
	public void initStepIfRunning() throws Terminate {
		if ( animationMode ) {
            algorithmSynchronizer.startStep();
		}
	}

	public void pauseExecutionIfRunning() throws Terminate {
		if ( animationMode )
            algorithmSynchronizer.pauseExecution();
	}

    /**
     * Locks the current algorithm state if algorithm is running
     */
    public void lockIfRunning() {
        if ( animationMode )
            algorithmSynchronizer.lock();
    }

    /**
     * Unlocks the current algorithm state if algorithm is running
     */
    public void unlockIfRunning() {
        if ( animationMode )
            algorithmSynchronizer.unlock();
    }

    public boolean algorithmMovesNodes() {
        return this.algorithmMovesNodes;
    }

    /**
     * @todo may need to set positions of nodes using their fixed positions
     * as starting points.
     */
    public void setAlgorithmMovesNodes(boolean algorithmMovesNodes) {
        this.algorithmMovesNodes = algorithmMovesNodes;
    }

	public void pushToGraphEditor() {
		notifyListeners(GRAPH_UPDATE, null, null);
	}

    /**
     * @todo this apparently causes the GraphMLParser to read the text in the
     * editor and create a new graph from it; probably should be prevented
     * somewhere.
     */
	public void pushToTextEditor() {
        if ( ! animationMode ) {
            notifyListeners(TEXT_UPDATE, null, null);
        }
	}

	private void notifyListeners(String property, Object oldValue, Object newValue) {
		for (PropertyChangeListener name : listener) {
			name.propertyChange(new PropertyChangeEvent(this, property, oldValue, newValue));
		}
	}

	public void addChangeListener(PropertyChangeListener newListener) {
		listener.add(newListener);
	}

	public int getWindowWidth() {
		return windowWidth;
	}

	public void setWindowWidth(int windowWidth) {
		this.windowWidth = windowWidth;
	}

	public int getWindowHeight() {
		return windowHeight;
	}

	public void setWindowHeight(int windowHeight) {
		this.windowHeight = windowHeight;
	}

	public void setWindowSize(int height, int width) {
		LogHelper.enterMethod(getClass(), "setWindowSize()");
		this.windowHeight = height;
		this.windowWidth = width;
		LogHelper.exitMethod(getClass(), "setWindowSize()");
	}

	public GraphWindow getGraphWindow(){
		return graphWindow;
	}

	public void setGraphWindow(GraphWindow graphWindow){
		this.graphWindow = graphWindow;
	}

}

//  [Last modified: 2016 12 13 at 20:28:34 GMT]
