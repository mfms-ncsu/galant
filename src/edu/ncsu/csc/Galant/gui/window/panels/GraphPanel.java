package edu.ncsu.csc.Galant.gui.window.panels;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;             // DecimalFormat
import java.util.List;

import javax.swing.JPanel;

import edu.ncsu.csc.Galant.GalantException;
import edu.ncsu.csc.Galant.GalantPreferences;
import edu.ncsu.csc.Galant.GraphDispatch;
import edu.ncsu.csc.Galant.graph.component.Edge;
import edu.ncsu.csc.Galant.graph.component.Node;
import edu.ncsu.csc.Galant.graph.component.Graph;
import edu.ncsu.csc.Galant.graph.component.GraphState;
import edu.ncsu.csc.Galant.graph.component.GraphElementState;
import edu.ncsu.csc.Galant.gui.util.guiStepExecutor;
import edu.ncsu.csc.Galant.gui.window.GraphWindow;
import edu.ncsu.csc.Galant.gui.window.GraphWindow.GraphDisplays;
import edu.ncsu.csc.Galant.logging.LogHelper;

/**
 * Component used to manage <code>Graph</code> actions and visual graph manipulations.
 * @author Jason Cockrell, Ty Devries, Alex McCabe, Michael Owoc
 *
 */
public class GraphPanel extends JPanel{
	
	private GraphWindow gw;
	public boolean setGraphWindow(GraphWindow a){
		this.gw = a;
		return false;
	}

    /**
     * color of a node boundary or edge if none is specified
     */
    public final Color DEFAULT_COLOR = Color.black;

    /**
     * Default width of an edge or node boundary
     */
    public static final int DEFAULT_WIDTH = 2;
    
    /**
     * Maximum width of a node boundary or edge that can be specified by a
     * spinner in the preferences panel
     */
    public static final int MAXIMUM_LINE_WIDTH = 7;

    /**
     * default width for a highlighted or colored edge or node boundary
     */
    public static final int DEFAULT_HIGHLIGHT_WIDTH = 5;

    /**
     * This is the default value if none is specified in the preferences
     */
    public static final int DEFAULT_NODE_RADIUS = 12;

    /**
     * This is the maximum value that can be specified via a spinner in the
     * preferences
     */
    public static final int MAXIMUM_NODE_RADIUS = 15;

    /**
     * Minimum node radius that allows display of node id
     */
    private static final int MINIMUM_ID_RADIUS = 10;

    /**
     * Characteristics of the boundary of a selected node
     * (line width setting does not work because this has to be a float)
     */
    private final float SELECTED_NODE_LINE_WIDTH = 5;
    private final Color SELECTED_NODE_LINE_COLOR = Color.red;

    /**
     * diameter of a node for selection purposes
     */
    private final int NODE_SELECTION_RADIUS = 12;

    /**
     * width of an edge for selection purposes
     */
    private final int EDGE_SELECTION_WIDTH = 8;

    /**
     * Node weights are stacked on top of labels; their left edge is
     * tangent to the rightmost point of the node. The line separating them
     * is level with the center point of the node
     *
     * Edge weights are to the left of labels. The (vertical) separating line
     * intersects the midpoint of the edge. Centers of labels and weights are
     * aligned vertically with the (horizontal) line that intesects the
     * midpoint of the edge
     *
     * Node labels/weights are on top of a (white) filled rectangle with
     * padding all around.
     *
     * Edge labels/weights are the same with the addition of a border.
     *
     * In the relevant code, the position of each label/weight is recorded in
     * the appropriate variable: it is the position of the top left corner of
     * the label or weight without padding. Since strings are drawn with
     * respect to the <emph>lower</emph> left corner, the font height must be
     * added when the label/weight is drawn. 
     */

    /**
     * Fonts used for various labels/weights
     */
    private final Font NODE_LABEL_FONT = new Font( Font.MONOSPACED, Font.PLAIN, 20 );
    private final Font NODE_WEIGHT_FONT = new Font( Font.MONOSPACED, Font.BOLD, 20 );
    private final Font EDGE_LABEL_FONT = new Font( Font.MONOSPACED, Font.PLAIN, 20 );
    private final Font EDGE_WEIGHT_FONT = new Font( Font.MONOSPACED, Font.BOLD, 20 );

