package edu.ncsu.csc.Galant;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import edu.ncsu.csc.Galant.graph.component.Graph;
import edu.ncsu.csc.Galant.gui.window.GraphWindow;
import edu.ncsu.csc.Galant.logging.LogHelper;

/**
 * Dispatch for managing working graphs in the editor;
 * also used for passing information about window width and height to other
 * classes as appropriate;
 * and for passing information about current mode (animation vs. editing) 
 * @author Jason Cockrell, Ty Devries, Alex McCabe, Michael Owoc
 */
public class GraphDispatch {

	private static GraphDispatch instance;
	private Graph workingGraph;
	private UUID graphSource;

	private int windowWidth;
	private int windowHeight;

	private boolean animationMode = false;

	private GraphWindow graphWindow;

    private boolean algorithmMovesNodes = false;

    /** if true, this goes through the animation in slow motion but does not
     * allow backtracking */
    private boolean movieMode = false;

	public static final String ANIMATION_MODE = "animationMode";
	public static final String GRAPH_UPDATE = "graphUpdate";
	public static final String TEXT_UPDATE = "textUpdate";
	
	public static final String ADD_NODE = "addNode";
	public static final String ADD_EDGE = "addEdge";
	public static final String DELETE_COMPONENT = "deleteComponent";

	private List<PropertyChangeListener> listener = new ArrayList<PropertyChangeListener>();
	
	private GraphDispatch() {
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
  
  public boolean isAlgorithmMovesNodes() {
		return this.algorithmMovesNodes;
	}
	
	public void setAnimationMode(boolean mode) {
		boolean old = this.animationMode;
		this.animationMode = mode;
		notifyListeners(ANIMATION_MODE, new Boolean(old), new Boolean(this.animationMode) );
	}

	public void pushToGraphEditor() {
		notifyListeners(GRAPH_UPDATE, null, null);
	}
	
	public void pushToTextEditor() {
		notifyListeners(TEXT_UPDATE, null, null);
	}
	
	/*
	public void notifyAddNode(Node n) {
		notifyListeners(ADD_NODE, n, n);
	}
	
	public void notifyAddEdge(Edge e) {
		notifyListeners(ADD_EDGE, e, e);
	}
	
	public void notifyDeleteComponent() {
		notifyListeners(DELETE_COMPONENT, null, null);
	}*/

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

//  [Last modified: 2015 07 03 at 14:40:22 GMT]
