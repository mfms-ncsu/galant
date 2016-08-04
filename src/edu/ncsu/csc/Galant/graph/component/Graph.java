/**
 * Provides access to all information about the graph
 */

package edu.ncsu.csc.Galant.graph.component;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import edu.ncsu.csc.Galant.GalantException;
import edu.ncsu.csc.Galant.GraphDispatch;
import edu.ncsu.csc.Galant.gui.window.GraphWindow;
import edu.ncsu.csc.Galant.gui.util.EdgeSelectionDialog;
import edu.ncsu.csc.Galant.gui.util.NodeSelectionDialog;
import edu.ncsu.csc.Galant.algorithm.Terminate;
import edu.ncsu.csc.Galant.logging.LogHelper;

/**
 * @todo in future this can be used as a return value for nodes and edges
 * that are currently returned as lists.
 */
import edu.ncsu.csc.Galant.graph.container.EdgeSet;

/**
 * Stores all nodes, edges and related information for use in graph algorithms.
 * 
 * @author Michael Owoc, Ty Devries (major modifications by Matt Stallmann)
 */
public class Graph {
	public GraphWindow graphWindow;
    private GraphDispatch dispatch;

    private String name;
    private String comment;
    private boolean directed;

    private boolean layered = false;
    private LayerInformation layerInformation;
	
	private List<Node> nodes;

    private TreeMap<Integer,Node> nodeById = new TreeMap<Integer,Node>();

	private List<Edge> edges;

    private MessageBanner banner;

    /**
     * Keeps track of an edge selected during algorithm execution.
     */
    private Edge selectedEdge;

    /**
     * Keeps track of an node selected during algorithm execution.
     */
    private Node selectedNode;

    /**
     * The list of states that this graph has been in up to this point --
     * essentially the list of all changes to the graph (independent of its
     * nodes and edges). At this point the only relevant attributes relate to
     * visibility of labels and weights.
     */
	protected List<GraphState> states;

    /**
     * @todo Never clear what this meant. A better name might be startNode,
     * but then there's getStartNode(), which has lots of side effects, and
     * is apparently not used anywhere (probably intended for algorithms that
     * have a start node such as dfs, bfs, and Dijkstra's).
     */
	private Node rootNode;
	
    /**
     * An integer that can be used as the id of the next edge if id's are not
     * explicit in the input.
     */
    private int nextEdgeId = 0;

	/**
	 * Default constructor.
	 */
	public Graph() {
        dispatch = GraphDispatch.getInstance();
		nodes = new ArrayList<Node>();
		edges = new ArrayList<Edge>();
        states = new ArrayList<GraphState>();
        this.addState(new GraphState());
        banner = new MessageBanner(this);
	}

    /**
     * Resets the graph to its original state at the end of an
     * animation.
     */
    public void reset() {
        // first, reset any graph visibility attributes
        ArrayList<GraphState> initialStates
            = new ArrayList<GraphState>();
        for ( GraphState state : this.states ) {
            if ( state.getState() > 0 ) break;
            initialStates.add(state);
        }
        this.states = initialStates;

        // then reset the attributes of all nodes and edges
        for ( Node node : this.nodes ) {
            node.reset();
        }
        for ( Edge edge : this.edges ) {
            edge.reset();
        }
        // then reinitialize the message banner
        banner = new MessageBanner(this);
    }

    /**
     * @return a new state for this graph; the new state will be identical
     * to the current (latest one) except that it will be tagged with the
     * current algorithm state.
     *
     * @todo there is no reason to create new states when parsing and the
     * only reason to do it when editing is for a possible "undo" mechanism,
     * which is not yet implemented
     */
    private GraphState newState() throws Terminate {
		dispatch.startStepIfRunning();
		GraphState latest = latestState();
		GraphState state
            = new GraphState(latest);
		
        LogHelper.logDebug( "newState (graph) = " + state.getState() );
		return state;
    }

    /**
     * @return The last state on the list of states. This is the default for
     * retrieving information about any attribute.
     */
    public GraphState latestState() {
        LogHelper.enterMethod(getClass(), "latestState, states = " + states);
        GraphState state = null;
        if ( states.size() != 0 ) {
            state = states.get(states.size() - 1);
        }
        LogHelper.exitMethod(getClass(), "latestState, state = " + state);
        return state; 
    }

