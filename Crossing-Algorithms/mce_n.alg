/**
 * @file mce.alg
 * @author Matthias Stallmann
 * @date 2014/3/13
 * An implementation of the mce algorithm for crossing minimization.
 *
 * Caution: assumes that the graph is paritioned into layers, defined by
 * y-coordinates of the nodes and that all the edges are between nodes that
 * lie on adjacent layers. Edges are directed from lower numbered layers to
 * higher numbered ones. See LayeredGraph.java for more information
 * 
 * $Id: mce_n.alg 104 2015-03-27 16:23:25Z mfms $
 */

import edu.ncsu.csc.Galant.local.LayeredGraph;

final int MAX_ITERATIONS = 200;
class Globals {
    int minCrossings = Integer.MAX_VALUE;
    int iteration = 0;
    int pass = 0;
    int minIteration = 0;
    int minPass = 0;
}
final Globals globals = new Globals();

final LayeredGraph layeredGraph
    = new LayeredGraph( getGraph() );

setDirected( true );

function checkCrossings() {
    int crossings = layeredGraph.numberOfCrossings();
    if ( crossings < globals.minCrossings ) {
        globals.minCrossings = crossings;
        globals.minIteration = globals.iteration;
        globals.minPass = globals.pass;
        layeredGraph.savePositions();
    }
}

/**
 * @param v the node to be highlighted
 * @param e the edge to be highlighted
 */
function displayMessage() {
    int crossings = layeredGraph.numberOfCrossings();
    layeredGraph.displayPositions();
    layeredGraph.displayMarks();
    graph.writeMessage( "pass = " + globals.pass
                            + ", iteration = " + globals.iteration
                            + ", crossings = " + crossings 
                            );
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

function sift( Node x ) {
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

    if ( minPosition < position ) {
        layeredGraph.insert( layer, position, minPosition + 1 );
    }
    else {
        layeredGraph.insert( layer, position, minPosition );
    }
} // end, sift

while ( globals.iteration < MAX_ITERATIONS ) {
    globals.pass++;
    if ( globals.pass % 100 == 0 ) System.out.println( "pass = " + globals.pass );
    beginStep();
    displayMessage();
    layeredGraph.clearMarks();
    layeredGraph.displayMarks();
    endStep();
    Edge maxCrossingsEdge = layeredGraph.getMaxCrossingsEdge( true );
    while ( maxCrossingsEdge != null ) {
        globals.iteration++;
        beginStep();
        displayMessage();
        maxCrossingsEdge.highlight();
        endStep();
        Node v = maxCrossingsEdge.getSourceNode();
        Node w = maxCrossingsEdge.getDestNode();
        if ( ! layeredGraph.isMarked( v ) ) {
            beginStep();
            displayMessage();
            v.highlight();
            endStep();
            beginStep();
            sift( v );
            checkCrossings();
            displayMessage();
            endStep();
            beginStep();
            displayMessage();
            v.mark();
            layeredGraph.mark( v );
            v.unHighlight();
            endStep();
        }
        if ( ! layeredGraph.isMarked( w ) ) {
            beginStep();
            displayMessage();
            w.highlight();
            endStep();
            beginStep();
            sift( w );
            checkCrossings();
            displayMessage();
            endStep();
            beginStep();
            displayMessage();
            w.mark();
            layeredGraph.mark( w );
            w.unHighlight();
            endStep();
        }
        beginStep();
        displayMessage();
        maxCrossingsEdge.unHighlight();
        endStep();
        maxCrossingsEdge = layeredGraph.getMaxCrossingsEdge();
    }
} 
endStep();

layeredGraph.displaySavedPositions();
graph.writeMessage( "min pass = " + globals.minPass
                    + ", min iteration = " + globals.minIteration
                    + ", min crossings = " + globals.minCrossings 
                    );

System.out.println( "Minimum crossings = " + globals.minCrossings
                    + " at iteration " + globals.minIteration );

//  [Last modified: 2014 12 10 at 22:00:59 GMT]
