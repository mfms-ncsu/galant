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

import edu.ncsu.csc.Galant.GalantException;
import edu.ncsu.csc.Galant.GraphDispatch;
import edu.ncsu.csc.Galant.graph.component.Edge;
import edu.ncsu.csc.Galant.graph.component.Graph;
import edu.ncsu.csc.Galant.graph.component.Node;
import edu.ncsu.csc.Galant.gui.window.GraphWindow;
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
     * @todo The NodeList and EdgeList "typedefs" don't appear to work as expected.
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

    /** @see edu.ncsu.csc.Galant.graph.component.Graph */
    public Graph getGraph() {
        return graph;
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

    public int nodeIds() {
        return graph.nodeIds();
    }

    public int edgeIds() {
        return graph.edgeIds();
    }

    /**
     * Sets the current <code>Graph</code> to the specified </code>Graph</code>
     * @param graph the new <code>Graph</code> on which this <code>Algorithm</code> will run
     */
    public void setGraph(Graph graph){
        this.graph = graph;
    }

    // methods for subclasses to access API methods directly
    // can be easily added to in Eclipse with Source > Generate Delegate Methods...
    /**
     * @todo some of these algorithm methods might not be supposed to be
     * accessible by the user?
     */

    /**
     * Displays a message during algorithm execution
     * @see edu.ncsu.csc.Galant.graph.component.Graph#writeMessage(String)
     */
    public void display(String message) throws Terminate {
        graph.writeMessage(message);
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

    /** @see edu.ncsu.csc.Galant.graph.component.Graph#select(int) */
    public void select(int id) throws Terminate {
        graph.select(id);
    }

    /** @see edu.ncsu.csc.Galant.graph.component.Graph#select(edu.ncsu.csc.Galant.graph.component.Node) */
    public void select(Node n) throws Terminate {
        graph.select(n);
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

//  [Last modified: 2015 12 08 at 14:02:35 GMT]