    /**
     * Padding used for *both* weights and labels
     */
    private final int LABEL_PADDING = 3;

    /**
     * Distance from node for labels and weights
     */
    private final int NODE_LABEL_DISTANCE = 2;

	/** Refers to the singleton GraphDispatch to push global information */
	private final GraphDispatch dispatch;
	
	/** 
	 * Holds the integer value of the graph state to be displayed.
	 * By default, this is the initial state of a graph
	 */
	private int state = GraphState.GRAPH_START_STATE;
	
	/**
	 * Holds the width to draw edges and node boundaries. Pulled on each
	 * repaint from the Galant Preferences 
	 */
	private int lineWidth = DEFAULT_WIDTH;

    /**
     * Holds the width to draw edges and node boundaries when these are
     * highlighted or colored
     */
    private int highlightWidth = DEFAULT_HIGHLIGHT_WIDTH;

	/**
	 * Holds the radius for drawing nodes. Pulled on each repaint
	 * from the Galant Preferences
	 */
	private int nodeRadius = DEFAULT_NODE_RADIUS;
	
    /**
     * whether or not to display the id's of nodes
     */
    private boolean displayIds = false;

	private Node previousNode;
	private Node selectedNode;
	private Edge selectedEdge;
	private Point edgeTracker;

	private static final String EMPTY_STRING = "";

    /**
     * @return a "nice" string version of a double: no decimal point if it's
     * an integer, 'inf' if it's infinity, and only two decimal places
     * otherwise
     */
    private String doubleToString( double number ) {
        /* Not needed: the infinity symbol is printed automagically, and
         * 0.0/0.0 is printed as a ? 
         * Just in case:  
         *   -1.0/0.0 = Double.POSITIVE_INFINITY
         *  and 0.0/0.0 = NaN */
       // if ( number == Double.POSITIVE_INFINITY ) {
       //      return "inf";
       // }
       if ( (int) number == number ) {
           // integer
           return String.format("%d", (int) number);
       }
       // round to two decimal digits (and cut trailing 0's ?)
       return new DecimalFormat("#.##").format(number);
    }

	/** 
	 * Sets whether the user has pressed the mouse button and is dragging the mouse
	 * Used by GraphWindow to determine certain graph changes. 
	 */
	private boolean isDragging;

	public boolean isDragging() {
		return isDragging;
	}

	public void setDragging(boolean isDragging) {
		this.isDragging = isDragging;
	}

	/**
	 * Initializes the dispatch field and the size of this panel.
	 * 
	 * @param _dispatch A reference to the GraphDispatch
	 */
	public GraphPanel(GraphDispatch _dispatch, GraphWindow a) {
		LogHelper.enterConstructor(getClass());
		
		this.dispatch = _dispatch;
		this.gw = a;
		
		this.setSize(600, 600);
		//this.setMinimumSize(new Dimension(600, 600));
		
        this.setBackground(Color.WHITE);
		LogHelper.exitConstructor(getClass());
	}

	@Override
	public void paintComponent(Graphics g) {
        try {

            // Get the graph to draw
            Graph graph = dispatch.getWorkingGraph();
		
            // Get the normal width of an edge or node boundary
            this.lineWidth = GalantPreferences.NORMAL_WIDTH.get();

            // Get the width of a node boundary that's highlighted or colored
            this.highlightWidth = GalantPreferences.HIGHLIGHT_WIDTH.get();

            // Get node radius
            this.nodeRadius = GalantPreferences.NODE_RADIUS.get();
	
            // display id's only if radius is large enough
            displayIds = ( nodeRadius >= MINIMUM_ID_RADIUS );

            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // If you're drawing an edge, draw a line between the first node and
            // the cursor
            if ( ! dispatch.isAnimationMode() && this.selectedNode != null && this.edgeTracker != null) {
                Point p1 = edgeTracker;
                Point p2 = selectedNode.getFixedPosition();
                g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
            }
		
            if (graph != null) {
                List<Node> nodes = graph.getNodes(state);
                List<Edge> edges = graph.getEdges(state);
			
                // Draw edges first to put them behind nodes
                for (Edge e : edges) {
                    drawEdge(graph, e, g2d);
                }
			
                for (Node n : nodes) {
                    drawNode(n, g2d);
                }
			
                // If there is a message, draw it
                String message = graph.getMessage(state);
                if (message != null) {
                    drawMessageBanner(message, g2d);
                }
		
            }
        }
        catch (GalantException e) {
            e.report( "error while redrawing" );
            e.display();
        }
    }

