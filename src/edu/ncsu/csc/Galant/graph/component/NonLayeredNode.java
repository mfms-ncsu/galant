package edu.ncsu.csc.Galant.graph.component;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import edu.ncsu.csc.Galant.GalantException;
import edu.ncsu.csc.Galant.GraphDispatch;
import edu.ncsu.csc.Galant.algorithm.Terminate;
import edu.ncsu.csc.Galant.graph.datastructure.EdgeList;
import edu.ncsu.csc.Galant.graph.datastructure.NodeList;
import edu.ncsu.csc.Galant.graph.datastructure.NodeSet;

/**
 * Nodes that are used for Non-LayeredGraph
 *
 * @author Tianxin Jia
 *
 */
public class NonLayeredNode extends Node {

	/**
	 * create a blank instance for use when copying state at start of algorithm
	 * execution
	 */
	public NonLayeredNode(){
		
	}


	/**
	 * This is called during parsing.
	 *
	 * @param L an AttributeList created by the GraphMLParser from attributes of the
	 *          node as given in the input text
	 * @throw GalantException if there is a problem in the format of an id,
	 *        x/y-coordinate, or, in case of layered graphs, layer information
	 */
	public NonLayeredNode(Graph graph, AttributeList L) throws GalantException{
		super(graph, L);
	}

	/**
	 * This is called during the transition from edit mode to animation mode, so
	 * that the animation modifies a copy of the edit graph and will have no impact
	 * on it.
	 *
	 * @param currentGraph the graph accessed by the animation - see Algorithm.java
	 */
	public Node copyNode(Graph currentGraph){		
		NonLayeredNode copy = new NonLayeredNode();
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

		// made by 2021 Galant Team
		// add a line for the new flag
		System.out.println("NLGHere");
		copy.setpos = this.setpos;
		return copy;
	}
	
	// made by 2021 Galant Team
	// this used to be in GraphPanel class
	// Now we move it to Node class and LayeredGraphNode class. 
	public Point getNodeCenter() throws GalantException{
		int state = dispatch.getDisplayState();
		Point nodeCenter = null;

		if ( dispatch.isAnimationMode() && GraphDispatch.getInstance().algorithmMovesNodes() ){
			nodeCenter = this.getPosition(state);
		} else{
			nodeCenter = this.getFixedPosition();
		}
		

		if ( nodeCenter == null ){
			throw new GalantException("Unable to compute center for node " + this);
		}
		return nodeCenter;
	}

}

//  [Last modified: 2021 01 31 at 14:33:33 GMT]
