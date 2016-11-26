/** 
 * Has the all the basic infrastructure for running an algorithm in a
 * separate thread and various utility classes and methods for algorithm
 * implementations.
 *
 * @author Jason Cockrell, Ty Devries, Alex McCabe, Michael Owoc; with major
 * enhancements by Matthias Stallmann
 *
 * @todo Once the thread situation is straightened out, it is high time to
 * consider some methods used in GDR, for example,
 *       - String query(String message)
 * This should prompt the display to pop up a window
 * with the message and pause execution, to be resumed when the user has
 * produced the appropriate response. Done as a query for getting a node or
 * edge specifically.
 *
 * @todo Add method(s) to change the direction of an edge; this would involve
 * both swapping source and target and changing the lists of incoming and
 * outgoing edges of each endpoint.
 */

package edu.ncsu.csc.Galant.algorithm;

import java.util.Collection;
import java.util.AbstractQueue;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Stack;
import java.awt.Color;
import java.awt.Point;

//import java.util.Collections;

import edu.ncsu.csc.Galant.GalantException;
import edu.ncsu.csc.Galant.gui.util.ExceptionDialog;
import edu.ncsu.csc.Galant.GraphDispatch;
import edu.ncsu.csc.Galant.graph.component.Graph;
import edu.ncsu.csc.Galant.graph.component.LayeredGraph;
import edu.ncsu.csc.Galant.graph.component.GraphElement;
import edu.ncsu.csc.Galant.graph.component.Node;
import edu.ncsu.csc.Galant.graph.component.Edge;
import edu.ncsu.csc.Galant.graph.container.NodeSet;
import edu.ncsu.csc.Galant.graph.container.EdgeSet;
import edu.ncsu.csc.Galant.graph.container.NodePriorityQueue;
import edu.ncsu.csc.Galant.graph.container.EdgePriorityQueue;
import edu.ncsu.csc.Galant.gui.window.GraphWindow;
import edu.ncsu.csc.Galant.gui.window.GraphWindow.GraphDisplays;
import edu.ncsu.csc.Galant.gui.util.StringQuery;
import edu.ncsu.csc.Galant.gui.util.IntegerQuery;
import edu.ncsu.csc.Galant.gui.util.DoubleQuery;
import edu.ncsu.csc.Galant.algorithm.AlgorithmSynchronizer;
import edu.ncsu.csc.Galant.algorithm.AlgorithmExecutor;
import edu.ncsu.csc.Galant.logging.LogHelper;

public abstract class Algorithm implements Runnable {
    /** A list of all the runnable algorithms. */
    public static final List<Algorithm> algorithms = new ArrayList<Algorithm>();

    /** The graph on which the algorithm is being run. */
    public Graph graph;

    /**
     * If the graph is layered, it can be referenced as such using the
     * variable below
     */
    public LayeredGraph layeredGraph;

    /** The dispatch instance used to communicate among the algorithm, the
     * graph and the display */
    public GraphDispatch dispatch;

    /**
     * The object that communicates with the display (user interaction) to
     * accomplish forward and backward steps and termination of execution.
     */
    public AlgorithmSynchronizer synchronizer;

    /**
     * Numerical constants
     */
    public static final Double INFINITY = Double.POSITIVE_INFINITY;
    public static final Double Infinity = Double.POSITIVE_INFINITY;
    public static final Double NEGATIVE_INFINITY = Double.NEGATIVE_INFINITY;

    /**
     * Color constants: included for convenience since Galant represents
     * colors as hexidecimal strings.
     */
    public static final String RED       = "#ff0000";
    public static final String GREEN     = "#00ff00";
    public static final String BLUE      = "#0000ff";
    public static final String YELLOW    = "#ffff00";
    public static final String MAGENTA   = "#ff00ff";
    public static final String CYAN      = "#00ffff";
    public static final String TEAL      = "#009999";
    public static final String VIOLET    = "#9900cc";
    public static final String ORANGE    = "#ff8000";
    public static final String GRAY      = "#808080";
    public static final String BLACK     = "#000000";
    public static final String WHITE     = "#ffffff";

