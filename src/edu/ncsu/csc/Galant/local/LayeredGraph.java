/**
 * @file LayeredGraph.java
 * @version $Id: LayeredGraph.java 103 2015-03-26 20:46:06Z mfms $
 * Can be used as a basis for Galant implementations of a variety of crossing
 * minimization algorithms.
 *
 * It is assumed (for now) that all edges are directed from nodes on layer i
 * to ones on layer i+1 for some i.
 *
 * Also assume is that node id's range from 0 to n-1, where n is the number
 * of nodes, and edge id's are in the range 0 to m-1, where m is the number
 * of edges.
 *
 * @todo When the layered graph is created, original layer and position
 * information in the graph is not retained when the graph is saved or
 * modified. [This may have been fixed]
 *
 * @todo Come up with a way, perhaps via a compile-time option, to run the
 * heuristics decoupled from Galant displays and GUI's, so that their
 * performance can be evaluated before animations are undertaken. This may
 * also be useful for Galant in general.
 *
 * @todo There appears to be a bug in adjust_weights. The weight of a node
 * with no neighbors is based on an already assigned weight of the node to
 * the left. Consider iteration 7 with mod_bary on the graph
 * r_100_110_10_1_1_1.graphml: node 77 should have a weight of 10, based on
 * its right neighbor -- its left neighor, 76, has no weight; however, it is
 * given a weight of 10.5, based on both neighbors. This is correct for
 * sequential algorithms (such as mod_bary), so a distinction needs to be
 * made via, for example, a second argument. 
 */

package edu.ncsu.csc.Galant.local;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.ncsu.csc.Galant.GalantException;
import edu.ncsu.csc.Galant.graph.component.Edge;
import edu.ncsu.csc.Galant.graph.component.Graph;
import edu.ncsu.csc.Galant.graph.component.Node;
import edu.ncsu.csc.Galant.GraphDispatch;
import edu.ncsu.csc.Galant.logging.LogHelper;

/**
 * @todo Need to fix some inconsistencies in (a) division of labor between
 * class Layer and class LayeredGraph; and (b) computation and update of
 * number of crossings in channels versus up/down.
 */

/**
 * Keeps track of position and display information for nodes on a single
 * layer. The <em>logical</em> order of nodes on a layer is determined by the
 * ArrayList <code>nodes</code>, while the display order is determined by
 * setting the positionInLayer attribute of a node. These two are brought in
 * sync by displayPositions. Also in the mix is an array stored with the
 * graph, that keeps track of logical positions and is updated via
 * updatePositions.
 *
 * @todo Make this process more transparent, i.e., have only one level of
 * logical node positions. I'm guessing that the current arrangement has to
 * do with the ease of sorting the ArrayList of nodes on a layer versus the
 * random access of a node position. The latter may be obviated now that the
 * display is smart about layered graphs. 
 */
class Layer {
    LayeredGraph graph;
    boolean marked;
    ArrayList<Node> nodes;
    Layer( LayeredGraph graph ) {
        this.graph = graph;
        this.marked = false;
        nodes = new ArrayList<Node>();
    }

    /**
     * adds new positions numbered layer.nodes.size(), ... , position to the
     * nodes list of the given layer; the new positions are filled with null
     * nodes; can be called even if not needed - it does nothing, safely, in
     * that case
     */
    void ensurePosition( int position ) {
        for ( int i = nodes.size(); i <= position; i++ ) {
            nodes.add( null );
        }
    }

    /**
     * puts node v into the i-th position
     */
    public void addNode( Node v, int i ) {
        ensurePosition( i );
        nodes.set( i, v );
    }

    /**
     * @return the node at the given position on this layer
     */
    public Node getNodeAt( int position ) {
        return nodes.get( position );
    }


    /**
     * @return true if this layer is marked (for mod_bary)
     */
    public boolean isMarked() {
        return marked;
    }

    public void mark() {
        marked = true;
    }

    public void unMark() {
        marked = false;
    }

    /**
     * Displays the marked/unmarked state of all nodes on this layer
     */
    public void displayMarks() {
        for ( Node v: nodes ) {
            v.setVisited( graph.isMarked( v ) );
        }
    }

    /**
     * Removes displayed marks from all nodes on this layer without affecting
     * their logical status.
     */
    public void removeMarks() {
        for ( Node v: nodes ) {
            v.setVisited( false );
        }
    }

    /**
     * logically unmarks all nodes on this layer
     */
    public void clearMarks() {
        for ( Node v: nodes ) {
            graph.unMark( v );
        }
    }

    /**
     * Sets node labels to be blank
     */
    public void clearLabels() {
        for ( Node v: nodes ) {
            v.setLabel("");
        }
    }

    /**
     * Highlights nodes on this layer and, if appropriate, incident edges --
     * see "enum Scope"
     */
    public void highlight( LayeredGraph.Scope scope ) {
        for ( Node v: nodes ) {
            v.setSelected( true );
            if ( scope == LayeredGraph.Scope.UP
                 || scope == LayeredGraph.Scope.BOTH ) {
                for ( Edge e: v.getOutgoingEdges() ) {
                    e.setSelected( true );
                }
            }
            if ( scope == LayeredGraph.Scope.DOWN
                 || scope == LayeredGraph.Scope.BOTH ) {
                for ( Edge e: v.getIncomingEdges() ) {
                    e.setSelected( true );
                }
            }
        }
    }

    /**
     * Highlights the nodes between the two given positions, inclusive (used
     * to highlight an insertion)
     */
    public void highlightNodes( int positionOne, int positionTwo ) {
        for ( int i = positionOne; i <= positionTwo; i++ ) {
            nodes.get(i).setSelected( true );
        }
    }

    /**
     * undoes highlighting for the nodes and any edges incident on this layer
     */
    public void unHighlight() {
        for ( Node v: nodes ) {
            v.setSelected( false );
            for ( Edge e: v.getIncidentEdges() ) {
                e.setSelected( false );
            }
        }
    }


    /**
     * Displays logical weights assigned to the nodes
     */
    public void displayWeights() {
        for ( Node v: nodes ) {
            v.setWeight( graph.getWeight( v ) );
        }
    }

