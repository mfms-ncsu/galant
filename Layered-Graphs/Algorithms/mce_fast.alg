/**
 * @file mce_fast.alg
 * @author Matthias Stallmann
 * @date 2014/3/13
 * A version of mce.alg that stops only when there is an improvement
 *
 * Caution: assumes that the graph is paritioned into layers, defined by
 * y-coordinates of the nodes and that all the edges are between nodes that
 * lie on adjacent layers. Edges are directed from lower numbered layers to
 * higher numbered ones. See LayeredGraph.java for more information
 * 
 * $Id: mce_fast.alg 104 2015-03-27 16:23:25Z mfms $
 */

import edu.ncsu.csc.Galant.local.LayeredGraph;

final int NUMBER_OF_PASSES = 2000;
class Globals {
    int minCrossings = Integer.MAX_VALUE;
    int iteration = 0;
}
final Globals globals = new Globals();

beginStep();
final LayeredGraph layeredGraph
    = new LayeredGraph( getGraph() );
endStep();

setDirected( true );

/**
 * @param v the node to be highlighted
 * @param e the edge to be highlighted
 */
function checkCrossings( Node v, Edge e ) {
    int crossings = layeredGraph.numberOfCrossings();
    if ( crossings < globals.minCrossings ) {
        v.setSelected( true );
        e.setSelected( true );
        layeredGraph.displayPositions();
        layeredGraph.displayMarks();
        globals.minCrossings = crossings;
        graph.writeMessage( "Crossings = " + globals.minCrossings 
                            + ", iteration = " + globals.iteration );
        endStep();
        beginStep();
        v.setSelected( false );
        e.setSelected( false );
    }
}

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
}

beginStep();
for ( int pass = 1; pass <= NUMBER_OF_PASSES; pass++ ) {
    if ( pass % 100 == 0 ) System.out.println( "pass = " + pass );
    layeredGraph.clearMarks();
    Edge maxCrossingsEdge = layeredGraph.getMaxCrossingsEdge( true );
    while ( maxCrossingsEdge != null ) {
        globals.iteration++;
        Node v = maxCrossingsEdge.getSourceNode();
        Node w = maxCrossingsEdge.getDestNode();
        if ( ! layeredGraph.isMarked( v ) ) {
            edgeSift( v, maxCrossingsEdge );
            layeredGraph.mark( v );
        }
        checkCrossings( v, maxCrossingsEdge );
        if ( ! layeredGraph.isMarked( w ) ) {
            edgeSift( w, maxCrossingsEdge );
            layeredGraph.mark( w );
        }
        checkCrossings( w, maxCrossingsEdge );

        maxCrossingsEdge = layeredGraph.getMaxCrossingsEdge();
    }
} 
endStep();

//  [Last modified: 2014 07 14 at 20:53:33 GMT]
