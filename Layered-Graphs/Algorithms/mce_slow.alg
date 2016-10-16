/**
 * @file mce_slow.alg
 * @author Matthias Stallmann
 * @date 2014/3/7
 * An animation of mce algorithm for crossing minimization. Code is taken
 * directly from the paper ... (actually it fixes a bug in the pseudocode).
 *
 * This version shows a detailed view of how the position of a vertex is
 * chosen.
 *
 * Caution: assumes that the graph is paritioned into layers, defined by
 * y-coordinates of the nodes and that all the edges are between nodes that
 * lie on adjacent layers. Edges are directed from lower numbered layers to
 * higher numbered ones. See LayeredGraph.java for more information
 * 
 * $Id: mce_slow.alg 104 2015-03-27 16:23:25Z mfms $
 */

import edu.ncsu.csc.Galant.local.LayeredGraph;

final int NUMBER_OF_PASSES = 1;

beginStep();
final LayeredGraph layeredGraph
    = new LayeredGraph( getGraph() );
layeredGraph.setEdgeWeights();
endStep();

setDirected( true );

function edgeSift( Node x, Edge maxCrossingsEdge ) {
    int pos = layeredGraph.getPosition( x );
    int layer = layeredGraph.getLayer( x );
    int min_crossings = layeredGraph.getCrossings( maxCrossingsEdge );
    int min_position = pos;
    // maximum distance from the current position of a place where the swap
    // achieved the minimum bottleneck crossings
    int max_distance = 0;
    // try positions to the left of x
    for ( int i = pos - 1; i >= 0; i-- ) {
        Node y = layeredGraph.getNodeAt( layer, i );
        int crossings = layeredGraph.bottleneckSwap( y, x );
        if ( crossings < min_crossings
             || ( crossings == min_crossings
                  && pos - i > max_distance ) ) {
            min_crossings = crossings;
            min_position = i;
            max_distance = pos - i;
        }
    }
    // undo swaps related to positions to the left
    for ( int i = 0; i < pos; i++ ) {
        Node y = layeredGraph.getNodeAt( layer, i );
        int crossings = layeredGraph.bottleneckSwap( x, y );
    }
    // try positions to the right of x
    for ( int i = pos + 1; i < layeredGraph.getLayerSize( layer ); i++ ) {
        Node y = layeredGraph.getNodeAt( layer, i );
        int crossings = layeredGraph.bottleneckSwap( x, y );
        if ( crossings < min_crossings
             || ( crossings == min_crossings
                  && Math.abs( pos - i ) > max_distance ) ) {
            min_crossings = crossings;
            min_position = i;
            max_distance = Math.abs( pos - i );
        }
    }
    layeredGraph.insert( layer, pos, min_position );
    layeredGraph.setEdgeWeights();
}

int iteration = 0;
for ( int pass = 1; pass <= NUMBER_OF_PASSES; pass++ ) {
    beginStep();
    layeredGraph.clearMarks();
    endStep();
    Edge maxCrossingsEdge = layeredGraph.getMaxCrossingsEdge( true );
    while ( maxCrossingsEdge != null ) {
        iteration++;
        maxCrossingsEdge.setSelected( true );
        Node v = maxCrossingsEdge.getSourceNode();
        Node w = maxCrossingsEdge.getDestNode();
        if ( ! layeredGraph.isMarked( v ) ) {
            beginStep();
            v.setSelected( true );
            edgeSift( v, maxCrossingsEdge );
            endStep();
            layeredGraph.displayPositions( layeredGraph.getLayer( v ) );
            beginStep();
            v.setSelected( false );
            layeredGraph.mark( v );
            layeredGraph.displayMarks();
            int minimumCrossings = layeredGraph.numberOfCrossings();
            graph.writeMessage( "Crossings = " + minimumCrossings 
                                + ", iteration = " + iteration );
            endStep();
        }
        if ( ! layeredGraph.isMarked( w ) ) {
            beginStep();
            w.setSelected( true );
            edgeSift( w, maxCrossingsEdge );
            endStep();
            layeredGraph.displayPositions( layeredGraph.getLayer( w ) );
            beginStep();
            w.setSelected( false );
            layeredGraph.mark( w );
            layeredGraph.displayMarks();
            int minimumCrossings = layeredGraph.numberOfCrossings();
            graph.writeMessage( "Crossings = " + minimumCrossings 
                                + ", iteration = " + iteration );
            endStep();
        }
        maxCrossingsEdge.setSelected( false );
        maxCrossingsEdge = layeredGraph.getMaxCrossingsEdge();
    }
} 

//  [Last modified: 2014 07 14 at 20:52:43 GMT]