    /**
     * Gives node weights a default value that makes them invisible
     */
    public void clearWeights() {
        for ( Node v: nodes ) {
            v.clearWeight();
        }
    }

    /**
     * @return the list of nodes on this layer
     */
    public List<Node> getNodes() {
        return nodes;
    }

    /**
     * inserts the node currently at position originalPosition so
     * node currently at newPosition, shifting the intervening nodes to the
     * right
     */
    public void insert( int originalPosition, int newPosition ) {
        Node toBeInserted = nodes.remove( originalPosition );
        nodes.add( newPosition, toBeInserted );
        updatePositions();
    }

    /**
     * sorts the nodes by their weight (as assigned by Galant code)
     */
    public void sort() {
        Collections.sort( nodes );
        updatePositions();
    }

    /**
     * sorts nodes by their logical weight (for use with 'fast' versions of
     * barycenter-related algorithms)
     */
    final Comparator<Node> WEIGHT_COMPARATOR = new Comparator<Node>() {
        public int compare( Node x, Node y ) {
            double wx = graph.getWeight( x );
            double wy = graph.getWeight( y );
            if ( wx > wy ) return 1;
            if ( wx < wy ) return -1;
            return 0;
        }
    };

    public void sortByWeight() {
        Collections.sort( nodes, WEIGHT_COMPARATOR );
        updatePositions();
    }

    /**
     * sorts the nodes on this layer by increasing degree
     */
    public void sortByIncreasingDegree() {
        LayeredGraph.sortByIncreasingDegree( nodes );
        updatePositions();
    }

    /**
     * Puts nodes with largest (smallest) degree in the middle and puts
     * subsequent nodes farther toward the outside.
     *
     * @param largestInMiddle if true then the node with largest degree goes
     * in the middle.
     */
    public void middleDegreeSort( boolean largestMiddle ) {
       LayeredGraph.sortByIncreasingDegree( nodes );
       if ( largestMiddle ) Collections.reverse( nodes );
       ArrayList<Node> tempNodeList = new ArrayList<Node>();
       boolean addToFront = true;
       for ( Node node : nodes ) {
           if ( addToFront ) {
               tempNodeList.add( 0, node );
           }
           else {
               tempNodeList.add( tempNodeList.size(), node );
           }
           addToFront = ! addToFront;
       }
       nodes = tempNodeList;
       updatePositions();
    }

    /**
     * Uses the order in the list 'nodes' to update the positions of the
     * nodes in the graph.
     */
    public void updatePositions() {
        int i = 0;
        for ( Node v: nodes ) {
            // sets the position information in the layered graph
            graph.setPosition( v, i );
            i++;
        }
    }

    /**
     * Updates the display based on the order of the nodes on this layer;
     * assumes the only y-coordinate changes occurred for nodes whose
     * positions changed: see previewPositionChanges()
     */
    public void displayPositions() {
        int i = 0;
        for ( Node v: nodes ) {
            if ( v.getPositionInLayer() != i ) {
                v.setPositionInLayer( i );
            }
            i++;
        }
    }

    public void markPositionChanges() {
        int i = 0;
        for ( Node v: nodes ) {
            if ( v.getPositionInLayer() != i ) {
                graph.mark( v );
            }
            i++;
        }
    }

    /**
     * Saves positions of all the nodes
     */
    public void savePositions() {
        for ( Node v: nodes ) {
            graph.setSavedPosition( v, graph.getPosition( v ) );
        }
    }

    /**
     * Restores saved positions of all the nodes
     */
    public void restoreSavedPositions() {
        for (Node v: nodes ) {
            graph.setPosition( v, graph.getSavedPosition( v ) );
        }
    }

    /**
     * Updates the display based on previously saved positions
     */
    public void displaySavedPositions() {
        for ( Node v: nodes ) {
            int position = graph.getSavedPosition( v );
            if ( v.getPositionInLayer() != position ) {
                v.setPositionInLayer( position );
            }
        }
    }

    /**
     * Puts node v at position i on the display (does nothing to its logical
     * position)
     */
    public void displayPosition( Node v, int i ) {
        if ( v.getPositionInLayer() != i ) {
            v.setPositionInLayer( i );
        }
    }

    public String toString() {
        String s = "";
        s += "[";
        for ( Node node : nodes ) {
            s += " " + node.getId() + " " +
                "(" + node.getLayer() + "," + node.getPositionInLayer() + "),";
        }
        s += " ]";
        return s;
    }

} // end, class Layer

/**
 * The class LayeredGraph is used as a basis for Galant implementations of a
 * variety of crossing minimization algorithms. It provides several
 * functionalities:
 * <ol>
 *
 * <li>
 * Methods that translate layers and positions within layers into
 * y-coordinates and x-coordinates, respectively. These coordinates are
 * scaled with respect to the window dimensions: layers are evenly spaced
 * with padding at the top and bottom equal to the interlayer distance. The
 * same scaling principle is applied to each layer.
 *
 * <li>
 * Methods that supply infrastructure for crossing minimization algorithms
 * such as numberOfCrossings, assignWeights and sortByWeight (for barycenter
 * and related heuristics).
 *
 * <li> Methods that separate logical attributes from display attributes. For
 * example, dynamic versions of sifting and barycenter (logically) mark
 * nodes/layers to indicate that they will not be chosen again during the
 * current pass; at various points in the animation it is desirable to
 * display the logical marks in some way; methods such as mark(), unMark(),
 * and displayMarks() accomplish this functionality.
 *
 * <li> Methods that facilitate the highlighting of groups of nodes and edges
 * in the specific context of (layered) crossing minimization algorithms;
 * these include highlight() -- with arguments speficifying a layer and the
 * direction of the edges to be highlighted, unhighlight(),
 *
 * </ol>
 *
 * It is assumed that all edges are directed from nodes on layer i
 * to ones on layer i+1 for some i.
 *
 * Also assumed is that node id's range from 0 to n-1, where n is the number
 * of nodes, and edge id's are in the range 0 to m-1, where m is the number
 * of edges.
 */

public class LayeredGraph {

