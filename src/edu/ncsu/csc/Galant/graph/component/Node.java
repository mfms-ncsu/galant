package edu.ncsu.csc.Galant.graph.component;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import edu.ncsu.csc.Galant.logging.LogHelper;
import edu.ncsu.csc.Galant.GraphDispatch;
import edu.ncsu.csc.Galant.GalantException;

/**
 * Represents node entities as elements of a graph.
 * 
 * @author Michael Owoc
 * @author Ty Devries
 *
 */
public class Node extends GraphElement implements Comparable<Node> {
	
	private Map<String, Object> attributes;
	
	private GraphState graphCurrentState;
	private List<NodeState> nodeStates;
	private List<Edge> edges;
	
	private Point position;
	
	public Node(GraphState currentState, int id) {
		this.graphCurrentState = currentState;
		nodeStates = new ArrayList<NodeState>();
		attributes = new HashMap<String, Object>();
		
		this.position = genRandomPosition();
		
		NodeState ns = new NodeState(currentState, id);
		addNodeState(ns);
		
		edges = new ArrayList<Edge>();
	}
	
	public Node(GraphState currentState, int _id, double _weight, String _color, String _label, boolean _selected, boolean _visited) {
		this(currentState, _selected, _visited, new ArrayList<Edge>(), _id, _weight, _color, _label, genRandomPosition());
	}

	public Node(GraphState currentState, boolean _selected, boolean _visited, List<Edge> _edges, int _id, double _weight, String _color, String _label, Point _position) { //TODO sanitize
        LogHelper.enterConstructor( getClass(), "not layered" );
		this.graphCurrentState = currentState;
		nodeStates = new ArrayList<NodeState>();
		attributes = new HashMap<String, Object>();
		
		this.position = _position;
		
		NodeState ns = new NodeState(currentState, _selected, _visited, _id, _weight, _color, _label, _position);
		addNodeState(ns);
		
		edges = _edges;

        LogHelper.exitConstructor( getClass(), "ns = " + ns );
	}

    /**
     * Constructor for layered graphs
     */
	public Node(GraphState currentState, boolean _selected, boolean _visited, int _id, double _weight, String _color, String _label, int _layer, int _positionInLayer) { //TODO sanitize
        LogHelper.enterConstructor( getClass(), "layered" );
		this.graphCurrentState = currentState;
		nodeStates = new ArrayList<NodeState>();
		attributes = new HashMap<String, Object>();

        /**
         * @todo The following is just a hack to give the node <em>some</em>
         * fixed position intially. The actual position will be determined
         * later by window dimensions.
         */
        position = new Point( positionInLayer * 100 + 50, layer * 100 + 50 );
		
		NodeState ns = new NodeState(currentState, _selected, _visited, _id, _weight, _color, _label, _layer, _positionInLayer);
		addNodeState(ns);

        edges = new ArrayList<Edge>();
        LogHelper.exitConstructor( getClass(), "ns = " + ns );
	}

	public boolean inScope() {
		return !isDeleted();
	}
	
	public boolean inScope(int state) 
    {
		return isCreated(state) && !isDeleted(state);
	}
	
	/**
	 * @return the weight of the edge
	 */
	@Override
	public double getWeight() {
		return latestState().getWeight();
	}
	
	@Override
	public double getWeight(int state)
        throws GalantException
        {
            NodeState ns = getLatestValidState(state);
            return ns==null ? null : ns.getWeight();
        }

	/**
	 * @param weight the weight of the edge
	 */
	@Override
	public void setWeight(double weight) {
		NodeState ns = newState();
		ns.setWeight(weight);
		nodeStates.add(ns);
	}

    /**
     * @return true if the node has a weight in the current state
     */
    public boolean hasWeight() {
        return latestState().hasWeight();
    }

    /**
     * @return true if this node had a non-empty weight at the given state
     */
    @Override
        public boolean hasWeight(int state)
        {
            NodeState ns = getLatestValidState(state);
            return ns == null ? false : ns.hasWeight();
        }

	/**
	 * Postcondition: hasWeight() == false
	 */
	@Override
        public void clearWeight() {
		NodeState ns = newState();
		ns.clearWeight();
		addNodeState(ns);
	}

	/**
	 * @return true if the node is highlighted, false otherwise
	 */
	public boolean isSelected() {
		return latestState().isSelected();
	}
	
	public boolean isSelected(int state)
        throws GalantException
    {
		NodeState ns = getLatestValidState(state);
		
		return ns==null ? null : ns.isSelected();
	}

