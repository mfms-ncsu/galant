/**
 * mod_bary.alg
 * An animation of the modified barycenter algorithm for crossing minimization.
 *
 * The algorithm finds the layer with the maximum number of crossings, sorts
 * its nodes based on positions of both neighboring layers and completes a
 * pass going in both directions from the max layer.
 * 
 * $Id: mod_bary.alg 104 2015-03-27 16:23:25Z mfms $
 */

import edu.ncsu.csc.Galant.local.LayeredGraph;

final int NUMBER_OF_PASSES = 4;

class Globals {
    int minCrossings = Integer.MAX_VALUE;
    int iteration = 1;
    int pass = 1;
    int minIteration = 1;
    int numLayers = -1;
}

final Globals globals = new Globals();
final LayeredGraph layeredGraph
    = new LayeredGraph( getGraph() );

/**
 * writes message about crossings
 */
function displayMessage( LayeredGraph.Scope scope ) {
    int crossings = layeredGraph.numberOfCrossings();
    String scopeString = "up";
    if ( scope == LayeredGraph.Scope.DOWN ) scopeString = "down";
    else if ( scope == LayeredGraph.Scope.BOTH ) scopeString = "max layer"; 
    graph.writeMessage( "pass = " + globals.pass
                        + ", iteration = " + globals.iteration
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

function reset() {
    layeredGraph.setWeightsToPositions();
    layeredGraph.displayWeights();
    layeredGraph.clearMarks();
    layeredGraph.displayMarks();
    layeredGraph.unHighlight();
}

function upSweep( int layer, int numLayers ) {
    LayeredGraph.Scope scope = LayeredGraph.Scope.DOWN;
    for ( ; layer < numLayers; layer++ ) {
        beginStep();
        reset();
        layeredGraph.assignWeights( layer, scope );
        layeredGraph.displayWeights( layer );
        layeredGraph.sortByWeight( layer );
        layeredGraph.markPositionChanges( layer );
        layeredGraph.displayMarks( layer );
        layeredGraph.highlight( layer, scope );
        displayMessage( scope );
        endStep();
        checkCrossings();
        globals.iteration++;
        beginStep();
        displayMessage( scope );
        layeredGraph.displayPositions( layer );
        endStep();
    }
}

function downSweep( int layer ) {
    LayeredGraph.Scope scope = LayeredGraph.Scope.UP;
    for ( ; layer >= 0; layer-- ) {
        beginStep();
        reset();
        layeredGraph.assignWeights( layer, scope );
        layeredGraph.displayWeights( layer );
        layeredGraph.sortByWeight( layer );
        layeredGraph.markPositionChanges( layer );
        layeredGraph.displayMarks( layer );
        layeredGraph.highlight( layer, scope );
        displayMessage( scope );
        endStep();
        checkCrossings();
        globals.iteration++;
        beginStep();
        displayMessage( scope );
        layeredGraph.displayPositions( layer );
        endStep();
    }
}

setDirected( true );

/**
 * Now the modified barycenter algorithm.
 */

int numLayers = layeredGraph.numberOfLayers();

beginStep();
layeredGraph.setWeightsToPositions();
graph.writeMessage( "Crossings = " + layeredGraph.numberOfCrossings() );
endStep();

for ( int pass = 0; pass < NUMBER_OF_PASSES; pass++ ) {
    /** what follows is a single pass */
    layeredGraph.clearLayerMarks();
    int maxCrossingsLayer = layeredGraph.getMaxCrossingsLayer();
    while ( maxCrossingsLayer >= 0 ) {
        LayeredGraph.Scope scope = LayeredGraph.Scope.BOTH;
        beginStep();
        reset();
        layeredGraph.markLayer( maxCrossingsLayer );
        layeredGraph.displayLayerMarks();
        displayMessage( scope );
        endStep();
        beginStep();
        layeredGraph.assignWeights( maxCrossingsLayer, scope );
        layeredGraph.displayWeights( maxCrossingsLayer );
        layeredGraph.sortByWeight( maxCrossingsLayer );
        layeredGraph.markPositionChanges( maxCrossingsLayer );
        layeredGraph.displayMarks( maxCrossingsLayer );
        layeredGraph.highlight( maxCrossingsLayer, scope );
        displayMessage( scope );
        endStep();
        checkCrossings();
        globals.iteration++;
        beginStep();
        displayMessage( scope );
        layeredGraph.displayPositions( maxCrossingsLayer );
        endStep();

        upSweep( maxCrossingsLayer + 1, numLayers );
        downSweep( maxCrossingsLayer - 1 );

        maxCrossingsLayer = layeredGraph.getMaxCrossingsLayer();
    }
    globals.pass++;
} // end, pass

layeredGraph.displaySavedPositions();
graph.writeMessage( "min iteration = " + globals.minIteration
                    + ", min crossings = " + globals.minCrossings 
                    );

System.out.println( "Minimum crossings = " + globals.minCrossings
                    + " at iteration " + globals.minIteration );

//  [Last modified: 2014 07 21 at 20:31:24 GMT]