    /**
     * These constants are used to determine both highlighting and assignment
     * of weights to nodes on a layer.
     * LAYER = highlight only the nodes, assign weights based on node positions;
     * UP = highlight nodes and outgoing edges, assign weights based on
     * average position of outgoing neighbors;
     * DOWN = highlight nodes and incoming edges, assign weights based on
     * average position of incoming neighbors;
     * BOTH = highlight nodes and both incoming and outgoing edges, assign
     * weights based on average position of incoming neighbors + average
     * position of outgoing neighbors.
     */
    public enum Scope { LAYER, UP, DOWN, BOTH };

    /**
     * distances used to define layers and positions within each layer, these
     * gaps are also used as padding between the top edge and left edge of the
     * window, respectively.
     *
     * @todo Eventually layers and positions will be assigned as node attributes
     * when the graph file is parsed and can be used directly.
     */
    final int LAYER_GAP = 100;
    final int POSITION_GAP = 100;

    /**
     * The vertical gap between layers when drawing the graph
     */
    private int layerGap;
    /**
     * true as soon as the positions are displayed for the first time, since
     * they will not change thereafter
     */
    private boolean verticalPositionsFixed = false;

    private Graph graph;
    private ArrayList<Layer> layers;
    private int [] positionOfNode;
    private int [] layerOfNode;
    private int [] savedPositionOfNode;

    /**
     * weightOfNode and isMarked are designed to avoid graph state changes
     * during fast versions of barycenter and friends; the weight; default
     * value for weight is the position
     */
    private double [] weightOfNode;
    private boolean [] isMarked;

    /** typically displayed as edge weight */
    private int [] crossingsOfEdge;

    /**
     * Creates a new instance based on node positions in the graph.
     */
    public LayeredGraph( Graph graph ) 
    {
        LogHelper.enterConstructor( getClass() );
        this.graph = graph;
        System.out.printf( "-> LayeredGraph, nodes = %d, edges = %d\n",
                           this.graph.numberOfNodes(),
                           this.graph.numberOfEdges() );
        layers = new ArrayList<Layer>();
        positionOfNode = new int[ graph.getNodes().size() ];
        savedPositionOfNode = new int[ graph.getNodes().size() ];
        layerOfNode = new int[ graph.getNodes().size() ];
        weightOfNode = new double[ graph.getNodes().size() ];
        isMarked = new boolean[ graph.getNodes().size() ];
        crossingsOfEdge = new int[ graph.getEdges().size() ];

        // record layer and position information for all the nodes
        for ( Node u: graph.getNodes() ) {
            int layer = u.getLayer();
            int position = u.getPositionInLayer();
            LogHelper.logDebug( "  adding node " + u );
            addNode( u, layer, position );
            layerOfNode[ u.getId() ] = layer;
            positionOfNode[ u.getId() ] = position;
        }

        // sort the nodes on each layer by their position and then set the
        // horizontal gap between them based on window width
        for ( Layer theLayer: layers ) {
            theLayer.sort();
        }

        // initialize edge crossing counts
        for ( int layer = 0; layer < layers.size() - 1; layer++ ) {
            updateCrossingsInChannel( layer );
        }
        LogHelper.exitConstructor( getClass() );
    }

    /**
     * adds new empty layers numbered layers.size(), ... , layer_number; can be
     * called even if not needed - it does nothing, safely, in that case
     */
    void ensureLayer( int layerNumber ) {
        for ( int layer = layers.size(); layer <= layerNumber; layer++ ) {
            layers.add( new Layer( this ) );
        }
    }

    /**
     * Puts node v on the given layer at the given position and sets its
     * weight to be that position (so that nodes in the layer can be sorted
     * by position initially
     */
    public void addNode( Node v, int layer, int position ) {
        ensureLayer( layer );
        layers.get( layer ).addNode( v, position );
        this.positionOfNode[ v.getId() ] = position;
        this.layerOfNode[ v.getId() ] = layer;
    }

    /**
     * @return a list containing the nodes on the i-th layer.
     */
    public List<Node> getLayer( int layer ) {
        return layers.get( layer ).getNodes();
    }

    /**
     * @return the number of nodes on the layer.
     */
    public int getLayerSize( int layer ) {
        return getLayer( layer ).size();
    }

    /**
     * @return the number of layers
     */
    public int numberOfLayers() { return layers.size(); } 

    /**
     * @return the (number of the) layer on which node v appears.
     */
    public int getLayer( Node v ) {
        return layerOfNode[ v.getId() ];
    }

    /**
     * @return the position of v in its layer.
     */
    public int getPosition( Node v ) {
        return positionOfNode[ v.getId() ];
    }

    public String toString() {
        String s = "";
        for ( int layerNumber = 0; layerNumber < layers.size(); layerNumber++ ) {
            s += "Layer " + layerNumber + " " + layers.get( layerNumber ) + "\n";
        }
        return s;
    }

    /********************************************************************/
    // Algorithm-related code begins here
    /********************************************************************/

    /**
     * Displays the node v (as if it were) at position i on its layer. The
     * logical position of node v is not changed.
     */
    public void displayPosition( Node v, int i ) {
        layers.get( layerOfNode[ v.getId() ] ).displayPosition( v, i );
    }

    /**
     * Updates the display to reflect the logical position information of
     * nodes in layer i.
     */
    public void displayPositions( int i ) {
        layers.get( i ).displayPositions();
    }

    /**
     * Updates the display to reflect the logical position information of
     * all nodes.
     */
    public void displayPositions() {
        for ( int layer = 0; layer < numberOfLayers(); layer++ ) {
            displayPositions( layer );
        }
    }

    /**
     * Changes positions of nodes based on an insertion of a single node
     * either before or after another.
     *
     * @param layer the layer on which the insertion takes place
     * @param nodeLocation index of node to be inserted
     * @param insertLocation index of the new position of the node
     */
    public void insert( int layer, int nodeLocation, int insertLocation ) {
        layers.get( layer ).insert( nodeLocation, insertLocation );
        // need to update position information for the nodes on this layer
        int position = 0;
        for ( Node v: getLayer( layer ) ) {
            positionOfNode[ v.getId() ] = position++;
        }
        // update edges crossings in the relevant channels - should probably
        // be done separately
        if ( layer > 0 ) {
            updateCrossingsInChannel( layer - 1 );
        }
        if ( layer < layers.size() - 1 ) {
            updateCrossingsInChannel( layer );
        }
    }

