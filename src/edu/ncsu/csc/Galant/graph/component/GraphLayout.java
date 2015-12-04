/**
 * Provides algorithms that change the fixed positions of nodes based on
 * various layout algorithms. Currently the only one provided is a force
 * directed one; see implementation below.
 */

package edu.ncsu.csc.Galant.graph.component;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import edu.ncsu.csc.Galant.GalantException;
import edu.ncsu.csc.Galant.GraphDispatch;
import edu.ncsu.csc.Galant.logging.LogHelper;

public class GraphLayout {

    private Graph graph;
    private List<Node> nodes;
    private List<Edge> edges;
    private int progress;       // need this to be an instance variable, for
                                // now because it gets modified as a side
                                // effect of a method

		
    /**
     * Maps nodes to indexes in an array that keeps track of their
     * (temporary) positions during a layout algorithm; necessary because
     * node id's may not be contiguous.
     */
    private Map<Node,Integer> nodeToIndex;

    /**
     * Need to recover nodes at the end
     */
    private Node[] indexToNode;
        
    /**
     * maximum number of repositioning iterations in force-directed layout =
     * 100,000
     */
    final private static int MAX_REPOSITION_ITERATIONS = 100000;

    /**
     * how often to print progress (never)
     */
    final private static int PRINT_FREQUENCY = 100000;

    /** 
     * minimum distance from the edge of a window when fitting a graph to
     * the window
     *
     * @todo should be tied to size of a node and sizes of labels somehow and
     * probably incorporated into the GraphPanel class
     */ 
    final static int WINDOW_PADDING = 50;
    
    /**
     * Offset to account for the fact that (0,0) is not a visible part of the
     * window.
     *
     * @todo This is actually more relevant for the top edge than the left
     * one and it also needs to take into account the message at the top
     * (which should eventually go into a separate window)
     */
    final static int WINDOW_OFFSET = 50;

    /**
     * minimum window width or height when scaling to fit window
     */
    final static int MIN_WINDOW_DIMENSION = 100;

    GraphLayout( Graph graph ) {
        this.graph = graph;
        this.nodes = graph.getNodes();
        this.edges = graph.getEdges();
        nodeToIndex = new HashMap<Node,Integer>();
        indexToNode = new Node[nodes.size()];
    }