    /**
     * @param graph the Graph object on which this algorithm will run; used
     * when algorithm is started up in GAlgorithmEditorPanel
     */
    public void setGraph(Graph graph) {
        this.graph = graph;
        if ( graph instanceof LayeredGraph )
            this.layeredGraph = (LayeredGraph) graph;
    }

    public Graph getGraph() { return this.graph; }

    /**
     * throws an exception if the element is null or not in the current scope
     */
    public void checkGraphElement(GraphElement ge) throws GalantException {
        if ( ge == null || ! ge.inScope() )
            throw new GalantException("Nonexistent node or edge: " + ge);
    }

    public class NodeList extends ArrayList<Node> {
    }

    public class EdgeList extends ArrayList<Edge> {
    }

    public void add(Node v, NodeList L) { L.add(v); }
    public void remove(Node v, NodeList L) { L.remove(v); }
    public Node first(NodeList L) { return L.get(0); }
    public void add(Edge e, EdgeList L) { L.add(e); }
    public void remove(Edge e, EdgeList L) { L.remove(e); }
    public Edge first(EdgeList L) { return L.get(0); }

    /**
     * A queue of nodes
     *
     * @todo Node and Edge Queue, Stack, PriorityQueue and List should be
     * turned into independent classes that can be used elsewhere, such as
     * when a Node object returns a list of incident edges. This should
     * prevent the Collections.sort() issue with List<Edge>
     */
    public class NodeQueue extends AbstractQueue<Node> {
        private Queue<Node> Q = new ArrayDeque<Node>();
        public void enqueue(Node v) { Q.offer(v); }
        public Node dequeue() { return Q.poll(); }
        public Node remove() { return Q.remove(); }
        public Node element() { return Q.element(); }
        @Override
            public boolean offer(Node e) { return Q.offer(e); }
        @Override
            public Node poll() { return Q.poll(); }
        @Override
            public Node peek() { return Q.peek(); }
        @Override
            public Iterator<Node> iterator() { return Q.iterator(); }
        @Override
            public int size() { return Q.size(); }
    }
    public class EdgeQueue extends AbstractQueue<Edge> {
        private Queue<Edge> Q = new ArrayDeque<Edge>();

        public void enqueue(Edge e) { Q.offer(e); }
        public Edge dequeue() { return Q.poll(); }
        public Edge remove() { return Q.remove(); }
        public Edge element() { return Q.element(); }
        @Override
            public boolean offer(Edge e) { return Q.offer(e); }
        @Override
            public Edge poll() { return Q.poll(); }
        @Override
            public Edge peek() { return Q.peek(); }
        @Override
            public Iterator<Edge> iterator() { return Q.iterator(); }
        @Override
            public int size() { return Q.size(); }
    }
    public class NodeStack extends Stack<Node>
    {}
    public class EdgeStack extends Stack<Edge>
    {}

    // Pre-existing queue/stack/priority queue objects
    public NodeQueue nodeQ = new NodeQueue();
    public EdgeQueue edgeQ = new EdgeQueue();
    public NodeStack nodeStack = new NodeStack();
    public EdgeStack edgeStack = new EdgeStack();
    public NodePriorityQueue nodePQ = new NodePriorityQueue();
    public EdgePriorityQueue edgePQ = new EdgePriorityQueue();

    public Algorithm() {
        algorithms.add(this);
    }

    /**
     * Initializes data structures and synchronization object
     */
    public void initialize() {
        LogHelper.enterMethod(getClass(), "initialize");
		nodeQ = new NodeQueue();
		edgeQ = new EdgeQueue();
		nodeStack = new NodeStack();
		edgeStack = new EdgeStack();
		nodePQ = new NodePriorityQueue();
		edgePQ = new EdgePriorityQueue();
        dispatch = GraphDispatch.getInstance();
        dispatch.setAlgorithmMovesNodes(false);
        synchronizer = dispatch.getAlgorithmSynchronizer();
        try {
            synchronizer.pauseExecution();
        }
        catch ( Terminate t ) {
            System.out.println("Algorithm.initialize() threw Terminate");
        }
        LogHelper.exitMethod(getClass(), "initialize");
    }

    /**
     * Signals the synchronizer that the algorithm has reached the end
     */
    public void finishAlgorithm() {
        synchronizer.finishAlgorithm();
    }