    /**
     * marks nodes on layer i whose positions are about to change
     */
    public void markPositionChanges( int i ) {
        layers.get(i).markPositionChanges();
    }

    /**
     * sets the position of the given node in its layer.
     */
    public void setPosition( Node v, int positionInLayer ) {
        positionOfNode[ v.getId() ] = positionInLayer;
    }
    
    /**
     * Saves the current (logical) positions for later restoration; useful
     * for remembering positions that minimize crossings.
     */
    public void savePositions() {
        for ( Layer layer: layers ) {
            layer.savePositions();
        }
    }

    /**
     * Restores the previously saved positions.
     */
    public void restoreSavedPositions() {
        for ( Layer layer: layers ) {
            layer.restoreSavedPositions();
        }
    }

    /**
     * Displays the previously saved positions.
     */
    public void displaySavedPositions() {
        for ( Layer layer: layers ) {
            layer.displaySavedPositions();
        }
    }

    /**
     * @return the saved position of node v
     */
    public int getSavedPosition( Node v ) {
        return savedPositionOfNode[ v.getId() ];
    }

    /**
     * Sets the saved position of node v.
     */
    public void setSavedPosition( Node v, int positionInLayer ) {
        savedPositionOfNode[ v.getId() ] = positionInLayer;
    }

    /**
     * @return the node on the given layer at the given position
     */
    public Node getNodeAt( int layerNumber, int position ) {
        Layer layer = layers.get( layerNumber );
        return layer.getNodeAt( position ); 
    }

    /**
     * @return the node to the left of v on the same layer as v
     */
    public Node getNodeToTheLeft( Node v ) {
        int layer = getLayer( v );
        int position = getPosition( v );
        if ( position > 0 ) {
            return getNodeAt( layer, position - 1 );
        }
        return null;
    }

    /**
     * @return the node to the right of v on the same layer as v
     */
    public Node getNodeToTheRight( Node v ) {
        int layer = getLayer( v );
        int position = getPosition( v );
        if ( position < getLayerSize( layer ) - 1 ) {
            return getNodeAt( layer, position + 1 );
        }
        return null;
    }

    /**
     * Sets the logical weight of node v to the given weight without changing
     * the display.
     */
    public void setWeight( Node v, double weight ) {
        weightOfNode[ v.getId() ] = weight;
    }

 
    /**
     * @return the logical weight of node v.
     */
    public double getWeight( Node v ) {
        return weightOfNode[ v.getId() ];
    }

    private double getUpperAverage( Node v ) {
        // a -1 signals that there are no nodes on the layer above/below
        // on which to base a weight; in these cases, a final adjustment
        // bases the weights on those of the left and right neighbors
        double upperAverage = -1;
        int outdegree = v.getOutdegree();
        int sumOfPositions = 0;
        for ( Edge e: v.getOutgoingEdges() ) {
            Node w = v.travel( e );
            sumOfPositions += positionOfNode[ w.getId() ];
        }
        if ( outdegree > 0 ) {
            upperAverage = ((double) sumOfPositions) / outdegree;
        }
        return upperAverage;
    }

    private double getLowerAverage( Node v ) {
        // a -1 signals that there are no nodes on the layer above/below
        // on which to base a weight; in these cases, a final adjustment
        // bases the weights on those of the left and right neighbors
        double lowerAverage = -1;
        int indegree = v.getIndegree();
        int sumOfPositions = 0;
        for ( Edge e: v.getIncomingEdges() ) {
            Node w = v.travel( e );
            sumOfPositions += positionOfNode[ w.getId() ];
        }
        if ( indegree > 0 )  {
            lowerAverage = ((double) sumOfPositions) / indegree;
        }
        return lowerAverage;
    }

    /**
     * Assign a weight based on the two neighbors for each node that does
     * not have a well-defined weight (indegree or outdegree 0)
     *
     * @param nodeList the list of nodes on this layer in the current left to
     * right order
     * @param weightArray the weights of the corresponding nodes (left to
     * right) with a -1 for each node that has no weight
     */
    private void adjustWeights( List<Node> nodeList, double [] weightArray ) {
        int length = nodeList.size();
        for ( int i = 0; i < length; i++ ) {
            // do nothing if this node already has a weight
            if ( weightArray[i] >= 0 ) continue;
            // if both neighbors are present and have weights, take the
            // average of their weights
            if ( i > 0 && i < length - 1
                 && weightArray[i-1] != -1 && weightArray[i+1] != -1 ) {
                weightArray[i] = (weightArray[i-1] + weightArray[i+1]) / 2;
            }
            else if ( i > 0 && weightArray[i-1] != -1 ) {
                // only the left neighbor has a weight
                weightArray[i] = weightArray[i-1];
            }
            else if ( i < length - 1 && weightArray[i+1] != -1 ) {
                // only the right neighbor has a weight
                weightArray[i] = weightArray[i+1];
            }
            else {
                // neither neighbor has a weight; occurs in sequential
                // implementations only if the leftmost node has a right
                // neighbor with no weight; should fix this if simulating
                // parallel algorithms
                weightArray[i] = 0;
            }
        }
    }

    /**
     * Assigns logical weights to nodes on the given layer based on average
     * position of adjacent nodes on neighboring layer(s).
     *
     * @param scope If this is UP, sorting is based on the higher numbered
     * layer, if it's DOWN it's based on the lower numbered layer, if it's
     * BOTH it will be the average of the two.
     */
    public void assignWeights( int layer, Scope scope ) {
        List<Node> nodeList = layers.get( layer ).getNodes();
        double [] upperWeightArray = new double[ nodeList.size() ];
        double [] lowerWeightArray = new double[ nodeList.size() ];
        if ( scope == Scope.UP ) {
            int i = 0;
            for ( Node v: nodeList ) {
                upperWeightArray[ i++ ] = getUpperAverage( v );
            }
            adjustWeights( nodeList, upperWeightArray );
        }
        else if ( scope == Scope.DOWN ) {
            int i = 0;
            for ( Node v: nodeList ) {
                lowerWeightArray[ i++ ] = getLowerAverage( v );
            }
            adjustWeights( nodeList, lowerWeightArray );
        }
        else { // scope is BOTH
            int i = 0;
            for ( Node v: nodeList ) {
                double upper = getUpperAverage( v );
                double lower = getLowerAverage( v );
                upperWeightArray[ i ] = upper >= 0 ? upper : 0;
                lowerWeightArray[ i ] = lower >= 0 ? lower : 0;
                i++;
            }
        }
        int i = 0;
        for ( Node v: nodeList ) {
            if ( scope == Scope.DOWN )
                weightOfNode[ v.getId() ] = lowerWeightArray[i];
            else if ( scope == Scope.UP )
                weightOfNode[ v.getId() ] = upperWeightArray[i];
            else {
                weightOfNode[ v.getId() ]
                    = (upperWeightArray[i] + lowerWeightArray[i]) / 2;
            }
            i++;
        }
    }