    public boolean isHighlighted() {
        return isSelected();
    }
	
    public boolean isHighlighted(int state)
        throws GalantException
    {
        return isSelected(state);
    }
	
	public boolean isVisited() {
		return this.latestState().isVisited();
	}

	public Boolean isVisited(int state)
        throws GalantException
    {
		NodeState ns = getLatestValidState(state);
		return ns==null ? null : ns.isVisited();
	}
	
	public boolean isMarked() {
		return this.latestState().isVisited();
	}

	public Boolean isMarked(int state)
        throws GalantException
    {
		NodeState ns = getLatestValidState(state);
		return ns==null ? null : ns.isVisited();
	}

	public void setVisited(boolean b) {
		NodeState ns = newState();
		ns.setVisited(b);
		addNodeState(ns);
	}

	/**
	 * sets the latest state of the node to marked without adding a new
	 * state; synonym for setVisited(true)
	 */
	public void mark() {
		NodeState ns = newState();
		ns.setVisited(true);
		addNodeState(ns);
	}
	
	/**
	 * sets the latest state of the node to unmarked without adding a new
	 * state; synonym for setVisited(false)
	 */
	public void unMark() {
		NodeState ns = newState();
		ns.setVisited(false);
		addNodeState(ns);
	}
	
	/**
	 * @return the node's outgoing edges, based on source and target
	 * specs. ignoring whether the graph is directed or not
	 */
	public List<Edge> getOutgoingEdges() {
		List<Edge> currentEdges = new ArrayList<Edge>();
		
		for ( Edge e : edges ) {
			if ( e.inScope() && ! e.isDeleted() ) {
				if ( this.equals( e.getSourceNode() ) 
                    || ! graphCurrentState.isDirected() ) {
					currentEdges.add(e);
				}
			}
		}
		
		return currentEdges;
	}
	
	public List<Edge> getOutgoingEdges(int state) {
		return getOutgoingEdges();
	}
	
	/**
	 * @return the node's incoming edges, based on source and target
	 * specs; if the graph is undirected, all edges are incoming
	 */
	public List<Edge> getIncomingEdges() {
		List<Edge> currentEdges = new ArrayList<Edge>();
		
		for ( Edge e : edges ) {
			if ( e.inScope() && ! e.isDeleted() ) {
				if ( this.equals( e.getDestNode() )
                     || ! graphCurrentState.isDirected() ) {
					currentEdges.add( e );
				}
			}
		}
		
		return currentEdges;
	}
	
	public List<Edge> getIncomingEdges(int state) {
		return getIncomingEdges();
	}
	
    /**
     * @return a list of edges incident on this node regardless of whether
     * they are incoming or outgoing.
     */
    public List<Edge> getIncidentEdges() {
 		List<Edge> currentEdges = new ArrayList<Edge>();
		
		for ( Edge e : edges ) {
			if ( e.inScope() && ! e.isDeleted() ) {
                currentEdges.add(e);
            }
        }
		return currentEdges;
    }

	public List<Edge> getIncidentEdges(int state) {
		return getIncidentEdges();
	}
	
    /**
     * The following methods use the edge list getters to return degrees
     */
    public int getOutdegree() { return getOutgoingEdges().size(); }
    public int getOutdegree(int state) { return getOutdegree(); }
    public int getIndegree() { return getIncomingEdges().size(); }
    public int getIndegree(int state) { return getIndegree(); }
    public int getDegree() { return getIncidentEdges().size(); }
    public int getDegree(int state) { return getDegree(); }

	/**
	 * Gets a list of Edges incident to this node whose visited flag
	 * is set to false.
	 */
	public List<Edge> getUnvisitedPaths() {
		List<Edge> unvisited = new ArrayList<Edge>();
		for (Edge e : edges) {
			if (!e.inScope() || e.isDeleted()) {
				continue;
			}
			
			Node source = e.getSourceNode();
			Node dest = e.getDestNode();
			Node adjacent;
			if (source.getId() == this.getId()) {
				adjacent = dest;
			} else {
				if (graphCurrentState.isDirected()) continue;
				adjacent = source;
			}
			
			if (!adjacent.isVisited()) {
				unvisited.add(e);
			}
		}
		
		return unvisited;
	}
	
