package edu.ncsu.csc.Galant.graph.component;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import edu.ncsu.csc.Galant.GalantException;
import edu.ncsu.csc.Galant.GraphDispatch;
import edu.ncsu.csc.Galant.logging.LogHelper;

/**
 * Represents node entities as elements of a graph. Encapsulates attributes
 * that are unique to nodes.
 * 
 * @author Michael Owoc
 * @author Ty Devries
 *
 * a major refactoring by Matthias Stallmann, based on a more extended
 * version of the GraphElement class.
 */
public class Node extends GraphElement implements Comparable<Node> {
    private Integer id;
	private List<Edge> incidentEdges;

    public Node(GraphState algorithmState, int id) {
        super(algorithmState);
        this.id = id;
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
        return super.getIntegerAttribute("x");
    }
    public Integer getY() {
        return super.getIntegerAttribute("y");
    }
    public Integer getX(int state) {
        return super.getIntegerAttribute(state, "x");
    }
    public Integer getY(int state) {
        return super.getIntegerAttribute(state, "y");
    }
    public Point getPosition() {
        return new Point(getX(), getY());
    }
    public Point getPosition(int state) {
        return new Point(getX(state), getY(state));
    }

    public void setX(Integer x) { setAttribute("x", x); }
    public void setY(Integer y) { setAttribute("y", y); }
    public void setPosition(Integer x, Integer y) {
        setX(x);
        setY(y);
    }
    public void setPosition(Point point) {
        setX(point.x);
        setY(point.y);
    }

    public Integer getLayer() {
        return getIntegerAttribute("layer");
    }
    public Integer getPositionInLayer() {
        return getIntegerAttribute("positionInLayer");
    }
    public Integer getLayer(int state) {
        return getIntegerAttribute(state, "layer");
    }
    public Integer getPositionInLayer(int state) {
        return getIntegerAttribute(state, "positionInLayer");
    }
    public void setLayer(Integer layer) {
        setAttribute("layer", layer);
    }
    public void setPositionInLayer(Integer positionInLayer) {
        setAttribute("positionInLayer", positionInLayer);
    }
	
	public Node(GraphState currentState) {
        super(currentState);
		incidentEdges = new ArrayList<Edge>();
	}

    /**
     * Makes sure that all the attributes specific to nodes are properly
     * initialized.
     *
     * @todo still need to make LayeredGraphNode a subclass of Node.
     */
    public void initializeAfterParsing()
    throws GalantException {
        LogHelper.enterMethod( getClass(), "initializeAfterParsing: " + this );
        id = getIntegerAttribute("id");
        if ( id == null )
            throw new GalantException("missing or malformed id for node " + this);
        else {
            // don't want or need to track the id -- it won't ever change
            removeAttribute("id");
        }
        if ( GraphDispatch.getInstance().getWorkingGraph().isLayered() ) {
            Integer layer = getIntegerAttribute("layer");
            if ( layer == null )
                throw new GalantException("missing or malformed layer for"
                                          + " layered graph node " + this);
            Integer positionInLayer = getIntegerAttribute("positiionInLayer");
            if ( positionInLayer == null )
                throw new GalantException("missing or malformed positionInLayer for"
                                          + " layered graph node " + this);
        }
        else {
            Integer x = getIntegerAttribute("x");
            Integer y = getIntegerAttribute("y");
            if ( x == null || y == null ) {
                Random r = new Random();
                if ( x == null )
                    x = r.nextInt( GraphDispatch.getInstance().getWindowWidth() );
                if ( y == null )
                    y = r.nextInt( GraphDispatch.getInstance().getWindowHeight() );
            }
        }
        super.intializeAfterParsing();
        LogHelper.exitMethod(getClass(), "initializeAfterParsing: id = " + id
                             + ", x = " + getX() + ", y = " + getY()
                             + ", node = " + this);
   }

    /**************** marking *******************/
	public Boolean isVisited() {
		return super.getBooleanAttribute("marked");
	}
	public Boolean isVisited(int state) {
		return super.getBooleanAttribute(state, "marked");
	}
	
