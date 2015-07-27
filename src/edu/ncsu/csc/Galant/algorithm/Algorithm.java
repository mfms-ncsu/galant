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
// import java.util.ArrayDeque;
// import java.util.ArrayList;
// import java.util.Iterator;
// import java.util.List;
// import java.util.PriorityQueue;
// import java.util.Queue;
// import java.util.Stack;
import edu.ncsu.csc.Galant.graph.component.Edge;
import edu.ncsu.csc.Galant.graph.component.Graph;
import edu.ncsu.csc.Galant.graph.component.GraphState;
import edu.ncsu.csc.Galant.graph.component.Node;
import edu.ncsu.csc.Galant.gui.window.GraphWindow;

/** 
 * Represents a runnable algorithm. 
 * @author Jason Cockrell, Ty Devries, Alex McCabe, Michael Owoc
 */
public abstract class Algorithm implements Runnable{
	
	public GraphWindow gw;
	
		// Specialized Node/Edge types for Queues/Stacks/Priority Queues
		public class NodeQueue extends AbstractQueue<Node>
			{
				private Queue<Node> Q = new ArrayDeque<Node>();
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
		public class EdgeQueue extends AbstractQueue<Edge>{
				private Queue<Edge> Q = new ArrayDeque<Edge>();
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
		public class NodePriorityQueue extends PriorityQueue<Node>
			{}
		public class EdgePriorityQueue extends PriorityQueue<Edge>
			{}

		/** A list of all the runnable algorithms. */
		public static final List<Algorithm> algorithms = new ArrayList<Algorithm>();

		/** The graph on which the algorithm is being run. */
		public Graph graph;

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
     * Initializes data structures as a convenience
     * @todo could be used more generally
     */
    public void initialize() {
		nodeQ = new NodeQueue();
		edgeQ = new EdgeQueue();
		nodeStack = new NodeStack();
		edgeStack = new EdgeStack();
		nodePQ = new NodePriorityQueue();
		edgePQ = new EdgePriorityQueue();
    }

		/** @see edu.ncsu.csc.Galant.graph.component.Graph */
		public Graph getGraph(){
				return graph;
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
		// TODO: some of these might not be supposed to be accessible by the user?

		/** @see edu.ncsu.csc.Galant.graph.component.Graph#addNode(Node) */
		public void addNode(Node n){
				graph.addNode(n);
			}

		/** @see edu.ncsu.csc.Galant.graph.component.Graph#addNode() */
		public Node addNode(){
				return graph.addNode();
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

		/** @see edu.ncsu.csc.Galant.graph.component.Graph#setNodes(java.util.List) */
		public void setNodes(List<Node> nodes){
				graph.setNodes(nodes);
			}

		/** @see edu.ncsu.csc.Galant.graph.component.Graph#getEdges() */
		public List<Edge> getEdges()
        {
            return graph.getEdges();
        }

		/** @see edu.ncsu.csc.Galant.graph.component.Graph#setEdges(java.util.List) */
		public void setEdges(List<Edge> edges){
				graph.setEdges(edges);
			}

		/** @see edu.ncsu.csc.Galant.graph.component.Graph#getRootNode() */
		public Node getRootNode()
			{
				return graph.getRootNode();
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
		public void select(int id){
				graph.select(id);
			}

		/** @see edu.ncsu.csc.Galant.graph.component.Graph#select(edu.ncsu.csc.Galant.graph.component.Node) */
		public void select(Node n){
				graph.select(n);
			}

    /** id does not matter; hence the -1 */
		public void addEdge(int sourceId, int targetId){
            graph.addEdge(sourceId, targetId);
        }

		/**
		 * @see edu.ncsu.csc.Galant.graph.component.Graph#addEdge(edu.ncsu.csc.Galant.graph.component.Node,
		 *      edu.ncsu.csc.Galant.graph.component.Node)
		 */
		public void addEdge(Node source, Node target){
				graph.addEdge(source, target);
			}

		/** @see edu.ncsu.csc.Galant.graph.component.Graph#addEdge(edu.ncsu.csc.Galant.graph.component.Edge) */
		public void addEdge(Edge e){
				graph.addEdge(e);
			}

		/** @see edu.ncsu.csc.Galant.graph.component.Graph#getState() */
		public int getState(){
				return graph.getState();
			}

		/** @see edu.ncsu.csc.Galant.graph.component.Graph#getGraphState() */
		public GraphState getGraphState(){
				return graph.getGraphState();
			}

        /** @see edu.ncsu.csc.Galant.GraphDispatch#getWindowWidth() */
        public int windowWidth(){
            return GraphDispatch.getInstance().getWindowWidth();
        }

        /** @see edu.ncsu.csc.Galant.GraphDispatch#getWindowHeight() */
        public int windowHeight(){
            return GraphDispatch.getInstance().getWindowHeight();
        }

		/** @see edu.ncsu.csc.Galant.graph.component.Graph#smartReposition() */
		public void smartReposition(){
				graph.smartReposition();
			}

		/** @see edu.ncsu.csc.Galant.graph.component.GraphState */
		public void beginStep(){
				if(graph.getGraphState().isLocked()) endStep();
				graph.getGraphState().incrementState();
				graph.getGraphState().setLocked(true);
			}

		/** @see edu.ncsu.csc.Galant.graph.component.GraphState */
		public void endStep(){
				graph.getGraphState().setLocked(false);
				graph.getGraphState().pauseExecution();
			}

		/** Runs this algorithm on the given graph. */
		public abstract void run();
	}

//  [Last modified: 2015 07 27 at 15:51:26 GMT]
