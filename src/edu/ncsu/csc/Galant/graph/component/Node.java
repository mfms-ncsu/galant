package edu.ncsu.csc.Galant.graph.component;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import edu.ncsu.csc.Galant.GalantException;
import edu.ncsu.csc.Galant.GraphDispatch;
import edu.ncsu.csc.Galant.algorithm.Terminate;
import edu.ncsu.csc.Galant.logging.LogHelper;

/**
 * Represents node entities as elements of a graph. Encapsulates attributes
 * that are unique to nodes.
 * 
 * @author Michael Owoc
 * @author Ty Devries
 * @author Matthias Stallmann, major modifications
 *
 * a major refactoring by Matthias Stallmann, based on a more extended
 * version of the GraphElement class.
 */
public class Node extends GraphElement implements Comparable<Node> {
    private static final String MARKED = "marked";

    private int id;
    private int xCoordinate;
    private int yCoordinate;
	private List<Edge> incidentEdges;

    /**
     * When a node is created during parsing and id is not known.
     */
	public Node(Graph graph) {
        super(graph);
		incidentEdges = new ArrayList<Edge>();
	}

    /**
     * @param id is the next available id as determined by the graph.
     * @todo not clear that this is used anywhere
     */
    public Node(Graph graph, int id) {
        super(graph);
        this.id = id;
		incidentEdges = new ArrayList<Edge>();
    }

    /**
     * To add a node during editing or algorithm execution:
     * - id is the next available one as determined by the graph
     * - the position of the node is known to the algorithm, fixed unless the
     * algorithm moves nodes
     */
    public Node(Graph graph, int id, Integer x, Integer y) {
        super(graph);
        LogHelper.logDebug("-> Node, id = " + id + ", x =" + x + ", y =" + y);
        this.id = id;
		incidentEdges = new ArrayList<Edge>();
        xCoordinate = x;
        yCoordinate = y;
        // set starting position based on the initial one
        if ( GraphDispatch.getInstance().algorithmMovesNodes() ) { 
            GraphElementState startingState = latestState();
            startingState.set("x", x);
            startingState.set("y", y);
        }
        else {
            this.xCoordinate = x;
            this.yCoordinate = y;
        }
        LogHelper.logDebug("<- Node, node = " + this);
   }

    /**
     * Setters and getters for node-specific information that does not change.
     */
    public Integer getId() { return id; }

    public void setIncidentEdges(List<Edge> edges) {
        this.incidentEdges = edges;
    }

    /**
     * Setters and getters for node-specific information that may change
     * during algorithm execution.
     */
    public Integer getX() {
        return super.getInteger("x");
    }
    public Integer getY() {
        return super.getInteger("y");
    }
    public Integer getX(int state) {
        return super.getInteger(state, "x");
    }
    public Integer getY(int state) {
        return super.getInteger(state, "y");
    }
    public Point getPosition() {
        return new Point(getX(), getY());
    }
    public Point getPosition(int state) {
        LogHelper.enterMethod(getClass(), "getPosition, state = " + state + ", node = " + this);
        Point p = new Point(getX(state), getY(state));
        LogHelper.exitMethod(getClass(), "getPosition, point = " + p);
        return p;
    }

    public void setX(Integer x) throws Terminate { super.set("x", x); }
    public void setY(Integer y) throws Terminate { super.set("y", y); }
    public void setPosition(Integer x, Integer y) throws Terminate {
        LogHelper.enterMethod(getClass(), "setPosition, x = " + x + ", y = " + y);
        setX(x);
        setY(y);
        LogHelper.exitMethod(getClass(),
                             "setPosition, node = "
                             + this.xmlString(dispatch.getAlgorithmState()));
    }
    public void setPosition(Point point) throws Terminate {
        LogHelper.enterMethod(getClass(), "setPosition, point = " + point);
        setX(point.x);
        setY(point.y);
        LogHelper.exitMethod(getClass(),
                             "setPosition, node = "
                             + this.xmlString(dispatch.getAlgorithmState()));
    }

    public Integer getLayer() {
        return super.getInteger("layer");
    }
    public Integer getPositionInLayer() {
        return super.getInteger("positionInLayer");
    }
    public Integer getLayer(int state) {
        return super.getInteger(state, "layer");
    }
    public Integer getPositionInLayer(int state) {
        return super.getInteger(state, "positionInLayer");
    }
    public void setLayer(Integer layer) throws Terminate {
        super.set("layer", layer);
    }
    public void setPositionInLayer(Integer positionInLayer) throws Terminate {
        super.set("positionInLayer", positionInLayer);
    }
	