    /**
     * Throws an exception; this provides a simple interface for the algorithm
     * programmer.
     */
    public void error(String message) throws GalantException {
        throw new GalantException(message);
    }

    /**
     * The following methods control the display of labels and weights during
     * algorithm execution; usually used at the beginning to have the
     * algorithm declare what information needs to be shown in order for the
     * algorithm animation to be effective. They have the same effect as the
     * user does when using the toggle buttons or keyboard shortcuts.
     * 
     * Individual showing and hiding of Node/Edge labels or weights takes
     * effect only if these are visible globally in the graph. Initially, all
     * labels and weights are visible.
     *
     * @todo There's an unnecessary level of indirection here; we could call
     * on a GraphWindow method directly instead of going through the graph first.
     */

    /** makes node labels invisible */
    public void hideNodeLabels() { graph.showNodeLabels(false); }
    /** makes edge labels invisible */
    public void hideEdgeLabels() { graph.showEdgeLabels(false); }
    /** makes node labels visible */
    public void showNodeLabels() { graph.showNodeLabels(true); }
    /** makes edge labels visible */
    public void showEdgeLabels() { graph.showEdgeLabels(true); }
    /** makes node weights invisible */
    public void hideNodeWeights() { graph.showNodeWeights(false); }
    /** makes edge weights invisible */
    public void hideEdgeWeights() { graph.showEdgeWeights(false); }
    /** makes node weights visible */
    public void showNodeWeights() { graph.showNodeWeights(true); }
    /** makes edge weights visible */
    public void showEdgeWeights() { graph.showEdgeWeights(true); }

    /**
     * The following show or hide all Node/Edge labels or weights during
     * execution. They take effect only if the corresponding items are
     * visible overall as controlled by the previous set of methods or the
     * user. What they do is to iteratively show or hide weight or label of
     * each individual Node/Edge
     */
    public void hideAllNodeLabels() throws Terminate { graph.hideAllNodeLabels(); }
    public void hideAllEdgeLabels() throws Terminate { graph.hideAllEdgeLabels(); }
    public void hideAllNodeWeights() throws Terminate { graph.hideAllNodeWeights(); }
    public void hideAllEdgeWeights() throws Terminate { graph.hideAllEdgeWeights(); }
    public void showAllNodeLabels() throws Terminate { graph.showAllNodeLabels(); }
    public void showAllEdgeLabels() throws Terminate { graph.showAllEdgeLabels(); }
    public void showAllNodeWeights() throws Terminate { graph.showAllNodeWeights(); }
    public void showAllEdgeWeights() throws Terminate { graph.showAllEdgeWeights(); }

    /**
     * It's sometimes useful to reveal all nodes and/or edges that have been hidden
     */
    public void showNodes() throws Terminate { graph.showNodes(); }
    public void showEdges() throws Terminate { graph.showEdges(); }

    /**
     * Also useful to do blanket clearing of attributes
     */
    public void clearNodeMarks() throws Terminate {
        graph.clearNodeMarks();
    }
    public void clearMarks() throws Terminate {
        clearNodeMarks();
    }
    public void clearNodeHighlighting() throws Terminate {
        graph.clearNodeHighlighting();
    }
    public void clearEdgeHighlighting() throws Terminate {
        graph.clearEdgeHighlighting();
    }
    public void clearNodeLabels() throws Terminate {
        graph.clearNodeLabels();
    }
    public void clearEdgeLabels() throws Terminate {
        graph.clearEdgeLabels();
    }
    public void clearNodeWeights() throws Terminate {
        graph.clearNodeWeights();
    }
    public void clearEdgeWeights() throws Terminate {
        graph.clearEdgeWeights();
    }
    public void clearAllNode(String attribute) throws Terminate {
        graph.clearAllNode(attribute);
    }
    public void clearAllEdge(String attribute) throws Terminate {
        graph.clearAllEdge(attribute);
    }

    /**
     * The following methods are designed to make convenient graph methods
     * accessible to the algorithm program. For more details, see the
     * corresponding methods in class Graph.
     */
    public int id(Node n) throws GalantException {
        checkGraphElement(n);
        return n.getId();
    }

