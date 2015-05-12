/**
 * bary_fast.alg
 * An animation of barycenter algorithm for crossing minimization. Unlike
 * barycenter.alg, this one ends a step only if the number of crossings
 * decreases and is therefore more suited for research.
 *
 * Caution: assumes that the graph is paritioned into layers, defined by
 * y-coordinates of the nodes and that all the edges are between nodes that
 * lie on adjacent layers. Edges are directed from lower numbered layers to
 * higher numbered ones. See LayeredGraph.java for more information
 * 
 * $Id: bary_fast.alg 104 2015-03-27 16:23:25Z mfms $
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
layeredGraph.setWeightsToPositions();
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
        layeredGraph.displayWeights( layer );
        if ( direction.equals( "up" ) || direction.equals( "both" ) ) {
            layeredGraph.highlight( layer, LayeredGraph.Scope.DOWN );
        }
        if ( direction.equals( "down" ) || direction.equals( "both" ) ) {
            layeredGraph.highlight( layer, LayeredGraph.Scope.UP );
        }
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

function upSweep( int numLayers ) {
    for ( int layer = 1; layer < numLayers; layer++ ) {
        layeredGraph.assignWeights( layer, LayeredGraph.Scope.DOWN );
        layeredGraph.sortByWeight( layer );
        globals.iteration++;
        checkCrossings( layer, "up" );
        // assign weights by position
        layeredGraph.assignWeights( layer );
    }
}

function downSweep( int numLayers ) {
    for ( int layer = numLayers - 2; layer >= 0; layer-- ) {
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
 * Now the barycenter algorithm.
 */

int numLayers = layeredGraph.numberOfLayers();

// end a step whenever the number of crossings decreases
beginStep();

while ( globals.iteration < MAX_ITERATIONS ) {
    /** what follows is a single pass */
    globals.pass++;
    if ( globals.pass % 10000 == 0 )
        System.out.println( "pass = " + globals.pass );
    upSweep( numLayers );
    downSweep( numLayers );
}

//  [Last modified: 2014 07 14 at 20:54:39 GMT]