    /**
     * This method is vital for retrieving the most recent information about
     * a graph, where most recent is defined relative to a given time stamp,
     * as defined by forward and backward stepping through the animation.
     * @see edu.ncsu.csc.Galant.algorithm.AlgorithmExecutor
     * @param stateNumber the numerical indicator (timestamp) of a state,
     * usually the current display state
     * @return the latest instance of GraphState that was created
     * before the given time stamp, or null if the element did not exist
     * before the time stamp.
     */
	public GraphState getLatestValidState(int stateNumber) {
        LogHelper.enterMethod(getClass(), "getLatestValidState("
                              + stateNumber + "), " + this);
        GraphState toReturn = null;
        int stateIndex = states.size() - 1;
		while ( stateIndex >= 0 ) {
			GraphState state = states.get(stateIndex);
			if ( state.getState() <= stateNumber ) {
				toReturn = state;
                break;
			}
            stateIndex--;
		}
        LogHelper.exitMethod(getClass(), "getLatestValidState("
                              + stateNumber + "), " + toReturn);
        return toReturn;
	}
	
	/**
     * Adds the given state to the list of states for this graph. If there is
     * already a state having the same algorithm state (time stamp), there is
     * no need to add another one. Such a situation might arise if there are
     * multiple state changes to this element between a beginStep()/endStep()
     * pair or if no algorithm is running.  If an algorithm is running, this
     * method initiates synchronization with the master thread to indicate
     * that the changes corresponding to the added state are completed
     *
     * @invariant states are always sorted by state number.
     */
	private void addState(GraphState stateToAdd) {
        LogHelper.enterMethod(getClass(), "addState, state number = "
                              + stateToAdd.getState());
        int stateNumber = stateToAdd.getState();
        boolean found = false;
        for ( int i = states.size() - 1; i >= stateNumber; i-- ) {
            GraphState state = states.get(i);
            LogHelper.logDebug("addState loop, i = " + i + ", state(i) = " + state.getState());
            if ( state.getState() == stateNumber ) {
                states.set(i, stateToAdd);
                found = true;
                break;
            }
        }
        if ( ! found ) {
            states.add(stateToAdd);
            dispatch.pauseExecutionIfRunning();
        }
        LogHelper.exitMethod(getClass(), "addState, found = " + found);
	}

    /**
     * sets the selected edge; called from EdgeSelectionDialog
     */
    public void setSelectedEdge(Node source, Node target)
        throws GalantException
    {
        selectedEdge = getEdge(source, target);
    }

    /**
     * @param prompt a message displayed in the edge selection dialog popup
     * @return an edge selected via a dialog during algorithm execution
     */
    public Edge getEdge(String prompt) throws Terminate {
        dispatch.startStepIfRunning();
        EdgeSelectionDialog dialog = new EdgeSelectionDialog(prompt);
        dispatch.pauseExecutionIfRunning();
        dialog = null;          // to keep window from lingering when
                                // execution is terminated
        return selectedEdge;
    }

    /**
     * sets the selected node; called from NodeSelectionDialog
     */
    public void setSelectedNode(Node node) {
        selectedNode = node;
    }

    /**
     * @param prompt a message displayed in the node selection dialog popup
     * @return a node selected via a dialog during algorithm execution
     */
    public Node getNode(String prompt) throws Terminate {
        dispatch.startStepIfRunning();
        NodeSelectionDialog dialog = new NodeSelectionDialog(prompt);
        dispatch.pauseExecutionIfRunning();
        dialog = null;          // to keep window from lingering when
                                // execution is terminated
        return selectedNode;
    }


    /* Only Boolean and String attributes are needed for now */

    private static final String HIDDEN_EDGE_LABELS = "edgeLabelsHidden";
    private static final String HIDDEN_EDGE_WEIGHTS = "edgeWeightsHidden";
    private static final String HIDDEN_NODE_LABELS = "nodeLabelsHidden";
    private static final String HIDDEN_NODE_WEIGHTS = "nodeWeightsHidden";


    /**
     * Removes the attribute with the given key from the list and updates
     * state information appropriately.
     */
    public void remove(String key) throws Terminate {
        GraphState newState = newState();
        newState.remove(key);
        addState(newState);
    }
	
    /************** Boolean attributes ***************/