    public int id(Edge e) throws GalantException {
        checkGraphElement(e);
        return e.getId();
    }

    /**
     * @return the largest id of any node + 1; this should be used when
     * allocating an array of nodes, as there is no longer a guarantee that
     * id's start at 0 and are contiguous.
     */
    public int nodeIds() {
        return graph.nodeIds();
    }

    /**
     * @return the largest id of any edge + 1; this should be used when
     * allocating an array of nodes, as there is no longer a guarantee that
     * id's start at 0 and are contiguous.
     */
    public int edgeIds() {
        return graph.edgeIds();
    }

    /**
     * Methods to make syntax friendlier for procedural programmers
     */
    public void highlight(GraphElement ge) throws Terminate, GalantException { 
        checkGraphElement(ge);
        ge.highlight();
    }
    public void unHighlight(GraphElement ge) throws Terminate, GalantException { 
        checkGraphElement(ge);
        ge.unHighlight();
    }
    public void unhighlight(GraphElement ge) throws Terminate, GalantException {
        checkGraphElement(ge);
        ge.unHighlight();
    }
    public Boolean highlighted(GraphElement ge) throws GalantException {
        checkGraphElement(ge);
        return ge.isHighlighted();
    }
    public Boolean isHighlighted(GraphElement ge) throws GalantException {
        checkGraphElement(ge);
        return ge.isHighlighted();
    }
    /** selected is a synonym for highlighted */
    public void select(GraphElement ge) throws Terminate, GalantException {
        checkGraphElement(ge);
        ge.setSelected(true);
    }
    public void deselect(GraphElement ge) throws Terminate, GalantException {
        checkGraphElement(ge);
        ge.setSelected(false);
    }
    public Boolean selected(GraphElement ge) throws GalantException {
        checkGraphElement(ge);
        return ge.isSelected();
    }
    public Boolean isSelected(GraphElement ge) throws GalantException {
        checkGraphElement(ge);
        return ge.isSelected();
    }

    public void mark(Node n) throws Terminate, GalantException {
        checkGraphElement(n);
        n.mark();
    }
    public void unMark(Node n) throws Terminate, GalantException {
        checkGraphElement(n);
        n.unMark();
    }
    public void unmark(Node n) throws Terminate, GalantException {
        checkGraphElement(n);
        n.unMark();
    }
    public Boolean marked(Node n) throws GalantException {
        checkGraphElement(n);
        return n.isMarked();
    }
    public Boolean isMarked(Node n) throws GalantException {
        checkGraphElement(n);
        return n.isMarked();
    }

    public Double weight(GraphElement ge) throws GalantException {
        checkGraphElement(ge);
        Double weight = ge.getWeight();
        if ( weight == null )
            throw new GalantException("node or edge has no weight: " + ge);
        return ge.getWeight();
    }
    public void setWeight(GraphElement ge, double weight) throws Terminate, GalantException {
        checkGraphElement(ge);
        ge.setWeight(weight);
    }
    public String label(GraphElement ge) throws GalantException {
        checkGraphElement(ge);
        return ge.getLabel();
    }
    public boolean hasLabel(GraphElement ge) throws GalantException {
        checkGraphElement(ge);
        return ge.hasLabel();
    }
    public boolean hasWeight(GraphElement ge) throws GalantException {
        checkGraphElement(ge);
        return ge.hasWeight();
    }

    /**
     * @todo It would be useful to have a class AttributeParser with a method
     * parse(String) that returns an Integer, Double, Boolean or String,
     * depending on whether the string can be successfully parse as one of
     * these (attempted in the given order). The mechanism already exists in
     * GraphMLParser. The new class should probably live in graph/component.
     */
    public void setLabel(GraphElement ge, Object s) throws Terminate, GalantException {
        checkGraphElement(ge);
        ge.setLabel("" + s);
    }
    public void label(GraphElement ge, Object s) throws Terminate, GalantException {
        checkGraphElement(ge);
        ge.setLabel("" + s);
    }
    public void clearLabel(GraphElement ge) throws Terminate, GalantException {
        checkGraphElement(ge);
        ge.clearLabel();
    }
    public void clearWeight(GraphElement ge) throws Terminate, GalantException {
        checkGraphElement(ge);
        ge.clearWeight();
    }

