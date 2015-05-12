/**
 * alt_bary_fast.alg
 *
 * An animation of an algorithm that applies barycenter sorting, alternating
 * between even numbered and odd numbered layers. Very well suited to
 * parallelism. This version changes the display only if the number of
 * crossings has improved.
 *
 * $Id: alt_bary_fast.alg 104 2015-03-27 16:23:25Z mfms $
 */

import edu.ncsu.csc.Galant.local.LayeredGraph;

final int NUMBER_OF_PASSES = 200;


class Globals {
    int minCrossings = Integer.MAX_VALUE;
    int iteration = 0;
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
function checkCrossings( int layer ) {
    int crossings = layeredGraph.numberOfCrossings();
    if ( crossings < globals.minCrossings ) {
        globals.minCrossings = crossings;
        layeredGraph.displayWeights( layer );
        layeredGraph.highlight( layer, LayeredGraph.Scope.BOTH );
        layeredGraph.displayPositions();
        graph.writeMessage( "crossings = " + crossings 
                            + ", iteration = " + globals.iteration );
        endStep();
        beginStep();
        layeredGraph.unHighlight( layer );
    }
}

setDirected( true );

/**
 * Now the modified barycenter algorithm.
 */

int numLayers = layeredGraph.numberOfLayers();

beginStep();
for ( int pass = 0; pass < NUMBER_OF_PASSES; pass++ ) {
    // first, reorder based on odd-numbered layers
    for ( int layer = 1; layer < numLayers; layer += 2 ) {
        layeredGraph.assignWeights( layer, LayeredGraph.Scope.BOTH );
        layeredGraph.sortByWeight( layer );
        globals.iteration++;
        checkCrossings( layer );
        // assign weights by position
        layeredGraph.assignWeights( layer );
    }

    // then, reorder based on even-numbered layers
    for ( int layer = 0; layer < numLayers; layer += 2 ) {
        layeredGraph.assignWeights( layer, LayeredGraph.Scope.BOTH );
        layeredGraph.sortByWeight( layer );
        globals.iteration++;
        checkCrossings( layer );
        // assign weights by position
        layeredGraph.assignWeights( layer );
    }
} // end, pass

//  [Last modified: 2014 07 14 at 20:56:11 GMT]
