package edu.ncsu.csc.Galant.graph.component;

import java.awt.Point;

import edu.ncsu.csc.Galant.GalantException;
import edu.ncsu.csc.Galant.GraphDispatch;

public class LayeredGraphNode extends Node {
	
	private final int HORIZONTAL_PADDING = 100;
	private final int VERTICAL_PADDING = 100;
	

	public LayeredGraphNode(Graph graph, AttributeList L) throws GalantException {
		super(graph, L);
	}


	public Point getlayeredNodeCenter() throws GalantException{
		int state = dispatch.getDisplayState();
		Point nodeCenter = null;

		if ( dispatch.isAnimationMode() && GraphDispatch.getInstance().algorithmMovesNodes() ){
			nodeCenter = this.getPosition(state);
		} else{
			nodeCenter = this.getFixedPosition();
		}

		// made by 2021 Galant Team
		// if graph is layered and node has layer and position in layer
		// information, base its location on that

		// THIS PARAGRAPH IS NOT APPLIED ANYMORE, I left it here to track the previous version
		// If the node is already dragged, don't reposition it.
		// This mainly for the scaling since I think this method is called constantly
		// So I must stop it to allow us reposition the node.
		// I should reset the flag when scaling, but I don't know where is it.

		// Now my strategy is only call this part of node if the physical position
		// is not set. That means the Graph is just loaded or the window is just
		// resized.
		
		
		if ( ! this.setpos ){
			int x = 0;
			int y = 0;
			int layer = this.getLayer(); // should not change during an
										// animation of a layered graph algorithm
			int position = this.getPositionInLayer(state);
			int layerSize = 1;
			// vertical layered graphs have gaps in positions on some layers,
			// i.e., positions on some layers are not contiguous; in that
			// case, positions should be taken "literally", i.e., position p
			// means the same thing on every layer
			if ( dispatch.getWorkingGraph().isVertical() ){
				layerSize = dispatch.getWorkingGraph().maxPositionInAnyLayer() + 1;
			} else{
				layerSize = dispatch.getWorkingGraph().numberOfNodesOnLayer(layer);
			}
			int width = dispatch.getWindowWidth();
			// center node in layer if it's unique; else do the usual
			if ( layerSize == 1 ){
				x = width / 2;
			} else{
				int positionGap = (width - 2 * HORIZONTAL_PADDING) / (layerSize - 1);
				x = HORIZONTAL_PADDING + position * positionGap;
			}

			int numberOfLayers = dispatch.getWorkingGraph().numberOfLayers();
			int height = dispatch.getWindowHeight();
			// center layer in window if it's unique; else do the usual
			if ( numberOfLayers == 1 ){
				y = height / 2;
			} else{
				int layerGap = (height - 2 * VERTICAL_PADDING) / (numberOfLayers - 1);
				y = VERTICAL_PADDING + this.getLayer() * layerGap;
				// + (numberOfLayers - n.getLayer() - 1) * layerGap;
			}
			nodeCenter = new Point(x, y);

			// made by 2021 Galant Team
			// I treat fixedposition as the physical position.
			this.setFixedPosition(nodeCenter);

			// the physical position is set
			this.setpos = true;
		}

		if ( nodeCenter == null ){
			throw new GalantException("Unable to compute center for node " + this);
		}
		return nodeCenter;
	}

}