    /**
     * @return the point at the center of node n, based on whether or not
     * you're in animation mode or whether the graph is layered.
     *
     * @todo Once LayeredGraph becomes a subclass of Graph, and LayeredNode a
     * subclass of Node, we can override getPosition(), getX(), and getY() in
     * LayeredNode() so that they do the right thing.
     */
    private Point getNodeCenter( Node n ) throws GalantException{
        LogHelper.enterMethod( getClass(), "getNodeCenter, n = " + n );

        // ns is the latest state in the animation if in animation mode
        GraphElementState currentState = null;
        if ( dispatch.isAnimationMode() )
            currentState = n.getLatestValidState(state);

        Point nodeCenter = n.getFixedPosition();

        if ( currentState != null ) {
            nodeCenter = n.getPosition(currentState.getState());
        }
            
        // if graph is layered and node has layer and position in layer
        // information, base its location on that
        /**
         * @todo should be done with inheritance: nodes should have their
         * own draw methods and then call on appropriate methods to do
         * the actual drawing of circles and labels after the center is
         * calculated.
         */
        if ( dispatch.getWorkingGraph().isLayered() ) {
            int layer = n.getLayer(); // should not change during an
                                      // animation of a layered graph algorithm
            int position = n.getPositionInLayer();
            if ( currentState != null )
                position = n.getPositionInLayer(currentState.getState());
            int layerSize
                = dispatch.getWorkingGraph().numberOfNodesOnLayer( layer );
            int width = dispatch.getWindowWidth();
            int positionGap = width / ( layerSize + 1 );
            int x = ( position + 1 ) * positionGap;
            int numberOfLayers = dispatch.getWorkingGraph().numberOfLayers();
            int height = dispatch.getWindowHeight();
            int layerGap = height / (numberOfLayers + 1);
            int y = (numberOfLayers - n.getLayer()) * layerGap;
            nodeCenter = new Point( x, y );
            LogHelper.logDebug( " getNodeCenter: layered graph, center = " + nodeCenter );
        }
        LogHelper.exitMethod( getClass(), "getNodeCenter, nodeCenter = " + nodeCenter );
        return nodeCenter;
    }
	