    public Node source(Edge e) throws GalantException { 
        checkGraphElement(e);
        return e.getSourceNode();
    }
    public Node target(Edge e) throws GalantException { 
        checkGraphElement(e);
        return e.getTargetNode();
    }

    public void set(GraphElement ge, String s) throws Terminate, GalantException {
        checkGraphElement(ge);
        ge.set(s);
    }
    public void clear(GraphElement ge, String s) throws Terminate, GalantException {
        checkGraphElement(ge);
        ge.clear(s);
    }
    public Boolean is(GraphElement ge, String s) throws GalantException {
        checkGraphElement(ge);
        return ge.is(s);
    }

    public void set(GraphElement ge, String s, Integer i) throws Terminate, GalantException {
        checkGraphElement(ge);
        ge.set(s, i);
    }
    public void set(GraphElement ge, String s, Double d) throws Terminate, GalantException {
        checkGraphElement(ge);
        ge.set(s, d);
    }
    public void set(GraphElement ge, String key, String value) throws Terminate, GalantException {
        checkGraphElement(ge);
        ge.set(key, value);
    }
    public Integer getInteger(GraphElement ge, String s) throws GalantException { 
        checkGraphElement(ge);
        return ge.getInteger(s);
    }
    public Double getDouble(GraphElement ge, String s) throws GalantException {
        checkGraphElement(ge);
        return ge.getDouble(s);
    }
    public String getString(GraphElement ge, String s) throws GalantException {
        checkGraphElement(ge);
        return ge.getString(s);
    }

    public void color(GraphElement ge, String color) throws Terminate, GalantException {
        checkGraphElement(ge);
        ge.setColor(color);
    }
    public void unColor(GraphElement ge) throws Terminate, GalantException {
        checkGraphElement(ge);
        ge.clearColor();
    }
    public void uncolor(GraphElement ge) throws Terminate, GalantException {
        checkGraphElement(ge);
        ge.clearColor();
    }
    public String color(GraphElement ge) throws GalantException {
        checkGraphElement(ge);
        return ge.getColor();
    }

    /** hiding of nodes differs - all incident edges must be hidden as well */
    public void hide(Node v) throws Terminate, GalantException { 
        checkGraphElement(v);
        v.hide();
    }
    public void hide(Edge e) throws Terminate, GalantException {
        checkGraphElement(e);
        e.hide();
    }
    public void show(Node v) throws Terminate, GalantException {
        checkGraphElement(v);
        v.show();
    }
    public void show(Edge e) throws Terminate, GalantException {
        checkGraphElement(e);
        e.show();
    }