    /**
     * Sorts nodes on the given layer by their logical weights.
     */
    public void sortByWeight( int layer ) {
        layers.get( layer ).sortByWeight();
    }

    /**
     * Sets weights of all nodes on layer i to 0
     */
    public void resetNodeWeights( int i ) {
        for ( Node v: getLayer( i ) ) {
            v.setWeight( 0 );
        }
    }

    /**
     * Sets all node weights to 0.
     */
    public void resetNodeWeights() {
        for ( int layer = 0; layer < numberOfLayers(); layer++ ) {
            resetNodeWeights( layer );
        }
    }

    /**
     * Gives nodes on layer i default weights that make them invisible
     */
    public void clearNodeWeights( int i ) {
        layers.get(i).clearWeights();
    }

    /**
     * Sets weights of all nodes on layer i to their positions.
     */
    public void setWeightsToPositions( int i ) {
        for ( Node v: getLayer( i ) ) {
            weightOfNode[ v.getId() ] = positionOfNode[ v.getId() ];
        }
    }

    /**
     * Sets the weights of all nodes to be their positions.
     */
    public void setWeightsToPositions() {
        for ( int layer = 0; layer < numberOfLayers(); layer++ ) {
            setWeightsToPositions( layer );
        }
    }

    /**
     * Displays the weights of nodes on the given layer.
     */
    public void displayWeights( int layer ) {
        layers.get( layer ).displayWeights();
    }

    /**
     * Displays the weights of all nodes.
     */
    public void displayWeights() {
        for ( int layer = 0; layer < numberOfLayers(); layer++ ) {
            displayWeights( layer );
        }
    }

    /**
     * Sorts the list of nodes on layer i by their <em>displayed</em> weights.
     */
    public void sort( int i ) {
        layers.get( i ).sort();
    }

    /**
     * assigns weights to the layer based on node positions
     * @deprecated see setWeightsToPositions
     */
    public void assignWeights( int layer ) {
        for ( Node v: layers.get( layer ).getNodes() ) {
            weightOfNode[ v.getId() ] = positionOfNode[ v.getId() ];
        }
    }

    /**
     * Assigns weights to all of the nodes based on their positions
     * @deprecated see setWeightsToPositions
     */
    public void assignWeights() {
        for ( int layer = 0; layer < layers.size(); layer++ ) {
            assignWeights( layer );
        }
    }

    /**
     * Makes all node labels on layer i blank
     */
    public void clearNodeLabels( int i ) {
        layers.get( i ).clearLabels();
    }

    /**
     * Logically marks node v.
     */
    public void mark( Node v ) {
        isMarked[ v.getId() ] = true;
    }

    /**
     * Logically undoes any logical mark on node v.
     */
    public void unMark( Node v ) {
        isMarked[ v.getId() ] = false;
    }

    /**
     * @return true if node v is marked.
     */
    public boolean isMarked( Node v ) {
        return isMarked[ v.getId() ];
    }

    /**
     * Removes the logical marks from all nodes on layer i.
     */
    public void clearMarks( int i ) {
        layers.get(i).clearMarks();
    }

    /**
     * Removes all logical marks from all nodes.
     */
    public void clearMarks() {
        for ( Node v: graph.getNodes() ) {
            isMarked[ v.getId() ] = false;
        }
    }

    /**
     * Displays all the current logical marks on layer i.
     */
    public void displayMarks( int i ) {
        layers.get(i).displayMarks();
    }

    /**
     * Displays the current logical marks of all nodes.
     */
    public void displayMarks() {
        for ( Node v: graph.getNodes() ) {
            v.setVisited( isMarked[ v.getId() ] );
        }
    }

    /**
     * Removes all displayed marks from layer i without affecting their
     * logical status.
     */
    public void removeMarks( int i ) {
        layers.get(i).removeMarks();
    }

    /**
     * Removes displayed marks from all nodes without affecting their logical
     * status.
     */
    public void removeMarks() {
        for ( Node v: graph.getNodes() ) {
            v.setVisited( false );
        }
    }

   /**
    * Logically marks layer i: used in the animation of a dynamic version of
    * the barycenter heuristic.
    */
    public void markLayer( int i ) {
        layers.get(i).mark();
    }

   /**
    * Logically undoes the marking of layer i: used in the animation of a
    * dynamic version of the barycenter heuristic.
    */
    public void unMarkLayer( int i ) {
        layers.get(i).unMark();
    }

   /**
    * Logically undoes the marking of all layers: used in the animation of a
    * dynamic version of the barycenter heuristic.
    */
    public void clearLayerMarks() {
        for ( int i = 0; i < layers.size(); i++ ) {
            layers.get(i).unMark();
        }
    }

    /**
     * Displays the marked layers: all of the nodes on each marked layer are
     * shown as marked.
     */
    public void displayLayerMarks() {
        for ( int i = 0; i < layers.size(); i++ ) {
            layers.get( i ).displayMarks();
        }
    }

    /**
     * highlights the edges incident on node v.
     */
    public void highlightEdges( Node v ) {
        for ( Edge e: v.getIncidentEdges() ) {
            e.setSelected( true );
        }
    }

    /**
     * Undoes highlighting of the edges incident on node v.
     */
    public void unHighlightEdges( Node v ) {
        for ( Edge e: v.getIncidentEdges() ) {
            e.setSelected( false );
        }
    }

    /**
     * Highlights nodes on layer i.
     * @param scope determines which incident edges, if any, should be
     * highlighted: if scope is UP, the edges pointing to the higher-numbered
     * layers are highlighted; if DOWN, those to the lower-numbered layer; if
     * both, all incident edges; if LAYER, the nodes only.
     */
    public void highlight( int i, Scope scope ) {
        layers.get(i).highlight( scope );
    }

