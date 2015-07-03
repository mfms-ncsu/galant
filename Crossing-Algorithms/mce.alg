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
 * $Id: mce.alg 104 2015-03-27 16:23:25Z mfms $
 */

import edu.ncsu.csc.Galant.local.LayeredGraph;

int MAX_ITERATIONS = 5;
int minCrossings = Integer.MAX_VALUE;
int minEdgeCrossings = Integer.MAX_VALUE;
int iteration = 0;
int pass = 0;
int minIteration = 0;
int minPass = 0;

Edge maxCrossingsEdge;
LayeredGraph layeredGraph;

function checkCrossings() {
    int crossings = layeredGraph.numberOfCrossings();
    if ( crossings < minCrossings ) {
        minCrossings = crossings;
        minIteration = iteration;
        minPass = pass;
        layeredGraph.savePositions();
    }
    if ( layeredGraph.getMaxEdgeCrossings() < minEdgeCrossings ) {
        minEdgeCrossings = layeredGraph.getMaxEdgeCrossings();
    }
}

function displayCrossings() {
    int crossings = layeredGraph.numberOfCrossings();
    layeredGraph.displayPositions();
    layeredGraph.displayMarks();
    graph.writeMessage( "pass = " + pass
                        + ", iteration = " + iteration
                        + ", crossings = " + crossings
                        + ", edge_crossings = " + minEdgeCrossings
                        );
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

algorithm {

    System.out.println( "nodes = " + getGraph().numberOfNodes()
                        + ", edges = " + getGraph().numberOfEdges() );
    beginStep();
    layeredGraph = new LayeredGraph( getGraph() );
    System.out.println( "nodes = " + getGraph().numberOfNodes()
                        + ", edges = " + getGraph().numberOfEdges() );

    System.out.println( "\n" + layeredGraph );
    setDirected( true );

    System.out.printf( "iteration = %d, MAX_ITERATIONS = %d\n",
                       iteration, MAX_ITERATIONS );
    while ( iteration < MAX_ITERATIONS ) {
        pass++;
        if ( pass % 100 == 0 ) System.out.println( "pass = " + pass );
        layeredGraph.clearMarks();
        maxCrossingsEdge = layeredGraph.getMaxCrossingsEdge( true );
        while ( maxCrossingsEdge != null && iteration < MAX_ITERATIONS ) {
            System.out.println(  "iteration " + iteration + "\n" + layeredGraph );
            checkCrossings();
            displayCrossings();
            endStep(); beginStep();
            maxCrossingsEdge.highlight();
            iteration++;
            Node v = maxCrossingsEdge.getSourceNode();
            Node w = maxCrossingsEdge.getDestNode();
            checkCrossings();
            displayCrossings();
            endStep(); beginStep();
            if ( ! layeredGraph.isMarked( v ) ) {
                v.highlight();
                displayCrossings();
                endStep(); beginStep();
                edgeSift( v, maxCrossingsEdge );
                layeredGraph.mark( v );
            }
            checkCrossings();
            displayCrossings();
            endStep(); beginStep();
            v.unHighlight();
            if ( ! layeredGraph.isMarked( w ) ) {
                w.highlight();
                displayCrossings();
                endStep(); beginStep();
                edgeSift( w, maxCrossingsEdge );
                layeredGraph.mark( w );
            }
            checkCrossings();
            displayCrossings();
            w.unHighlight();
            endStep(); beginStep();

            maxCrossingsEdge.unHighlight();
            maxCrossingsEdge = layeredGraph.getMaxCrossingsEdge();
        }
    }
    displayCrossings();
    endStep();

    layeredGraph.displaySavedPositions();
    graph.writeMessage( "min pass = " + minPass
                        + ", min iteration = " + minIteration
                        + ", min crossings = " + minCrossings 
                        );

    System.out.println( "Minimum crossings = " + minCrossings
                        + " at iteration " + minIteration );

    System.out.println( "Minimum edge crossings = " + minEdgeCrossings );
}
//  [Last modified: 2015 07 03 at 18:58:06 GMT]