    /**
     * changes positions of the nodes so that the width of the graph is
     * xScale times the current width and the height is yScale times the
     * current height
     *
     * @todo implement
     *    fitToWindow() to scale using current window width and height
     *    zoom( double factor ) to scale both width and height the same way
     *    not clear whether scale should be based on "original" positions or
     *    ones determined by algorithm
     */
    public void scale( double xScale, double yScale ) {
        try {
            for ( Node n: graph.getNodes() ) {
                n.setFixedPosition( (int) (xScale * n.getX()),
                                    (int) (yScale * n.getY()) );
            }
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    /**
     * Scales the node positions so that the rightmost node is close to the
     * right boundary of the window and the bottom-most node is close to the
     * bottom of the window. "Close to" is defined by the WINDOW_PADDING
     * constant.
     */
    public void fitWindow() {
        nudgeToEdge();
        scaleToWindow();
    }

	/**
	 * Moves all of the nodes to the top and left edges of the screen
	 */
	public void nudgeToEdge() {
		int x_least = Integer.MAX_VALUE;
		int y_least = Integer.MAX_VALUE;
		
		for ( Node node: graph.getNodes() ) {
			x_least = (node.getX() < x_least) ? node.getX() : x_least;
			y_least = (node.getY() < y_least) ? node.getY() : y_least;
		}
        
		try {
            for ( Node node: graph.getNodes() ) {
                // padding because 0,0 will be half off screen for a node
                node.setFixedPosition( node.getX() - x_least + WINDOW_OFFSET,
                                       node.getY() - y_least + WINDOW_OFFSET );
            }
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
	}
	
	/**
	 * Scales the graph to the current window size
	 */
	public void scaleToWindow() {
		int x_max = 0;
		int y_max = 0;
		
		int windowWidth = GraphDispatch.getInstance().getWindowWidth();
		int windowHeight = GraphDispatch.getInstance().getWindowHeight();
		int xScaleBase = windowWidth - WINDOW_PADDING;
        int yScaleBase = windowHeight - WINDOW_PADDING;
		
		xScaleBase = (xScaleBase < 0.0) ? MIN_WINDOW_DIMENSION : xScaleBase;
		yScaleBase = (yScaleBase < 0.0) ? MIN_WINDOW_DIMENSION : yScaleBase;
		
		for ( Node node: graph.getNodes() ) {
			x_max = (node.getX() > x_max) ? node.getX() : x_max;
			y_max = (node.getY() > y_max) ? node.getY() : y_max;
		}
		
        scale( (double) xScaleBase / x_max, (double) yScaleBase / y_max );
	}
	
	/**
	 * Repositions the graph so that the nodes are arranged in an
	 * aesthetically pleasing way.
	 * 
	 * @todo Currently, only works if graph is connected, otherwise
	 * disconnected pieces repel each other nonstop; the fix is add back in
	 * the test for connectivity, which is currently an O(n^4) algorithm in
	 * the worst case.
	 * 
	 * @see <a href="http://www.mathematica-journal.com/issue/v10i1/contents/graph_draw/graph_draw_3.html">Force-Directed Algorithms (2006); Hu, Yifan</a>

     * @todo would be good to have a way to undo this
	 */
	public void forceDirected() {
        /**
         * These store the previous and current positions of the nodes
         */
		Point2D.Double[] points = new Point2D.Double[nodes.size()];
		Point2D.Double[] previousPoints = new Point2D.Double[points.length];

        /**
         * used in step update for force-directed layout
         */
        progress = 0;
	
		// initialize the starting points
        int index = 0;
		for ( Node node: nodes ) {
            nodeToIndex.put( node, index );
            indexToNode[index] = node;
//             System.out.printf( "initializing: index = %d, node = %s\n", index, node );
//             System.out.printf( "checking:     index = %d, node = %s\n", nodeToIndex.get(node), indexToNode[index] );
			Point p = node.getFixedPosition();
			points[index] = new Point2D.Double(p.x, p.y);
            index++;
		}
		
		if (nodes == null || nodes.size() == 0) {
			return;
		}
		
		boolean cvg = false;
		double step = 1.0;
		double energy = Double.MAX_VALUE; // very large number pretending to be infinity
		double c = 1.0; // scalar. won't make much difference since we rescale at the end anyway
		double k = 120.0; // natural spring (Edge) length
		double tol = .1; // the tolerance in change before the algorithm concludes itself
		
		int iter = 0;
        while (!cvg && iter < MAX_REPOSITION_ITERATIONS ) {
			iter++;
			
            if ( iter % PRINT_FREQUENCY == 0 )
                System.out.println( "force directed layout, iteration " + iter );
			// copy your new points to your old points
			for (int i=0; i < points.length; i++) {
				previousPoints[i] = (Point2D.Double) points[i].clone();
			}
			
			// store the last energy of the graph. minimize this.
			double energy_0 = energy;
			
			// reset energy
			energy = 0.0;
			
			// loop through the Graph nodes and calculate new forces
			for (int i=0; i < points.length; i++) {
				double[] f = {0.0,0.0};
				
				// calculate attractive force of edges
				for (Edge e : edges) {
					int j = -1;
//                     System.out.printf( "source: index = %d, node = %s\n",
//                                        nodeToIndex.get( e.getSourceNode() ).intValue(), e.getSourceNode() );
//                     System.out.printf( "dest:   index = %d, node = %s\n",
//                                        nodeToIndex.get( e.getTargetNode() ).intValue(), e.getTargetNode() );
//                     if (e.getSourceNode().getId() == i) {
//                         j = e.getTargetNode().getId();
//                     } else if (e.getTargetNode().getId() == i) {
//                         j = e.getSourceNode().getId();
//                     }
					if ( nodeToIndex.get( e.getSourceNode() ).intValue() == i ) {
						j = nodeToIndex.get( e.getTargetNode() );
                    }
                    else if ( nodeToIndex.get( e.getTargetNode() ).intValue() == i ) {
						j = nodeToIndex.get( e.getSourceNode() );
					}
					if (j != -1 && j != i) {
						double attractive = forceAttractive(points[i], points[j], k);
						double[] unitVector = unitVector(points[i], points[j]);
						f[0] += unitVector[0] * attractive;
						f[1] += unitVector[1] * attractive;
					}
				}
				
				// calculate repulsive force from other nodes
				for (int j=0; j < points.length; j++) {
					if (j != i
//                         && pathExists(i,j)
                        ) {
						double repulsive = forceRepulsive(points[i], points[j], c, k);
						double[] unitVector = unitVector(points[i], points[j]);
						f[0] += unitVector[0] * repulsive;
						f[1] += unitVector[1] * repulsive;
					}
				}
				
				// calculate new x position, scaling the force by a step size
				double x = points[i].getX();
				if (Math.abs(f[0]) > 0) {
					x += (step * f[0] / magnitude(f));
				}
				
				// calculate new y position, scaling the force by a step size
				double y = points[i].getY();
				if (Math.abs(f[1]) > 0) {
					y += (step * f[1] / magnitude(f));
				}
				points[i] = new Point2D.Double(x, y);
				
				// update the energy of this iteration
				energy += magnitude(f) * magnitude(f);
			}
			
			// update step length with adaptive cooling scheme
			step = updateStepLength(step, energy, energy_0);
			
			// check to see if we've converged
			if (totalChange(points, previousPoints) < tol) {
				cvg = true;
			}
		}
		
		// we've converged, now scale it and center it in the window
		points = centerInWindow(points);
		
        try {
            // update the nodes with their new positions and push to the display
            for (int i=0; i < this.nodes.size(); i++) {
                int x = (int) points[i].getX();
                int y = (int) points[i].getY();

                indexToNode[i].setFixedPosition( new Point(x,y) );
			
                GraphDispatch.getInstance().pushToGraphEditor();
            }
		}
        catch ( Exception e ) {
            e.printStackTrace();
        }
	}
	
	/**
	 * Centers the <code>Graph</code> to the center of the window, taking into account the current window size.
	 * 
	 * @param points the array of <code>Node</code> positions in the <code>Graph</code>
	 * @return the positions in the window of each <code>Node</code>
	 */
	private static Point2D.Double[] centerInWindow(Point2D.Double[] points) {
		points = nudgeToEdge(points);
		points = scaleToWindow(points);
		points = nudgeToCenter(points);

		return points;
	}
	
	/**
	 * Moves the <code>Node</code>s to the center of the window, taking into account the current window size.
	 * 
	 * @param points the array of <code>Node</code> positions in the <code>Graph</code>
	 * @returnthe positions in the window of each <code>Node</code>
	 */
	private static Point2D.Double[] nudgeToCenter(Point2D.Double[] points) {
		double x_least = points[0].x;
		double y_least = points[0].y;
		
		double x_most = points[0].x;
		double y_most = points[0].y;
		
		double windowWidth = GraphDispatch.getInstance().getWindowWidth();
		double windowHeight = GraphDispatch.getInstance().getWindowHeight();
		
		for (Point2D.Double p : points) {
			x_least = (p.x < x_least) ? p.x : x_least;
			x_most = (p.x > x_most) ? p.x : x_most;
			
			y_least = (p.y < y_least) ? p.y : y_least;
			y_most = (p.y > y_most) ? p.y : y_most;
		}
		
		double xPadding = (x_least) + (windowWidth - x_most);
		double yPadding = (y_least) + (windowHeight - y_most);
		
		xPadding = (xPadding / 2.0) - x_least;
		yPadding = (yPadding / 2.0) - y_least;
		
		for (Point2D.Double p : points) {
			p.setLocation(p.x + xPadding, p.y + yPadding);
		}
		
		return points;
	}
	
	/**
	 * Scales the <code>Graph</code> to the current window size
	 * @param points
	 * @return the positions on the window of each <code>Node</code>
	 */
	private static Point2D.Double[] scaleToWindow(Point2D.Double[] points) {
		double x_max = 0.0;
		double y_max = 0.0;
		
		double windowWidth = GraphDispatch.getInstance().getWindowWidth();
		double windowHeight = GraphDispatch.getInstance().getWindowHeight();
		double scaleBase = (windowWidth < windowHeight) ?
            windowWidth - WINDOW_PADDING :
            windowHeight - WINDOW_PADDING;
		
		if (scaleBase < 0.0) {
			scaleBase = 100.0;
		}
		
		for (Point2D.Double p : points) {
			x_max = (p.x > x_max) ? p.x : x_max;
			y_max = (p.y > y_max) ? p.y : y_max;
		}
		
		double scale = 1.0;
		
		if (y_max > x_max) {
			scale = scaleBase/y_max;
		} else {
			scale = scaleBase/x_max;
		}
		
		for (Point2D.Double p : points) {
			p.setLocation(p.x*scale, p.y*scale);
		}
		
		return points;
	}
	
	/**
	 * Moves all of the <code>Node</code>s to the edge of the screen
	 * @param points
	 * @return the position in the window of each <code>Node</code>
	 */
	private static Point2D.Double[] nudgeToEdge(Point2D.Double[] points) {
		double x_least = points[0].x;
		double y_least = points[0].y;
		
		for (Point2D.Double p : points) {
			x_least = (p.x < x_least) ? p.x : x_least;
			y_least = (p.y < y_least) ? p.y : y_least;
		}
		
		for (Point2D.Double p : points) {
			// padding because 0,0 will be half off screen for a node
			p.setLocation(p.x - x_least + WINDOW_OFFSET,
                          p.y - y_least + WINDOW_OFFSET );
		}
		
		return points;
	}
	
	/**
	 * Calculate spring force between two points based on natural spring length. Assumes
	 * there is an edge between p1 and p2.
	 * @param p1 The position of an edge endpoint
	 * @param p2 The position of the other edge endpoint
	 * @param k The natural spring length
	 * @return The attractive force between the two nodes
	 */
	private static double forceAttractive(Point2D p1, Point2D p2, double k) {
		return (p1.distance(p2)*p1.distance(p2)) / k;
	}
	
	/**
	 * Calculate the repulsive force between two nodes
	 * @param p1 The position of a node
	 * @param p2 The position of a second node
	 * @param c A scalar
	 * @param k The natural length
	 * @return The force between the two components
	 */
	private static double forceRepulsive(Point2D p1, Point2D p2, double c, double k) {
		return ( (-1*c) * k * k) / (p1.distance(p2)) ;
	}
	
	/**
	 * Returns a unit vector indicating the direction from source i to destination j
	 * @param i the source point
	 * @param j the destination point
	 * @return The unit vector towards your destination
	 */
	private static double[] unitVector(Point2D i, Point2D j) {
		double twoNorm = j.distance(i);
		double[] unit = { (j.getX() - i.getX()) / twoNorm, (j.getY() - i.getY()) / twoNorm};
		return unit;
	}
	
	/**
	 * Returns the magnitude of a vector
	 * @param vector the vector to calculate
	 * @return the magnitude of the vector
	 */
	private static double magnitude(double[] vector) {
		double sum = 0;
		for (int i=0; i < vector.length; i++) {
			sum += (vector[i]*vector[i]);
		}
		
		return Math.sqrt(sum);
	}
	
	/**
	 * Calculate the total change in energy between two states of positions
	 * @param x the old set of points
	 * @param x0 the new set of points
	 * @return the amount of change between the two sets
	 */
	private static double totalChange(Point2D[] x, Point2D[] x0) {
		if (x.length != x0.length) {
			return 0;
		}
		
		double totalChange = 0.0;
		for (int i=0; i < x.length; i++) {
			double dist = x[i].distance(x0[i]); 
			if (dist > 0)
				totalChange += x[i].distance(x0[i]);
		}
		
		return totalChange;
	}
	
	/**
	 * Adaptively update the step length to avoid settling into a local minimum
	 * @param step the current step size
	 * @param energy the previous energy
	 * @param energy_0 the new energy
	 * @return the new step length
	 */
	private double updateStepLength(double step, double energy, double energy_0) {
		double t = .9;
		
		if (energy < energy_0) {
			progress++;
			if (progress >= 5) {
				progress = 0;
				step /= t;
			}
		} else {
			progress = 0;
			step = t*step;
		}
		
		return step;
	}
	
	/**
	 * See if a path exists between two components. 
	 * @param i the index of the start node
	 * @param j the index of the end node
	 * @return true if a path exists
     *
     * @todo This is, to say the least, stupid. An O(n) BFS is performed for
     * every *pair* of nodes during each iteration. It would be simple to do
     * a single BF at the beginning to identify the connected components.
	 */
	private boolean pathExists(int i, int j) {
		List<Node> visited = new ArrayList<Node>();
		List<Node> frontier = new ArrayList<Node>();
		
		Node destination = indexToNode[j];
		
		Node n = indexToNode[i];
		visited.add(n);
		frontier.add(n);
		
		while (frontier.size() > 0) {
			Node front = frontier.remove(0);
			for (Edge e : front.getEdges()) {
				Node current = e.getSourceNode();
				if (current.equals(destination)) {
					return true;
				} else if (!visited.contains(current)) {
					visited.add(current);
					frontier.add(current);
				}
				
				current = e.getTargetNode();
				if (current.equals(destination)) {
					return true;
				} else if (!visited.contains(current)) {
					visited.add(current);
					frontier.add(current);
				}
			}
		}
		
		return false;
	}

}	

//  [Last modified: 2015 12 04 at 21:46:58 GMT]