	/**
	 * Draws the specified node and its properties to the screen
	 * 
	 * @param n The node to be drawn (assumed to be non-null)
	 * @param g2d The graphics object used to draw the elements
     *
     * @todo Because the class NodeState no longer exists, we have to use
     * n.hasX(stateNumber) instead of currentState.hasX(), for example. The
     * former adds a level of indirection. There's probably a way to avoid
     * this.
	 */
	private void drawNode(Node n, Graphics2D g2d)
        throws GalantException
    {
		GraphElementState currentState = n.getLatestValidState(state);
        if ( currentState == null ) return;
        Point nodeCenter = getNodeCenter( n );
        if ( nodeCenter == null ) return;
        LogHelper.logDebug( "drawing node: node = " + n.getId()
                            + ", position = " + nodeCenter );

        int stateNumber = currentState.getState();
        g2d.setColor(Color.BLACK);
		
        // If there is a label and drawing node labels is on, show it
        if ( GraphDisplays.NODE_LABELS.isShown()
             && n.hasLabel(stateNumber) ) {
            String label = n.getLabel(stateNumber);
            if ( ! label.trim().equals("") ) {
                TextLayout layout
                    = new TextLayout( label, NODE_LABEL_FONT,
                                      g2d.getFontRenderContext() );
                Rectangle2D bounds = layout.getBounds();
                // upper left corner of label: treats the bounding box of
                // as that of the label text only, without any padding;
                // ditto with weight below
                Point labelPosition
                    = new Point( nodeCenter.x
                                 + nodeRadius
                                 + NODE_LABEL_DISTANCE + LABEL_PADDING,
                                 nodeCenter.y
                                 + LABEL_PADDING );
                g2d.setColor(Color.WHITE);
                g2d.fillRect( labelPosition.x - LABEL_PADDING,
                              labelPosition.y - LABEL_PADDING,
                              (int) (bounds.getWidth() + 2 * LABEL_PADDING),
                              (int) (bounds.getHeight() + 2 * LABEL_PADDING) );
                g2d.setColor(Color.BLACK);
                // text is anchored at *lower* left corner
                layout.draw( g2d, (float) labelPosition.getX(),
                             (float) (labelPosition.getY() 
                                      + bounds.getHeight()) );
            }
        }
			
        // If drawing node weights is on, show weight
        if ( GraphDisplays.NODE_WEIGHTS.isShown() 
             && n.hasWeight(stateNumber) ) {
            String weight = doubleToString(n.getWeight(stateNumber));
            TextLayout layout = new TextLayout( weight, NODE_WEIGHT_FONT,
                                                g2d.getFontRenderContext() );
            Rectangle2D bounds = layout.getBounds();
            // padding is 'shared' with node label
            Point weightPosition = new Point( nodeCenter.x
                                              + nodeRadius
                                              + NODE_LABEL_DISTANCE
                                              + LABEL_PADDING,
                                              nodeCenter.y
                                              - (int) (bounds.getHeight()) );
            g2d.setColor(Color.WHITE);
            g2d.fillRect( weightPosition.x - LABEL_PADDING,
                          weightPosition.y - LABEL_PADDING,
                          (int) (bounds.getWidth()) + 2 * LABEL_PADDING,
                          (int) (bounds.getHeight()) + 2 * LABEL_PADDING);
            g2d.setColor(Color.BLACK);
            // text is anchored at *lower* left corner
            layout.draw( g2d, (float) weightPosition.getX(),
                         (float) (weightPosition.getY()
                                  + bounds.getHeight()) );
        } // end, draw node weight

        /* Define node circle: used to create both outline and fill.
           Circle is filled first so that outline can be drawn on top of
           the filled circle */
        Ellipse2D.Double nodeCircle
            = new Ellipse2D.Double( nodeCenter.x - nodeRadius,
                                    nodeCenter.y - nodeRadius,
                                    2 * nodeRadius,
                                    2 * nodeRadius );

        /* draw node interior */
        System.out.println("check if marked: node = " + n);
        System.out.println(" isMarked: state = " + state + ", isMarked = " + n.isMarked(state));
        if ( n.isMarked(state) ) {
            g2d.setColor( Color.LIGHT_GRAY );
        }
        else {
            g2d.setColor( Color.WHITE );
        }
        g2d.fill( nodeCircle );

        /* draw node boundary */
        if ( n.isSelected(stateNumber) ) {
            g2d.setColor( SELECTED_NODE_LINE_COLOR );
            g2d.setStroke( new BasicStroke( highlightWidth ) );
        }
        else if ( n.getColor(stateNumber) == null ) {
            // no declared color, use default color with default line width 
            g2d.setColor( DEFAULT_COLOR );
            g2d.setStroke( new BasicStroke( lineWidth ) );
        }
        else {
            // color declared, use it and make stroke thicker
            String nodeColor = n.getColor(stateNumber);
            Color c = Color.decode( nodeColor );
            g2d.setColor(c);
            g2d.setStroke( new BasicStroke( highlightWidth ) );
        }

        // draw node boundary
        g2d.draw( nodeCircle );

        /* draw node id if desired */
        /** @todo get rid of magic numbers here */
        if ( displayIds ) {
            g2d.setColor(Color.BLACK);
            String idStr = "" + n.getId();
            if (idStr.length() > 1) {
                g2d.drawChars( idStr.toCharArray(), 0,
                               idStr.length(),
                               nodeCenter.x-8, nodeCenter.y+4 );
            }
            else {
                g2d.drawChars( idStr.toCharArray(), 0,
                               idStr.length(),
                               nodeCenter.x-5, nodeCenter.y+4 );
            }
        }

        // draw translucency to darken node if it is selected in edit mode
        if ( selectedNode != null
             && selectedNode.equals(n)
             && ! dispatch.isAnimationMode()) {
            Color color = new Color(0, 0, 1, .2f);
            g2d.setColor(color);
            g2d.fill( nodeCircle );
        }
			
    } // end, drawNode


