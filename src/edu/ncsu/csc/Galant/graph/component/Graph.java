package edu.ncsu.csc.Galant.graph.component;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.List;
import java.util.Map;
import edu.ncsu.csc.Galant.GalantException;
import edu.ncsu.csc.Galant.GraphDispatch;
import edu.ncsu.csc.Galant.logging.LogHelper;

/**
 * Stores information peculiar to layered graphs.
 *
 * @todo LayeredGraph clearly needs to be a subclass of Graph and
 * LayeredGraphNode a subclass of Node, but this may introduce complications
 * related to GraphState and NodeState.
 */
class LayerInformation {
    int numberOfLayers;
    /**
     * Stores the number of nodes on each layer
     */
    ArrayList<Integer> layerSize;
    
    LayerInformation() {
        layerSize = new ArrayList<Integer>();
    }

    /**
     * Uses layer and position in layer information about node v to update
     * information about number of layers and/or layer size
     */
    void addNode( Node v ) {
        LogHelper.enterMethod( getClass(),
                               "addNode: node = " + v 
                               + ", numberOfLayers = " + numberOfLayers );
        int layer = v.getLayer();
        if ( layer >= numberOfLayers ) {
            numberOfLayers = layer + 1;
            int tempNumberOfLayers = layerSize.size();
            while ( tempNumberOfLayers < numberOfLayers ) {
                layerSize.add( 0 );
                tempNumberOfLayers++;
            }
            layerSize.set( layer, 1 );
        }
        else {
            int sizeOfLayer = layerSize.get( layer );
            layerSize.set( layer, sizeOfLayer + 1 );
        }
        LogHelper.exitMethod( getClass(),
                              "addNode: numberOfLayers = " + numberOfLayers
                              + ", sizeOfLayer = " + layerSize.get( layer ) );
    }

}

/**
 * Stores all <code>Node</code>s and <code>Edge</code>s for use in graph algorithms.
 * Additionally stores its own <code>GraphState</code> that will be used to store changes made to the <code>Graph</code>
 * from compiling and running algorithms on the <code>Graph</code>.
 * 
 * @author Michael Owoc, Ty Devries (modifications by Matt Stallmann)
 *
 * $Id: Graph.java 113 2015-05-05 15:31:47Z mfms $
 */
public class Graph {

    public final static Double NOT_A_WEIGHT = Double.NaN;
    public final static String NOT_A_LABEL = "";

    /**
     * NOT_A_COLOR is used when no color is specified; when the GraphElement
     * is drawn on the panel NOT_A_COLOR is rendered as the default color
     * (black); in other situations (e.g., when converting to text), it may
     * be omitted
     */
    public final static String NOT_A_COLOR = "";

    /** 
     * minimum distance from the edge of a window when fitting a graph to
     * the window
     *
     * @todo should be tied to size of a node and sizes of labels somehow and
     * probably incorporated into the GraphPanel class
     */ 
    final static int WINDOW_PADDING = 50;
    
    /**
     * Offset to account for the fact that (0,0) is not a visible part of the
     * window.
     *
     * @todo This is actually more relevant for the top edge than the left
     * one and it also needs to take into account the message at the top
     * (which should eventually go into a separate window)
     */
    final static int WINDOW_OFFSET = 50;

    /**
     * minimum window width or height when scaling to fit window
     */
    final static int MIN_WINDOW_DIMENSION = 100;

	private GraphState currentGraphState;

    private String name;
    private String comment;
    private boolean layered = false;
    private LayerInformation layerInformation;
	
	private Map<Integer, String> messages;
	
	private List<Node> nodes;

    private TreeMap<Integer,Node> nodeById = new TreeMap<Integer,Node>();

	private List<Edge> edges;
	private Node rootNode;
	
	/**
     * used in step update for force-directed layout
     */
	private int progress;
	
	/**
	 * Default constructor.
	 */
	public Graph() {
		messages = new TreeMap<Integer, String>();
		currentGraphState = new GraphState();
		nodes = new ArrayList<Node>();
		edges = new ArrayList<Edge>();
	}
	