    /**
     * Makes sure that all the attributes specific to nodes are properly
     * initialized. The relevant one are ...
     * - x, y: integer
     * - layer, positionInLayer: integer (layered graphs -- these will go away) 
     * - marked: boolean
     *
     * @todo this is too long; consider breaking out a method that deals with
     * handling integer attributes
     * @todo still need to make LayeredGraphNode a subclass of Node.
     */
    public void initializeAfterParsing()
        throws GalantException {
        LogHelper.enterMethod( getClass(), "initializeAfterParsing: " + this );
        super.initializeAfterParsing();
        Integer idAttribute = super.getInteger("id");
        if ( idAttribute == null ) {
            throw new GalantException("Missing id for node " + this);
        }
        else if ( super.graph.nodeIdExists(idAttribute) ) {
            throw new GalantException("Duplicate id: " + id 
                                      + " when processing node " + this);
        }
        try { // need to catch a Terminate exception - should not happen
            // don't want or need to track the id -- it won't ever change
            id = idAttribute;
            super.remove("id");
            if ( super.graph.isLayered() ) {
                String layerString = getString("layer");
                String positionString = getString("position");
                if ( layerString == null )
                    throw new GalantException("missing layer for"
                                              + " layered graph node " + this);
                if ( positionString == null )
                    throw new GalantException("missing positionInLayer for"
                                              + " layered graph node " + this);
                Integer layer = Integer.MIN_VALUE;
                Integer positionInLayer = Integer.MIN_VALUE;
                try {
                    layer = Integer.parseInt(layerString);
                }
                catch ( NumberFormatException e ) {
                    throw new GalantException("Bad layer " + layerString);
                }
                try {
                    positionInLayer = Integer.parseInt(positionString);
                }
                catch ( NumberFormatException e ) {
                    throw new GalantException("Bad positionInLayer "
                                              + positionString);
                }
                remove("layer");
                remove("positionInLayer");
                set("layer", layer); 
                set("positionInLayer", positionInLayer); 
            } // layered graph
            else { // not a layered graph
                String xString = super.getString("x");
                String yString = super.getString("y");
                Integer x = Integer.MIN_VALUE;
                Integer y = Integer.MIN_VALUE;
                if ( xString == null || yString == null ) {
                    Random r = new Random();
                    if ( xString == null ) {
                        x = r.nextInt(GraphDispatch.getInstance().getWindowWidth());
                    }
                    if ( yString == null ) {
                        y = r.nextInt(GraphDispatch.getInstance().getWindowHeight());
                    }
                }
                else {
                    try {
                        x = Integer.parseInt(xString);
                    }
                    catch ( NumberFormatException e ) {
                        throw new GalantException("Bad x-coordinate " + xString);
                    }
                    try {
                        y = Integer.parseInt(yString);
                    }
                    catch ( NumberFormatException e ) {
                        throw new GalantException("Bad y-coordinate " + yString);
                    }
                } // x and y coordinates specified
                
                remove("x");
                remove("y");
            
                // establish fixed positions
                xCoordinate = x;
                yCoordinate = y;
            } // not a layered graph
            String markedString = getString(MARKED);
            if ( markedString != null ) {
                Boolean marked = Boolean.parseBoolean(markedString);
                remove(MARKED);
                if ( marked )
                    set(MARKED, marked); 
            }
        }
        catch ( Terminate t ) {
            // should not happen
            t.printStackTrace();
        }
        LogHelper.exitMethod(getClass(), "initializeAfterParsing: id = " + id
                             + ", x = " + getX() + ", y = " + getY()
                             + ", node = " + this);
    } // end, intializeAfterParsing

    /**************** marking *******************/
	public Boolean isVisited() {
		return super.getBoolean(MARKED);
	}
	public Boolean isVisited(int state) {
		return super.getBoolean(state, MARKED);
	}
	
	public boolean isMarked() {
		return isVisited();
	}
	public Boolean isMarked(int state) {
        return isVisited(state);
	}

	public void setVisited(Boolean visited) throws Terminate {
        super.set(MARKED, visited);
	}

    public void mark() throws Terminate {
        setVisited(true);
    }
    public void unmark() throws Terminate {
        setVisited(false);
    }
    /**
     * Some algorithms use this alternate "spelling"
     */
    public void unMark() throws Terminate {
        setVisited(false);
    }


    /********************** incident edges **********************/

    public void addEdge(Edge edge) {
		incidentEdges.add(edge);
	}

