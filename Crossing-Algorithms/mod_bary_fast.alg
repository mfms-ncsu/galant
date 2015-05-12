/**
 * mod_bary.alg
 * An animation of the modified barycenter algorithm for crossing minimization.
 *
 * The algorithm finds the layer with the maximum number of crossings, sorts
 * its nodes based on positions of both neighboring layers and completes a
 * pass going in both directions from the max layer.
 * 
 * $Id: mod_bary_fast.alg 104 2015-03-27 16:23:25Z mfms $
 */

import edu.ncsu.csc.Galant.local.LayeredGraph;

final int MAX_ITERATIONS = 100000;


class Globals {
    int minCrossings = Integer.MAX_VALUE;
    int iteration = 0;
    int pass = 0;
}
final Globals globals = new Globals();

beginStep();
final LayeredGraph layeredGraph
    = new LayeredGraph( getGraph() );
layeredGraph.setNodeWeights();
graph.writeMessage( "Crossings = " + layeredGraph.numberOfCrossings() );
endStep();

/**
 * highlights current layer and displayes other useful information if
 * crossings have decreased; direction determines which incident edges are
 * highlighted
 */
function checkCrossings( int layer, String direction ) {
    int crossings = layeredGraph.numberOfCrossings();
    if ( crossings < globals.minCrossings ) {
        globals.minCrossings = crossings;
        if ( direction.equals( "up" ) || direction.equals( "both" ) ) {
            layeredGraph.highlight( layer, LayeredGraph.Scope.DOWN );
        }
        if ( direction.equals( "down" ) || direction.equals( "both" ) ) {
            layeredGraph.highlight( layer, LayeredGraph.Scope.UP );
        }
        layeredGraph.displayWeights( layer );
        layeredGraph.displayLayerMarks();
        layeredGraph.displayPositions();
        graph.writeMessage( "pass = " + globals.pass
                            + ", direction = " + direction
                            + ", iteration = " + globals.iteration
                            + ", crossings = " + crossings 
                            );
        endStep();
        beginStep();
        layeredGraph.unHighlight( layer );
    }
}

function upSweep( int layer, int numLayers ) {
    for ( ; layer < numLayers; layer++ ) {
        layeredGraph.assignWeights( layer, LayeredGraph.Scope.DOWN );
        layeredGraph.sortByWeight( layer );
        globals.iteration++;
        checkCrossings( layer, "up" );
        // assign weights by position
        layeredGraph.assignWeights( layer );
    }
}

function downSweep( int layer ) {
    for ( ; layer >= 0; layer-- ) {
        layeredGraph.assignWeights( layer, LayeredGraph.Scope.UP );
        layeredGraph.sortByWeight( layer );
        globals.iteration++;
        checkCrossings( layer, "down" );
        // assign weights by position
        layeredGraph.assignWeights( layer );
    }
}

setDirected( true );

/**
 * Now the modified barycenter algorithm.
 */

int numLayers = layeredGraph.numberOfLayers();

beginStep();
for ( int pass = 0; pass < NUMBER_OF_PASSES; pass++ ) {
   if ( pass % 100 == 0 ) System.out.println( "pass = " + pass );
    layeredGraph.clearLayerMarks();
    int maxCrossingsLayer = layeredGraph.getMaxCrossingsLayer();
    while ( maxCrossingsLayer >= 0 ) {
        layeredGraph.assignWeights( maxCrossingsLayer, LayeredGraph.Scope.BOTH );
        layeredGraph.sortByWeight( maxCrossingsLayer );
        globals.iteration++;
        checkCrossings( maxCrossingsLayer, "both" );
        // assign weights by position
        layeredGraph.assignWeights( maxCrossingsLayer );
        layeredGraph.markLayer( maxCrossingsLayer );

        upSweep( maxCrossingsLayer + 1, numLayers );
        downSweep( maxCrossingsLayer - 1 );

        maxCrossingsLayer = layeredGraph.getMaxCrossingsLayer();
    }
} // end, pass

//  [Last modified: 2014 07 14 at 20:51:31 GMT]