	public boolean set(String key, Boolean value) throws Terminate {
        LogHelper.enterMethod(getClass(),
                              "set, key = " + key + ", value = " + value);
        GraphState newState = newState();
        boolean found = newState.set(key, value);
        addState(newState);
        LogHelper.exitMethod(getClass(),
                              "set, object = " + this);
        return found;
	}

    /**
     * If value is not specified, assume it's boolean and set to true
     */
    public boolean set(String key) throws Terminate {
        return this.set(key, true);
    }
 
    public void clear(String key) throws Terminate {
        this.remove(key);
    }

    /**
     * For boolean attributes, assume that the absense of an attribute means
     * that it's false.
     */
	public Boolean getBoolean(String key) {
        GraphState state = latestState();
        if ( state == null ) return false;
		return state.getAttributes().getBoolean(key);
	}
	public Boolean getBoolean(int state, String key) {
        GraphState validState = getLatestValidState(state);
		return validState == null ? false : validState.getAttributes().getBoolean(key);
	}

    /**
     * Synonyms (for readability in algorithms)
     */
    public Boolean is(String key) {
        return getBoolean(key);
    }
    public Boolean is(int state, String key) {
        return getBoolean(state, key);
    }
    
    /************** String attributes ***************/
	public boolean set(String key, String value) throws Terminate {
        GraphState newState = newState();
        boolean found = newState.set(key, value);
        addState(newState);
        return found;
	}
	public String getString(String key) {
        GraphState state = latestState();
        if ( state == null ) return null;
		return state.getAttributes().getString(key);
	}
	public String getString(int state, String key) {
        GraphState validState = getLatestValidState(state);
		return validState == null ? null : validState.getAttributes().getString(key);
	}

    /**
     * Methods that cause labels and weights to be shown or hidden during
     * algorithm execution. Typically, an algorithm will declare its intent
     * at the beginning. By default, labels and weights are shown unless the
     * algorithm hides them.
     */
    public void unhideEdgeLabels() throws Terminate { 
        clear(HIDDEN_EDGE_LABELS);
    }
    public void showEdgeLabels(Boolean show) throws Terminate { 
        set(HIDDEN_EDGE_LABELS, ! show);
    }
    public void hideEdgeLabels() throws Terminate { 
        set(HIDDEN_EDGE_LABELS);
    }
    public void unhideEdgeWeights() throws Terminate { 
        clear(HIDDEN_EDGE_WEIGHTS);
    }
    public void showEdgeWeights(Boolean show) throws Terminate { 
        set(HIDDEN_EDGE_WEIGHTS, ! show);
    }
    public void hideEdgeWeights() throws Terminate { 
        set(HIDDEN_EDGE_WEIGHTS);
    }
    public void unhideNodeLabels() throws Terminate { 
        clear(HIDDEN_NODE_LABELS);
    }
    public void showNodeLabels(Boolean show) throws Terminate { 
        set(HIDDEN_NODE_LABELS, ! show);
    }
    public void hideNodeLabels() throws Terminate { 
        set(HIDDEN_NODE_LABELS);
    }
    public void unhideNodeWeights() throws Terminate { 
        clear(HIDDEN_NODE_WEIGHTS);
    }
    public void showNodeWeights(Boolean show) throws Terminate { 
        set(HIDDEN_NODE_WEIGHTS, ! show);
    }
    public void hideNodeWeights() throws Terminate { 
        set(HIDDEN_NODE_WEIGHTS);
    }

    /**
     * Individually hide Node/Edge labels or weights; has no effect if they
     * are already hidden via, e.g., hideNodeLabels()
     */
    public void hideAllNodeLabels() throws Terminate {
        for ( Node node: nodes ) {
            node.hideLabel();
        }
    }
    public void hideAllEdgeLabels() throws Terminate {
        for ( Edge edge: edges ) {
            edge.hideLabel();
        }
    }
    public void hideAllNodeWeights() throws Terminate {
        for ( Node node: nodes ) {
            node.hideWeight();
        }
    }
    public void hideAllEdgeWeights() throws Terminate {
        for ( Edge edge: edges ) {
            edge.hideWeight();
        }
    }

    /**
     * Undo hiding of individual Node/Edge labels or weights; has no effect
     * if they are already hidden via, e.g., hideNodeLabels()
     */
    public void showAllNodeLabels() throws Terminate {
        for ( Node node: nodes ) {
            node.showLabel();
        }
    }
    public void showAllEdgeLabels() throws Terminate {
        for ( Edge edge: edges ) {
            edge.showLabel();
        }
    }
    public void showAllNodeWeights() throws Terminate {
        for ( Node node: nodes ) {
            node.showWeight();
        }
    }
    public void showAllEdgeWeights() throws Terminate {
        for ( Edge edge: edges ) {
            edge.showWeight();
        }
    }