    /**
     * Undoes any highlighting of nodes on layer i and their incident edges.
     */
    public void unHighlight( int i ) {
        layers.get(i).unHighlight();
    }

    /**
     * Undoes highlighting for all nodes and edges.
     */
    public void unHighlight() {
        for ( int layer = 0; layer < numberOfLayers(); layer++ ) {
            unHighlight( layer );
        }
    }

    /**
     * Highlights the nodes between the two given positions, inclusive; used,
     * for example, to highlight an insertion.
     */
    public void highlightNodes( int layer, int positionOne, int positionTwo ) {
        layers.get( layer ).highlightNodes( positionOne, positionTwo );
    }

    /**
     * @return total number of crossings based on current logical positions.
     */
    public int numberOfCrossings() {
        int crossings = 0;
        for ( int layer = 0; layer < numberOfLayers() - 1; layer++ ) {
            crossings += crossingsBetweenLayers( layer );
        }
        return crossings;
    }

    /**
     * @return the number of edges that cross edge e, based on current
     * logical positions.
     */
    public int getCrossings( Edge e ) {
        return crossingsOfEdge[ e.getId() ];
    }

    /**
     * @return the number of crossings that arise among the edges incident to
     * and y if x is to the left of y. Assumes that both are on the same layer.
     */
    public int getCrossings( Node leftNode, Node rightNode ) {
        int layer = getLayer( leftNode );
        int crossings = 0;
        // compute crossings on upward edges (if any)
        if ( layer < numberOfLayers() - 1 ) {
            List<Edge> upEdges = createUpEdgeArray( leftNode, rightNode );
            List<Integer> destination_positions = getDestinationPositions( upEdges );
            crossings += countInversions( destination_positions );
         }

        // compute crossings on downward edges (if any)
        if ( layer > 0 ) {
            List<Edge> upEdges = createDownEdgeArray( leftNode, rightNode );
            List<Integer> source_positions = getSourcePositions( upEdges );
            crossings += countInversions( source_positions );
        }
        return crossings;
    }

    /**
     * @return number of crossings among edges between layer and layer + 1.
     * Uses O(|E|+|C|) algorithm from "Simple and efficient bilayer cross
     * counting," W. Barth, M. Juenger, P. Mutzel, in JGAA, 2004.
     */
    public int crossingsBetweenLayers( int layer ) {
        List<Node> sourceNodes = layers.get(layer).getNodes();
        // create an array of the positions of nodes at the other ends of
        // outgoing edges from the source; for each source node, sort these
        // by position 
        ArrayList<Integer> targetPositions = new ArrayList<Integer>();
        for ( Node v: sourceNodes ) {
            ArrayList<Integer> localTargetPositions = new ArrayList<Integer>();
            for ( Edge e: v.getOutgoingEdges() ) {
                Node w = v.travel(e);
                localTargetPositions.add( positionOfNode[ w.getId() ] );
            }
            Collections.sort( localTargetPositions );
            targetPositions.addAll( localTargetPositions );
        }
        int inversions = countInversions( targetPositions );
        return inversions;
    }

    /**
     * @return the number of crossings for the edges incident on nodes of the
     * given layer.
     */
    int getLayerCrossings( int layer ) {
        int crossings = 0;
        for ( Node v: getLayer( layer ) ) {
            for ( Edge e: v.getIncidentEdges() ) {
                crossings += crossingsOfEdge[ e.getId() ];
            }
        }
        return crossings;
    }

    /**
     * @return the index of the (logically) unmarked layer with the maximum
     * number of crossings or -1 if all layers are marked.
     */
    public int getMaxCrossingsLayer() {
        updateEdgeCrossings();
        int maxCrossings = -1;
        int maxLayer = -1;
        for ( int i = 0; i < layers.size(); i++ ) {
            if ( layers.get(i).isMarked() ) continue;
            int currentCrossings = getLayerCrossings( i );
            if ( currentCrossings > maxCrossings ) {
                maxCrossings = currentCrossings;
                maxLayer = i;
            }
        }
        return maxLayer;
    }

    /**
     * @return the maximum, over all edges e of the graph, of the number of
     * edges crossing e
     */
    public int getMaxEdgeCrossings()
    {
        int maxCrossings = Integer.MIN_VALUE;
        for ( Edge e: graph.getEdges() ) {
            if ( crossingsOfEdge[ e.getId() ] > maxCrossings ) {
                maxCrossings = crossingsOfEdge[ e.getId() ];
            }
        }
        return maxCrossings;
    }

    /**
     * @return the edge with the most crossings among those that still have
     * one (logically) unmarked endpoint or null if all edges have both
     * endpoints marked.
     */
    public Edge getMaxCrossingsEdge()
    {
        Edge maxEdge = null;
        int maxCrossings = Integer.MIN_VALUE;
        for ( Edge e: graph.getEdges() ) {
            if ( ( ! isMarked[ e.getSourceNode().getId() ]
                   || ! isMarked[ e.getDestNode().getId() ] )
                 && crossingsOfEdge[ e.getId() ] > maxCrossings ) {
                maxEdge = e;
                maxCrossings = crossingsOfEdge[ e.getId() ];
            }
        }
        return maxEdge;
    }

    static int indexOfLastReturned = 0;