	/**
	 * @return the node's outgoing edges, based on source and target
	 * specs. ignoring whether the graph is directed or not
	 */
	public List<Edge> getOutgoingEdges() {
		List<Edge> currentEdges = new ArrayList<Edge>();
		
		for ( Edge e : incidentEdges ) {
			if ( e.inScope() && ! e.isDeleted() ) {
				if ( this.equals( e.getSourceNode() ) 
                    || ! graph.isDirected() ) {
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
		
		for ( Edge e : incidentEdges ) {
			if ( e.inScope() && ! e.isDeleted() ) {
				if ( this.equals( e.getTargetNode() )
                     || ! graph.isDirected() ) {
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
		
		for ( Edge e : incidentEdges ) {
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
     * The following are used at various other parts of the code.
     */
    public List<Edge> getEdges() {
        return getIncidentEdges();
    }

	public List<Edge> getEdges(int state) {
		return getIncidentEdges(state);
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
		for (Edge e : incidentEdges) {
			if (!e.inScope() || e.isDeleted()) {
				continue;
			}
			
			Node source = e.getSourceNode();
			Node target = e.getTargetNode();
			Node adjacent;
			if (source.getId() == this.getId()) {
				adjacent = target;
			} else {
				if (graph.isDirected()) continue;
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
		for (Edge e : incidentEdges) {
			if (!e.inScope() || e.isDeleted()) {
				continue;
			}
			
			Node source = e.getSourceNode();
			Node target = e.getTargetNode();
			Node adjacent;
			if (source.getId() == this.getId()) {
				adjacent = target;
			} else {
				if (graph.isDirected()) continue;
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
		for (Edge e : incidentEdges) {
			if (!e.inScope() || e.isDeleted()) {
				continue;
			}
			
			Node source = e.getSourceNode();
			Node target = e.getTargetNode();
			Node adjacent;
			if (source.getId() == this.getId()) {
				adjacent = target;
			} else {
				if (graph.isDirected()) continue;
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
			return e.getTargetNode();
		} else if (e.getTargetNode().equals(this)){
			return e.getSourceNode();
		}
		
		return null;
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
     * The "right way to handle this is to have the algorithm declare whether
     * or not it intends to move nodes -- the hook for that is already there
     * -- and use this information to allow arbitrary position changes by the
     * user during execution *unless* the algorithm wants to make them. In
     * the latter case, the user is prevented from changing position. 
     */

    public Integer getFixedX() {
        return xCoordinate;
    }

    public Integer getFixedY() {
        return yCoordinate;
    }

    public Point getFixedPosition() {
        return new Point(getFixedX(), getFixedY());
    }

	public void setFixedPosition(Point position) {
        LogHelper.enterMethod( getClass(), "setFixedPosition: " + position 
                               + "\n node = " + this );
        setFixedPosition(position.x, position.y);
        LogHelper.exitMethod( getClass(), "setFixedPosition"
                              + "\n node = " + this );
	}
	
	public void setFixedPosition(int x, int y) {
        LogHelper.enterMethod( getClass(), "setFixedPosition: x = " + x
                               + ", y = " + y
                               + "\n node = " + this );
        xCoordinate = x;
        yCoordinate = y;
//         try {
//             if ( getX() == null ) super.set("x", x);
//             if ( getY() == null ) super.set("y", y);
//         }
//         catch ( Terminate t ) {
//             // should not happen
//             t.printStackTrace();
//         }
        LogHelper.exitMethod( getClass(), "setFixedPosition"
                              + "\n node = " + this );
	}

    public static Point genRandomPosition() {
		Random r = new Random();
		int x = r.nextInt(GraphDispatch.getInstance().getWindowWidth());
		int y = r.nextInt(GraphDispatch.getInstance().getWindowHeight());
		return new Point(x, y);
	}

    /**
     * This version is used after the graph is originally read or when it is
     * refreshed during editing. Also when saved to a file.
     *
     * @todo Leads to complaint of x already been specified. If you omit the
     * printing of x and y, nodes that were not moved during the algorithm
     * execution end up in random positions when the algorithm quits.
     */
	public String xmlString()
    {
        String s = "<node" + " id=\"" + this.getId() + "\"";
        s += " x=\"" + this.getFixedX() + "\"";
        s += " y=\"" + this.getFixedY() + "\" ";
        s += super.attributesWithoutPosition();
        s += " />";
		return s;
	}

    /**
     * This version is called when the current state of the animation is
     * exported.
     */
	public String xmlString(int state) {
        if ( ! inScope(state) ) {
            return "";
        }
        String s = "<node" + " id=\"" + this.getId() + "\"";
        // if algorithm doesn't move nodes, only the fixed position is set
        if ( ! dispatch.algorithmMovesNodes() ) {
            s += " x=\"" + this.getFixedX() + "\"";
            s += " y=\"" + this.getFixedY() + "\" ";
            s += super.attributesWithoutPosition(state);
        }
        else {
            s += super.xmlString(state);
        }
        s += "/>";
		return s;
	}

    /**
     * For debugging only
     */
    @Override
	public String toString()
    {
        String s = "<node" + " id=\"" + this.getId() + "\"";
        s += " x=\"" + this.getFixedX() + "\"";
        s += " y=\"" + this.getFixedY() + "\" ";
        s += super.attributesWithoutPosition();
        s += " />";
		return s;
	}

	@Override
	public int compareTo(Node other) {
        Double thisDouble = new Double( this.getWeight() );
        Double otherDouble = new Double( other.getWeight() );
		return thisDouble.compareTo( otherDouble );
	}
}

//  [Last modified: 2015 12 08 at 16:20:14 GMT]