	/**
	 * Gets a list of Edges incident to this node whose visited flag
	 * is set to true.
	 */
	public List<Edge> getVisitedPaths() {
		List<Edge> visited = new ArrayList<Edge>();
		for (Edge e : edges) {
			if (!e.inScope() || e.isDeleted()) {
				continue;
			}
			
			Node source = e.getSourceNode();
			Node dest = e.getDestNode();
			Node adjacent;
			if (source.getId() == this.getId()) {
				adjacent = dest;
			} else {
				if (graphCurrentState.isDirected()) continue;
				adjacent = source;
			}
			
			if (adjacent.isVisited()) {
				visited.add(e);
			}
		}
		
		return visited;
	}
	
	public List<Node> getUnvisitedAdjacentNodes() {
		List<Node> nodes = new ArrayList<Node>();
		for (Edge e : edges) {
			if (!e.inScope() || e.isDeleted()) {
				continue;
			}
			
			Node source = e.getSourceNode();
			Node dest = e.getDestNode();
			Node adjacent;
			if (source.getId() == this.getId()) {
				adjacent = dest;
			} else {
				if (graphCurrentState.isDirected()) continue;
				adjacent = source;
			}
			
			if (!adjacent.isVisited()) {
				nodes.add(adjacent);
			}
		}
		
		return nodes;
	}
	
	
	/**
	 * Returns the adjacent node along a given incident edge.
	 * 
	 * If the edge is not incident to this node, then null is returned
	 * 
	 * Compares the nodes of the edge to This and returns the other one.
	 * 
	 * No issue on a self loop: will find that the first Node is This 
	 * and return the other
	 * 
	 * @param e
	 */
	public Node travel(Edge e) {
		if (e.getSourceNode().equals(this)) {
			return e.getDestNode();
		} else if (e.getDestNode().equals(this)){
			return e.getSourceNode();
		}
		
		return null;
	}

	/**
	 * @param selected toggles highlighting on the node
	 */
	public void setSelected(boolean selected) {
		NodeState ns = newState();
		ns.setSelected(selected);
		addNodeState(ns);
	}

    public void highlight() {
        setSelected( true );
    }

    public void unHighlight() {
        setSelected( false );
    }

	public List<Edge> getEdges() {
		return this.edges;
	}
	
	public void addEdge(Edge _edge) {
		edges.add(_edge);
	}

	/**
	 * @param edges the edges to set
	 */
	public void setEdges(List<Edge> edges) {
		this.edges = edges;
	}

	/**
	 * @return the unique ID of the node
	 */
	public int getId() {
		return latestState().getId();
	}
	
	public void setId(int id) {
		NodeState ns = newState();
		ns.setId(id);
		addNodeState(ns);
	}

	/**
	 * @return the color of the node stored in six-digit hex representation
	 */
	@Override
	public String getColor() {
		return latestState().getColor();
	}
	
	@Override
	public String getColor(int state)
        {
            NodeState ns = getLatestValidState(state);
            return ns==null ? null : ns.getColor();
        }

	/**
	 * @param color the color of the node to set, stored in six-digit hex representation
	 */
	@Override
	public void setColor(String color) {
		NodeState ns = newState();
		ns.setColor(color);
		addNodeState(ns);
	}

	/**
	 * @return the label
	 */
	@Override
	public String getLabel() {
		return latestState().getLabel();
	}

	@Override
	public String getLabel(int state)
        {
            NodeState ns = getLatestValidState(state);
            return ns==null ? null : ns.getLabel();
        }

	/**
	 * @param label the label to set
	 */
	@Override
	public void setLabel(String label) {
		NodeState ns = newState();
		ns.setLabel(label);
		addNodeState(ns);
	}

    /**
     * @return true if the node has a label in the current state
     */
    public boolean hasLabel() {
        return latestState().hasLabel();
    }

    /**
     * @return true if this node had a non-empty label at the given state
     */
    @Override
        public boolean hasLabel(int state)
        {
            NodeState ns = getLatestValidState(state);
            return ns == null ? false : ns.hasLabel();
        }

	/**
	 * Postcondition: hasLabel() == false
	 */
	@Override
        public void clearLabel() {
		NodeState ns = newState();
		ns.clearLabel();
		addNodeState(ns);
	}
	
	public boolean isCreated(int state)
    {
		NodeState ns = getLatestValidState(state);
		return (ns != null);
	}
	
	public boolean isDeleted() {
		return latestState().isDeleted();
	}
	
	public boolean isDeleted(int state)
    {
		NodeState ns = getLatestValidState(state);
		return ns==null ? false : ns.isDeleted();
	}

	public void setDeleted(boolean deleted) {
		NodeState ns = newState();
		ns.setDeleted(deleted);
		addNodeState(ns);
	}
	