    /**
     * @return unmarked edge with the most crossings, as with
     * getMaxCrossingsEdge().
     * @param roundRobin if true, start the search one index beyond that of
     * the last edge returned <em>by this method</em>
     */
    public Edge getMaxCrossingsEdge( boolean roundRobin )
    {
        LogHelper.enterMethod( getClass(), "getMaxCrossingsEdge( " + roundRobin + " )" );
        if ( ! roundRobin ) return getMaxCrossingsEdge();
        int maxEdgeIndex = -1;
        Edge maxEdge = null;
        int maxCrossings = Integer.MIN_VALUE;
        List<Edge> edgeList = graph.getEdges();
        int i = indexOfLastReturned + 1;
        i = i % edgeList.size();
        while ( i != indexOfLastReturned ) {
            Edge e = edgeList.get( i );
            LogHelper.logDebug( " maxEdge loop: i = " + i
                                + ", lastIndex = " + indexOfLastReturned );
            LogHelper.logDebug( "    e = " + e );
            LogHelper.logDebug( "    maxEdge = " + maxEdge );
            if ( ( ! isMarked[ e.getSourceNode().getId() ]
                   || ! isMarked[ e.getDestNode().getId() ] )
                 && crossingsOfEdge[ e.getId() ] > maxCrossings ) {
                maxEdgeIndex = i;
                maxEdge = e;
                maxCrossings = crossingsOfEdge[ e.getId() ];
            }
            i = (i + 1) % edgeList.size();
        }
        if ( maxEdgeIndex >= 0 ) {
            indexOfLastReturned = maxEdgeIndex; 
        }
        LogHelper.exitMethod( getClass(), "getMaxCrossingsEdge: index = " + indexOfLastReturned + "\n     edge = " + maxEdge );
        return maxEdge;
    }

    public static final Comparator<Node> DEGREE_COMPARATOR = new Comparator<Node>() {
        public int compare( Node x, Node y ) {
            return x.getDegree()
            - y.getDegree();
        }
    };

    /**
     * Sorts nodes by their degree (for use in sifting).
     */
    public static void sortByIncreasingDegree( List<Node> nodes ) {
        Collections.sort( nodes, DEGREE_COMPARATOR );
    }

    /**
     * Sorts nodes on each layer by increasing degree -- for use in
     * middleDegreeSort() and its reversed version
     */
    public void sortLayersByIncreasingDegree() {
        for ( Layer layer: layers ) {
            layer.sortByIncreasingDegree();
        }
    }

    /**
     * On each layer: puts nodes with largest (smallest) degree in the middle
     * and puts subsequent nodes farther toward the outside.
     *
     * @param largestInMiddle if true then the node with largest degree goes
     * in the middle.
     */
    public void middleDegreeSort( boolean largestInMiddle ) {
        for ( Layer layer: layers ) {
            layer.middleDegreeSort( largestInMiddle );
        }
    }

    /**
     * Updates crossings for edges when two edges form an inversion. Used by
     * the maximum crossings edge heuristic reported by Stallmann in JEA (2012).
     *
     * @param diff indicates whether to increment the number of crossings for
     * the edges (+1) or decrement them (-1)
     */
    void updateEdgeCrossings( Edge e, Edge f, int diff ) {
        crossingsOfEdge[ e.getId() ] += diff;
        crossingsOfEdge[ f.getId() ] += diff;
    }
    
    /**
     * Updates the number of crossings for each edge in edgeList based on the
     * inversions in the positions of the heads of the edges. Used by
     * the maximum crossings edge heuristic reported by Stallmann in JEA (2012).
     *
     * @param diff indicates whether to increment the crossing counts (+1) or
     * decrement them (-1) each time there is an inversion
     */
    void updateUpperEdgeCrossings( List<Edge> edgeList, int diff ) {
        // use insertion sort
        for ( int i = 1; i < edgeList.size(); i++ ) {
            Edge toBeInserted = edgeList.get(i);
            int j = i - 1;
            while ( j >= 0
                    && ( getPosition( edgeList.get(j).getDestNode() )
                         > getPosition( toBeInserted.getDestNode() ) ) ) {
                updateEdgeCrossings( edgeList.get(j), toBeInserted, diff );
                edgeList.set( j + 1, edgeList.get(j) );
                j--;
            }
            edgeList.set( j + 1, toBeInserted );
        }
    }

    /**
     * Updates the number of crossings for each edge in edgeList based on the
     * inversions in the positions of the tails of the edges. Used by the
     * maximum crossings edge heuristic reported by Stallmann in JEA (2012).
     *
     * @param diff indicates whether to increment the crossing counts (+1) or
     * decrement them (-1) each time there is an inversion
     */
    void updateLowerEdgeCrossings( List<Edge> edgeList, int diff ) {
        // use insertion sort
        for ( int i = 1; i < edgeList.size(); i++ ) {
            Edge toBeInserted = edgeList.get(i);
            int j = i - 1;
            while ( j >= 0
                    && ( getPosition( edgeList.get(j).getSourceNode() )
                         > getPosition( toBeInserted.getSourceNode() ) ) ) {
                updateEdgeCrossings( edgeList.get(j), toBeInserted, diff );
                edgeList.set( j + 1, edgeList.get(j) );
                j--;
            }
            edgeList.set(j + 1, toBeInserted);
        }
    }

    /**
     * @return an ArrayList of the outgoing edges of the two nodes with those
     * from leftNode appearing before those from rightNode
     */
    List<Edge> createUpEdgeArray( Node leftNode, Node rightNode ) {
        ArrayList<Edge> outgoingEdges = new ArrayList<Edge>();
        List<Edge> leftNodeEdges = leftNode.getOutgoingEdges();
        List<Edge> rightNodeEdges = rightNode.getOutgoingEdges();
        sortByDestPosition( leftNodeEdges );
        sortByDestPosition( rightNodeEdges );
        outgoingEdges.addAll( leftNodeEdges );
        outgoingEdges.addAll( rightNodeEdges );
        return outgoingEdges;
    }

    /**
     * @return an ArrayList of the outgoing edges of the two nodes with those
     * from leftNode appearing before those from rightNode
     */
    List<Edge> createDownEdgeArray( Node leftNode, Node rightNode ) {
        ArrayList<Edge> incomingEdges = new ArrayList<Edge>();
        List<Edge> leftNodeEdges = leftNode.getIncomingEdges();
        List<Edge> rightNodeEdges = rightNode.getIncomingEdges();
        sortBySourcePosition( leftNodeEdges );
        sortBySourcePosition( rightNodeEdges );
        incomingEdges.addAll( leftNodeEdges );
        incomingEdges.addAll( rightNodeEdges );
        return incomingEdges;
    }

