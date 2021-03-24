package edu.ncsu.csc.Galant.graph.component;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Collection;

import edu.ncsu.csc.Galant.GalantException;
import edu.ncsu.csc.Galant.GraphDispatch;
import edu.ncsu.csc.Galant.algorithm.Terminate;
import edu.ncsu.csc.Galant.logging.LogHelper;
import edu.ncsu.csc.Galant.graph.datastructure.NodeList;
import edu.ncsu.csc.Galant.graph.datastructure.EdgeList;
import edu.ncsu.csc.Galant.graph.datastructure.NodeSet;
import edu.ncsu.csc.Galant.graph.datastructure.EdgeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents node entities as elements of a graph. Encapsulates attributes that
 * are unique to nodes.
 *
 * @author Michael Owoc
 * @author Ty Devries
 * @author Matthias Stallmann, major modifications
 *
 * a major refactoring by Matthias Stallmann, based on a more extended version
 * of the GraphElement class.
 */
public class Node extends GraphElement {

    private static final String MARKED = "marked";

    /**
     * The following attributes - id, xCoordinate, yCoordinate, and
     * incidentEdges, are fixed, i.e., not able to be changed by an
     * animation.
     * The id attribute is unique and cannot be modified even by the editor.
     */
    private int id;
    private int xCoordinate;
    private int yCoordinate;
    private EdgeList incidentEdges;
    // a flag that marks some nodes are dragged or not.
    // not used anymore
    public boolean drag = false;
    // to decide if the physical position is already set
    public boolean setpos = false;

    /**
     * create a blank instance for use when copying state at start of algorithm
     * execution
     */
    public Node() {
    }

    /**
     * When a node is created during parsing and id is not known.
     */
    public Node(Graph graph) {
        super(graph);
        incidentEdges = new EdgeList();
    }

    /**
     * @param id is the next available id as determined by the graph.
     * @todo not clear that this is used anywhere
     */
    public Node(Graph graph, int id) {
        super(graph);
        this.id = id;
        incidentEdges = new EdgeList();
    }

    /**
     * To add a node during editing or algorithm execution: - id is the next
     * available one as determined by the graph - the position of the node is
     * known to the algorithm, fixed unless the algorithm moves nodes
     */
    public Node(Graph graph, int id, Integer x, Integer y) {
        super(graph);
        this.id = id;
        incidentEdges = new EdgeList();
        this.xCoordinate = x;
        this.yCoordinate = y;
        // set starting position based on the initial one
        if (GraphDispatch.getInstance().algorithmMovesNodes()) {
            GraphElementState startingState = latestState();
            startingState.set("x", x);
            startingState.set("y", y);
        }
    }

    /**
     * This is called during parsing.
     * @param L an AttributeList created by the GraphMLParser from attributes
     * of the node as given in the input text
     * @throw GalantException if there is a problem in the format of an id,
     * x/y-coordinate, or, in case of layered graphs, layer information
     */
    public Node(Graph graph, AttributeList L) throws GalantException {
        super(graph, L);
        this.initializeAfterParsing(L);
        incidentEdges = new EdgeList();
    }

    /**
     * This is called during the transition from edit mode to
     * animation mode, so that the animation modifies a copy of the
     * edit graph and will have no impact on it.
     * @param currentGraph the graph accessed by the animation - see
     * Algorithm.java
     */
    public Node copyNode(Graph currentGraph) {
        Node copy = new Node();
        copy.dispatch = GraphDispatch.getInstance();
        copy.id = this.id;
        copy.xCoordinate = this.xCoordinate;
        copy.yCoordinate = this.yCoordinate;
        copy.graph = currentGraph;
        // edges are added to this list when they are copied into the
        // copied graph
        copy.incidentEdges = new EdgeList();
        ArrayList<GraphElementState> statesCopy = super.copyCurrentState();
        copy.states = statesCopy;
        
        // add a line for the new flag
        copy.setpos = this.setpos;
        return copy;
    }

    /**
     * Setters and getters for node-specific information that does not change.
     */
    public Integer getId() {
        return id;
    }

    public void setIncidentEdges(Collection<Edge> edges) {
        this.incidentEdges = new EdgeList(edges);
    }

    /**
     * Setters and getters for node-specific information that may change during
     * algorithm execution.
     */
    public Integer getX() {
        Integer x = super.getInteger("x");
        if (x == null) {
            x = this.xCoordinate;
        }
        return x;
    }

    public Integer getY() {
        Integer y = super.getInteger("y");
        if (y == null) {
            y = this.yCoordinate;
        }
        return y;
    }

    public Integer getX(int state) {
        Integer x = super.getInteger(state, "x");
        if (x == null) {
            x = this.xCoordinate;
        }
        return x;
    }