	public boolean isMarked() {
		return isVisited();
	}
	public Boolean isMarked(int state) {
        return isVisited(state);
	}

	public void setVisited(Boolean visited) {
        super.setAttribute("marked", visited);
	}

    public void mark() {
        setVisited(true);
    }
    public void unmark() {
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
                    || ! algorithmState.isDirected() ) {
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
				if ( this.equals( e.getDestNode() )
                     || ! algorithmState.isDirected() ) {
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
			Node dest = e.getDestNode();
			Node adjacent;
			if (source.getId() == this.getId()) {
				adjacent = dest;
			} else {
				if (algorithmState.isDirected()) continue;
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
			Node dest = e.getDestNode();
			Node adjacent;
			if (source.getId() == this.getId()) {
				adjacent = dest;
			} else {
				if (algorithmState.isDirected()) continue;
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
			Node dest = e.getDestNode();
			Node adjacent;
			if (source.getId() == this.getId()) {
				adjacent = dest;
			} else {
				if (algorithmState.isDirected()) continue;
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

    private static final int INITIAL_STATE = 1;

    public Integer getFixedX() {
        return getIntegerAttribute(INITIAL_STATE, "x");
    }

    public Integer getFixedY() {
        return getIntegerAttribute(INITIAL_STATE, "y");
    }

    public Point getFixedPosition() {
        return new Point(getFixedX(), getFixedY());
    }

    /**
     * Right now this is a hack that changes positions in all previous states
     * to guarantee that it looks to the user like the position has been
     * permanently changed (as desired).
     */
	public void setFixedPosition(Point position) {
        LogHelper.enterMethod( getClass(), "setFixedPosition: " + position 
                               + "\n node = " + this );
        for ( int i = super.states.size() - 1; i >= 0; i-- ) {
            GraphElementState state = super.states.get(i);
            state.setAttribute("x", position.x);
            state.setAttribute("y", position.y);
        }
        LogHelper.exitMethod( getClass(), "setFixedPosition"
                              + "\n node = " + this );
	}
	
	public void setFixedPosition(int x, int y) {
        LogHelper.enterMethod( getClass(), "setFixedPosition: x = " + x
                               + ", y = " + y
                               + "\n node = " + this );
        for ( int i = super.states.size() - 1; i >= 0; i-- ) {
            GraphElementState state = super.states.get(i);
            state.setAttribute("x", x);
            state.setAttribute("y", y);
        }
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
     * This version is called when the graph window editor pushes changes to
     * the text editor and at various other points.
     *
     * @todo perhaps need some way to distinguish between the text window
     * function and others such as debugging
     */
	@Override
	public String toString() {
		double weight = this.getWeight();
		String label = "";
		if (this.getLabel() != null) {
			label = this.getLabel();
		}
		
		String s = "<node"
            + " id=\"" + this.getId() + "\"";
        if ( GraphDispatch.getInstance().getWorkingGraph().isLayered() ) {
            s += " layer=\"" + this.getLayer() + "\""
                + " positionInLayer=\"" + this.getPositionInLayer() + "\"";
        }
        else {
            s += " x=\"" + this.getX() + "\""
                + " y=\"" + this.getY() + "\"";
        }
        s += super.toString();
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

        GraphElementState latestState = super.getLatestValidState(state);

        String s = "<node" + " id=\"" + this.getId() + "\"";
        if ( GraphDispatch.getInstance().getWorkingGraph().isLayered() ) {
            s += " layer=\"" + latestState.getIntegerAttribute("layer") + "\""
                + " positionInLayer=\""
                + latestState.getIntegerAttribute("positionInLayer") + "\"";
        }
        else {
            s += " x=\"" + latestState.getIntegerAttribute("x") + "\""
                + " y=\"" + latestState.getIntegerAttribute("y") + "\"";
        }
        s += super.toString();
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

//  [Last modified: 2015 07 24 at 21:21:21 GMT]