    /**
     * Change counts based on crossings when left_node appears to the left and
     * right node to the right.
     * @param diff +1 to increase crossing counts, -1 to decrease
     * - used by mce heuristic only
     */
    void change_crossings( Node leftNode, Node rightNode, int diff ) {
        int layer = getLayer( leftNode );
        int numberOfLayers = layers.size();
        // update crossings on upward edges (if any)
        if ( layer < numberOfLayers - 1 ) {
            List<Edge> upEdges = createUpEdgeArray( leftNode, rightNode );
            updateUpperEdgeCrossings( upEdges, diff );
         }

        // update crossings on downward edges (if any)
        if ( layer > 0 ) {
            List<Edge> downEdges = createDownEdgeArray( leftNode, rightNode );
            updateLowerEdgeCrossings( downEdges, diff );
        }
    }

    /**
     * Swaps nodes x and y and updates the edge crossings for their incident
     * edges (the only ones that should be affected). Assumes that x and y
     * are on the same layer and that x is initially to the left of y. Used by
     * the maximum crossings edge heuristic reported by Stallmann in JEA (2012).
     *
     * @return the number of crossings for the edge that has the most
     * crossings after the swap (among those involved in the swap).
     */
    public int bottleneckSwap( Node left_node, Node right_node ) {
        change_crossings( left_node, right_node, -1 );
        change_crossings( right_node, left_node, +1 );
        // now find the maximum number of crossings among the edges
        // incident on the two nodes. 
        List<Edge> incidentEdges = left_node.getIncidentEdges();
        incidentEdges.addAll( right_node.getIncidentEdges() );
        int maxCrossings = Integer.MIN_VALUE;
        for ( Edge e: incidentEdges ) {
            int id = e.getId();
            if ( crossingsOfEdge[ id ] > maxCrossings ) {
                maxCrossings = crossingsOfEdge[ id ];
            }
        }
        incidentEdges = null;   // for gc
        return maxCrossings;
    }

    /**
     * Updates the crossings for each edge in a channel.
     *
     * @param sourceLayer the layer containing the source nodes for all the
     * edges whose number of crossings will be updated.
     */
    void updateCrossingsInChannel( int sourceLayer ) {
        ArrayList<Edge> channelEdges = new ArrayList<Edge>();
        for ( Node v: getLayer( sourceLayer ) ) {
            List<Edge> outgoingEdges = v.getOutgoingEdges();
            sortByDestPosition( outgoingEdges );
            for ( Edge e: outgoingEdges ) {
                crossingsOfEdge[ e.getId() ] = 0;
                insertAndUpdateCrossings( e, channelEdges );
            }
            outgoingEdges = null; // for gc
        }
        channelEdges = null;    // for gc
    }

    /**
     * Updates all edge crossings
     */
    private void updateEdgeCrossings() {
        for ( int i = 0; i < layers.size() - 1; i++ ) {
            updateCrossingsInChannel( i );
        }
    }

    /**
     * Sorts the edges in the list by their destination positions
     */
    void sortByDestPosition( List<Edge> edgeList ) {
        for ( int i = 1; i < edgeList.size(); i++ ) {
            Edge e = edgeList.remove( i );
            int j = i - 1;
            int destPosition = positionOfNode[ e.getDestNode().getId() ];
            while ( j >= 0
                    && destPosition < positionOfNode[ edgeList.get( j ).getDestNode().getId() ] ) {
                j--;
            }
            edgeList.add( j + 1, e );
        }
    }

    /**
     * Sorts the edges in the list by their source positions
     *
     * @todo this should go away when focus is on channels
     */
    void sortBySourcePosition( List<Edge> edgeList ) {
        for ( int i = 1; i < edgeList.size(); i++ ) {
            Edge e = edgeList.remove( i );
            int j = i - 1;
            int sourcePosition = positionOfNode[ e.getSourceNode().getId() ];
            while ( j >= 0
                    && sourcePosition < positionOfNode[ edgeList.get( j ).getSourceNode().getId() ] ) {
                j--;
            }
            edgeList.add( j + 1, e );
        }
    }

    /**
     * Inserts an edge into a list of edges, incrementing the crossing count
     * for every inversion
     *
     * @param e the edge to be inserted, presumed to have crossing count of 0
     * @param edgeList a list of edges sorted by the positions of their
     * destination nodes; the sorted order is maintained after the insertion
     */
    void insertAndUpdateCrossings( Edge e, List<Edge> edgeList ) {
        int index = edgeList.size() - 1;
        int destPosition = positionOfNode[ e.getDestNode().getId() ];
        while ( index >= 0
                && destPosition < positionOfNode[ edgeList.get( index ).getDestNode().getId() ] ) {
            crossingsOfEdge[ e.getId() ]++;
            crossingsOfEdge[ edgeList.get( index ).getId() ]++;
            index--;
        }
        edgeList.add( index + 1, e );
    }

    /**
     * Sets the weight of each edge to be the number of crossings. Used for
     * the slow version of the maximum crossings edge heuristic, as reported
     * by Stallmann (JEA, 2012)
     */
    public void setEdgeWeights() 
    {
        for ( Edge e: graph.getEdges() ) {
            e.setWeight( crossingsOfEdge[ e.getId() ] );
        }
    }

    /**
     * @return a list of the positions of all the sources of the edges;
     * the list will be in the same order as the edges
     */
    List<Integer> getSourcePositions( List<Edge> edges ) {
        List<Integer> positions = new ArrayList<Integer>();
        for ( Edge e: edges ) {
            positions.add( positionOfNode[ e.getSourceNode().getId() ] );
        }
        return positions;
    }

    /**
     * @return a list of the positions of all the sources of the edges;
     * the list will be in the same order as the edges
     */
    List<Integer> getDestinationPositions( List<Edge> edges ) {
        List<Integer> positions = new ArrayList<Integer>();
        for ( Edge e: edges ) {
            positions.add( positionOfNode[ e.getDestNode().getId() ] );
        }
        return positions;
    }


    /**
     * @return the number of inversions in 'integers'
     */
    int countInversions( List<Integer> integers ) {
        int inversions = 0;
        for ( int i = 1; i < integers.size(); i++ ) {
            int x = integers.get(i);
            int j = i - 1;
            while ( j >= 0 && x < integers.get(j) ) {
                integers.set( j + 1, integers.get( j ) );
                inversions++;
                j--;
            }
            integers.set( j + 1, x );
        }
        return inversions;
    }

} // end, class LayeredGraph

//  [Last modified: 2015 07 07 at 14:41:54 GMT]