    /**
     * Shows all Nodes that have been hidden individually
     */
    public void showNodes() throws Terminate {
        for ( Node node: nodes ) {
            node.show();
        }
    }

    /**
     * Shows all edges that have been hidden individually
     */
    public void showEdges() throws Terminate {
        for ( Edge edge: edges ) {
            edge.show();
        }
    }

    /**
     * The following are used to query label and weight visibility during
     * algorithm execution and are called from GraphPanel; note that only the
     * versions with the state argument are needed -- these are never called
     * outside of algorithm execution. The implementation also implies that
     * labels and weights are visible by default during algorithm
     * execution. The algorithm must explicitly hide them, either globally or
     * for individual nodes and edges.
     */
    public Boolean edgeLabelsAreVisible(int state) {
        return ! is(state, HIDDEN_EDGE_LABELS);
    }
    public Boolean edgeWeightsAreVisible(int state) {
        return ! is(state, HIDDEN_EDGE_WEIGHTS);
    }
    public Boolean nodeLabelsAreVisible(int state) {
        return ! is(state, HIDDEN_NODE_LABELS);
    }
    public Boolean nodeWeightsAreVisible(int state) {
        return ! is(state, HIDDEN_NODE_WEIGHTS);
    }

    /**
     * The following are used to do blanket clearing of attributes
     */
    public void clearNodeMarks() throws Terminate {
        for ( Node node: nodes ) node.unmark();
    }
    public void clearNodeHighlighting() throws Terminate {
        for ( Node node: nodes ) node.unHighlight();
    }
    public void clearEdgeHighlighting() throws Terminate {
        for ( Edge edge: edges ) edge.unHighlight();
    }
    public void clearNodeLabels() throws Terminate {
        for ( Node node: nodes ) node.clearLabel();
    }
    public void clearEdgeLabels() throws Terminate {
        for ( Edge edge: edges ) edge.clearLabel();
    }
    public void clearNodeWeights() throws Terminate {
        for ( Node node: nodes ) node.clearWeight();
    }
    public void clearEdgeWeights() throws Terminate {
        for ( Edge edge: edges ) edge.clearWeight();
    }
    public void clearAllNode(String attribute) throws Terminate {
        for ( Node node: nodes ) node.clear(attribute);
    }
    public void clearAllEdge(String attribute) throws Terminate {
        for ( Edge edge: edges ) edge.clear(attribute);
    }

    /** Graph methods that are independent of state */

    public void setName( String name ) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setComment( String comment ) {
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }

    public void setLayered( boolean layered ) {
        this.layered = layered;
        if ( layerInformation == null ) {
            layerInformation = new LayerInformation();
        }
    }

    public boolean isLayered() {
        return layered;
    }

	/**
     * Changes the contents of the current message banner
	 */
	public void writeMessage(String message) throws Terminate {
        LogHelper.enterMethod(getClass(), "writeMessage: " + message);
        banner.set(message);
        LogHelper.exitMethod(getClass(), "writeMessage: " + message);
	}
	
	/**
     * @param state the algorithm state for the desired message
	 * @return the current message banner
	 */
	public String getMessage(int state) {
		return banner.get(state);
	}
	
	/**
	 * @return the number of <code>Node</code>s in the current <code>Graph</code>
	 */
	public int numberOfNodes() {
		int count = 0;
		
		for (Node n : nodes) {
			if (n.inScope())
				count++;
		}
		
		return count;
	}
	
    /**
     * @return the largest id of any node + 1; this should be used when
     * allocating an array of nodes, as there is no longer a guarantee that
     * id's start at 0 and are contiguous.
     */
    public int nodeIds() {
		int maxId = 0;
		for ( Node currentNode : nodes ) {
			if ( currentNode.inScope() && currentNode.getId() > maxId )
				maxId = currentNode.getId();
		}
		return maxId + 1;
    }

	/**
	 * @return the number of <code>Edge</code>s in the current <code>Graph</code>
	 */
	public int numberOfEdges() {
		int count = 0;
		
		for (Edge n : edges) {
			if (n.inScope())
				count++;
		}
		
		return count;
	}
	
