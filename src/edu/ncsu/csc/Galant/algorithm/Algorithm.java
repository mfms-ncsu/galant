package edu.ncsu.csc.Galant.algorithm;

import java.util.*;
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
import edu.ncsu.csc.Galant.GalantException;

/** 
 * Represents a runnable algorithm. 
 * @author Jason Cockrell, Ty Devries, Alex McCabe, Michael Owoc
 */
public abstract class Algorithm
	{
		// Specialized Node/Edge types for Queues/Stacks/Priority Queues
		protected class NodeQueue extends AbstractQueue<Node>
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
		protected class EdgeQueue extends AbstractQueue<Edge>
			{
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
		protected class NodeStack extends Stack<Node>
			{}
		protected class EdgeStack extends Stack<Edge>
			{}
		protected class NodePriorityQueue extends PriorityQueue<Node>
			{}
		protected class EdgePriorityQueue extends PriorityQueue<Edge>
			{}

		/** A list of all the runnable algorithms. */
		public static final List<Algorithm> algorithms = new ArrayList<Algorithm>();

		/** The graph on which the algorithm is being run. */
		protected Graph graph;

		// Pre-existing queue/stack/priority queue objects
		protected NodeQueue nodeQ = new NodeQueue();
		protected EdgeQueue edgeQ = new EdgeQueue();
		protected NodeStack nodeStack = new NodeStack();
		protected EdgeStack edgeStack = new EdgeStack();
		protected NodePriorityQueue nodePQ = new NodePriorityQueue();
		protected EdgePriorityQueue edgePQ = new EdgePriorityQueue();

		public Algorithm()
			{
				algorithms.add(this);
			}

		/** @see edu.ncsu.csc.Galant.graph.component.Graph */
		public Graph getGraph()
			{
				return graph;
			}

		/**
		 * Sets the current <code>Graph</code> to the specified </code>Graph</code>
		 * @param graph the new <code>Graph</code> on which this <code>Algorithm</code> will run
		 */
		public void setGraph(Graph graph)
			{
				this.graph = graph;
			}

		// methods for subclasses to access API methods directly
		// can be easily added to in Eclipse with Source > Generate Delegate Methods...
		// TODO: some of these might not be supposed to be accessible by the user?

		/** @see edu.ncsu.csc.Galant.graph.component.Graph#addNode(Node) */
		protected void addNode(Node n)
			{
				graph.addNode(n);
			}

		/** @see edu.ncsu.csc.Galant.graph.component.Graph#addNode() */
		protected Node addNode()
			{
				return graph.addNode();
			}

		/** @see edu.ncsu.csc.Galant.graph.component.Graph#isDirected() */
		protected boolean isDirected()
			{
				return graph.isDirected();
			}

		/** @see edu.ncsu.csc.Galant.graph.component.Graph#setDirected(boolean) */
		protected void setDirected(boolean directed)
			{
				graph.setDirected(directed);
			}

		/** @see edu.ncsu.csc.Galant.graph.component.Graph#getNodes() */
		protected List<Node> getNodes()
            throws GalantException
        {
            return graph.getNodes();
        }

		/** @see edu.ncsu.csc.Galant.graph.component.Graph#setNodes(java.util.List) */
		protected void setNodes(List<Node> nodes)
			{
				graph.setNodes(nodes);
			}

		/** @see edu.ncsu.csc.Galant.graph.component.Graph#getEdges() */
		protected List<Edge> getEdges()
            throws GalantException
        {
            return graph.getEdges();
        }

		/** @see edu.ncsu.csc.Galant.graph.component.Graph#setEdges(java.util.List) */
		protected void setEdges(List<Edge> edges)
			{
				graph.setEdges(edges);
			}

		/** @see edu.ncsu.csc.Galant.graph.component.Graph#getRootNode() */
		protected Node getRootNode() throws GalantException
			{
				return graph.getRootNode();
			}

		/** @see edu.ncsu.csc.Galant.graph.component.Graph#setRootNode(edu.ncsu.csc.Galant.graph.component.Node) */
		protected void setRootNode(Node rootNode)
			{
				graph.setRootNode(rootNode);
			}

		/** @see edu.ncsu.csc.Galant.graph.component.Graph#getNodeById(int) */
		protected Node getNodeById (int id) throws GalantException
			{
				return graph.getNodeById(id);
			}

		/** @see edu.ncsu.csc.Galant.graph.component.Graph#getEdgeById(int) */
		protected Edge getEdgeById(int id) throws GalantException
			{
				return graph.getEdgeById(id);
			}

		/** @see edu.ncsu.csc.Galant.graph.component.Graph#select(int) */
		protected void select(int id)
			{
				graph.select(id);
			}

		/** @see edu.ncsu.csc.Galant.graph.component.Graph#select(edu.ncsu.csc.Galant.graph.component.Node) */
		protected void select(Node n)
			{
				graph.select(n);
			}

		/** @see edu.ncsu.csc.Galant.graph.component.Graph#addEdge(int, int) */
		protected void addEdge(int sourceId, int targetId)
			{
				graph.addEdge(sourceId, targetId);
			}

		/**
		 * @see edu.ncsu.csc.Galant.graph.component.Graph#addEdge(edu.ncsu.csc.Galant.graph.component.Node,
		 *      edu.ncsu.csc.Galant.graph.component.Node)
		 */
		protected void addEdge(Node source, Node target)
			{
				graph.addEdge(source, target);
			}

		/** @see edu.ncsu.csc.Galant.graph.component.Graph#addEdge(edu.ncsu.csc.Galant.graph.component.Edge) */
		protected void addEdge(Edge e)
			{
				graph.addEdge(e);
			}

		/** @see edu.ncsu.csc.Galant.graph.component.Graph#getState() */
		protected int getState()
			{
				return graph.getState();
			}

		/** @see edu.ncsu.csc.Galant.graph.component.Graph#getGraphState() */
		protected GraphState getGraphState()
			{
				return graph.getGraphState();
			}

		/** @see edu.ncsu.csc.Galant.graph.component.Graph#smartReposition() */
		protected void smartReposition()
			{
				graph.smartReposition();
			}

		/** @see edu.ncsu.csc.Galant.graph.component.GraphState */
		protected void beginStep()
			{
				graph.getGraphState().resetLocks();
				graph.getGraphState().incrementState();
				graph.getGraphState().setLocked(true);
			}

		/** @see edu.ncsu.csc.Galant.graph.component.GraphState */
		protected void endStep()
			{
				graph.getGraphState().setLocked(false);
			}

		/** Runs this algorithm on the given graph. */
		public abstract void run();
	}

//  [Last modified: 2015 05 14 at 15:59:06 GMT]
