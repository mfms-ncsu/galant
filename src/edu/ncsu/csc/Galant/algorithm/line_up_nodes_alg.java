package edu.ncsu.csc.Galant.algorithm;
import java.util.*;
import edu.ncsu.csc.Galant.algorithm.Algorithm;
import edu.ncsu.csc.Galant.graph.component.Graph;
import edu.ncsu.csc.Galant.graph.component.Node;
import edu.ncsu.csc.Galant.graph.component.Edge;
import edu.ncsu.csc.Galant.algorithm.code.macro.Function;
import edu.ncsu.csc.Galant.algorithm.code.macro.Pair;
import edu.ncsu.csc.Galant.GalantException;
import edu.ncsu.csc.Galant.graph.component.GraphState;
public class line_up_nodes_alg extends Algorithm {
	public line_up_nodes_alg() {
		super();
		System.out.println("Created new line_up_nodes_alg using hardcoded implementation.");
		
	}
	
	@Override public void run(){
		GraphState gs = this.getGraph().getGraphState();
		gs.setInitializationComplete();
		
		/**
	
		    * A wrapper wrapper class. Compiler didn't like having
		    * multiple classes which refer to each other, so we'll
		    * do this and just resort to actual Java.
		    */
		    class Boruvka {
		        /**
		        * Class representing a cloud or set of nodes
		        * while running Boruvka's algorithm.
		        *
		        * Supports merging clouds, which removes all
		        * nodes from the other set, and adding nodes.
		        * Can also mark/unmark all nodes in set.
		        */
		        class NodeSet {
		            /** list of all nodes in the set */
		            public ArrayList<Node> list = new ArrayList<Node>();
		    
		            /** The lowest connecting edge on the set */
		            public Edge lowest;
		     
		            /** Remove all nodes from a set s and add them to the list */
		            public void merge(NodeSet s) {
		                // While the other set still has nodes in it
		                while (s.list.size() > 0) {
		                    // Remove the Node
		                    add(s.list.remove(0));
		                }
		            }
		      
		            /** Add a node to the set */
		            public void add(Node n) {
		                list.add(n);
		            }
		      
		            /** Mark all nodes */
		            public void mark() {
		                for (int i = 0; i < list.size(); i++)
		                    list.get(i).mark();
		            }
		      
		            /** Unmark all nodes */
		            public void unmark() {
		                for (int i = 0; i < list.size(); i++)
		                    list.get(i).unMark();
		            }
		        }
		      
		        /**
		        * Find the set associated with a node
		        */
		        public NodeSet findSet(Node n) {
		            for (int i = 0; i < sets.size(); i++) {
		                if (sets.get(i).list.contains(n))
		                    return sets.get(i);
		            }
		            return null;
		        }
		      
		        /** Track the disjoint sets of nodes */
		        ArrayList<NodeSet> sets;
		      
		        /**
		        * Implementation of Boruvka's algorithm for finding a minimal spanning tree.
		        */
		        public void start() {
		            sets = new ArrayList<NodeSet>();
		      
		            // Set graph to undirected
		            setDirected(false);
		      
		            // Initialize sets of nodes
		            for (Node n: graph.getNodes()) {
		                NodeSet s = new NodeSet();
		                s.add(n);
		                sets.add(s);
		            }
		      
		            // Continue to coalesce NodeSets until only one remains
		            while (sets.size() > 1) {
		                // Step through each set, find the lowest connecting edge
		                for (int i = 0; i < sets.size(); i++) {
		                    beginStep();
		                    Edge lowest = null;
		                    double weight = Double.POSITIVE_INFINITY;
		      
		                    // Step through the set
		                    NodeSet currentSet = sets.get(i);
		                    currentSet.mark();
		                    for (int j = 0; j < currentSet.list.size(); j++) {
		                        Node x = currentSet.list.get(j); // Grab the current node
		                        x.setSelected(true);
		                        beginStep();
		      
		                        // For all adjacent nodes
		                        for(Edge e : x.getIncidentEdges()) {
									Node o = x.travel(e);
		                            o.mark();
		                            beginStep();
		      
		                            // If not in the same set and lower edge weight, set as min
		                            if (findSet(o) != currentSet) {
		                                o.unMark();
		      
		                                if (e.getWeight() < weight) {
		                                    lowest = e;
		                                    weight = lowest.getWeight();
		                                }
		                            }
		                        }
		                        x.setSelected(false);
		                    }
		      
		                    // Set the sets lowest edge
		                    currentSet.lowest = lowest;
		                    currentSet.unmark();
		                }
		      
		                // Step through each set again to merge them
		                for (int i = 0; i < sets.size(); i++) {
		                    NodeSet current = sets.get(i);
		                    current.mark();
		                    beginStep();
		      
		                    while (current.lowest != null) {
		                        // Find the connecting set
		                        NodeSet opp;
		                        if (findSet(current.lowest.getSourceNode()) == current) {
		                            opp = findSet(current.lowest.getDestNode());
		                        }
		                        else {
		                            opp = findSet(current.lowest.getSourceNode());
		                        }
		      
		      
		                        // Merge them
		                        sets.get(i).merge(opp);
		                        current.lowest.setSelected(true);
		                        current.mark();
		      
		                        if (opp.lowest != null && findSet(opp.lowest.getSourceNode()) == current &&
		                            findSet(opp.lowest.getDestNode()) == current) {
		                            current.lowest = null;
		                        }
		                        else {
		                            current.lowest = opp.lowest;
		                            beginStep();
		                        }
		      
		                        // Remove the opposite set
		                        sets.remove(opp);
		                        i--;
		                    }
		      
		                    current.unmark();
		                }
		            }
		            // Mark all nodes
		            sets.get(0).mark();
		        }
		    }
		      
		    // Run the algorithm
		    new Boruvka().start();
		    
		    this.gw.getGraphPanel().setAlgorithmComplete();
		      
		    }
	
	
	 
}