    public Integer getY(int state) {
        Integer y = super.getInteger(state, "y");
        if (y == null) {
            y = this.yCoordinate;
        }
        return y;
    }

    /**
     * @return the point that represents the current position of this
     * node; called during animation - defaults to fixed position if
     * the algorithm did not move nodes
     */
    public Point getPosition() {
        Integer x = getX();
        Integer y = getY();
        Point p = null;
        if (x == null || y == null) {
            p = getFixedPosition();
        } else {
            p = new Point(x, y);
        }
        return p;
    }

    /**
     * @return the point that represents the current position of this
     * node in the given display state
     */
    public Point getPosition(int state) {
        Integer x = getX(state);
        Integer y = getY(state);
        Point p = null;
        if (x == null || y == null) {
            p = getFixedPosition();
        } else {
            p = new Point(x, y);
        }
        return p;
    }

    public void setX(Integer x) throws Terminate {
        super.set("x", x);
    }

    public void setY(Integer y) throws Terminate {
        super.set("y", y);
    }

    public void setPosition(Integer x, Integer y) throws Terminate {
        setX(x);
        setY(y);
    }

    public void setPosition(Point point) throws Terminate {
        setX(point.x);
        setY(point.y);
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
     This method takes ID in a string format and processes it. 
     * It returns the ID parsed as an integer or throws a GalantException for missing or duplicate ID.
     * @param idAsString indicates the ID is string format.
     * @return idAsInteger which is the ID parsed to an integer if there is no GalantException thrown
     */
    public Integer getID(String idAsString) throws GalantException
    {
        Integer idAsInteger=null;
         if (idAsString != null)
        {
            try 
            {
                idAsInteger = Integer.parseInt(idAsString);
            } 
            catch (NumberFormatException e) 
            {
                throw new GalantException("Bad id " + idAsString);
            }
        }
        return idAsInteger;
    }
    /**
     * Makes sure that all the attributes specific to nodes are properly
     * initialized. The relevant one are ... - x, y: integer - layer,
     * positionInLayer: integer (layered graphs -- these will go away) - marked:
     * boolean
     *
     * @todo this is too long; consider breaking out a method that deals with
     * handling integer attributes
     */
    public void initializeAfterParsing(AttributeList L) throws GalantException {
        Integer idAttribute = null;
        String xString = null;
        String yString = null;
        for (int i = 0; i < L.attributes.size(); i++) {
            Attribute attributeOfNode = L.attributes.get(i);
            if (attributeOfNode.key.equals("id")) {
               // String attributeValue = attributeOfNode.toString().split("=")[1];
               String attributeValue = attributeOfNode.getStringValue();
                idAttribute = getID(attributeValue);
            } else if (attributeOfNode.key.equals("x")) {
                String attributeValue = attributeOfNode.getStringValue();
                xString = attributeValue;
            } else if (attributeOfNode.key.equals("y")) {
                String attributeValue = attributeOfNode.getStringValue();
                yString = attributeValue;
            }
        } // end, for attribute in list
        
        if (idAttribute == null) {
            throw new GalantException("Missing id for node " + this);
        } else if (super.graph.nodeIdExists(idAttribute)) {
            throw new GalantException("Duplicate id: " + idAttribute
                    + " when processing node " + this);
        }
        id = idAttribute;
        L.remove("id");
        if (super.graph.isLayered()) {
            String layerString = L.getString("layer");
            String positionString = L.getString("positionInLayer");
            if (layerString == null) {
                throw new GalantException("Missing layer for"
                                          + " layered graph node " + this);
            }
            if (positionString == null) {
                throw new GalantException("Missing positionInLayer for"
                                          + " layered graph node " + this);
            }
            Integer layer = Integer.MIN_VALUE;
            Integer positionInLayer = Integer.MIN_VALUE;
            try {
                layer = Integer.parseInt(layerString);
            } catch (NumberFormatException e) {
                throw new GalantException("Bad layer " + layerString);
            }
            try {
                positionInLayer = Integer.parseInt(positionString);
            } catch (NumberFormatException e) {
                throw new GalantException("Bad positionInLayer "
                                          + positionString);
            }
            L.remove("layer");
            L.remove("positionInLayer");
            L.set("layer", layer);
            L.set("positionInLayer", positionInLayer);
        } // layered graph
        else { // not a layered graph
            Integer x = Integer.MIN_VALUE;
            Integer y = Integer.MIN_VALUE;
            if (xString == null || yString == null) {
                Random r = new Random();
                if (xString == null) {
                    x = r.nextInt(GraphDispatch.getInstance().getWindowWidth());
                }
                if (yString == null) {
                    y = r.nextInt(GraphDispatch.getInstance().getWindowHeight());
                }
            } else {
                try {
                    x = Integer.parseInt(xString);
                } catch (NumberFormatException e) {
                    throw new GalantException("Bad x-coordinate " + xString);
                }
                try {
                    y = Integer.parseInt(yString);
                } catch (NumberFormatException e) {
                    throw new GalantException("Bad y-coordinate " + yString);
                }
            } // x and y coordinates specified
            L.remove("x");
            L.remove("y");

            // establish fixed positions
            xCoordinate = x;
            yCoordinate = y;
        } // not a layered graph
        String markedString = L.getString(MARKED);
        if (markedString != null) {
            Boolean marked = Boolean.parseBoolean(markedString);
            L.remove(MARKED);
            if (marked) {
                L.set(MARKED, marked);
            }
        }
        super.initializeAfterParsing(L);
    } // end, intializeAfterParsing

    /**
     * ************** marking ******************
     */
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
        clear(MARKED);
    }

