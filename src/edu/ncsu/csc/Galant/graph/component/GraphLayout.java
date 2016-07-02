/**
 * Specifies a set of positions for all nodes of a graph. Currently, this is
 * used only for creating a force directed layout (or saving positions of
 * nodes before such a layout), but there are many other potential applications.
 */

package edu.ncsu.csc.Galant.graph.component;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.LinkedList;
import edu.ncsu.csc.Galant.GalantException;
import edu.ncsu.csc.Galant.GraphDispatch;
import edu.ncsu.csc.Galant.logging.LogHelper;

public class GraphLayout {

    /** used to scale the repulsive force of edges */
    private static final double REPULSIVE_SCALE_FACTOR = -1.0;
    /** spring length for edges (attractive force) and nodes (repulsive) */
    private static final double SPRING_LENGTH = 120.0;
    /** force directed method stops when node movement is less than this */
    private static final double FORCE_DIRECTED_TOLERANCE = 0.1;

    // see updateStepLength() for how the next two are used
    /** the "temperature" reduction factor: used to control how fast the nodes move */
    private static final double TEMPERATURE_REDUCTION_FACTOR = 0.9;
    /** number of progress increments before step length is decreased
     * (temperature reduced) */
    private static final int MAX_INCREASING_STEPS = 5;
    private int progress;       // need this to be an instance variable, for
                                // now because it gets modified as a side
                                // effect of a method

    /** maximum number of repositioning iterations in force-directed layout */
    private static final int MAX_REPOSITION_ITERATIONS = 100000;

    private Graph graph;
    private List<Node> nodes;
    private List<Edge> edges;
		
    /**
     * Maps nodes to their positions.
     */
    private Map<Node,Point> nodePositions;

    /**
     * Maps nodes to indexes in an array that keeps track of their
     * (temporary) positions during the force-directed layout algorithm;
     * necessary because node id's may not be contiguous and a map may not be
     * efficient enough with hundreds of thousands of updates.
     */
    private Map<Node,Integer> nodeToIndex;

    /**
     * Need to recover nodes at the end
     */
    private Node[] indexToNode;
        
    /**
     * keeps track of connected components via an array indexed by nodeToIndex
     */
    private int[] component;

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

    /**
     * Initializes the layout based on current positions of nodes in the graph
     */
    GraphLayout( Graph graph ) {
        this.graph = graph;
        this.nodes = graph.getNodes();
        this.edges = graph.getEdges();
        nodeToIndex = new HashMap<Node,Integer>();
        indexToNode = new Node[nodes.size()];
        nodePositions = new HashMap<Node,Point>();
        int index = 0;
		for ( Node node: nodes ) {
            nodeToIndex.put( node, index );
            indexToNode[index] = node;
			Point p = node.getFixedPosition();
            nodePositions.put(node, p);
            index++;
		}
    }