    /**
     * @return the largest id of any edge + 1; this should be used when
     * allocating an array of edges; unlike the case of nodes, it should not
     * really be needed -- edge id's are assigned contiguously; we provide it
     * to avoid confusion.
     */
    public int edgeIds() {
		int maxId = 0;
		for ( Edge currentEdge : edges ) {
			if ( currentEdge.inScope() && currentEdge.getId() > maxId )
				maxId = currentEdge.getId();
		}
		return maxId + 1;
    }

	/**
	 * @return true if the graph is directed, false otherwise
	 */
	public boolean isDirected() {
		return directed;
	}

	/**
	 * @param directed true if setting the graph to directed, false if undirected
	 */
	public void setDirected(boolean directed) {
		this.directed = directed;
	}

	/**
	 * @return all nodes in the graph; this version can only be used when
	 * there is no algorithm running; some nodes may not yet exist; inScope()
	 * without the 'state' argument only checks to see if a node has been
	 * deleted.
     *
     * @todo Just like the Algorithm provides NodeQueue, etc., it should also
     * provide NodeList as a data structure to avoid the template.
	 */
	public List<Node> getNodes() {
		List<Node> retNodes = new ArrayList<Node>();
		
		for (Node n : this.nodes) {
			if (n.inScope()) {
				retNodes.add(n);
			}
		}
		
		return retNodes;
	}
	
	/**
	 * @return all nodes in the graph that exist in the given state.
	 */
	public List<Node> getNodes(int state)
    {
		List<Node> retNodes = new ArrayList<Node>();
		
		for (Node n : this.nodes) {
			if (n.inScope(state)) {
				retNodes.add(n);
			}
		}
		
		return retNodes;
	}

	/**
	 * @param nodes new set of nodes to be added to the graph
	 */
	public void setNodes(List<Node> nodes) {
		this.nodes = nodes;
	}
	
	/**
	 * @return all edges as a list
     * @todo get rid of the template
	 */
	public ArrayList<Edge> getEdges()
    {
		ArrayList<Edge> retEdges = new ArrayList<Edge>();
		
		for (Edge e : this.edges) {
			if (e.inScope()) {
				retEdges.add(e);
			}
		}
		
		return retEdges;
	}
	
	/**
	 * @return all edges at the current algorithm state
	 */
	public List<Edge> getEdges(int state) 
    {
		List<Edge> retEdges = new ArrayList<Edge>();
		
		for (Edge e : this.edges) {
			if (e.inScope(state)) {
				retEdges.add(e);
			}
		}
		
		return retEdges;
	}
	
	/**
	 * Replaces the current <code>Edge</code>s.
	 * @param edges new set of edges to be added to the graph
	 */
	public void setEdges(List<Edge> edges) {
		this.edges = edges;
	}
	
	/**
	 * Removes the specified <code>Edge</code> from the <code>Graph</code>.
	 * @param e the edge to remove
	 */
	public void deleteEdge(Edge e) throws Terminate {
		e.setDeleted(true);
	}

    /**
     * @return an edge with the given source and target if one exists; throws
     * an exception otherwise; if the graph is directed, source and target
     * must match.
     */
    public Edge getEdge(Node source, Node target) throws GalantException {
        List<Edge> incidenceList
            = source.getOutgoingEdges();
        for ( Edge e : incidenceList ) {
            if ( source.travel(e) == target ) {
                return e;
            }
        }
        throw new GalantException("no edge with source " + source.getId()
                                  + " and target " + target.getId() + " exists");
    }

    /**
     * Deletes an edge with the given source and target if one exists; throws
     * an exception otherwise; if the graph is directed, source and target
     * must match. This is called only during algorithm execution. Not clear
     * how it would be used since we can hide edges.
     */
    public void deleteEdge(Node source, Node target) throws Terminate, GalantException {
        Edge edge = getEdge(source, target);
        deleteEdge(edge);
    }

	/**
	 * Removes the specified <code>Node</code> from the <code>Graph</code>.
	 * Starts a new algorithm step if appropriate
	 */
	public void deleteNode(Node n) throws Terminate {
        LogHelper.enterMethod(getClass(), "deleteNode " + n);
		dispatch.startStepIfRunning();
		dispatch.lockIfRunning();
		
		n.setDeleted(true);
		for (Edge e : n.getIncidentEdges()) {
			e.setDeleted(true);
            removeEdge(e);
		}
		
		dispatch.unlockIfRunning();
        LogHelper.exitMethod(getClass(), "deleteNode");
	}