    /**
     * Some algorithms use this alternate "spelling"
     */
    public void unMark() throws Terminate {
        clear(MARKED);
    }

    /**
     * ******************** incident edges *********************
     */
    public void addEdge(Edge edge) {
        incidentEdges.add(edge);
    }

    /**
     * @return the node's outgoing edges, based on source and target specs; if
     * the graph is undirected, all incident edges are returned
     */
    public EdgeList getOutgoingEdges() {
        EdgeList currentEdges = new EdgeList();
        for (Edge e : incidentEdges) {
            if (e.inScope()) {
                if ( this.equals(e.getSourceNode() )
                        || ! graph.isDirected() ) {
                    currentEdges.add(e);
                }
            }
        }
        return currentEdges;
    }

    /**
     * @return the node's incoming edges, based on source and target specs; if
     * the graph is undirected, all edges are incoming
     */
    public EdgeList getIncomingEdges() {
        EdgeList currentEdges = new EdgeList();
        for (Edge e : incidentEdges) {
            if (e.inScope()) {
                if (this.equals(e.getTargetNode())
                        || !graph.isDirected()) {
                    currentEdges.add(e);
                }
            }
        }
        return currentEdges;
    }

    /**
     * @return a list of edges incident on this node regardless of whether they
     * are incoming or outgoing.
     */
    public EdgeList getIncidentEdges() {
        EdgeList currentEdges = new EdgeList();

        for (Edge e : incidentEdges) {
            if (e.inScope()) {
                currentEdges.add(e);
            }
        }
        return currentEdges;
    }

    /**
     * @return all edges incident on this node, even deleted ones;
     * Caution: returns a reference (for now), but is only called in one
     * place - see deleteNode() in Graph.java
     */
    public EdgeList getEdges() {
        return incidentEdges;
    }

    /**
     * @return the nodes adjacent to this node (as a templated list)
     */
    public NodeList getAdjacentNodes() {
        EdgeList edges = getIncidentEdges();
        NodeList nodes = new NodeList();
        for (Edge e : edges) {
            nodes.add(travel(e));
        }
        return nodes;
    }

    /**
     * @return the visible neighbors of this node (as a NodeList)
     */
    public NodeList visibleNeighbors() {
        NodeList neighbors = new NodeList();
        for (Edge e : incidentEdges) {
            if (e.inScope() && !e.isHidden()) {
                Node neighbor = travel(e);
                if (neighbor.inScope() && !neighbor.isHidden()) {
                    neighbors.add(neighbor);
                }
            }
        }
        return neighbors;
    }

    /**
     * @return the visible incident edges of this node
     */
    public EdgeList visibleEdges() {
        EdgeList visibleEdges = new EdgeList();
        for (Edge e : incidentEdges) {
            if (e.inScope() && !e.isHidden()) {
                visibleEdges.add(e);
            }
        }
        return visibleEdges;
    }

    /**
     * @return the visible incoming edges of this node
     */
    public EdgeList visibleIncomingEdges() {
        EdgeList visibleEdges = new EdgeList();
        for (Edge e : incidentEdges) {
            if (e.inScope() && !e.isDeleted() && !e.isHidden()) {
                if (this.equals(e.getTargetNode())
                        || !graph.isDirected()) {
                    visibleEdges.add(e);
                }
            }
        }
        return visibleEdges;
    }

    /**
     * @return the visible outgoing edges of this node
     */
    public EdgeList visibleOutgoingEdges() {
        EdgeList visibleEdges = new EdgeList();
        for (Edge e : incidentEdges) {
            if (e.inScope() && !e.isDeleted() && !e.isHidden()) {
                if (this.equals(e.getSourceNode())
                        || !graph.isDirected()) {
                    visibleEdges.add(e);
                }
            }
        }
        return visibleEdges;
    }

    /**
     * The following methods use the edge list getters to return degrees
     */
    public int getOutdegree() {
        return getOutgoingEdges().size();
    }

    public int getIndegree() {
        return getIncomingEdges().size();
    }