    public Boolean isHidden(GraphElement ge) throws GalantException {
        checkGraphElement(ge);
        return ge.isHidden();
    }
    public Boolean isVisible(GraphElement ge) throws GalantException {
        checkGraphElement(ge);
        return ! ge.isHidden();
    }
    public Boolean hidden(GraphElement ge) throws GalantException {
        checkGraphElement(ge);
        return ge.isHidden();
    }
    public Boolean visible(GraphElement ge) throws GalantException { 
        checkGraphElement(ge);
        return ! ge.isHidden();
    }
    public void hideLabel(GraphElement ge) throws Terminate, GalantException {
        checkGraphElement(ge);
        ge.hideLabel();
    }
    public void showLabel(GraphElement ge) throws Terminate, GalantException {
        checkGraphElement(ge);
        ge.showLabel();
    }
    public Boolean labelIsHidden(GraphElement ge) throws GalantException {
        checkGraphElement(ge);
        return ge.labelIsHidden(getState());
    }
    public Boolean labelIsVisible(GraphElement ge) throws GalantException {
        checkGraphElement(ge);
        return ! labelIsHidden(ge);
    }
    public void setLabel(GraphElement ge, String label) throws Terminate, GalantException {
        checkGraphElement(ge);
        ge.setLabel(label);
    }
    public void hideWeight(GraphElement ge) throws Terminate, GalantException {
        checkGraphElement(ge);
        ge.hideWeight();
    }
    public void showWeight(GraphElement ge) throws Terminate, GalantException {
        checkGraphElement(ge);
        ge.showWeight();
    }
    public Boolean weightIsHidden(GraphElement ge) throws GalantException {
        checkGraphElement(ge);
        return ge.weightIsHidden(getState());
    }
    public Boolean weightIsVisible(GraphElement ge) throws GalantException {
        checkGraphElement(ge);
        return ! weightIsHidden(ge);
    }
    public Integer degree(Node v) throws GalantException {
        checkGraphElement(v);
        return v.getDegree();
    }
    public Integer indegree(Node v) throws GalantException {
        checkGraphElement(v);
        return v.getIndegree();
    }
    public Integer outdegree(Node v) throws GalantException {
        checkGraphElement(v);
        return v.getOutdegree();
    }
    public Node otherEnd(Node v, Edge e) throws GalantException {
        checkGraphElement(v);
        checkGraphElement(e);
        return v.travel(e); }
    public Node otherEnd(Edge e, Node v) throws GalantException {
        checkGraphElement(v);
        checkGraphElement(e);
        return v.travel(e);
    }
    public List<Node> neighbors(Node v) throws GalantException {
        checkGraphElement(v);
        return v.getAdjacentNodes();
    }
    public List<Edge> edges(Node v) throws GalantException {
        checkGraphElement(v);
        return v.getIncidentEdges();
    }
    public List<Edge> inEdges(Node v) throws GalantException {
        checkGraphElement(v);
        return v.getIncomingEdges();
    }
    public List<Edge> outEdges(Node v) throws GalantException {
        checkGraphElement(v);
        return v.getOutgoingEdges();
    }

    public NodeSet visibleNeighbors(Node v) throws GalantException {
        checkGraphElement(v);
        return v.visibleNeighbors();
    }

    public EdgeSet visibleEdges(Node v) throws GalantException {
        checkGraphElement(v);
        return v.visibleEdges();
    }
    public EdgeSet visibleInEdges(Node v) throws GalantException {
        checkGraphElement(v);
        return v.visibleIncomingEdges();
    }
    public EdgeSet visibleOutEdges(Node v) throws GalantException {
        checkGraphElement(v);
        return v.visibleOutgoingEdges();
    }