	/**
	 * @return the root node
	 */
	public Node getRootNode()
        throws GalantException
    {
        if ( rootNode == null ) {
            throw new GalantException( "no root node has been set"
                                       + "\n - in getRootNode" );
        }
		return rootNode;
	}

	/**
	 * @param rootNode the new root node
	 */
	public void setRootNode(Node rootNode) {
		this.rootNode = rootNode;
	}
	
    /**
     * @returns true if a node with the given id exists
     */
    public boolean nodeIdExists( Integer id ) {
        return nodeById.containsKey( id );
    }

	/**
	 * Returns the Node in the graph represented by the given unique ID.
	 * 
	 * @param id
	 * @return the specified Node if it exists, null otherwise
	 */
	public Node getNodeById( int id )
        throws GalantException
    {
        if ( this.nodes.size() == 0 ) {
            throw new GalantException( "empty graph"
                                       + "\n - in getNodeById" );
        }

		if ( ! nodeById.containsKey( id ) ) {
            throw new GalantException( "no node with id = "
                                       + id
                                       + " exists"
                                       + "\n - in getNodeById" );
        }

        Node n = nodeById.get( id );

        if ( n.isDeleted() ) {
            throw new GalantException( "node has been deleted, id = "
                                       + id
                                       + "\n - in getNodeById" );
		}
		
		return n;
	}

    /**
     * @return the next available edge id; these are in numerical sequence
     * starting at 0; to be used only when the input has no explicit id's for
     * edges.
     */
    int getNextEdgeId() {
        return nextEdgeId++;
    }
	
    /**
     * @return true if the input had explicit edge id's as determined by the
     * fact that none had to be assigned internally -- the assumption being
     * that either all or none of the edges have explicit id's.
     */
    boolean inputHasEdgeIds() {
        return nextEdgeId == 0;
    }

	/**
	 * @return A node that can be used as a starting node by an algorithm
	 * that requires it
	 */
	public Node getStartNode() throws GalantException {
        if ( this.nodes.size() == 0 ) {
            throw new GalantException("getStartNode: graph is empty.");
        }
        return this.nodes.get(0);
	}
	
	/**
	 * Returns the Edge in the graph represented by the given unique ID.
	 * 
	 * @param id
	 * @return the specified Edge if it exists, null otherwise
	 */
	public Edge getEdgeById(int id)
        throws GalantException
    {
        if ( this.edges.size() == 0 ) {
            throw new GalantException( "graph has no edges"
                                       + "\n - in getEdgeById" );
        }

		if ( id < 0 || id >= this.nodes.size() ) {
            throw new GalantException( "edge out of range, id = "
                                       + id 
                                       + "\n - in getEdgeById" );
        }

        Edge e = this.edges.get(id);

        if ( e.isDeleted() ) {
            throw new GalantException( "edge has been deleted, id = "
                                       + id
                                       + "\n - in getEdgeById" );
		}

        return e;
	}

	/**
	 * Adds a new <code>Node</code> to the <code>Graph</code>
	 * @param x the x coordinate of the new node 
	 * @param y the y coordinate of the new node 
     * @return the added <code>Node</code>; called only during editing
	 */
	public Node addInitialNode(Integer x, Integer y) {
        LogHelper.enterMethod( getClass(), "addInitialNode(), x = " + x + ", y = " + y);
        Integer newId = nextNodeId();
		Node n = new Node(this, newId, x, y);
		nodes.add(n);
        nodeById.put(newId, n);
		
		if (this.rootNode == null) {
			this.rootNode = n;
		}

        // seems like we need an addState call as is the case with state
        // changes in GraphElement.java

        LogHelper.exitMethod( getClass(), "addInitialNode() " + n );
		return n;
	}
	
