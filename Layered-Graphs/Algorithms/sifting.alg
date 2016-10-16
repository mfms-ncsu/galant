/**
 * @file sifting.alg
 * @author Matthias Stallmann
 * @date 2014/3/16 An animation of the sifting algorithm for crossing
 * minimization. Code is taken directly from the C implementation in my
 * crossing minimization software.
 *
 * Caution: assumes that the graph is paritioned into layers, defined by
 * y-coordinates of the nodes and that all the edges are between nodes that
 * lie on adjacent layers. Edges are directed from lower numbered layers to
 * higher numbered ones. See LayeredGraph.java for more information
 * 
 * $Id: sifting.alg 104 2015-03-27 16:23:25Z mfms $
 */

import edu.ncsu.csc.Galant.local.LayeredGraph;

int iteration = 1;
int pass = 1;
int minCrossings = Integer.MAX_VALUE;
int minIteration = -1;

LayeredGraph layeredGraph;

int MAX_ITERATIONS = 100;

/**
 * writes message about crossings
 */
function displayMessage() {
    int crossings = layeredGraph.numberOfCrossings();
    display( "pass = " + pass
             + ", iteration = " + iteration
             + ", crossings = " + crossings 
             );
}

/**
 * saves the current positions if the number of crossings has decreased
 */
function checkCrossings() {
    int crossings = layeredGraph.numberOfCrossings();
    if ( crossings < minCrossings ) {
        minCrossings = crossings;
        minIteration = iteration;
        layeredGraph.savePositions();
    }
}

/**
 * Computes the incremental effect of swapping x with each of the nodes on
 * its layer; diff[i] records the difference in number of crossings that
 * would result if x were swapped with the node at position i
 */
function computeDiff( Node x, int layer, Integer [] diff ) {
    for ( int i = 0; i < diff.length; i++ ) {
        Node y = layeredGraph.getNodeAt( layer, i );
        if ( y != x ) {
            diff[i] = layeredGraph.getCrossings( y, x )
                - layeredGraph.getCrossings( x, y );
        }
        else diff[i] = 0;
    }
}

/** 
 * Computes the minimum prefix sum and its position in the diff array;
 * biases the decision in favor of maximum distance from the current
 * position; this does consistently better in preliminary experiments,
 * possibly because it's good to cycle through a lot of possible
 * configurations
 */
function int getMinPosition( Integer [] diff, int position ) {
    int prefixSum = 0;
    int minPrefixSum = 0;
    int minPosition = -1;
    int maxDistance = position + 1;
    // INVARIANT: prefix_sum = crossings(i) - crossings(-1), where
    // crossings(i) represents the number of crossings that would result if x
    // is moved between the nodes originally at positions i and i+1 (at the
    // far left if i = -1)
    for ( int i = 0; i < diff.length; i++ ) {
        prefixSum += diff[i];
        if ( prefixSum < minPrefixSum 
             || ( prefixSum == minPrefixSum
                  && Math.abs( i - position ) > maxDistance ) ) {
            minPrefixSum = prefixSum;
            minPosition = i;
            maxDistance = Math.abs( i - position );
        }
    }
    return minPosition;
}

function sift( Node x ) {
    beginStep();
    displayMessage();
    x.setSelected( true );      // highlights node x
    int position = layeredGraph.getPosition( x );
    int layer = layeredGraph.getLayer( x );
    int layerSize = layeredGraph.getLayerSize( layer );
    Integer [] diff = new Integer[ layerSize ];
    computeDiff( x, layer, diff );
    int minPosition = getMinPosition( diff, position );

    // if min_position is i, then the node belongs between the nodes at
    // positions i and i + 1 (following the design in the Min-Crossings
    // software); caution: we need to take into account the fact the insert()
    // method removes the node before inserting it into its new position

    endStep();
    beginStep();
    displayMessage();
    x.mark();
    if ( minPosition < position ) {
        layeredGraph.highlightNodes( layer, minPosition + 1, position );
        layeredGraph.insert( layer, position, minPosition + 1 );
    }
    else {
        layeredGraph.highlightNodes( layer, position, minPosition );
        layeredGraph.insert( layer, position, minPosition );
    }
    endStep(); beginStep();
    displayMessage();
    layeredGraph.displayPositions( layer );
    layeredGraph.unHighlight( layer );
    endStep();
}

/**
 * @return true if the number of crossings has decreased
 */
function boolean siftIncreasing( List<Node> nodes ) {
    int initial_crossings = layeredGraph.numberOfCrossings();
    for ( int i = 0; i < nodes.size(); i++ ) {
        sift( nodes.get( i ) );
        checkCrossings();
        iteration++;
    }
    int crossings = layeredGraph.numberOfCrossings();
    if ( crossings < initial_crossings ) return true;
    return false;
}

/**
 * @return true if the number of crossings has decreased
 */
function boolean siftDecreasing( List<Node> nodes ) {
    int initial_crossings = layeredGraph.numberOfCrossings();
    for ( int i = nodes.size() - 1; i >= 0; i-- ) {
        sift( nodes.get( i ) );
        checkCrossings();
        iteration++;
    }
    int crossings = layeredGraph.numberOfCrossings();
    if ( crossings < initial_crossings ) return true;
    return false;
}

algorithm {
    setDirected( true );

    layeredGraph = new LayeredGraph( getGraph() );

    beginStep();
    displayMessage();
    endStep();

    List<Node> nodes = graph.getNodes();
    layeredGraph.sortByIncreasingDegree( nodes );
    while ( iteration <= MAX_ITERATIONS ) {
        if ( pass % 1000 == 0 )
            print( "pass = " + pass );
        beginStep();
        displayMessage();
        layeredGraph.clearMarks();
        layeredGraph.displayMarks();
        endStep();
        boolean improved = siftDecreasing( nodes );
        if ( ! improved ) {
            beginStep();
            displayMessage();
            layeredGraph.clearMarks();
            layeredGraph.displayMarks();
            endStep();
            improved = siftIncreasing( nodes );
        }
        pass++;
    }

    layeredGraph.displaySavedPositions();
    display( "min iteration = " + minIteration
             + ", min crossings = " + minCrossings 
             );

    print( "Minimum crossings = " + minCrossings
           + " at iteration " + minIteration );

}

//  [Last modified: 2016 10 16 at 15:34:31 GMT]