    /**
     * @return true if (v,w) is an edge; direction important if the graph is
     * directed
     */
    public Boolean isEdge(Node v, Node w) throws GalantException {
        checkGraphElement(v);
        checkGraphElement(w);
        List<Edge> incidenceList
            = v.getOutgoingEdges();
        for ( Edge e : incidenceList ) {
            if ( v.travel(e) == w ) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return true if edge e is incident on v
     */
    public Boolean isIncident(Edge e, Node v) throws GalantException {
        checkGraphElement(v);
        checkGraphElement(e);
        EdgeSet incidentEdges = new EdgeSet(v.getIncidentEdges());
        return incidentEdges.contains(e);
    }

    /**
     * The following are provided because, while it's okay to say
     *     NodeList L = nodes();
     * even though nodes() returns List<Node>, it's not okay then to say
     *     Node v = first(L);
     * since first would require a NodeList rather than a List<Node>; the
     * error is not reported until runtime
     */
    public Node firstNode(List<Node> L) { return L.get(0); }
    public Edge firstEdge(List<Edge> L) { return L.get(0); }

    /**
     * Procedural versions of getters and setters for node positions
     */
    public Integer getX(Node v) throws GalantException { 
        checkGraphElement(v);
        return v.getX();
    }
    public Integer getY(Node v) throws GalantException {
        checkGraphElement(v);
        return v.getY();
    }
    public Point getPosition(Node v) throws GalantException {
        checkGraphElement(v);
        return v.getPosition();
    }
    public void setX(Node v, int x) throws Terminate, GalantException {
        checkGraphElement(v);
        v.setX(x);
    }
    public void setY(Node v, int y) throws Terminate, GalantException {
        checkGraphElement(v);
        v.setY(y);
    }
    public void setPosition(Node v, int x, int y) throws Terminate, GalantException {
        checkGraphElement(v);
        v.setPosition(x, y);
    }

    /**
     * Displays a message during algorithm execution; the message could be
     * any object that has a toString() method
     */
    public void display(Object message) throws Terminate {
        graph.writeMessage("" + message);
    }

    /**
     * @return a string typed by the user in a dialog
     */
    public String getString(String prompt) throws Terminate {
        synchronizer.startStep();
        StringQuery query = new StringQuery(prompt);
        synchronizer.pauseExecution();
        query = null;           // to keep window from lingering when
                                // execution is terminated
        return dispatch.getStringAnswer();
    }

    /**
     * @return an integer typed by the user in a dialog
     */
    public Integer getInteger(String prompt) throws Terminate {
        synchronizer.startStep();
        IntegerQuery query = new IntegerQuery(prompt);
        synchronizer.pauseExecution();
        query = null;           // to keep window from lingering when
                                // execution is terminated
        return dispatch.getIntegerAnswer();
    }

    /**
     * @return a double typed by the user in a dialog
     */
    public Double getDouble(String prompt) throws Terminate {
        synchronizer.startStep();
        DoubleQuery query = new DoubleQuery(prompt);
        synchronizer.pauseExecution();
        query = null;           // to keep window from lingering when
                                // execution is terminated
        return dispatch.getDoubleAnswer();
    }

    /**
     * @todo need a consistent convention for use of Real in place of Double,
     * maybe even a new class
     */
    public Double getReal(String prompt) throws Terminate {
        return getDouble(prompt);
    }

    /**
     * @return an edge specified by the user in response to the prompt
     */
    public Edge getEdge(String prompt) throws Terminate {
        return graph.getEdge(prompt);
    }

    /**
     * @param prompt a message displayed in the edge selection dialog popup
     * @param restrictedSet the set from which the edge should be selected
     * @param errorMessage the message to be displayed if edge is not in
     * restrictedSet
     * @return a edge selected via a dialog during algorithm execution
     */
    public Edge getEdge(String prompt, EdgeSet restrictedSet, String errorMessage)
        throws Terminate {
        return graph.getEdge(prompt, restrictedSet, errorMessage);
    }

    /**
     * @return a node specified by the user in response to the prompt
     */
    public Node getNode(String prompt) throws Terminate {
        return graph.getNode(prompt);
    }

    /**
     * @param prompt a message displayed in the node selection dialog popup
     * @param restrictedSet the set from which the node should be selected
     * @param errorMessage the message to be displayed if node is not in
     * restrictedSet
     * @return a node selected via a dialog during algorithm execution
     */
    public Node getNode(String prompt, NodeSet restrictedSet, String errorMessage)
        throws Terminate {
        return graph.getNode(prompt, restrictedSet, errorMessage);
    }

    /**
     * Prints a string on the console (e.g., for debugging); as with display,
     * the message can be any object with a toString() method
     */
    public void print(Object o) {
        System.err.println("" + o);
    }

    /**
     * @todo Make sure these exceptions are caught by functions and algorithm
     * conversions from strings to numbers
     */
    public Integer integer(String s) throws NumberFormatException {
        return Integer.parseInt(s);
    }
    public Double real(String s) throws NumberFormatException {
        return Double.parseDouble(s);
    }

    /**
     * Displays the exception in a dialog window
     */
    public void displayException(Exception e) {
        ExceptionDialog.displayExceptionInDialog(e);
    }

    /** @see edu.ncsu.csc.Galant.graph.component.Graph#addNode() */
    public Node addNode(Integer x, Integer y) throws Terminate {
        return graph.addNode(x, y);
    }

    /** @see edu.ncsu.csc.Galant.graph.component.Graph#deleteNode(Node) */
    public void deleteNode(Node n) throws Terminate, GalantException {
        checkGraphElement(n);
        graph.deleteNode(n);
    }

    /** @see edu.ncsu.csc.Galant.graph.component.Graph#isDirected() */
    public boolean isDirected(){
        return graph.isDirected();
    }

    /** @see edu.ncsu.csc.Galant.graph.component.Graph#setDirected(boolean) */
    public void setDirected(boolean directed){
        graph.setDirected(directed);
    }

    public List<Node> getNodes() {
        return graph.getNodes();
    }

    public NodeSet getNodeSet() {
        return graph.getNodeSet();
    }

    /** this and the corresponding incantation for edges don't work; the
     * type/class NodeList has to be created inside the Graph class or we
     * need an additional copy constructor for NodeList */
//     public NodeList nodes() { return (NodeList) getNodes(); }

    public Integer numberOfNodes() {
        return graph.getNodes().size();
    }

    public List<Edge> getEdges() {
        return graph.getEdges();
    }

//     public EdgeList edges() { return (EdgeList) getEdges(); }

    public Integer numberOfEdges() {
        return graph.getEdges().size();
    }

    /** @see edu.ncsu.csc.Galant.graph.component.Graph#getStartNode() */
    public Node getStartNode() throws GalantException {
        return graph.getStartNode();
    }

    public Node startNode() throws GalantException {
        return getStartNode();
    }

    /** @see edu.ncsu.csc.Galant.graph.component.Graph#setRootNode(edu.ncsu.csc.Galant.graph.component.Node) */
    public void setRootNode(Node rootNode) {
        graph.setRootNode(rootNode);
    }

    /** @see edu.ncsu.csc.Galant.graph.component.Graph#getNodeById(int) */
    public Node getNodeById(int id) throws GalantException
    {
        return graph.getNodeById(id);
    }

    /** @see edu.ncsu.csc.Galant.graph.component.Graph#getEdgeById(int) */
    public Edge getEdgeById(int id) throws GalantException
    {
        return graph.getEdgeById(id);
    }

    /**
     * @return an Edge with the given source and target; if the graph is
     * undirected it doesn't matter which is which; returns null if no such
     * edge exists
     */
    public Edge getEdge(Node source, Node target) throws GalantException {
        return graph.getEdge(source, target);
    }

    /**
     * adds an edge based on the integer id's of the two endpoints
     * @see edu.ncsu.csc.Galant.graph.component.Graph#addEdge(int, int)
     */
    public Edge addEdge(int sourceId, int targetId) throws Terminate, GalantException {
        return graph.addEdge(sourceId, targetId);
    }

    /**
     * @see edu.ncsu.csc.Galant.graph.component.Graph#addEdge(edu.ncsu.csc.Galant.graph.component.Node,
     *      edu.ncsu.csc.Galant.graph.component.Node)
     */
    public Edge addEdge(Node source, Node target) throws Terminate {
        return graph.addEdge(source, target);
    }

    /** @see edu.ncsu.csc.Galant.graph.component.Graph#deleteEdge(Node) */
    public void deleteEdge(Edge e) throws Terminate {
        graph.deleteEdge(e);
    }

    /** @see edu.ncsu.csc.Galant.GraphDispatch#getAlgorithmState() */
    public int getState(){
        return dispatch.getAlgorithmState();
    }

    /** @see edu.ncsu.csc.Galant.GraphDispatch#getWindowWidth() */
    public int windowWidth(){
        return dispatch.getWindowWidth();
    }

    /** @see edu.ncsu.csc.Galant.GraphDispatch#getWindowHeight() */
    public int windowHeight(){
        return dispatch.getWindowHeight();
    }

    /** @see edu.ncsu.csc.Galant.graph.component.Graph#smartReposition() */
    public void smartReposition(){
        graph.smartReposition();
    }

    /**
     * used by algorithm to declare that it moves nodes
     */
    public void movesNodes() {
        dispatch.setAlgorithmMovesNodes(true);
    }

    /**
     * Defines the beginning of an animation step. Actions between a
     * beginStep() - endStep() pair all take place with a single invocation
     * of the step forward command
     *
     * If a step is in progress, initiated by an earlier beginStep() but not
     * yet terminated, effectively does an endStep() - @see startStep()
     */
    public void beginStep() throws Terminate {
        synchronizer.startStep();
        synchronizer.lock();
    }

    /**
     * Defines the end of an algorithm step.
     */
    public void endStep() throws Terminate {
        synchronizer.unlock();
        synchronizer.pauseExecution();
    }

    /**
     * has the effect of an endStep(); beginStep() pair, without the need for
     * a prior beginStep()
     */
    public void step() throws Terminate {
        beginStep();
    }

    /** Runs this algorithm on the given graph. */
    public abstract void run();
}

//  [Last modified: 2016 11 26 at 13:54:39 GMT]