	/**
	 * Adds a new <code>Node</code> to the <code>Graph</code>
	 * @param x the x coordinate of the new node 
	 * @param y the y coordinate of the new node 
     * @return the added <code>Node</code>; called only during algorithm
	 * execution; the assumption here is that the algorithm has to "know" the
	 * position of the node it is adding. The only difference from the above
	 * is that an algorithm step is initiated if appropriate
	 */
	public Node addNode(Integer x, Integer y) throws Terminate {
        LogHelper.enterMethod( getClass(), "addNode(), x = " + x + ", y = " + y);
		dispatch.startStepIfRunning();
        Integer newId = nextNodeId();
		Node n = new Node(this, newId, x, y);
		nodes.add(n);
        nodeById.put( newId, n ); 
		
		if (this.rootNode == null) {
			this.rootNode = n;
		}

        // seems like we need an addState call as is the case with state
        // changes in GraphElement.java

        LogHelper.exitMethod( getClass(), "addNode() " + n );
		return n;
	}
	
	/**
	 * Adds a node to the graph during parsing. The node has already been created.
 	 */
	public void addNode(Node n) {
        LogHelper.enterMethod( getClass(), "addNode: node = " + n );

        /**
         * @todo subclass method for layered graphs
         */
        if ( layered ) {
            layerInformation.addNode( n );
        }

		nodes.add(n);
        nodeById.put( n.getId(), n );

        LogHelper.exitMethod( getClass(), "addNode( Node )" );
	}
	
	/**
	 * Adds a new <code>Edge</code> to the <code>Graph</code> with the specified source and target <code>Node</code> IDs.
	 * Note: for undirected <code>Graph</code>s, "source" and "target" are
	 * interchangeable.
	 * @param sourceId the ID of the source <code>Node</code>
	 * @param targetId the ID of the target <code>Node</code>
     *
     * This variant is used during parsing; also does the work of adding the
     * edge to the list of edges and those of its source and target.
	 */
	public void addEdge(Edge edge, int id) {
        edge.setId(id);
		edge.getSourceNode().addEdge(edge);
		edge.getTargetNode().addEdge(edge);
        edges.add(edge);
	}
	
	/**
	 * Adds a new edge to the graph with the specified source and
	 * target. Starts an algorithm step if appropriate.
	 * Note: for undirected graphs, "source" and "target" are meaningless.
	 * @return the added edge
     *
     * This variant is used during algorithm execution if the actual nodes
     * are known.
	 */
	public Edge addEdge(Node source, Node target) throws Terminate {	
		dispatch.startStepIfRunning();
        int id = edges.size();
		Edge e = new Edge(this, id, source, target);
        addEdge(e, id);
		return e;
	}
	
	/**
	 * Adds a new edge to the graph with the specified source and
	 * target. Starts an algorithm step if appropriate.
	 * Note: for undirected graphs, "source" and "target" are meaningless.
	 * @return the added edge
     *
     * This variant is used during algorithm execution when only the id's are known.
	 */
	public Edge addEdge(int sourceId, int targetId)
        throws Terminate, GalantException {	
        return addEdge(getNodeById(sourceId), getNodeById(targetId));
	}
	
	/**
	 * Adds a new edge to the graph with the specified source and
	 * target.
	 * Note: for undirected graphs, "source" and "target" are meaningless.
	 * @return the added edge
     *
     * This variant is used only during editing.
	 */
	public Edge addInitialEdge(Node source, Node target) {
		Edge e = new Edge(this, edges.size(), source, target);
		
		edges.add(e);
		source.addEdge(e);
		target.addEdge(e);
		
		return e;
	}
	
	/**
	 * Removes the specified <code>Edge</code> from the <code>Graph</code>
	 * @param e the <code>Edge</code> to remove
	 */
	public void removeEdge(Edge e) {
        LogHelper.enterMethod(getClass(), "removeEdge " + e);
		edges.remove(e);
		
		Node source = e.getSourceNode();
		source.getIncidentEdges().remove(e);		
		Node target = e.getTargetNode();
		target.getIncidentEdges().remove(e);
        LogHelper.exitMethod(getClass(), "removeEdge");
	}

    /**
     * Removes the edge with the specified source and target; throws an
     * exception if none exists; source and target must match if the graph is
     * directed
     */
    public void removeEdge(Node source, Node target) throws GalantException {
        Edge edge = getEdge(source, target);
        removeEdge(edge);
    }
	
	/**
	 * Removes the specified <code>Node</code> from the <code>Graph</code>
	 * @param n the <code>Node</code> to remove
	 */
	public void removeNode(Node n) {
		List<Edge> n_edges = n.getIncidentEdges();
        LogHelper.enterMethod(getClass(), "removeNode " + n + ", deg = " + n_edges.size());
		
		dispatch.lockIfRunning();
		for ( Edge e : n_edges ) {
			removeEdge(e);
		}

		nodes.remove(n);
		dispatch.unlockIfRunning();
        LogHelper.exitMethod(getClass(), "removeNode");
	}
	