	/**
	 * Draws the specified edge between its source and destination nodes
	 * 
	 * @param g The current graph, used to determine directedness
	 * @param e The edge to be drawn
	 * @param g2d The graphics object used to draw the elements
	 */
	private void drawEdge(Graph g, Edge e, Graphics2D g2d) 
        throws GalantException
    {
		
		int thickness = lineWidth; 
		
		if (e != null) {
			Node dest = e.getTargetNode();
			Node source = e.getSourceNode();
            if (dest != null && source != null) {
                Point p1 = getNodeCenter( source );
                Point p2 = getNodeCenter( dest );
                
				//determine color of the edge
				if (selectedEdge != null && selectedEdge.equals(e) && !dispatch.isAnimationMode()) {
					g2d.setColor(Color.BLUE);
				} else if (e.isSelected(state)) {
					g2d.setColor(Color.RED);
					thickness = highlightWidth;
				} else {
					try {
						Color c = Color.decode(e.getColor());
						g2d.setColor(c);
					} catch (Exception ex ) {
						g2d.setColor(Color.BLACK);
					}
				}
				
				//only draw if edge exists at current state
				if (p1 != null && p2 != null) {
					
					// Self loop
					if (dest.equals(source)) {
						Stroke oldStroke = g2d.getStroke();
						g2d.setStroke(new BasicStroke(thickness));
						g2d.drawOval(p1.x, p1.y, 24, 24);
						g2d.setStroke(oldStroke);
						if (g.isDirected()) {
							drawSelfLoopArrow(p1, g2d);
						}
						
					// Straight edge	
					} else {
						Stroke oldStroke = g2d.getStroke();
						g2d.setStroke(new BasicStroke(thickness));
						g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
						g2d.setStroke(oldStroke);

						if (g.isDirected()) {
							drawDirectedArrow(p1, p2, g2d);
						} 
						
                        if ( e.hasLabel( state ) )
                            drawEdgeLabel(e.getLabel(state), p1, p2, g2d);
                        if ( e.hasWeight( state ) )
                            drawEdgeWeight(e.getWeight(state), p1, p2, g2d);
					}
				}
				
				g2d.setColor(Color.BLACK);
			}
		}
		
	}
	
	/**
	 * Draws a directed arrow on the end of an edge between the specified nodes
	 * 
	 * @param source The source point of the relevant edge
	 * @param dest The destination point of the relevant edge
	 * @param g2d The graphics object used to draw the elements
	 */
	private void drawDirectedArrow(Point source, Point dest, Graphics2D g2d) {
		Graphics2D g = (Graphics2D) g2d.create();
		
        double dx = dest.getX() - source.getX(); 
        double dy = dest.getY() - source.getY();
        double angle = Math.atan2(dy, dx);
        
        int len = (int) Math.sqrt(dx*dx + dy*dy) - nodeRadius;
        
        AffineTransform at = AffineTransform.getTranslateInstance(source.getX(), source.getY());
        at.concatenate(AffineTransform.getRotateInstance(angle));
        g.transform(at);

        Stroke oldStroke = g.getStroke();
		
        g.fillPolygon(new int[] {len, len-6, len-6, len},
                      new int[] {0, -6, 6, 0}, 4);
    }
	
	/**
	 * Draws a directed arrow on the end of a self edge at the specified point
	 * 
	 * @param p1 The location of the relevant node
	 * @param g2d The graphics object used to draw the elements
	 */
	private void drawSelfLoopArrow(Point p1, Graphics2D g2d) {
		int x = p1.x + 1;
		int y = p1.y + nodeRadius;
		
		g2d.fillPolygon(new int[] {x, x-6, x+6, x},
                      new int[] {y, y+6, y+6, y}, 4);
	}
	