    /**
     * The methods getPosition() and setPosition() are intended for use by
     * the creator of an algorithm to, for example, exchange positions of
     * nodes during sorting or a crossing minimization algorithm. Once
     * setPosition() is invoked for a node it is no longer possible for the
     * user to change the position of that node via mouse dragging.
     */
	
	public Point getPosition() {
		return latestState().getPosition();
	}

	public Point getPosition(int state)
        throws GalantException
    {
		NodeState ns = getLatestValidState(state);
		return ns==null ? null : ns.getPosition();
    }

	public int getX() {
		return latestState().getPosition().x;
	}

	public int getX(int state)
        throws GalantException
    {
		NodeState ns = getLatestValidState(state);
		return ns==null ? null : ns.getPosition().x;
    }

	public int getY() {
		return latestState().getPosition().y;
	}

	public int getY(int state)
        throws GalantException
    {
		NodeState ns = getLatestValidState(state);
		return ns==null ? null : ns.getPosition().y;
    }

	public void setPosition(Point position) {
        LogHelper.enterMethod( getClass(), "setPosition: " + position 
                               + "\n node = " + this );
		NodeState ns = newState();
        ns.setPosition( position );
        addNodeState(ns);
        LogHelper.exitMethod( getClass(), "setPosition"
                              + "\n node = " + this );
	}
	
	public void setPosition(int x, int y) {
		NodeState ns = newState();
        ns.setPosition( new Point( x, y ) );
        addNodeState(ns);
 	}

    public void setX( int x ) {
		NodeState ns = newState();
        ns.setX( x );
        addNodeState(ns);
    }        
	
    public void setY( int y ) {
		NodeState ns = newState();
        ns.setY( y );
        addNodeState(ns);
    }        
	
    /**
     * The fixed versions, getFixedPosition() and setFixedPosition() are used
     * within the Galant software to access/modify positions of nodes during
     * parsing of GraphML input, in response to mouse dragging, or when the
     * force directed heuristic is applied to reposition nodes. During
     * execution of an algorithm, a position set by the algorithm takes
     * precedence, i.e., a node moved by the algorithm can no longer be moved
     * via dragging. However, any node that is *not* moved by the algorithm
     * *can* still be moved by the user.
     *
     * The main issue is that it is desirable not to have a state change when
     * the position of a node is modified during editing.
     *
     * @todo There needs to be a more elegant way to handle this so that, if
     * an algorithm does not move nodes, the user can move them during
     * execution.  
     */

    public Point getFixedPosition() {
        return position;
    }

	public void setFixedPosition(Point position) {
        LogHelper.enterMethod( getClass(), "setFixedPosition: " + position 
                               + "\n node = " + this );
		this.position = position;
        getInitialState().setPosition( position );
        LogHelper.exitMethod( getClass(), "setFixedPosition"
                              + "\n node = " + this );
	}
	
	public void setFixedPosition(int x, int y) {
		Point p = new Point(x, y);
		this.position = p;
        getInitialState().setPosition( p );
	}
	
    /**
     * The following have been added for layered graphs. The value -1 is used
     * to denote that the attribute has not been set.
     * @todo See comment in GraphMLParser for more
     */
    private int layer = -1;
    private int positionInLayer = -1;

    public int getLayer() { 
        return latestState().getLayer();
    }
    public int getLayer( int state )
    { 
		NodeState ns = getLatestValidState(state);
		return ns==null ? null : ns.getLayer();
    }
    public int getPositionInLayer() {
        return latestState().getPositionInLayer();
    }
    public int getPositionInLayer( int state )
    { 
		NodeState ns = getLatestValidState(state);
		return ns==null ? null : ns.getPositionInLayer();
    }
    public void setLayer( int layer ) {
		NodeState ns = newState();
        ns.setLayer( layer );
        addNodeState(ns);
    }
    public void setPositionInLayer( int positionInLayer ) { 
		NodeState ns = newState();
        ns.setPositionInLayer( positionInLayer );
        addNodeState(ns);
    }

    /**
     * Thefollowing allow the addition of arbitrary logical attributes for
     * nodes and edges.
     */
	public void setStringAttribute(String key, String value) {
		attributes.put(key, value);
	}
	public String getStringAttribute(String key) {
		Object o = attributes.get(key);
		
		if (o != null && String.class.isInstance(o)) {
			return (String) o;
		}
		
		return null;
	}
	