	/**
	 * @return an integer ID for the next <code>Node</code> to be
	 * added. This will always be the largest id so far + 1 
	 */
	private int nextNodeId() {
        LogHelper.enterMethod(getClass(), "nextNodeId");
        int id = 0;
        if ( ! nodeById.isEmpty() )
            id =nodeById.lastKey() + 1;
        LogHelper.exitMethod(getClass(), "nextNodeId, id = " + id);
        return id;
	}
	
    /**
     * @return the number of layers if this is a layered graph
     */
    public int numberOfLayers() {
        return layerInformation.numberOfLayers;
    }

    /**
     * @return the number of nodes on layer i
     */
    public int numberOfNodesOnLayer( int i ) {
        return layerInformation.layerSize.get( i );
    }

    private GraphLayout savedLayout;

	/**
	 * Call the repositioning algorithm
	 * Currently the only supported algorithm is Force directed repositioning
	 * 
	 * @see edu.ncsu.csc.Galant.graph.component.GraphLayout#forceDirected()
	 */
	public void smartReposition() {
        savedLayout = new GraphLayout(this);
        GraphLayout layoutToBeRepositioned = new GraphLayout(this);
		layoutToBeRepositioned.forceDirected();
        layoutToBeRepositioned.usePositions();
	}

    /**
     * Restores node positions as they were before the last smartReposition()
     * @todo maintain an another instance of GraphLayout to do this
     */
    public void undoReposition() {
        if ( savedLayout != null ) {
            savedLayout.usePositions();
        }
    }
	
	/**
	 * Returns a valid graphml representation of the graph; for use when no
	 * algorithm is running
	 */
	public String xmlString() {
        LogHelper.enterMethod(getClass(), "xmlString");
        String s = "";
		s += "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n"; 
		s += "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" \n";  
		s += "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" ;
		s += "xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns \n"; 
		s += "http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">\n"; 
		s += " <graph ";
        if ( name != null )
            s += " name=\"" + name + "\"";
        if ( comment != null )
            s += " comment=\"" + comment + "\"";
        if ( this.isLayered() ) {
            s += " type=\"layered\"";
        }
		s += " edgedefault=\"" + (this.isDirected() ? "directed" : "undirected") + "\"";
		s += ">\n";
		for(Node n : this.nodes) {
			s += "  " + n.xmlString() + "\n";
		}
		for(Edge e : this.edges) {
			s += "  " + e.xmlString() + "\n";
		}
		s += " </graph>";
		s += "</graphml>";
        LogHelper.exitMethod(getClass(), "xmlString");
		return s;
	}
	
	/**
	 * Returns a valid graphml representation of the graph; for use when you
	 * want to export the current state of a running algorithm.
	 */
	public String xmlString(int state) {
        LogHelper.enterMethod(getClass(), "xmlString(" + state + ")");
        String s = "";
		s += "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n"; 
		s += "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" \n";  
		s += "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" ;
		s += "xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns \n"; 
		s += "http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">\n"; 
		s += " <graph ";
        if ( name != null )
            s += " name=\"" + name + "\"";
        if ( comment != null )
            s += " comment=\"" + comment + "\"";
        if ( this.isLayered() ) {
            s += " type=\"layered\"";
        }
		s += " edgedefault=\"" + (this.isDirected() ? "directed" : "undirected") + "\""; //directed/undirected
		s += ">\n";
		for(Node n : this.nodes) {
            LogHelper.logDebug("  writing xml string for node with id " + n.getId());
            LogHelper.logDebug("  writing xml string for node " + n);
			String sN = n.xmlString(state);
			if ( ! sN.trim().isEmpty() ) 
				s += "  " + sN + "\n";
		}
		for(Edge e : this.edges) {
            LogHelper.logDebug("writing xml string for edge " + e);
			String sE = e.xmlString(state);
			if ( ! sE.trim().isEmpty() ) 
				s += "  " + sE + "\n";
		}
		s += " </graph>";
		s += "</graphml>";
        LogHelper.exitMethod(getClass(), "xmlString(" + state + ")");
		return s;
	}
}

//  [Last modified: 2016 08 04 at 15:03:04 GMT]