    public int getDegree() {
        return getIncidentEdges().size();
    }

    /**
     * when hiding a node, you also have to hide its incident edges
     */
    // public void hide() throws Terminate {
    //     GraphDispatch dispatch = GraphDispatch.getInstance();
    //     super.hide();
    //     for ( Edge edge: getEdges() ) {
    //         edge.hide();
    //     }
    // }
    /**
     * when showing (unhiding) a node, you also have to show its incident edges,
     * being careful not to show the hidden ones
     */
    // public void show() throws Terminate {
    //     super.show();
    //     for ( Edge edge: getEdges() ) {
    //       if ( ! edge.isHiddent() ) edge.show();
    //     }
    // }
    /**
     * Gets a list of Edges incident to this node whose visited flag is set to
     * false.
     */
    public EdgeList getUnvisitedPaths() {
        EdgeList unvisited = new EdgeList();
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
                if (graph.isDirected()) {
                    continue;
                }
                adjacent = source;
            }
            if (!adjacent.isVisited()) {
                unvisited.add(e);
            }
        }
        return unvisited;
    }

    /**
     * Gets a list of Edges incident to this node whose visited flag is set to
     * true.
     */
    public EdgeList getVisitedPaths() {
        EdgeList visited = new EdgeList();
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
                if (graph.isDirected()) {
                    continue;
                }
                adjacent = source;
            }
            if (adjacent.isVisited()) {
                visited.add(e);
            }
        }
        return visited;
    }

    public NodeList getUnvisitedAdjacentNodes() {
        NodeList nodes = new NodeList();
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
                if (graph.isDirected()) {
                    continue;
                }
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
     * No issue on a self loop: will find that the first Node is this and return
     * the other
     */
    public Node travel(Edge e) {
        if (e.getSourceNode().equals(this)) {
            return e.getTargetNode();
        } else if (e.getTargetNode().equals(this)) {
            return e.getSourceNode();
        }
        return null;
    }

    /**
     * The fixed versions, getFixedPosition() and setFixedPosition() are used
     * within the Galant software to access/modify positions of nodes during
     * parsing of GraphML input, in response to mouse dragging, or when the
     * force directed heuristic is applied to reposition nodes. During execution
     * of an algorithm, a position set by the algorithm takes precedence, i.e.,
     * a node moved by the algorithm can no longer be moved via dragging.
     * However, any node that is *not* moved by the algorithm *can* still be
     * moved by the user.
     *
     * The "right way to handle this is to have the algorithm declare whether or
     * not it intends to move nodes -- the hook for that is already there -- and
     * use this information to allow arbitrary position changes by the user
     * during execution *unless* the algorithm wants to make them. In the latter
     * case, the user is prevented from changing position.
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
        setFixedPosition(position.x, position.y);
    }

    public void setFixedPosition(int x, int y) {
        xCoordinate = x;
        yCoordinate = y;
        this.graph.setUserNodeMove();
    }

    public static Point genRandomPosition() {
        Random r = new Random();
        int x = r.nextInt(GraphDispatch.getInstance().getWindowWidth());
        int y = r.nextInt(GraphDispatch.getInstance().getWindowHeight());
        return new Point(x, y);
    }

    /**
     * natural syntax for set containment
     */
    public Boolean in(NodeSet S) {
        return S.contains(this);
    }

    /**
     * This version is used after the graph is originally read or when it is
     * refreshed during editing. Also when saved to a file.
     */
    public String xmlString() {
        String s = "<node" + " id=\"" + this.getId() + "\"";
        if ( ! GraphDispatch.getInstance().getWorkingGraph().isLayered() ) {
	        s += " x=\"" + this.getFixedX() + "\"";
	        s += " y=\"" + this.getFixedY() + "\" ";
        }
        s += super.attributesWithoutPosition();
        s += " />";
        return s;
    }

    /**
     * This version is called when the current state of the animation is
     * exported.
     */
    public String xmlString(int state) {
        if (!inScope(state)) {
            return "";
        }
        String s = "<node" + " id=\"" + this.getId() + "\"";
        // if algorithm doesn't move nodes, only the fixed position is set
        if ( ! GraphDispatch.getInstance().getWorkingGraph().isLayered() ) {
	        s += " x=\"" + this.getX(state) + "\"";
	        s += " y=\"" + this.getY(state) + "\" ";
        }
        s += super.attributesWithoutPosition(state);
        s += "/>";
        return s;
    }

    /**
     * For debugging only
     */
    @Override
    public String toString() {
        String s = "[node " + this.getId() + " (";
        s += this.xCoordinate + ",";
        s += this.yCoordinate + ") ";
        s += super.attributesWithoutId();
        s += "]";
        return s;
    }

}

//  [Last modified: 2021 01 31 at 14:33:33 GMT]