	public void setIntegerAttribute(String key, Integer value) {
		attributes.put(key, value);
	}
	public Integer getIntegerAttribute(String key) {
		Object o = attributes.get(key);
		
		if (o != null && Integer.class.isInstance(o)) {
			return (Integer) o;
		}
		
		return null;
	}
	
	public void setDoubleAttribute(String key, Double value) {
		attributes.put(key, value);
	}
	public Double getDoubleAttribute(String key) {
		Object o = attributes.get(key);
		
		if (o != null && Double.class.isInstance(o)) {
			return (Double) o;
		}
		
		return null;
	}
	
	private NodeState newState() {
		graphCurrentState.incrementState();
		NodeState latest = latestState();
		NodeState ns = new NodeState ( latest, this.graphCurrentState );
		
		return ns;
	}

    private NodeState getInitialState() {
        return nodeStates.get(0);
    }
	
	private NodeState latestState() {
		return nodeStates.get(nodeStates.size()-1);
	}
	
    /**
     * This method is vital for retrieving the most recent information about
     * a node, where most recent is defined relative to a given time stamp,
     * as defined by forward and backward stepping through the animation.
     * @param stateNumber the numerical indicator (timestamp) of a state,
     * usually the current one in the animation
     * @return the latest instance of NodeState that was created before the
     * given time stamp, or null if the node did not exist before the time
     * stamp.
     */
	public NodeState getLatestValidState( int stateNumber )
    {
		for ( int i = nodeStates.size() - 1; i >= 0; i-- ) {
			NodeState ns = nodeStates.get(i);
			if ( ns.getState() <= stateNumber ) {
				return ns;
			}
		}
		
        return null;
	}
	
	
	private void addNodeState(NodeState _ns) {
		for (int i=nodeStates.size()-1; i >= 0; i--) {
			NodeState ns = nodeStates.get(i);
			if (ns.getState() == _ns.getState()) {
				nodeStates.set(i, _ns);
				return;
			}
		}
		
		nodeStates.add(_ns);
	}
	
	public static Point genRandomPosition() {
		Random r = new Random();
		int x = r.nextInt( GraphDispatch.getInstance().getWindowWidth() );
		int y = r.nextInt( GraphDispatch.getInstance().getWindowHeight() );
		return new Point(x,y);
	}
	

    /**
     * This version is called when the graph window editor pushes changes to
     * the text editor and at various other points.
     *
     * @todo perhaps need some way to distinguish between the text window
     * function and others such as debugging
     *
     * @todo both toString() methods need to be fixed so that they write only
     * the attributes that are actually present.
     */
	@Override
	public String toString() {
		double weight = this.getWeight();
		String label = "";
		if (this.getLabel() != null) {
			label = this.getLabel();
		}
		
		String s = "<node"
            + " id=\"" + this.getId() + "\""
            + " weight=\"" + weight + "\""
            + " label=\"" + label + "\""
            + " x=\"" + this.position.x + "\""
            + " y=\"" + this.position.y + "\""
            + " color=\"" + this.getColor() + "\"";
        /**
         * @todo Need to decide whether to include ...
         *    + "\" highlighted=\"" + this.isSelected() + "\" />";
         * It might make a lot of sense for exports
         */
        if ( GraphDispatch.getInstance().getWorkingGraph().isLayered() ) {
            s = s 
                + " layer=\"" + this.getLayer() + "\""
                + " positionInLayer=\"" + this.getPositionInLayer() + "\"";
        }
        s += " />";
		return s;
	}
	
    /**
     * This version is called when the current state of the animation is
     * exported.
     */
	public String toString(int state)
    {
        if ( ! inScope(state) ) {
            return "";
        }

        NodeState ns = getLatestValidState( state );

        String s = "<node" + " id=\"" + this.getId() + "\""
            + " weight=\"" + ns.getWeight() + "\""
            + " label=\"" + ns.getLabel() + "\""
            + " x=\"" + ns.getPosition().x + "\""
            + " y=\"" + ns.getPosition().y + "\""
            + " color=\"" + ns.getColor() + "\"";
        
        if ( GraphDispatch.getInstance().getWorkingGraph().isLayered() )
            s = s 
                + " layer=\"" + ns.getLayer() + "\""
                + " positionInLayer=\"" + ns.getPositionInLayer() + "\"";
        s += " />";
        return s;
	}

	@Override
	public int compareTo(Node n) {
        Double thisDouble = new Double( this.getWeight() );
        Double otherDouble = new Double( n.getWeight() );
		return thisDouble.compareTo( otherDouble );
	}
}

//  [Last modified: 2015 05 21 at 18:52:52 GMT]
