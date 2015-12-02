package edu.ncsu.csc.Galant;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import edu.ncsu.csc.Galant.graph.component.Graph;
import edu.ncsu.csc.Galant.gui.window.GraphWindow;
import edu.ncsu.csc.Galant.logging.LogHelper;
import edu.ncsu.csc.Galant.algorithm.AlgorithmExecutor;
import edu.ncsu.csc.Galant.algorithm.AlgorithmSynchronizer;

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
 * and initialized appropriately when Galant execution begins.
 */
public class GraphDispatch {

	private static GraphDispatch instance;
	private Graph workingGraph;
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

	private GraphWindow graphWindow;

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
		boolean old = this.animationMode;
		this.animationMode = mode;
		notifyListeners(ANIMATION_MODE, new Boolean(old), new Boolean(this.animationMode) );
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

    /**
     * @todo algorithmMovesNodes is the only piece of information that is
     * relevant only during execution and must be initialized (to false) when
     * an algorithm starts. For now, I've built this into the Algorithm
     * initialize() method, but it probably should not be a part of
     * GraphDispatch at all.
     */
    public boolean algorithmMovesNodes() {
        return this.algorithmMovesNodes;
    }
  
    public void setAlgorithmMovesNodes(boolean algorithmMovesNodes) {
        this.algorithmMovesNodes = algorithmMovesNodes;
    }
  
	public void pushToGraphEditor() {
		notifyListeners(GRAPH_UPDATE, null, null);
	}
	
	public void pushToTextEditor() {
		notifyListeners(TEXT_UPDATE, null, null);
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

//  [Last modified: 2015 12 02 at 14:22:39 GMT]