	/**
	 * Draws an edge weight to the screen positioned along the edge itself
	 * 
	 * @param weight The value of the weight
	 * @param label The label associated with the same edge. This is used to align the weight with the label.
	 * @param source The source point of the relevant edge
	 * @param dest The destination point of the relevant edge
	 * @param g2d The graphics object used to draw the elements
	 */
	private void drawEdgeWeight( double weight, 
                                 Point source,
                                 Point dest,
                                 Graphics2D g2d ) {
        /** @todo need special handling for self loops, i.e., put label and
         * weight at the bottom of loop */
		
		if ( ! GraphDisplays.EDGE_WEIGHTS.isShown() ) return;
		
		String weightString = doubleToString( weight );

        Point2D edgeMiddle = new Point2D.Double( 0.5 * ( source.x + dest.x ),
                                          0.5 * ( source.y + dest.y ) );
        
        TextLayout layout
            = new TextLayout( weightString, EDGE_WEIGHT_FONT,
                              g2d.getFontRenderContext() );
        Rectangle2D bounds = layout.getBounds();

        Point2D weightPosition
            = new Point2D.Double( edgeMiddle.getX() - LABEL_PADDING - bounds.getWidth(),
                           edgeMiddle.getY() - 0.5 * bounds.getHeight() );
        g2d.setColor(Color.WHITE);
        g2d.fillRect( (int) (weightPosition.getX() - LABEL_PADDING),
                      (int) (weightPosition.getY() - LABEL_PADDING),
                      (int) (bounds.getWidth() + 2 * LABEL_PADDING),
                      (int) (bounds.getHeight() + 2 * LABEL_PADDING) );
        g2d.setColor(Color.BLACK);
        // border
        g2d.drawRect( (int) (weightPosition.getX() - LABEL_PADDING),
                      (int) (weightPosition.getY() - LABEL_PADDING),
                      (int) (bounds.getWidth() + 2 * LABEL_PADDING),
                      (int) (bounds.getHeight() + 2 * LABEL_PADDING) );
        // text
        layout.draw( g2d, (float) weightPosition.getX(),
                     (float) (weightPosition.getY() 
                              + bounds.getHeight()) );
	}

	/**
	 * Draws the specified label along the edge between the specified points, making sure it is always drawn upright.
	 * 
	 * @param label The label associated with the edge
	 * @param source The source point of the relevant edge
	 * @param dest The destination point of the relevant edge
	 * @param g2d The graphics object used to draw the elements
	 */
	private void drawEdgeLabel( String label, Point source, Point dest, Graphics2D g2d ) {
        /** @todo need special handling for self loops, i.e., put label and
         * weight at the bottom of loop */
		
		if ( ! GraphDisplays.EDGE_LABELS.isShown() 
             || label.trim().equals("") ) return;
		
        Point2D edgeMiddle = new Point2D.Double( 0.5 * ( source.x + dest.x ),
                                          0.5 * ( source.y + dest.y ) );
        
        TextLayout layout
            = new TextLayout( label, EDGE_LABEL_FONT,
                              g2d.getFontRenderContext() );
        Rectangle2D bounds = layout.getBounds();

        Point2D labelPosition
            = new Point2D.Double( edgeMiddle.getX() + LABEL_PADDING,
                           edgeMiddle.getY() - 0.5 * bounds.getHeight() );
        g2d.setColor(Color.WHITE);
        g2d.fillRect( (int) (labelPosition.getX() - LABEL_PADDING),
                      (int) (labelPosition.getY() - LABEL_PADDING),
                      (int) (bounds.getWidth() + 2 * LABEL_PADDING),
                      (int) (bounds.getHeight() + 2 * LABEL_PADDING) );
        g2d.setColor(Color.BLACK);
        // border
        g2d.drawRect( (int) (labelPosition.getX() - LABEL_PADDING),
                      (int) (labelPosition.getY() - LABEL_PADDING),
                      (int) (bounds.getWidth() + 2 * LABEL_PADDING),
                      (int) (bounds.getHeight() + 2 * LABEL_PADDING) );
        // text
        layout.draw( g2d, (float) labelPosition.getX(),
                     (float) (labelPosition.getY() 
                              + bounds.getHeight()) );
	}
	
	/**
	 * Returns a graphics object transformed such that drawing horizontally from the origin will
	 * line up with an edge between the specified points.
	 * 
	 * @param p1 An endpoint of the edge to transform the canvas
	 * @param p2 An endpoint of the edge to transform the canvas
	 * @param g2d The graphics object used to draw the elements
	 * @return A transformed graphics object whose x axis is the edge between p1 and p2
	 */
	private TransformData getEdgeTransform(Point p1, Point p2, Graphics2D g2d) {
		Graphics2D g = (Graphics2D) g2d.create();
		boolean flip = false;
		
        double dx = p2.getX() - p1.getX(); 
        double dy = p2.getY() - p1.getY();
        double angle = Math.atan2(dy, dx);
        
        if ( ! (angle > (-1*Math.PI/2.0) && angle < (Math.PI/2.0)) ) {
        	 dx = p1.getX() - p2.getX(); 
             dy = p1.getY() - p2.getY();
             
             Point hold = p1;
             p1 = p2;
             p2 = hold;
             angle = Math.atan2(dy, dx);
             flip = true;
        }
        
        int len = (int) Math.sqrt(dx*dx + dy*dy) - nodeRadius;
        
        AffineTransform at = AffineTransform.getTranslateInstance(p1.getX(), p1.getY());
        at.concatenate(AffineTransform.getRotateInstance(angle));
        g.transform(at);
        
        return new TransformData(len, flip, g);
	}
	
