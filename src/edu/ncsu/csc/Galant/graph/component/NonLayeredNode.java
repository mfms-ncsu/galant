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
 * Subclass of node made by 2021 Galant Team. This non-layered graph node will
 * keep every thing in its
 * parent but with override method getNodeCenter() and copyNode()
 *
 * @author Tianxin Jia, Ji Li
 *
 */
public class NonLayeredNode extends Node {

	/**
	 * create a blank instance for use when copying state at start of algorithm
	 * execution
	 */
	public NonLayeredNode() {
	}

	/**
	 * Basic constructor for nonLayeredNode
	 * 
	 * @param y
	 *            the y coordinate
	 * @param x
	 *            the x coordinate
	 * @param newId
	 *            the id of node
	 * @param graph
	 *            which graph this node should go
	 */
	public NonLayeredNode(Graph graph, Integer newId, Integer x, Integer y) {
		super(graph, newId, x, y);
	}

	/**
	 * This is called during parsing.
	 *
	 * @param L
	 *            an AttributeList created by the GraphMLParser from attributes of
	 *            the
	 *            node as given in the input text
	 * @throw GalantException if there is a problem in the format of an id,
	 *        x/y-coordinate, or, in case of layered graphs, layer information
	 */
	public NonLayeredNode(Graph graph, AttributeList L) throws GalantException {
		super(graph, L);
	}

	/**
	 * This is called during the transition from edit mode to animation mode, so
	 * that the animation modifies a copy of the edit graph and will have no impact
	 * on it.
	 *
	 * @param currentGraph
	 *            the graph accessed by the animation - see Algorithm.java
	 */
	public Node copyNode(Graph currentGraph) {
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

		return copy;
	}

	// made by 2021 Galant Team
	// this used to be in GraphPanel class but now it's a abstract method in Node
	// class
	// here we override the abstract method to give it actual functionality
	public Point getNodeCenter() throws GalantException {
		int state = dispatch.getDisplayState();
		Point nodeCenter = null;

		if ( dispatch.isAnimationMode()
				&& GraphDispatch.getInstance().algorithmMovesNodes() ) {
			nodeCenter = this.getPosition(state);
		} else {
			nodeCenter = this.getFixedPosition();
		}

		if ( nodeCenter == null ) {
			throw new GalantException("Unable to compute center for node " + this);
		}
		return nodeCenter;
	}

	public void initializeAfterParsing(AttributeList L) throws GalantException {
		String xString = L.getString("x");
		String yString = L.getString("y");
		Integer x = Integer.MIN_VALUE;
		Integer y = Integer.MIN_VALUE;
		if ( xString == null || yString == null ) {
			Random r = new Random();
			if ( xString == null ) {
				x = r.nextInt(GraphDispatch.getInstance().getWindowWidth());
			}
			if ( yString == null ) {
				y = r.nextInt(GraphDispatch.getInstance().getWindowHeight());
			}
		} else {
			try {
				x = Integer.parseInt(xString);
			} catch ( NumberFormatException e ) {
				throw new GalantException("Bad x-coordinate " + xString);
			}
			try {
				y = Integer.parseInt(yString);
			} catch ( NumberFormatException e ) {
				throw new GalantException("Bad y-coordinate " + yString);
			}
		} // x and y coordinates specified
			// remove their string versions
		L.remove("x");
		L.remove("y");

		// establish fixed positions
		xCoordinate = x;
		yCoordinate = y;
	}
}

// [Last modified: 2021 01 31 at 14:33:33 GMT]
