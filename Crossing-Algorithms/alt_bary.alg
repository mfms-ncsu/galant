/**
 * alt_bary.alg
 * An animation of an algorithm that applies barycenter sorting, alternating
 * between even numbered and odd numbered layers. Very well suited to
 * parallelism.
 *
 * $Id: alt_bary.alg 104 2015-03-27 16:23:25Z mfms $
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
checkCrossings();
while ( globals.iteration < MAX_ITERATIONS ) {
    /** what follows is a (parallel) iteration */
    beginStep();
    displayMessage();
    for ( int layer = startLayer; layer < globals.numLayers; layer += 2 ) {
        layeredGraph.assignWeights( layer, LayeredGraph.Scope.BOTH );
        layeredGraph.highlight( layer, LayeredGraph.Scope.LAYER );
        layeredGraph.displayWeights( layer );
        layeredGraph.sortByWeight( layer );
        layeredGraph.markPositionChanges( layer );
        layeredGraph.displayMarks( layer );
    }
    endStep();
    beginStep();
    layeredGraph.displayPositions();
    displayMessage();
    endStep();
    beginStep();
    layeredGraph.setWeightsToPositions();
    layeredGraph.displayWeights();
    layeredGraph.clearMarks();
    layeredGraph.displayMarks();
    layeredGraph.unHighlight();
    displayMessage();
    endStep();

    checkCrossings();
    globals.iteration++;
    startLayer = 1 - startLayer;
}

layeredGraph.displaySavedPositions();
graph.writeMessage( "min iteration = " + globals.minIteration
                    + ", min crossings = " + globals.minCrossings 
                    );

System.out.println( "Minimum crossings = " + globals.minCrossings
                    + " at iteration " + globals.minIteration );

//  [Last modified: 2014 07 14 at 20:56:41 GMT]