	/**
	 * Draw a text box big enough to contain the specified text.
	 * 
	 * @param topLeft The top left corner of the new text box
	 * @param text The text that the textbox needs to be sized to contain
	 * @param bordered Whether or not to draw a border around the textbox
	 * @param g2d The graphics object used to draw the elements
	 */
	private void drawTextBox(Point topLeft, String text, boolean bordered, Graphics2D g2d) {
		Color prevColor = g2d.getColor();
		
		FontMetrics metrics = g2d.getFontMetrics(g2d.getFont());
		int h = metrics.getHeight();
		int w = metrics.stringWidth(text) + 6; //6 pixels for padding
		
		Color color = new Color(1, 1, 1, .7f); 
		g2d.setPaint(color);
		Rectangle2D.Double r = new Rectangle2D.Double(topLeft.getX() + 1, topLeft.getY() + 1, w - 2, h - 2);
		g2d.fill(r);
		
		if (bordered) {
			g2d.setColor(Color.BLACK);
			r = new Rectangle2D.Double(topLeft.getX(), topLeft.getY(), w, h);
			g2d.draw(r);
		}
		
		g2d.setColor(prevColor);
	}
	
	private void drawMessageBanner(String text, Graphics2D g2d) {
		Color prevColor = g2d.getColor();
		
		FontMetrics metrics = g2d.getFontMetrics(g2d.getFont());
		int h = metrics.getHeight();
		int w = dispatch.getWindowWidth();
		
		Color color = new Color(1, 1, 1, .5f); 
		g2d.setPaint(color);
		Rectangle2D.Double r = new Rectangle2D.Double(1, 1, w, h);
		g2d.fill(r);
		
		g2d.setColor(Color.BLACK);
		r = new Rectangle2D.Double(0, 0, w+2, h+2);
		g2d.draw(r);
		
		g2d.drawChars(text.toCharArray(), 0, text.length(), 10, h-1);
		
		g2d.setColor(prevColor);
	}
	
	// MPM: It's a little odd to me that the graph panel maintains information about whether the algorithm has completed. I think it
	// might be better to keep a reference to the Algorithm instance and just directly ask it if it is complete.
	// MPM: And this is not starting the algorithm, it is allowing the algorithm to advance.
	public void resumeAlgorithmExecution(){
		
		dispatch.getWorkingGraph().getGraphState().setStepComplete(false);
		if ( ! dispatch.getAlgorithmComplete() ) {
			//System.out.println("GraphPanel is notifying the worker thread so it will wake up and work.");
			synchronized(dispatch.getWorkingGraph().getGraphState()){
				dispatch.getWorkingGraph().getGraphState().notify();
			}
			if ( dispatch.getAlgorithmComplete() ) {
			}
			//System.out.printf("Algorithm is started");
		}
        else {
            this.gw.updateStatusLabel("Execution is finished");
		}
	}
	
	/*
	 * This is THE function to call whenever the graph state needs to be updated.  Currently it is called whenever the user clicks on the
	 * "next" button or the right arrow key, but also aught to be the one called for future extensions that change the interface used
	 * It will determine if the display state merely needs to be updated, or if first a new graphState must be calculated
	 * If the latter, it will wake up the waiting algorithm, wait for it to run one step, and then increment the graphState
	 * Then whatever called it can re-paint the frame 
	 */
	public void incrementDisplayState() {
		System.out.printf("(+) Incrementing the graph display state: displayState = %d, graphState = %d\n",
                          state, dispatch.getWorkingGraph().getState() );
		guiStepExecutor t = new guiStepExecutor(this);
		t.execute();
		
		
	}
	public int getState(){
		return this.state;
	}
	public void setState(int state){
		this.state = state;
	}
	
