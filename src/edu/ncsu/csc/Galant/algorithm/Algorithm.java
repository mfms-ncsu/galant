/** 
 * Has the all the basic infrastructure for running an algorithm in a
 * separate thread and various utility classes and methods for algorithm
 * implementations.
 *
 * @author Jason Cockrell, Ty Devries, Alex McCabe, Michael Owoc; with major
 * enhancements by Matthias Stallmann
 *
 * @todo Once the thread situation is straightened out, it is high time to
 * consider two methods used in GDR:
 * - Node getNodeByQuery(String message)
 * - String query(String message)
 * These should prompt the display to pop up a window with the message and
 * pause execution, to be resumed when the user has produced the appropriate
 * response. Done for edge using getEdge(String). Work in progress in the
 * component-selection branch
 *
 * @todo Add method(s) to change the direction of an edge; this would involve
 * both swapping source and target and changing the lists of incoming and
 * outgoing edges of each endpoint.
 */

package edu.ncsu.csc.Galant.algorithm;

import java.util.AbstractQueue;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Stack;
import java.util.Collections;
import java.awt.Color;

import edu.ncsu.csc.Galant.GalantException;
import edu.ncsu.csc.Galant.gui.util.ExceptionDialog;
import edu.ncsu.csc.Galant.GraphDispatch;
import edu.ncsu.csc.Galant.graph.component.Graph;
import edu.ncsu.csc.Galant.graph.component.LayeredGraph;
import edu.ncsu.csc.Galant.graph.component.GraphElement;
import edu.ncsu.csc.Galant.graph.component.Node;
import edu.ncsu.csc.Galant.graph.component.Edge;
import edu.ncsu.csc.Galant.gui.window.GraphWindow;
import edu.ncsu.csc.Galant.gui.window.GraphWindow.GraphDisplays;
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

    public class NodeList extends ArrayList<Node> {
    }

    public class EdgeList extends ArrayList<Edge> {
    }

    // sorting of edges and nodes
    /**
     * @todo these do not work; error is error: name clash: sort(List<Node>)
     * and sort(List<Edge>) have the same erasure; for now, I'm settling for
     * sorting edges only; sorting nodes makes sense for crossing
     * minimization but there it's in a specialized context.
     *
     * I also tried GraphElement, but that results in
     * incompatible types:
     * java.util.List<edu.ncsu.csc.Galant.graph.component.Edge> cannot be
     * converted to
     * java.util.List<edu.ncsu.csc.Galant.graph.component.GraphElement>
     */
    public void sort(List<Edge> L) {
        Collections.sort(L);
    }

    //     public void sort(NodeList L) {
