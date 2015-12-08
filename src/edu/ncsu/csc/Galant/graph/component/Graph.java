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
import edu.ncsu.csc.Galant.algorithm.Terminate;
import edu.ncsu.csc.Galant.logging.LogHelper;

/**
 * Stores information peculiar to layered graphs.
 *
 * @todo LayeredGraph clearly needs to be a subclass of Graph and
 * LayeredGraphNode a subclass of Node.
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
 * Stores all nodes, edges and related information for use in graph algorithms.
 * 
 * @author Michael Owoc, Ty Devries (major modifications by Matt Stallmann)
 */
public class Graph {
	
	public GraphWindow graphWindow;

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


    private GraphDispatch dispatch;

	/**
	 * Default constructor.
	 */
	public Graph() {
        dispatch = GraphDispatch.getInstance();
		nodes = new ArrayList<Node>();
		edges = new ArrayList<Edge>();
        banner = new MessageBanner(this);
	}

    /**
     * Resets the graph to its original state at the end of an
     * animation.
     */
    public void reset() {
        for ( Node node : this.nodes ) {
            node.reset();
        }
        for ( Edge edge : this.edges ) {
            edge.reset();
        }
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
	 * Highlights the node with the specified id and starts an algorithm step if
	 * appropriate
	 */
	public void select(int id) throws Terminate {
		dispatch.startStepIfRunning();
		dispatch.lockIfRunning();
		for (Node n : nodes) {
			if (n.getId() == id) {
				n.setSelected(true);
			} else if (n.isSelected()) {
				n.setSelected(false);
			}
		}
		dispatch.unlockIfRunning();
	}
	
	/**
	 * Highlights the given node and starts an algorithm step if appropriate
	 */
	public void select(Node toHighlight) throws Terminate {
        this.select(toHighlight.getId());
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
	public Edge addEdge(int sourceId, int targetId) throws Terminate {	
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
		
		dispatch.lockIfRunning();
		for ( Edge e : n_edges ) {
            //			n_edges.remove(e);
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
	
	/**
	 * Returns a valid graphml representation of the <code>Graph</code>.
	 */
	public String xmlString() {
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
		return s;
	}
	
	public String xmlString(int state) {
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
			String sN = n.xmlString(state);
			if ( ! sN.trim().isEmpty() ) 
				s += "  " + sN + "\n";
		}
		for(Edge e : this.edges) {
			String sE = e.xmlString(state);
			if ( ! sE.trim().isEmpty() ) 
				s += "  " + sE + "\n";
		}
		s += " </graph>";
		s += "</graphml>";
		return s;
	}
}

//  [Last modified: 2015 12 08 at 16:08:44 GMT]