	public void decrementDisplayState() {
		LogHelper.enterMethod(getClass(), "decrementDisplayState");
		
		if (this.state > 1) {
			this.state--;
		}
		System.out.printf("(-) Decrementing the graph display state: displayState = %d, graphState = %d\n",
                          state, dispatch.getWorkingGraph().getState() );
		
		LogHelper.exitMethod(getClass(), "decrementDisplayState");
		
		/* This is responsible for updating the status label at the top of the Galant screen so that it displays the correct graph state
		 * This happens whenever the user hits the right arrow key or the right arrow button
		 */
		dispatch.getGraphWindow().updateStatusLabel(this.getDisplayState());
		
	}
	
	public int getDisplayState() {
		LogHelper.enterMethod(getClass(), "getDisplayState");
		LogHelper.exitMethod(getClass(), "getDisplayState");
		return this.state;
	}
	
	public void setDisplayState(int _state) {
		LogHelper.enterMethod(getClass(), "setDisplayState");
		
		Graph graph = dispatch.getWorkingGraph(); 
		if (_state > 0 && _state <= graph.getState()) {
			this.state = _state;
		}
		
		LogHelper.exitMethod(getClass(), "setDisplayState");
	}
	
	public Node selectTopClickedNode(Point p) {
		LogHelper.enterMethod(getClass(), "selectTopClickedNode");
		
		Graph g = dispatch.getWorkingGraph();
		
		Node top = null;
		
		for (Node n : g.getNodes()) {
            LogHelper.logDebug( "next node = " + n.getId() + " position = " + n.getPosition() );
			if ( p.distance(n.getPosition()) < NODE_SELECTION_RADIUS ) {
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
	
	public void setSelectedNode(Node n) {
		LogHelper.enterMethod(getClass(), "setSelectedNode");
		
		this.previousNode = (n == null) ? null : this.selectedNode;
		this.selectedNode = n;
		
		LogHelper.exitMethod(getClass(), "setSelectedNode");
	}
	
	public Node getSelectedNode() {
		LogHelper.enterMethod( getClass(), "getSelectedNode" );
		LogHelper.exitMethod( getClass(), "getSelectedNode, node = "
                              + selectedNode );
		return this.selectedNode;
	}
		
	public void setPrevSelectedNode(Node n) {
		this.previousNode = n;
	}
	
	public Node getPrevSelectedNode() {
		return this.selectedNode;
	}
	
	public Edge selectTopClickedEdge(Point p) {
		LogHelper.enterMethod(getClass(), "selectTopClickedEdge");
		
		Graph g = dispatch.getWorkingGraph();
		
		Edge top = null;
		
		for (int i=1; i <= EDGE_SELECTION_WIDTH; i++) {
			double width = i;
			double centerVal = width/2;
            LogHelper.logDebug( "centerVal = " + centerVal );
			Rectangle2D clickArea = new Rectangle2D.Double(p.getX() - centerVal, p.getY() - centerVal - 1, i, i);
			
            for (Edge e : g.getEdges()) {
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
	
	public void setSelectedEdge(Edge e) {
		LogHelper.enterMethod(getClass(), "setSelectedEdge");
		this.selectedEdge = e;
		LogHelper.exitMethod(getClass(), "setSelectedEdge");
	}
	
	public void setEdgeTracker(Point p) {
		this.edgeTracker = p;
	}
	
	public Point getEdgeTracker() {
		return this.edgeTracker;
	}
	
	public boolean hasNextState() {
		int graphState = dispatch.getWorkingGraph().getState();
        if ( graphState > this.state ) return true;
		if ( ! dispatch.getAlgorithmComplete() ) return true;
        return false;
	}
	
	public boolean hasPreviousState() {
		return (this.state > 1);
	}
	
	private class TransformData {
		private int len;
		private boolean flip;
		private Graphics2D g;
		public TransformData(int len, boolean flip, Graphics2D g) {
			this.len = len;
			this.flip = flip;
			this.g = g;
		}
		
		public int getLen() {
			return len;
		}
		public void setLen(int len) {
			this.len = len;
		}
		public boolean isFlip() {
			return flip;
		}
		public void setFlip(boolean flip) {
			this.flip = flip;
		}
		public Graphics2D getG() {
			return g;
		}
		public void setG(Graphics2D g) {
			this.g = g;
		}
		
	}
	
}

//  [Last modified: 2015 07 27 at 18:53:58 GMT]