//         Collections.sort(L);
//     }

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

    public class NodePriorityQueue extends PriorityQueue<Node> {
        public Node removeMin() {
            return this.poll();
        }
        // not efficient, but it shouldn't matter with small graphs
        public void decreaseKey(Node v, double newKey) throws Terminate {
            this.remove(v);
            v.setWeight(newKey);
            this.add(v);
        }
    }

    public class EdgePriorityQueue extends PriorityQueue<Edge> {
        public Edge removeMin() {
            return this.poll();
        }
        // not efficient, but it shouldn't matter with small graphs
        public void decreaseKey(Edge e, double newKey) throws Terminate {
            this.remove(e);
            e.setWeight(newKey);
            this.add(e);
        }
    }

    // Pre-existing queue/stack/priority queue objects
    public NodeQueue nodeQ = new NodeQueue();
    public EdgeQueue edgeQ = new EdgeQueue();
    public NodeStack nodeStack = new NodeStack();
    public EdgeStack edgeStack = new EdgeStack();
    public NodePriorityQueue nodePQ = new NodePriorityQueue();
    public EdgePriorityQueue edgePQ = new EdgePriorityQueue();

    public Algorithm(){
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
        synchronizer.pauseExecution();
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
     * algorithm execution; usually used at the beginning to hide unnecessary
     * information. Individual showing and hiding of Node/Edge labels or
     * weights takes effect only if these have been made visible.
     */

    /** makes node labels invisible */
    public void hideNodeLabels() throws Terminate { graph.hideNodeLabels(); }
    /** makes edge labels invisible */
    public void hideEdgeLabels() throws Terminate { graph.hideEdgeLabels(); }
    /** makes node labels visible */
    public void unhideNodeLabels() throws Terminate { graph.unhideNodeLabels(); }
    /** makes node labels visible */
    public void showNodeLabels() throws Terminate { graph.unhideNodeLabels(); }
    /** makes edge labels visible */
    public void unhideEdgeLabels() throws Terminate { graph.unhideEdgeLabels(); }
    /** makes edge labels visible */
    public void showEdgeLabels() throws Terminate { graph.unhideEdgeLabels(); }
    /** makes node weights invisible */
    public void hideNodeWeights() throws Terminate { graph.hideNodeWeights(); }
    /** makes edge weights invisible */
    public void hideEdgeWeights() throws Terminate { graph.hideEdgeWeights(); }
    /** makes node weights visible */
    public void unhideNodeWeights() throws Terminate { graph.unhideNodeWeights(); }
    /** makes node weights visible */
    public void showNodeWeights() throws Terminate { graph.unhideNodeWeights(); }
    /** makes edge weights visible */
    public void unhideEdgeWeights() throws Terminate { graph.unhideEdgeWeights(); }
    /** makes edge weights visible */
    public void showEdgeWeights() throws Terminate { graph.unhideEdgeWeights(); }

    /**
     * The following show or hide all Node/Edge labels or weights during
     * execution. They take effect only if the corresponding items are
     * visible overall. What they do is to iteratively show or hide weight or
     * label of each individual Node/Edge
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
    public int id(Node n) {
        return n.getId();
    }

    public int id(Edge e) {
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
    public void highlight(GraphElement ge) throws Terminate { ge.highlight(); }
    public void unHighlight(GraphElement ge) throws Terminate { ge.unHighlight(); }
    public void unhighlight(GraphElement ge) throws Terminate { ge.unHighlight(); }
    public Boolean highlighted(GraphElement ge) { return ge.isHighlighted(); }
    public Boolean isHighlighted(GraphElement ge) { return ge.isHighlighted(); }
    /** selected is a synonym for highlighted */
    public void select(GraphElement ge) throws Terminate { ge.setSelected(true); }
    public void deselect(GraphElement ge) throws Terminate { ge.setSelected(false); }
    public Boolean selected(GraphElement ge) { return ge.isSelected(); }
    public Boolean isSelected(GraphElement ge) { return ge.isSelected(); }

    public void mark(Node n) throws Terminate { n.mark(); }
    public void unMark(Node n) throws Terminate { n.unMark(); }
    public Boolean marked(Node n) { return n.isMarked(); }
    public Boolean isMarked(Node n) { return n.isMarked(); }

    public Double weight(GraphElement ge) { return ge.getWeight(); }
    public String label(GraphElement ge) { return ge.getLabel(); }
    /**
     * @todo It would be useful to have a class AttributeParser with a method
     * parse(String) that returns an Integer, Double, Boolean or String,
     * depending on whether the string can be successfully parse as one of
     * these (attempted in the given order). The mechanism already exists in
     * GraphMLParser. The new class should probably live in graph/component.
     */
    public void setLabel(GraphElement ge, Object s) throws Terminate {
        ge.setLabel("" + s);
    }

    public Node source(Edge e) { return e.getSourceNode(); }
    public Node target(Edge e) { return e.getTargetNode(); }

    public void set(GraphElement ge, String s) throws Terminate { ge.set(s); }
    public void clear(GraphElement ge, String s) throws Terminate { ge.clear(s); }
    public Boolean is(GraphElement ge, String s) { return ge.is(s); }

    public void color(GraphElement ge, String color) throws Terminate { ge.setColor(color); }
    public void unColor(GraphElement ge) throws Terminate { ge.clearColor(); }
    public void uncolor(GraphElement ge) throws Terminate { ge.clearColor(); }
    public String color(GraphElement ge) { return ge.getColor(); }

    public void hide(GraphElement ge) throws Terminate { ge.hide(); }
    public void show(GraphElement ge) throws Terminate { ge.show(); }
    public Boolean isHidden(GraphElement ge) { return ge.isHidden(getState()); }
    public Boolean isVisible(GraphElement ge) { return ! ge.isHidden(getState()); }
    public void hideLabel(GraphElement ge) throws Terminate { ge.hideLabel(); }
    public void showLabel(GraphElement ge) throws Terminate { ge.showLabel(); }
    public Boolean labelIsHidden(GraphElement ge) {
        return ge.labelIsHidden(getState());
    }
    public Boolean labelIsVisible(GraphElement ge) { return ! labelIsHidden(ge); }
    public void setLabel(GraphElement ge, String label) throws Terminate {
        ge.setLabel(label);
    }
    public void hideWeight(GraphElement ge) throws Terminate { ge.hideWeight(); }
    public void showWeight(GraphElement ge) throws Terminate { ge.showWeight(); }
    public Boolean weightIsHidden(GraphElement ge) {
        return ge.weightIsHidden(getState());
    }
    public Boolean weightIsVisible(GraphElement ge) { return ! weightIsHidden(ge); }
    public void setWeight(GraphElement ge, double weight) throws Terminate {
        ge.setWeight(weight);
    }
    public int degree(Node v) { return v.getDegree(); }
    public int indegree(Node v) { return v.getIndegree(); }
    public int outdegree(Node v) { return v.getOutdegree(); }
    public Node otherEnd(Node v, Edge e) { return v.travel(e); }
    public Node otherEnd(Edge e, Node v) { return v.travel(e); }
    public List<Node> neighbors(Node v) { return v.getAdjacentNodes(); }
    public Node firstNode(List<Node> L) { return L.get(0); }
    public List rest(List L)  { return L.subList(1, L.size()); }

    /**
     error: name clash: first(List<Edge>) and first(List<Node>) have the same erasure
    [javac]     public Edge first(List<Edge> L) { return L.get(0); }
     */
     public Edge firstEdge(List<Edge> L) { return L.get(0); }
//     public List<Edge> rest(List<Edge> L) {
//         return L.subList(1, L.size());
//     }

    /**
     * Displays a message during algorithm execution; the message could be
     * any object that has a toString() method
     */
    public void display(Object message) throws Terminate {
        graph.writeMessage("" + message);
    }


    /**
     * @return an edge specified by the user in response to the prompt
     */
    public Edge getEdge(String prompt) throws Terminate {
        return graph.getEdge(prompt);
    }

    /**
     * @return a node specified by the user in response to the prompt
     */
    public Node getNode(String prompt) throws Terminate {
        return graph.getNode(prompt);
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

    /** @see edu.ncsu.csc.Galant.graph.component.Graph#addNode(Node) */
    public void addNode(Node n){
        graph.addNode(n);
    }

    /** @see edu.ncsu.csc.Galant.graph.component.Graph#addNode() */
    public Node addNode(Integer x, Integer y) throws Terminate {
        return graph.addNode(x, y);
    }

    /** @see edu.ncsu.csc.Galant.graph.component.Graph#deleteNode(Node) */
    public void deleteNode(Node n) throws Terminate {
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

    /** this and the corresponding incantation for edges don't work; the
     * type/class NodeList has to be created inside the Graph class */
    public NodeList nodes() { return (NodeList) getNodes(); }

    public Integer numberOfNodes() {
        return graph.getNodes().size();
    }

    public List<Edge> getEdges() {
        return graph.getEdges();
    }

    public EdgeList edges() { return (EdgeList) getEdges(); }

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
    public Edge getEdge(Node source, Node target) {
        Edge edge = null;
        try {
            edge = graph.getEdge(source, target);
        }
        catch (GalantException e) {
            e.report("Warning:");
        }
        return edge;
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

    public void beginStep() throws Terminate {
        synchronizer.startStep(); // needed to check for termination
        if ( synchronizer.isLocked() ) endStep();
        synchronizer.startStep();
        synchronizer.lock();
    }

    public void endStep() {
        synchronizer.unlock();
        synchronizer.pauseExecution();
    }

    /** Runs this algorithm on the given graph. */
    public abstract void run();
}

//  [Last modified: 2016 07 03 at 16:19:50 GMT]
