/**
 * Provides access to all information about the graph, and maintains a list
 * of the states the graph can be in.
 * @see GraphState.java
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
	
	public GraphWindow graphWindow;

    /**
     * The items below are deprecated; all attributes are objects, so we
     * simply check for null.
     */
    public final static Double NOT_A_WEIGHT = Double.NaN;
    public final static String NOT_A_LABEL = "";

    /**
     * NOT_A_COLOR is used when no color is specified; when the GraphElement
     * is drawn on the panel NOT_A_COLOR is rendered as the default color
     * (black); in other situations (e.g., when converting to text), it may
     * be omitted
     */
    public final static String NOT_A_COLOR = "";

	private GraphState currentGraphState;

    private String name;
    private String comment;
    private boolean layered = false;
    private LayerInformation layerInformation;
	
	private Map<Integer, String> messages;
	
	private List<Node> nodes;

    private TreeMap<Integer,Node> nodeById = new TreeMap<Integer,Node>();

	private List<Edge> edges;
    /**
     * @todo Never clear what this meant. A better name might be startNode.
     */
	private Node rootNode;
	
    /**
     * An integer that can be used as the id of the next edge if id's are not
     * explicit in the input.
     */
    private int nextEdgeId =0;

	/**
	 * Default constructor.
	 */
	public Graph() {
		messages = new TreeMap<Integer, String>();
		currentGraphState = new GraphState();
		currentGraphState.setGraph(this);
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
        LogHelper.enterMethod(getClass(), "deleteNode " + n);
		currentGraphState.incrementState();
		currentGraphState.setLocked(true);
		
		n.setDeleted(true);
		for (Edge e : n.getIncidentEdges()) {
			e.setDeleted(true);
            removeEdge(e);
		}
		
		currentGraphState.setLocked(false);
        LogHelper.exitMethod(getClass(), "deleteNode");
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
	 * Adds a new <code>Node</code> to the <code>Graph</code> and increments
	 * the <code>GraphState</code>
     *
	 * @param x the x coordinate of the new node 
	 * @param y the y coordinate of the new node 
     * @return the added <code>Node</code>; called only during algorithm
	 * execution; the assumption here is that the algorithm has to "know" the
	 * position of the node it is adding.
	 */
	public Node addNode(Integer x, Integer y) {
        LogHelper.enterMethod( getClass(), "addNode(), x = " + x + ", y = " + y);
		currentGraphState.incrementState();
        Integer newId = nextNodeId();
		Node n = new Node(currentGraphState, newId, x, y);
		nodes.add(n);
        nodeById.put( newId, n ); 
		
		if (this.rootNode == null) {
			this.rootNode = n;
		}

        // seems like we need an addState call as is the case with state
        // changes in GraphElement.java

        LogHelper.exitMethod( getClass(), "addNode() " + n.toString() );
		return n;
	}
	
	/**
	 * Adds a node to the graph during editing. This is followed up by an
	 * addFixedPosition() call to establish a position, which may be
	 * determined by a mouse click or, in case of a Ctrl-n key press,
	 * randomly.
     *
	 * @return the added node
     *
     * @todo The locking and unlocking seems bizarre since we know there
     * should not be a state change. Looks like the node is being created in
     * the initial graph state and then the state is reset to whatever it
     * currently is -- not necessary if there are no state changes during
     * editing (?)
	 */
	public Node addInitialNode() {
		int state = currentGraphState.getState();
		currentGraphState.setState(GraphState.GRAPH_START_STATE);
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
	 * Adds a node to the graph during parsing. The node has already been created.
 	 */
	public void addNode(Node n) {
        LogHelper.enterMethod( getClass(), "addNode: node = " + n.toString() );

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
	 * Adds a new <code>Edge</code> to the <code>Graph</code> with the specified source and target <code>Node</code> IDs and increments the <code>GraphState</code>
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
	 * Adds a new <code>Edge</code> to the <code>Graph</code> with the specified source and target <code>Node</code>s and increments the <code>GraphState</code>
	 * Note: for undirected</code>Graph</code>s, "source" and "target" are meaningless.
	 * @param source the source <code>Node</code>
	 * @param target the target <code>Node</code>
	 * @return the <code>Edge</code> added
     *
     * This variant is used during algorithm execution if the actual nodes
     * are known.
	 */
	public Edge addEdge(Node source, Node target) {	
		currentGraphState.incrementState();
        int id = edges.size();
		Edge e = new Edge(currentGraphState, id, source, target);
        addEdge(e, id);
		return e;
	}
	
	/**
	 * Adds a new <code>Edge</code> to the <code>Graph</code> with the specified source and target <code>Node</code>s and increments the <code>GraphState</code>
	 * Note: for undirected</code>Graph</code>s, "source" and "target" are meaningless.
	 * @param source the source <code>Node</code>
	 * @param target the target <code>Node</code>
	 * @return the <code>Edge</code> added
     *
     * This variant is used during algorithm execution when only the id's are known.
	 */
	public Edge addEdge(int sourceId, int targetId) {	
        return addEdge(getNodeById(sourceId), getNodeById(targetId));
	}
	
	/**
	 * Adds a new <code>Edge</code> to the <code>Graph</code> with the specified source and target <code>Node</code>s.
	 * Sets the <code>GraphState</code> to 1.
	 * Note: for undirected <code>Graph</code>s, "source" and "target" are meaningless.
	 * @return the added edge
     *
     * @todo Again, the locking of states is puzzling since this variant is
     * used only during editing.
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
	 * Removes the specified <code>Edge</code> from the <code>Graph</code>
	 * @param e the <code>Edge</code> to remove
	 */
	public void removeEdge(Edge e) {
        LogHelper.enterMethod(getClass(), "removeEdge " + e);
		edges.remove(e);
		
		Node source = e.getSourceNode();
		source.getIncidentEdges().remove(e);		
		Node dest = e.getTargetNode();
		dest.getIncidentEdges().remove(e);
        LogHelper.exitMethod(getClass(), "removeEdge");
	}
	
	/**
	 * Removes the specified <code>Node</code> from the <code>Graph</code>
	 * @param n the <code>Node</code> to remove
	 */
	public void removeNode(Node n) {
		List<Edge> n_edges = n.getIncidentEdges();
        LogHelper.enterMethod(getClass(), "removeNode " + n + ", deg = " + n_edges.size());
		
		currentGraphState.setLocked(true);
		for ( Edge e : n_edges ) {
            //			n_edges.remove(e);
			removeEdge(e);
		}

		nodes.remove(n);
		currentGraphState.setLocked(false);
        LogHelper.exitMethod(getClass(), "removeNode");
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
	 * Call the repositioning algorithm
	 * Currently the only supported algorithm is Force directed repositioning
	 * 
	 * @see edu.ncsu.csc.Galant.graph.component.GraphLayout#forceDirected()
	 */
	public void smartReposition() {
        GraphLayout graphLayout = new GraphLayout( this );
		graphLayout.forceDirected();
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

//  [Last modified: 2015 08 11 at 19:37:05 GMT]
