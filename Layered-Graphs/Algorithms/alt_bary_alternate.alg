/**
 * alt_bary_rotate.alg
 * An animation of an algorithm that applies barycenter sorting, alternating
 * between even numbered and odd numbered layers. Unlike the original
 * alt_bary, which bases sorting on both neighboring layers simultaneously, this
 * version alternates between doing a sort based on downward and upward based
 * sorting.
 *
 * $Id: alt_bary_alternate.alg 104 2015-03-27 16:23:25Z mfms $
 */

import edu.ncsu.csc.Galant.local.LayeredGraph;

final int MAX_ITERATIONS = 100;

final LayeredGraph layeredGraph
    = new LayeredGraph( getGraph() );

class Globals {
    int minCrossings = Integer.MAX_VALUE;
    int iteration = 1;
    int minIteration = 1;
    int numLayers = -1;
}
final Globals globals = new Globals();

beginStep();
layeredGraph.setWeightsToPositions();
layeredGraph.displayWeights();
graph.writeMessage( "Crossings = " + layeredGraph.numberOfCrossings() );
endStep();

/**
 * writes message about crossings
 */
function displayMessage() {
    int crossings = layeredGraph.numberOfCrossings();
    graph.writeMessage( "iteration = " + globals.iteration
                        + ", crossings = " + crossings 
                        );
}

/**
 * saves the current positions if the number of crossings has decreased
 */
function checkCrossings() {
    int crossings = layeredGraph.numberOfCrossings();
    if ( crossings < globals.minCrossings ) {
        globals.minCrossings = crossings;
        globals.minIteration = globals.iteration;
        layeredGraph.savePositions();
    }
}

function parallelSort( int startLayer, LayeredGraph.Scope scope ) {
    for ( int layer = startLayer; layer < globals.numLayers; layer += 2 ) {
        layeredGraph.assignWeights( layer, scope );
        layeredGraph.displayWeights( layer );
        layeredGraph.sortByWeight( layer );
        layeredGraph.markPositionChanges( layer );
        layeredGraph.displayMarks( layer );
        layeredGraph.highlight( layer, scope );
    }
}

function reset() {
    layeredGraph.setWeightsToPositions();
    layeredGraph.displayWeights();
    layeredGraph.clearMarks();
    layeredGraph.displayMarks();
    layeredGraph.unHighlight();
}

setDirected( true );

/**
 * Now the algorithm.
 */

globals.numLayers = layeredGraph.numberOfLayers();

graph.writeMessage( "Crossings = " + layeredGraph.numberOfCrossings() );

int startLayer = 1;             // 0 for even layers, 1 for odd

beginStep();
layeredGraph.setWeightsToPositions();
layeredGraph.displayWeights();
endStep();

LayeredGraph.Scope scope = LayeredGraph.Scope.DOWN;
while ( globals.iteration < MAX_ITERATIONS ) {
    /** what follows is a (parallel) iteration */
    if ( scope == LayeredGraph.Scope.DOWN )
        scope = LayeredGraph.Scope.UP;
    else
        scope = LayeredGraph.Scope.DOWN;
        
    beginStep();
    parallelSort( startLayer, LayeredGraph.Scope.DOWN );
    displayMessage();
    endStep();

    beginStep();
    layeredGraph.displayPositions();
    displayMessage();
    endStep();

    checkCrossings();
    globals.iteration++;

    beginStep();
    reset();
    displayMessage();
    endStep();

    startLayer = 1 - startLayer;
}

layeredGraph.displaySavedPositions();
graph.writeMessage( "min iteration = " + globals.minIteration
                    + ", min crossings = " + globals.minCrossings 
                    );

System.out.println( "Minimum crossings = " + globals.minCrossings
                    + " at iteration " + globals.minIteration );

//  [Last modified: 2014 07 14 at 20:56:24 GMT]
