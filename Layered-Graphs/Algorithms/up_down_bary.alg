/**
 * up_down_bary.alg
 * An animation of an algorithm that applies barycenter sorting, alternating
 * between even numbered and odd numbered layers. Unlike the original
 * alt_bary, which bases sorting on both neighboring layers simultaneously, this
 * version does the neighboring layers one at a time.
 *
 * $Id: up_down_bary.alg 104 2015-03-27 16:23:25Z mfms $
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
function displayMessage( LayeredGraph.Scope scope ) {
    int crossings = layeredGraph.numberOfCrossings();
    String scopeString = "up";
    if ( scope == LayeredGraph.Scope.DOWN ) scopeString = "down";
    graph.writeMessage( "iteration = " + globals.iteration
                        + ", scope = " + scopeString
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

function upOrDownStep( int startLayer, LayeredGraph.Scope scope ) {
    beginStep();
    parallelSort( startLayer, scope );
    displayMessage( scope );
    endStep();

    beginStep();
    layeredGraph.displayPositions();
    displayMessage( scope );
    endStep();

    checkCrossings();
    globals.iteration++;

    beginStep();
    reset();
    endStep();
}

setDirected( true );

/**
 * Now the algorithm.
 */

globals.numLayers = layeredGraph.numberOfLayers();

graph.writeMessage( "Crossings = " + layeredGraph.numberOfCrossings() );

beginStep();
layeredGraph.setWeightsToPositions();
layeredGraph.displayWeights();
endStep();

int startLayer = 1;             // 0 for even layers, 1 for odd
LayeredGraph.Scope scope = LayeredGraph.Scope.DOWN;
while ( globals.iteration < MAX_ITERATIONS ) {
    for ( int i = 0; i < globals.numLayers; i++ ) {
        upOrDownStep( startLayer, scope );
        startLayer = 1 - startLayer;
    }
    if ( scope == LayeredGraph.Scope.DOWN )
        scope = LayeredGraph.Scope.UP;
    else scope = LayeredGraph.Scope.DOWN;
}

layeredGraph.displaySavedPositions();
graph.writeMessage( "min iteration = " + globals.minIteration
                    + ", min crossings = " + globals.minCrossings 
                    );

System.out.println( "Minimum crossings = " + globals.minCrossings
                    + " at iteration " + globals.minIteration );

//  [Last modified: 2014 07 22 at 15:46:42 GMT]