	/**
	 * Constructor to instantiate a directed graph with a given <code>List</code> of nodes and a given root <code>Node</code>
	 * @param directed 	true if the <code>Graph</code> is directed, false otherwise
	 * @param nodes 	a list of all of the nodes in the <code>Graph</code>
	 * @param rootNode	the root <code>Node</code> of the <code>Graph</code>
	 */
	public Graph(boolean directed, List<Node> nodes, Node rootNode) {
		this();
		
		currentGraphState.setDirected(directed);
		this.nodes = nodes;
		this.rootNode = rootNode;
	}

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
	 * Adds a new message to the current <code>GraphState</code>
	 * @param message the message to add
     * @todo writing a message should create a new graph state like anything
     * else; I guess this is awkward because of the map.
	 */
	public void writeMessage(String message) {
		int state = this.currentGraphState.getState();
		messages.put(state, message);
	}
	
	/**
	 * Gets all of the messages at the given state of the <code>GraphState</code>
	 * @param state the state from which to retrieve messages
	 * @return all of the messages at the given state.
	 */
	public String getMessage(int state) {
		return messages.get(state);
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
	 * @return true if the graph is directed, false otherwise
	 */
	public boolean isDirected() {
		return currentGraphState.isDirected();
	}

	/**
	 * @param directed true if setting the graph to directed, false if undirected
	 */
	public void setDirected(boolean directed) {
		currentGraphState.setDirected(directed);
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
	 * @return all <code>Edge</code>s in the <code>Graph</code> at the current <code>GraphState</code>
	 */
	public List<Edge> getEdges()
    {
		List<Edge> retEdges = new ArrayList<Edge>();
		
		for (Edge e : this.edges) {
			if (e.inScope()) {
				retEdges.add(e);
			}
		}
		
		return retEdges;
	}
	
	/**
	 * Gets all <code>Edge</code>s in the <code>Graph</code> at the current <code>GraphState</code>
	 * 
	 * @param state
	 * @return
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
	 * Does not increment the <code>GraphState</code>
	 * @param e the edge to remove
	 */
	public void deleteEdge(Edge e) {
		e.setDeleted(true);
	}
	
	/**
	 * Removes the specified <code>Node</code> from the <code>Graph</code>.
	 * Increments the <code>GraphState</code>.
	 * @param n
	 */
	public void deleteNode(Node n) {
		currentGraphState.incrementState();
		currentGraphState.setLocked(true);
		
		n.setDeleted(true);
		for (Edge e : n.getEdges()) {
			e.setDeleted(true);
		}
		
		currentGraphState.setLocked(false);
	}

	/**
	 * @return the root node
	 */
	public Node getRootNode()
//         throws GalantException
    {
//         if ( rootNode == null ) {
//             throw new GalantException( "no root node has been set"
//                                        + "\n - in getRootNode" );
//         }
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
//         throws GalantException
    {
//         if ( this.nodes.size() == 0 ) {
//             throw new GalantException( "empty graph"
//                                        + "\n - in getNodeById" );
//         }

// 		if ( ! nodeById.containsKey( id ) ) {
//             throw new GalantException( "no node with id = "
//                                        + id
//                                        + " exists"
//                                        + "\n - in getNodeById" );
//         }

        Node n = nodeById.get( id );

//         if ( n.isDeleted() ) {
//             throw new GalantException( "node has been deleted, id = "
//                                        + id
//                                        + "\n - in getNodeById" );
// 		}
		
		return n;
	}
	
	/**
	 * Provides the root <code>Node</code>. If none has been specified, the method returns the first <code>Node</code>
	 * in the <code>List</code> of <code>Node</code>s
	 * @return
	 */
	public Node getStartNode() {
		if (rootNode == null && this.nodes.size() > 0) {
			if (this.nodes.size() > 0) {
				Node n = this.nodes.get(0);
			
				currentGraphState.incrementState();
				currentGraphState.setLocked(true);
				n.setSelected(true);
				n.setVisited(true);
				currentGraphState.setLocked(false);
			
				return n;
			} else {
				return null;
			}
		} else {
			currentGraphState.incrementState();
			currentGraphState.setLocked(true);
			rootNode.setSelected(true);
			rootNode.setVisited(true);
			currentGraphState.setLocked(false);
			
			return rootNode;
		}
		
	}
	
	/**
	 * Returns the Edge in the graph represented by the given unique ID.
	 * 
	 * @param id
	 * @return the specified Edge if it exists, null otherwise
	 */
	public Edge getEdgeById(int id)
//         throws GalantException
    {
//         if ( this.edges.size() == 0 ) {
//             throw new GalantException( "graph has no edges"
//                                        + "\n - in getEdgeById" );
//         }

// 		if ( id < 0 || id >= this.nodes.size() ) {
//             throw new GalantException( "edge out of range, id = "
//                                        + id 
//                                        + "\n - in getEdgeById" );
//         }

        Edge e = this.edges.get(id);

//         if ( e.isDeleted() ) {
//             throw new GalantException( "edge has been deleted, id = "
//                                        + id
//                                        + "\n - in getEdgeById" );
// 		}

        return e;
	}

	/**
	 * Selects the <code>Node</code> with the specified ID and increments the <code>GraphState</code>
	 * @param id the ID of the <code>Node</code> to select
	 */
	public void select(int id) {
		currentGraphState.incrementState();
		currentGraphState.setLocked(true);
		for (Node n : nodes) {
			if (n.getId() == id) {
				n.setSelected(true);
			} else if (n.isSelected()) {
				n.setSelected(false);
			}
		}
		currentGraphState.setLocked(false);
	}
	
	/**
	 * Selects the given <code>Node</code> if it exists and increments the <code>GraphState</code>
	 * @param _n the <code>Node</code> to select
	 */
	public void select(Node _n) {
		currentGraphState.incrementState();
		currentGraphState.setLocked(true);
		for (Node n : nodes) {
			if (n.getId() == _n.getId()) {
				n.setSelected(true);
			} else if (n.isSelected()) {
				n.setSelected(false);
			}
		}
		currentGraphState.setLocked(false);
	}
	
    /**
     * @todo There is a time during which a newly added node does not have a
     * position. In an algorithm, this is a problem unless the adding of a
     * node and giving it a position is within the same step.
     */

	/**
	 * Adds a new <code>Node</code> to the <code>Graph</code> and increments the <code>GraphState</code>
	 * @return the added <code>Node</code>; called only during algorithm
	 * execution
	 */
	public Node addNode() {
        LogHelper.enterMethod( getClass(), "addNode()" );
		currentGraphState.incrementState();
        Integer newId = nextNodeId();
		Node n = new Node(currentGraphState, newId );
		nodes.add(n);
        nodeById.put( newId, n ); 
		
		if (this.rootNode == null) {
			this.rootNode = n;
		}
		
        LogHelper.exitMethod( getClass(), "addNode()" + n.toString() );
        LogHelper.restoreState();
		return n;
	}
	
	/**
	 * Adds a new <code>Node</code> to the <code>Graph</code> and sets the <code>GraphState</code> to 1.
	 * @return the added <code>Node</code>
	 */
	public Node addInitialNode() {
		int state = currentGraphState.getState();
		currentGraphState.setState(1);
		currentGraphState.setLocked(true);

        Integer newId = nextNodeId();
		Node n = new Node( currentGraphState, newId );
		nodes.add(n);
        nodeById.put( newId, n ); 

		currentGraphState.setLocked(false);
		currentGraphState.setState(state);
		
		return n;
	}
	
	/**
	 * Adds the specified <code>Node</code> and increments the <code>GraphState</code>
	 * @param n the <code>Node</code> to add
	 */
	public void addNode(Node n) {
        LogHelper.enterMethod( getClass(), "addNode: node = " + n.toString() );
		currentGraphState.incrementState();

        /**
         * @todo subclass method for layered graphs
         */
        if ( layered ) {
            layerInformation.addNode( n );
        }

		nodes.add(n);
        nodeById.put( n.getId(), n );

        LogHelper.exitMethod( getClass(), "addNode( Node )" );
        LogHelper.restoreState();
	}
	
	/**
	 * Adds the specified <code>Node</code> at the specified index and increments the <code>GraphState</code>
	 * @param n the <code>Node</code> to add
	 * @param index the index at which to add the <code>Node</code>
     * (does not appear to be used)
	 */
// 	public void addNode(Node n, int index) {
// 		currentGraphState.incrementState();
// 		nodes.add(index, n);
// 	}
	
	/**
	 * Adds a new <code>Edge</code> to the <code>Graph</code> with the specified source and target <code>Node</code> IDs and increments the <code>GraphState</code>
	 * Note: for undirected <code>Graph</code>s, "source" and "target" are meaningless.
	 * @param sourceId the ID of the source <code>Node</code>
	 * @param targetId the ID of the target <code>Node</code>
	 */
	public void addEdge(int sourceId, int targetId) {
		if (sourceId < nodes.size() && targetId < nodes.size()) {
			currentGraphState.incrementState();
			
			Node source = nodes.get(sourceId);
			Node target = nodes.get(targetId);
			
			Edge e = new Edge(currentGraphState, edges.size(), source, target);
			
			//TODO add edge to nodes
			
			edges.add(e);
		}
	}
	
	/**
	 * Adds a new <code>Edge</code> to the <code>Graph</code> with the specified source and target <code>Node</code>s and increments the <code>GraphState</code>
	 * Note: for undirected</code>Graph</code>s, "source" and "target" are meaningless.
	 * @param source the source <code>Node</code>
	 * @param target the target <code>Node</code>
	 * @return the <code>Edge</code> added
	 */
	public Edge addEdge(Node source, Node target) {	
		currentGraphState.incrementState();
		Edge e = new Edge(currentGraphState, edges.size(), source, target);
		
		source.addEdge(e);
		target.addEdge(e);
		
		edges.add(e);
		
		return e;
	}
	
	/**
	 * Adds the specified <code>Edge</code> to the <code>Graph</code> at the specified index.
	 * @param e the <code>Edge</code> to add
	 * @param index the index in the <code>List</code> of <code>Edge</code>s at which to add the <code>Edge</code>
	 * @return the added <code>Edge</code>
	 */
	public Edge addEdge(Edge e, int index) {
		currentGraphState.incrementState();
		edges.add(index, e);
		
		return e;
	}
	
	/**
	 * Adds a new <code>Edge</code> to the <code>Graph</code> with the specified source and target <code>Node</code>s.
	 * Sets the <code>GraphState</code> to 1.
	 * Note: for undirected <code>Graph</code>s, "source" and "target" are meaningless.
	 * @param source
	 * @param target
	 * @return
	 */
	public Edge addInitialEdge(Node source, Node target) {
		int state = currentGraphState.getState();
		currentGraphState.setState(1);
		currentGraphState.setLocked(true);

		Edge e = new Edge(currentGraphState, edges.size(), source, target);
		
		edges.add(e);
		source.addEdge(e);
		target.addEdge(e);
		
		currentGraphState.setLocked(false);
		currentGraphState.setState(state);
		
		return e;
	}
	
	/**
	 * Adds the specified <code>Edge</code> to the <code>Graph</code> and increments the <code>GraphState</code>
	 * @param e the <code>Edge</code> to add.
	 */
	public void addEdge(Edge e) {
		currentGraphState.incrementState();
		edges.add(e);
	}
	
	/**
	 * Removes the specified <code>Edge</code> from the <code>Graph</code>
	 * @param e the <code>Edge</code> to remove
	 */
	public void removeEdge(Edge e) {
		edges.remove(e);
		
		Node source = e.getSourceNode();
		source.getEdges().remove(e);		
		Node dest = e.getDestNode();
		dest.getEdges().remove(e);
	}
	
	/**
	 * Removes the specified <code>Node</code> from the <code>Graph</code>
	 * @param n the <code>Node</code> to remove
	 */
	public void removeNode(Node n) {
		List<Edge> n_edges = n.getEdges();
		
		currentGraphState.setLocked(true);
		while (n_edges.size() > 0) {
			Edge e = n_edges.remove(0);
			removeEdge(e);
		}

		nodes.remove(n);
		
		for (int i=0; i < this.nodes.size(); i++) {
			this.nodes.get(i).setId(i);
		}
		
		currentGraphState.setLocked(false);
	}
	
	/**
	 * @return an integer ID for the next <code>Node</code> to be
	 * added. This will always be the largest id so far + 1 
	 */
	private int nextNodeId() {
        if ( nodeById.isEmpty() ) return 0;
        else return nodeById.lastKey() + 1;
	}
	
	/**
	 * Provides the integer representation of the current <code>GraphState</code>
	 * 
	 * @return the current state of the <code>Graph</code>
	 */
	public int getState() {
		return currentGraphState.getState();
	}
	
	/**
	 * @return the current <code>GraphState</code>
	 */
	public GraphState getGraphState() {
		return currentGraphState;
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

    /**
     * changes positions of the nodes so that the width of the graph is
     * xScale times the current width and the height is yScale times the
     * current height
     *
     * @todo implement
     *    fitToWindow() to scale using current window width and height
     *    zoom( double factor ) to scale both width and height the same way
     *    not clear whether scale should be based on "original" positions or
     *    ones determined by algorithm
     */
    public void scale( double xScale, double yScale ) {
        for ( Node n: getNodes() ) {
            n.setFixedPosition( (int) (xScale * n.getX()),
                                (int) (yScale * n.getY()) );
        }
    }

    /**
     * Scales the node positions so that the rightmost node is close to the
     * right boundary of the window and the bottom-most node is close to the
     * bottom of the window. "Close to" is defined by the WINDOW_PADDING
     * constant.
     */
    public void fitWindow() {
        nudgeToEdge();
        scaleToWindow();
    }

	/**
	 * Moves all of the nodes to the top and left edges of the screen
	 */
	public void nudgeToEdge() {
		int x_least = Integer.MAX_VALUE;
		int y_least = Integer.MAX_VALUE;
		
		for ( Node node: getNodes() ) {
			x_least = (node.getX() < x_least) ? node.getX() : x_least;
			y_least = (node.getY() < y_least) ? node.getY() : y_least;
		}
		
		for ( Node node: getNodes() ) {
			// padding because 0,0 will be half off screen for a node
			node.setFixedPosition( node.getX() - x_least + WINDOW_OFFSET,
                                   node.getY() - y_least + WINDOW_OFFSET );
		}
	}
	
	/**
	 * Scales the graph to the current window size
	 */
	public void scaleToWindow() {
		int x_max = 0;
		int y_max = 0;
		
		int windowWidth = GraphDispatch.getInstance().getWindowWidth();
		int windowHeight = GraphDispatch.getInstance().getWindowHeight();
		int xScaleBase = windowWidth - WINDOW_PADDING;
        int yScaleBase = windowHeight - WINDOW_PADDING;
		
		xScaleBase = (xScaleBase < 0.0) ? MIN_WINDOW_DIMENSION : xScaleBase;
		yScaleBase = (yScaleBase < 0.0) ? MIN_WINDOW_DIMENSION : yScaleBase;
		
		for ( Node node: getNodes() ) {
			x_max = (node.getX() > x_max) ? node.getX() : x_max;
			y_max = (node.getY() > y_max) ? node.getY() : y_max;
		}
		
        scale( (double) xScaleBase / x_max, (double) yScaleBase / y_max );
	}
	
	/**
	 * Call the repositioning algorithm
	 * Currently the only supported algorithm is Force directed repositioning
	 * 
	 * @see edu.ncsu.csc.Galant.graph.component.Graph#forceDirected()
	 */
	public void smartReposition() {
		forceDirected();
	}
	
	/**
	 * Repositions the graph so that the nodes are arranged in an aesthetically pleasing way.
	 * 
	 * This only applies forces to connected subgraphs, otherwise disconnected pieces would repel each other nonstop
	 * 
	 * @see <a href="http://www.mathematica-journal.com/issue/v10i1/contents/graph_draw/graph_draw_3.html">Force-Directed Algorithms (2006); Hu, Yifan</a>

     * @todo would be good to have a way to undo this
	 */
	private void forceDirected() {
		
		if (nodes == null || nodes.size() == 0) {
			return;
		}
		
		// Stores the a new set of positions and a previous set to compare progress
		Point2D.Double[] points = new Point2D.Double[nodes.size()];
		Point2D.Double[] points_0 = new Point2D.Double[points.length];
		progress = 0;
		
		// initialize the starting points
		for (int i=0; i < nodes.size(); i++) {
			Point p = nodes.get(i).getPosition();
			points[i] = new Point2D.Double(p.x, p.y);
		}
		
		boolean cvg = false;
		double step = 1.0;
		double energy = 1000000000000000f; // very large number pretending to be infinity
		double c = 1.0; // scalar. won't make much difference since we rescale at the end anyway
		double k = 120.0; // natural spring (Edge) length
		double tol = .1; // the tolerance in change before the algorithm concludes itself
		double iterMax = 100000; // caps the number of repositioning iterations 
		
		int iter = 0;
		while (!cvg && iter < iterMax) {
			iter++;
			
			// copy your new points to your old points
			for (int i=0; i < points.length; i++) {
				points_0[i] = (Point2D.Double) points[i].clone();
			}
			
			// store the last energy of the graph. minimize this.
			double energy_0 = energy;
			
			// reset energy
			energy = 0.0;
			
			// loop through the Graph nodes and calculate new forces
			for (int i=0; i < points.length; i++) {
				double[] f = {0.0,0.0};
				
				// calculate attractive force of edges
				for (Edge e : edges) {
					int j = -1;
					if (e.getSourceNode().getId() == i) {
						j = e.getDestNode().getId();
					} else if (e.getDestNode().getId() == i) {
						j = e.getSourceNode().getId();
					}
					if (j != -1 && j != i) {
						double attractive = forceAttractive(points[i], points[j], k);
						double[] unitVector = unitVector(points[i], points[j]);
						f[0] += unitVector[0] * attractive;
						f[1] += unitVector[1] * attractive;
					}
				}
				
				// calculate repulsive force from other nodes
				for (int j=0; j < points.length; j++) {
					if (j != i && pathExists(i,j)) {
						double repulsive = forceRepulsive(points[i], points[j], c, k);
						double[] unitVector = unitVector(points[i], points[j]);
						f[0] += unitVector[0] * repulsive;
						f[1] += unitVector[1] * repulsive;
					}
				}
				
				// calculate new x position, scaling the force by a step size
				double x = points[i].getX();
				if (Math.abs(f[0]) > 0) {
					x += (step * f[0] / magnitude(f));
				}
				
				// calculate new y position, scaling the force by a step size
				double y = points[i].getY();
				if (Math.abs(f[1]) > 0) {
					y += (step * f[1] / magnitude(f));
				}
				points[i] = new Point2D.Double(x, y);
				
				// update the energy of this iteration
				energy += magnitude(f) * magnitude(f);
			}
			
			// update step length with adaptive cooling scheme
			step = updateStepLength(step, energy, energy_0);
			
			// check to see if we've converged
			if (totalChange(points, points_0) < tol) {
				cvg = true;
			}
		}
		
		// we've converged, now scale it and center it in the window
		points = centerInWindow(points);
		
		// update the nodes with their new positions and push to the display
		for (int i=0; i < this.nodes.size(); i++) {
			int x = (int) points[i].getX();
			int y = (int) points[i].getY();

			this.nodes.get(i).setFixedPosition( new Point(x,y) );
			
			GraphDispatch.getInstance().pushToGraphEditor();
		}
	}
	
	/**
	 * Centers the <code>Graph</code> to the center of the window, taking into account the current window size.
	 * 
	 * @param points the array of <code>Node</code> positions in the <code>Graph</code>
	 * @return the positions in the window of each <code>Node</code>
	 */
	private static Point2D.Double[] centerInWindow(Point2D.Double[] points) {
		points = nudgeToEdge(points);
		points = scaleToWindow(points);
		points = nudgeToCenter(points);

		return points;
	}
	
	/**
	 * Moves the <code>Node</code>s to the center of the window, taking into account the current window size.
	 * 
	 * @param points the array of <code>Node</code> positions in the <code>Graph</code>
	 * @returnthe positions in the window of each <code>Node</code>
	 */
	private static Point2D.Double[] nudgeToCenter(Point2D.Double[] points) {
		double x_least = points[0].x;
		double y_least = points[0].y;
		
		double x_most = points[0].x;
		double y_most = points[0].y;
		
		double windowWidth = GraphDispatch.getInstance().getWindowWidth();
		double windowHeight = GraphDispatch.getInstance().getWindowHeight();
		
		for (Point2D.Double p : points) {
			x_least = (p.x < x_least) ? p.x : x_least;
			x_most = (p.x > x_most) ? p.x : x_most;
			
			y_least = (p.y < y_least) ? p.y : y_least;
			y_most = (p.y > y_most) ? p.y : y_most;
		}
		
		double xPadding = (x_least) + (windowWidth - x_most);
		double yPadding = (y_least) + (windowHeight - y_most);
		
		xPadding = (xPadding / 2.0) - x_least;
		yPadding = (yPadding / 2.0) - y_least;
		
		for (Point2D.Double p : points) {
			p.setLocation(p.x + xPadding, p.y + yPadding);
		}
		
		return points;
	}
	
	/**
	 * Scales the <code>Graph</code> to the current window size
	 * @param points
	 * @return the positions on the window of each <code>Node</code>
	 */
	private static Point2D.Double[] scaleToWindow(Point2D.Double[] points) {
		double x_max = 0.0;
		double y_max = 0.0;
		
		double windowWidth = GraphDispatch.getInstance().getWindowWidth();
		double windowHeight = GraphDispatch.getInstance().getWindowHeight();
		double scaleBase = (windowWidth < windowHeight) ?
            windowWidth - WINDOW_PADDING :
            windowHeight - WINDOW_PADDING;
		
		if (scaleBase < 0.0) {
			scaleBase = 100.0;
		}
		
		for (Point2D.Double p : points) {
			x_max = (p.x > x_max) ? p.x : x_max;
			y_max = (p.y > y_max) ? p.y : y_max;
		}
		
		double scale = 1.0;
		
		if (y_max > x_max) {
			scale = scaleBase/y_max;
		} else {
			scale = scaleBase/x_max;
		}
		
		for (Point2D.Double p : points) {
			p.setLocation(p.x*scale, p.y*scale);
		}
		
		return points;
	}
	
	/**
	 * Moves all of the <code>Node</code>s to the edge of the screen
	 * @param points
	 * @return the position in the window of each <code>Node</code>
	 */
	private static Point2D.Double[] nudgeToEdge(Point2D.Double[] points) {
		double x_least = points[0].x;
		double y_least = points[0].y;
		
		for (Point2D.Double p : points) {
			x_least = (p.x < x_least) ? p.x : x_least;
			y_least = (p.y < y_least) ? p.y : y_least;
		}
		
		for (Point2D.Double p : points) {
			// padding because 0,0 will be half off screen for a node
			p.setLocation(p.x - x_least + WINDOW_OFFSET,
                          p.y - y_least + WINDOW_OFFSET );
		}
		
		return points;
	}
	
	/**
	 * Calculate spring force between two points based on natural spring length. Assumes
	 * there is an edge between p1 and p2.
	 * @param p1 The position of an edge endpoint
	 * @param p2 The position of the other edge endpoint
	 * @param k The natural spring length
	 * @return The attractive force between the two nodes
	 */
	private static double forceAttractive(Point2D p1, Point2D p2, double k) {
		return (p1.distance(p2)*p1.distance(p2)) / k;
	}
	
	/**
	 * Calculate the repulsive force between two nodes
	 * @param p1 The position of a node
	 * @param p2 The position of a second node
	 * @param c A scalar
	 * @param k The natural length
	 * @return The force between the two components
	 */
	private static double forceRepulsive(Point2D p1, Point2D p2, double c, double k) {
		return ( (-1*c) * k * k) / (p1.distance(p2)) ;
	}
	
	/**
	 * Returns a unit vector indicating the direction from source i to destination j
	 * @param i the source point
	 * @param j the destination point
	 * @return The unit vector towards your destination
	 */
	private static double[] unitVector(Point2D i, Point2D j) {
		double twoNorm = j.distance(i);
		double[] unit = { (j.getX() - i.getX()) / twoNorm, (j.getY() - i.getY()) / twoNorm};
		return unit;
	}
	
	/**
	 * Returns the magnitude of a vector
	 * @param vector the vector to calculate
	 * @return the magnitude of the vector
	 */
	private static double magnitude(double[] vector) {
		double sum = 0;
		for (int i=0; i < vector.length; i++) {
			sum += (vector[i]*vector[i]);
		}
		
		return Math.sqrt(sum);
	}
	
	/**
	 * Calculate the total change in energy between two states of positions
	 * @param x the old set of points
	 * @param x0 the new set of points
	 * @return the amount of change between the two sets
	 */
	private static double totalChange(Point2D[] x, Point2D[] x0) {
		if (x.length != x0.length) {
			return 0;
		}
		
		double totalChange = 0.0;
		for (int i=0; i < x.length; i++) {
			double dist = x[i].distance(x0[i]); 
			if (dist > 0)
				totalChange += x[i].distance(x0[i]);
		}
		
		return totalChange;
	}
	
	/**
	 * Adaptively update the step length to avoid settling into a local minimum
	 * @param step the current step size
	 * @param energy the previous energy
	 * @param energy_0 the new energy
	 * @return the new step length
	 */
	private double updateStepLength(double step, double energy, double energy_0) {
		double t = .9;
		
		if (energy < energy_0) {
			progress++;
			if (progress >= 5) {
				progress = 0;
				step /= t;
			}
		} else {
			progress = 0;
			step = t*step;
		}
		
		return step;
	}
	
	/**
	 * See if a path exists between two components. 
	 * @param i the index of the start node
	 * @param j the index of the end node
	 * @return true if a path exists
	 */
	private boolean pathExists(int i, int j) {
		List<Node> visited = new ArrayList<Node>();
		List<Node> frontier = new ArrayList<Node>();
		
		Node destination = nodes.get(j);
		
		Node n = nodes.get(i);
		visited.add(n);
		frontier.add(n);
		
		while (frontier.size() > 0) {
			Node front = frontier.remove(0);
			for (Edge e : front.getEdges()) {
				Node current = e.getSourceNode();
				if (current.equals(destination)) {
					return true;
				} else if (!visited.contains(current)) {
					visited.add(current);
					frontier.add(current);
				}
				
				current = e.getDestNode();
				if (current.equals(destination)) {
					return true;
				} else if (!visited.contains(current)) {
					visited.add(current);
					frontier.add(current);
				}
			}
		}
		
		return false;
	}
	
	@Override
	/**
	 * Returns a valid graphml representation of the <code>Graph</code>.
	 */
	public String toString() {
		String s = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n"; 
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
			s += "  " + n.toString() + "\n";
		}
		for(Edge e : this.edges) {
			s += "  " + e.toString() + "\n";
		}
		s += " </graph>";
		s += "</graphml>";
		return s;
	}
	
	public String toString(int state) {
		String s = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n"; 
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
			String sN = n.toString(state);
			if ( ! sN.trim().isEmpty() ) 
				s += "  " + sN + "\n";
		}
		for(Edge e : this.edges) {
			String sE = e.toString(state);
			if ( ! sE.trim().isEmpty() ) 
				s += "  " + sE + "\n";
		}
		s += " </graph>";
		s += "</graphml>";
		return s;
	}
}

//  [Last modified: 2015 05 26 at 12:04:35 GMT]
