/**
 * Purpose of this file is to catalog all parts of the code where node
 * positions play a role.
 */

/**
 * getNodeCenter() is used for
 * drawing nodes, determining endpoints of edges, positions of labels
 * and weights for both nodes and edges.
 *
 * Two important points.
 * (i) the special case of layered graphs has details that can be used
 * as a model for the scaling
 * (ii) the decision of whether to use getPosition(state) or
 * getFixedPosition() may need to be hidden underneath the method that
 * does the scaling; the proposed enhancements will add complexity to
 * this decision and one of these two methods may go away
 *
 * The position we want may always be state-based: animation state if
 * the algorithm moves nodes; edit state otherwise;
 *  getDisplayState() returns animation state if the algorithm is
 *  running, regardless of whether it moves nodes.
 */

    private Point getNodeCenter( Node n ) throws GalantException{
        int state = dispatch.getDisplayState();
        Point nodeCenter = null;

        if ( dispatch.isAnimationMode()
             && GraphDispatch.getInstance().algorithmMovesNodes() ) {
            nodeCenter = n.getPosition(state);
        }
        else {
            nodeCenter = n.getFixedPosition();
        }

        // if graph is layered and node has layer and position in layer
        // information, base its location on that
        if ( dispatch.getWorkingGraph().isLayered() ) {
            int x = 0;
            int y = 0;
            int layer = n.getLayer(); // should not change during an
                                      // animation of a layered graph algorithm
            int position = n.getPositionInLayer(state);
            int layerSize = 1;
            // vertical layered graphs have gaps in positions on some layers,
            // i.e., positions on some layers are not contiguous; in that
            // case, positions should be taken "literally", i.e., position p
            // means the same thing on every layer
            if ( dispatch.getWorkingGraph().isVertical() )
                layerSize = dispatch.getWorkingGraph().maxPositionInAnyLayer() + 1;
            else
                layerSize = dispatch.getWorkingGraph().numberOfNodesOnLayer(layer);
            int width = dispatch.getWindowWidth();
            // center node in layer if it's unique; else do the usual
            if (layerSize == 1) {
                x = width / 2;
            }
            else {
                int positionGap
                    = (width - 2 * HORIZONTAL_PADDING) / (layerSize - 1);
                x = HORIZONTAL_PADDING + position * positionGap;
            }

            int numberOfLayers = dispatch.getWorkingGraph().numberOfLayers();
            int height = dispatch.getWindowHeight();
            // center layer in window if it's unique; else do the usual
            if (numberOfLayers == 1) {
                y = height / 2;
            }
            else {
                int layerGap
                    = (height - 2 * VERTICAL_PADDING) / (numberOfLayers - 1);
                y = VERTICAL_PADDING
                    + n.getLayer() * layerGap;
                // + (numberOfLayers - n.getLayer() - 1) * layerGap;
            }
            nodeCenter = new Point( x, y );
        }
        if ( nodeCenter == null )
            throw new GalantException("Unable to compute center for node " + n);
        return nodeCenter;
    }

/**
 * There are some important exceptions where getNodeCenter() is not
 * used, both related to user interaction with the panel.
 */

/**
 * One is determining what node or edge a user is selecting with the
 * mouse.
 * getFixedPosition() will need to be replaced by the scaled method
 */

	public Node selectTopClickedNode(Point p) {
            LogHelper.enterMethod(getClass(), "selectTopClickedNode");
            Graph g = dispatch.getWorkingGraph();
            int stateNumber = g.getEditState();
            Node top = null;
            for (Node n : g.getNodes(stateNumber)) {
                /** *** !!! *** */
                if ( p.distance(n.getFixedPosition()) < NODE_SELECTION_RADIUS ) {
                    top = n;
                }
            }
            previousNode = selectedNode;
            selectedNode = top;
            selectedEdge = null;
            LogHelper.exitMethod( getClass(), "selectTopClickedNode, node = "
                                  + (selectedNode == null ? "null"
                                     : selectedNode.getId() ) );
            return top;
	}
	
	public Edge selectTopClickedEdge(Point p) {
            LogHelper.enterMethod(getClass(), "selectTopClickedEdge");
		
            Graph g = dispatch.getWorkingGraph();
            int stateNumber = g.getEditState();
		
            Edge top = null;
		
            for (int i=1; i <= EDGE_SELECTION_WIDTH; i++) {
                double width = i;
                double centerVal = width/2;
                LogHelper.logDebug( "centerVal = " + centerVal );
                Rectangle2D clickArea
                    = new Rectangle2D.Double(p.getX() - centerVal,
                                             p.getY() - centerVal - 1, i, i);
			
                for (Edge e : g.getEdges(stateNumber)) {
                    /** *** !!! *** */
                    Point p1 = e.getSourceNode().getFixedPosition();
                    Point p2 = e.getTargetNode().getFixedPosition();

                    Line2D l = new Line2D.Double(p1, p2);
				
                    if (l.intersects(clickArea)) {
                        top = e;
                    }
                }
			
                if (top != null) break;
			
            }
			
            this.selectedEdge = top;
		
            this.selectedNode = null;
            this.previousNode = null;
		
            LogHelper.exitMethod(getClass(), "selectTopClickedEdge");
            return top;
	}
	
/**
 * Another is when the user is drawing an edge.
 *  Here, getFixedPosition() needs to be replaced by the scaling method
 */

	public void paintComponent(Graphics g) {
        try {
            // ...

            // If you're drawing an edge, draw a line between the first node and
            // the cursor
            if ( ! dispatch.isAnimationMode()
                 && this.selectedNode != null
                 && this.edgeTracker != null ) {
                Point p1 = edgeTracker;
                Point p2 = selectedNode.getFixedPosition();
                g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
            }
        }

/**
 * Some methods in Algorithm.java query the current position of a node.
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
 

/**
 * Also, there is a method that was incorporated into Algorithm.java
 * as a convenience for several algorithms that use distance between
 * nodes to make decisions, e.g., shortest paths and minimum spanning
 * trees. We should probably use physical position here.
 */

    /**
     * @return the distance between two nodes
     */
    public double distance(Node nodeOne, Node nodeTwo) {
        return nodeOne.getPosition().distance(nodeTwo.getPosition());
    }

/**
 * There are a few places where node positions are changed.
 */

/**
 * One is during editing, in GraphWindow.java.
 * Here, the physical position is known but not the logical one. Some
 * "snapping into place" may need to happen here if logical positions
 * involve small integers.
 */
        graphPanel.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent arg0) {
                // If you start dragging, set dragging mode so you don't
                // perform any other operations on the Node until after
                // releasing it

                Node sel = graphPanel.getSelectedNode();
                if (sel != null) {
                    graphPanel.setDragging(true);
                    graphPanel.setEdgeTracker(null);
                    if ( ! dispatch.isAnimationMode()
                            || ! dispatch.algorithmMovesNodes() ) {
                        try {
                            /** *** !!! *** */
                            sel.setFixedPosition(arg0.getPoint());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                frame.repaint();
            }

/**
 * The other is when an algorithm changes the position of a node
 */
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
  public void setPosition(Node v, Point pt) throws Terminate, GalantException {
    checkGraphElement(v);
    v.setPosition(pt);
  }

//  [Last modified: 2021 02 12 at 15:44:14 GMT]
