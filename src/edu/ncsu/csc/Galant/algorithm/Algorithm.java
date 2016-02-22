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
 * response.
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
    public static final String VIOLET    = "#8000ff";
    public static final String ORANGE    = "#ff8000"; 
    public static final String GRAY      = "#808080"; 
    public static final String BLACK     = "#000000"; 
    public static final String WHITE     = "#ffffff"; 

    /**
     * @param graph the Graph object on which this algorithm will run; used
     * when akgorithm is started up in GAlgorithmEditorPanel
     */
    public void setGraph(Graph graph){
        this.graph = graph;
    }
    public Graph getGraph() { return this.graph; }

    // sorting of edges and nodes
    /**
     * @todo these do not work; error is
     * error: name clash: sort(List<Node>) and sort(List<Edge>) have the same
     * erasure
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
//     public void sort(List<Node> L) {
//         Collections.sort(L);
//     }

    // Specialized Node/Edge types for Queues/Stacks/Priority Queues
    public class NodeQueue extends AbstractQueue<Node>
    {
        private Queue<Node> Q = new ArrayDeque<Node>();
 
        public void enqueue(Node v) {
            Q.offer(v);
        }
        public Node dequeue() {
            return Q.poll();
        }

        @Override
            public boolean offer(Node e)
            {
                return Q.offer(e);
            }
        @Override
            public Node poll()
            {
                return Q.poll();
            }
        @Override
            public Node peek()
            {
                return Q.peek();
            }
        @Override
            public Iterator<Node> iterator()
            {
                return Q.iterator();
            }
        @Override
            public int size()
            {
                return Q.size();
            }
    }
    public class EdgeQueue extends AbstractQueue<Edge> {
        private Queue<Edge> Q = new ArrayDeque<Edge>();

        public void enqueue(Edge e) {
            Q.offer(e);
        }
        public Edge dequeue() {
            return Q.poll();
        }

        @Override
            public boolean offer(Edge e)
            {
                return Q.offer(e);
            }
        @Override
            public Edge poll()
            {
                return Q.poll();
            }
        @Override
            public Edge peek()
            {
                return Q.peek();
            }
        @Override
            public Iterator<Edge> iterator()
            {
                return Q.iterator();
            }
        @Override
            public int size()
            {
                return Q.size();
            }
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

    /**
     * @todo These NodeList and EdgeList "typedefs" don't appear to work as
     * expected.
     */
    public interface NodeList extends List<Node> {
    }

    public interface EdgeList extends List<Edge> {
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
     * The following methods control the display of labels and weights during
     * algorithm execution; usually used at the beginning to hide unnecessary
     * information.
     */
    public void hideNodeLabels() throws Terminate { graph.hideNodeLabels(); }
    public void hideEdgeLabels() throws Terminate { graph.hideEdgeLabels(); }
    public void unhideNodeLabels() throws Terminate { graph.unhideNodeLabels(); }
    public void unhideEdgeLabels() throws Terminate { graph.unhideEdgeLabels(); }
    public void hideNodeWeights() throws Terminate { graph.hideNodeWeights(); }
    public void hideEdgeWeights() throws Terminate { graph.hideEdgeWeights(); }
    public void unhideNodeWeights() throws Terminate { graph.unhideNodeWeights(); }
    public void unhideEdgeWeights() throws Terminate { graph.unhideEdgeWeights(); }

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
    public void label(GraphElement ge, String s) throws Terminate {
        ge.setLabel(s);
    }

    public Node source(Edge e) { return e.getSourceNode(); }
    public Node target(Edge e) { return e.getTargetNode(); }

    public void set(GraphElement ge, String s) throws Terminate { ge.set(s); }
    public void clear(GraphElement ge, String s) throws Terminate { ge.clear(s); }
    public Boolean is(GraphElement ge, String s) { return ge.is(s); }

    public void color(GraphElement ge, String color) throws Terminate { ge.setColor(color); }
    public void unColor(GraphElement ge) throws Terminate { ge.clearColor(); }
    public String color(GraphElement ge) { return ge.getColor(); }

    public void hide(GraphElement ge) throws Terminate { ge.hide(); }
    public void show(GraphElement ge) throws Terminate { ge.show(); }
    public void hideLabel(GraphElement ge) throws Terminate { ge.hideLabel(); }
    public void showLabel(GraphElement ge) throws Terminate { ge.showLabel(); }
    public void setLabel(GraphElement ge, String label) throws Terminate { ge.setLabel(label); }
    public void hideWeight(GraphElement ge) throws Terminate { ge.hideWeight(); }
    public void showWeight(GraphElement ge) throws Terminate { ge.showWeight(); }
    public void setWeight(GraphElement ge, double weight) throws Terminate { ge.setWeight(weight); }

    /**
     * Displays a message during algorithm execution
     * @see edu.ncsu.csc.Galant.graph.component.Graph#writeMessage(String)
     */
    public void display(String message) throws Terminate {
        graph.writeMessage(message);
    }

    /**
     * Prints a string on the console (e.g., for debugging)
     */
    public void print(String string) {
        System.out.println(string);
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

    /** @see edu.ncsu.csc.Galant.graph.component.Graph#getNodes() */
    public List<Node> getNodes()
    {
        return graph.getNodes();
    }

    public Integer numberOfNodes() {
        return graph.getNodes().size();
    }

    /** @see edu.ncsu.csc.Galant.graph.component.Graph#setNodes(java.util.List) */
    public void setNodes(List<Node> nodes){
        graph.setNodes(nodes);
    }

    /** @see edu.ncsu.csc.Galant.graph.component.Graph#getEdges() */
    public List<Edge> getEdges()
    {
        return graph.getEdges();
    }

    public Integer numberOfEdges() {
        return graph.getEdges().size();
    }

    /** @see edu.ncsu.csc.Galant.graph.component.Graph#setEdges(java.util.List) */
    public void setEdges(List<Edge> edges){
        graph.setEdges(edges);
    }

    /** @see edu.ncsu.csc.Galant.graph.component.Graph#getStartNode() */
    public Node getStartNode() throws GalantException
    {
        return graph.getStartNode();
    }

    /** @see edu.ncsu.csc.Galant.graph.component.Graph#setRootNode(edu.ncsu.csc.Galant.graph.component.Node) */
    public void setRootNode(Node rootNode){
        graph.setRootNode(rootNode);
    }

    /** @see edu.ncsu.csc.Galant.graph.component.Graph#getNodeById(int) */
    public Node getNodeById (int id)
    {
        return graph.getNodeById(id);
    }

    /** @see edu.ncsu.csc.Galant.graph.component.Graph#getEdgeById(int) */
    public Edge getEdgeById(int id)
    {
        return graph.getEdgeById(id);
    }

    /**
     * adds an edge based on the integer id's of the two endpoints
     * @see edu.ncsu.csc.Galant.graph.component.Graph#addEdge(int, int)
     */
    public Edge addEdge(int sourceId, int targetId) throws Terminate {
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

//  [Last modified: 2016 02 21 at 22:53:48 GMT]