    /**
     * Sets positions of nodes in the graph to correspond to those given in
     * this layout.
     */
    void usePositions() {
        for ( Node node : nodes ) {
            Point position = nodePositions.get(node);
            if ( position != null ) {
                node.setFixedPosition(position);
            }
        }
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
     * POST: each node is labeled with an integer indentifying its connected
     * component; the information is stored in the components array
     */
    private void computeConnectedComponents() {
        component = new int[nodes.size()];
        int componentNumber = 0;
        for ( Node node : nodes ) {
            if ( component[nodeToIndex.get(node)] == 0 ) {
                // not yet encountered; start a new component and do BFS on it
                componentNumber++;
                Queue<Node> nodeQueue = new LinkedList<Node>();
                component[nodeToIndex.get(node)] = componentNumber;
                nodeQueue.offer(node);
                while ( nodeQueue.size() > 0 ) {
                    Node currentNode = nodeQueue.poll();
                    for ( Node neighbor: currentNode.getAdjacentNodes() ) {
                        if ( component[nodeToIndex.get(neighbor)] == 0 ) {
                            component[nodeToIndex.get(neighbor)] = componentNumber;
                            nodeQueue.offer(neighbor);
                        }
                    }
                }
            }
        }
    }

	/**
	 * Repositions the graph so that the nodes are arranged in an
	 * aesthetically pleasing way.
	 * 
	 * @see <a href="http://www.mathematica-journal.com/issue/v10i1/contents/graph_draw/graph_draw_3.html">Force-Directed Algorithms (2006); Hu, Yifan</a>

	 */
	public void forceDirected() {
        /**
         * These store the previous and current positions of the nodes
         */
		Point2D.Double[] points = new Point2D.Double[nodes.size()];
		Point2D.Double[] previousPoints = new Point2D.Double[points.length];

        /**
         * used in step update for force-directed layout; global -- side
         * effect in updateStepLength() 
         */
        progress = 0;
	
		// initialize the starting points
        int index = 0;
		for ( Node node: nodes ) {
			Point p = nodePositions.get(node);
			points[index] = new Point2D.Double(p.x, p.y);
            index++;
		}
		
		if (nodes == null || nodes.size() == 0) {
			return;
		}
		
		boolean converged = false;
		double step = 1.0;
		double energy = Double.MAX_VALUE;

        computeConnectedComponents();

		int iterations = 0;
        while ( ! converged && iterations < MAX_REPOSITION_ITERATIONS ) {
			iterations++;
			
            if ( iterations % PRINT_FREQUENCY == 0 )
                System.out.println("force directed layout, iteration " + iterations);
			// copy your new points to your old points
			for ( int i=0; i < points.length; i++ ) {
				previousPoints[i] = (Point2D.Double) points[i].clone();
			}
			
			// store the last energy of the graph. minimize this.
			double last_energy = energy;
			
			// reset energy
			energy = 0.0;
			
			// loop through the Graph nodes and calculate new forces
			for ( int i = 0; i < points.length; i++ ) {
				double[] force = {0.0, 0.0};
				
				// calculate attractive force of edges
				for (Edge e : edges) {
					int j = -1;
					if ( nodeToIndex.get(e.getSourceNode()).intValue() == i ) {
						j = nodeToIndex.get(e.getTargetNode());
                    }
                    else if ( nodeToIndex.get(e.getTargetNode()).intValue() == i ) {
						j = nodeToIndex.get(e.getSourceNode());
					}
					if ( j != -1 && j != i ) {
                        // ij is really an edge and not a self-loop
						double attractive = forceAttractive(points[i], points[j]);
						double[] unitVector = unitVector(points[i], points[j]);
						force[0] += unitVector[0] * attractive;
						force[1] += unitVector[1] * attractive;
					}
				}
				
				// calculate repulsive force from other nodes
				for ( int j = 0; j < points.length; j++ ) {
					if ( j != i && component[i] == component[j] ) {
						double repulsive = forceRepulsive(points[i], points[j]);
						double[] unitVector = unitVector(points[i], points[j]);
						force[0] += unitVector[0] * repulsive;
						force[1] += unitVector[1] * repulsive;
					}
				}
				
				// calculate new x position, scaling the force by a step size
				double x = points[i].getX();
				if ( Math.abs(force[0]) > 0 ) {
					x += (step * force[0] / magnitude(force));
				}
				
				// calculate new y position, scaling the force by a step size
				double y = points[i].getY();
				if (Math.abs(force[1]) > 0) {
					y += (step * force[1] / magnitude(force));
				}
				points[i] = new Point2D.Double(x, y);
				
				// update the energy of this iteration
				energy += magnitude(force) * magnitude(force);
			}
			
			// update step length with adaptive cooling scheme
			step = updateStepLength(step, energy, last_energy);
			
			// check to see if we've converged
			if ( totalChange(points, previousPoints) < FORCE_DIRECTED_TOLERANCE ) {
				converged = true;
			}
		}
		
		// we've converged, now scale it and center it in the window
		points = centerInWindow(points);
		
        // update the nodes with their new positions and push to the display
        for ( int i = 0; i < this.nodes.size(); i++ ) {
            int x = (int) points[i].getX();
            int y = (int) points[i].getY();
            Node node = indexToNode[i];
            nodePositions.put(node, new Point(x, y));
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
	 * @return The attractive force between the two nodes
	 */
	private static double forceAttractive(Point2D p1, Point2D p2) {
		return ( p1.distance(p2) * p1.distance(p2) ) / SPRING_LENGTH;
	}
	
	/**
	 * Calculate the repulsive force between two nodes
	 * @param p1 The position of a node
	 * @param p2 The position of a second node
	 * @param c A scalar
	 * @return The force between the two components
	 */
	private static double forceRepulsive(Point2D p1, Point2D p2) {
		return ( REPULSIVE_SCALE_FACTOR * SPRING_LENGTH * SPRING_LENGTH)
            / p1.distance(p2) ;
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
	 * @param energy the current energy
	 * @param previous_energy the previous energy
	 * @return the new step size
	 */
	private double updateStepLength(double step, double energy, double previous_energy) {
		if ( energy < previous_energy ) {
			progress++;
			if ( progress >= MAX_INCREASING_STEPS ) {
				progress = 0;
				step /= TEMPERATURE_REDUCTION_FACTOR;
			}
		} else {
			progress = 0;
			step = TEMPERATURE_REDUCTION_FACTOR * step;
		}
		
		return step;
	}

}
	
//  [Last modified: 2016 07 01 at 19:01:22 GMT]
