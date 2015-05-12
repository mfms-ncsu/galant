/**
 * barycenter.alg
 * An animation of barycenter algorithm for crossing minimization.
 *
 * Caution: assumes that the graph is paritioned into layers, defined by
 * y-coordinates of the nodes and that all the edges are between nodes that
 * lie on adjacent layers. Edges are directed from lower numbered layers to
 * higher numbered ones. See LayeredGraph.java for more information
 * 
 * $Id: barycenter.alg 104 2015-03-27 16:23:25Z mfms $
 */

import edu.ncsu.csc.Galant.local.LayeredGraph;

final int MAX_ITERATIONS = 200;

class Globals {
    int minCrossings = Integer.MAX_VALUE;
    int iteration = 0;
    int pass = 0;
    int minIteration = 0;
    int minPass = 0;
}
final Globals globals = new Globals();
final LayeredGraph layeredGraph
    = new LayeredGraph( getGraph() );

beginStep();
layeredGraph.setWeightsToPositions();
graph.writeMessage( "Crossings = " + layeredGraph.numberOfCrossings() );
endStep();

/**
 * writes a message about current number of crossings, along with context information
 */
function displayMessage( int layer, String sweepDirection ) {
    int crossings = layeredGraph.numberOfCrossings();
    graph.writeMessage( "layer = " + layer
                        + ", pass = " + globals.pass
                        + ", direction = " + sweepDirection
                        + ", iteration = " + globals.iteration
                        + ", crossings = " + crossings 
                        );
}

/**
 * displays weights, highlighting, and message; the beginStep that
 * corresponds to the endStep here occurred when weights and highlighting
 * were undone (reset) for the previous layer
 *
 * @param weightDirection direction of layer on which the current weight
 * assignment is based 
 */
function displayAfterWeightAssignment( int layer, LayeredGraph.Scope weightDirection ) {
    layeredGraph.highlight( layer, weightDirection );
    layeredGraph.displayWeights( layer );
    String sweepDirection =
        ( weightDirection == LayeredGraph.Scope.DOWN ) ? "up" : "down";
    displayMessage( layer, sweepDirection );
    endStep();
    beginStep();
}

/**
 * displays state of affairs after layer is sorted and begins a step that
 * resets weights and highlighting; nodes whose position is changed by the
 * sorting are marked
 */
function displayAfterSort( int layer, String sweepDirection ) {
    //    layeredGraph.displayWeights( layer );
    layeredGraph.markPositionChanges( layer );
    layeredGraph.displayMarks( layer );
    displayMessage( layer, sweepDirection );
    endStep(); beginStep();
    // layeredGraph.displayMarks( layer );
    layeredGraph.displayPositions( layer );
    displayMessage( layer, sweepDirection );
    endStep(); beginStep();
}

/**
 * changes the weights back to the positions of nodes on a layer and unmarks
 * the nodes that have been marked for position changes; the beginStep
 * matched an endStep after weights are assigned to a new layer
 */
function reset( int layer ) {
    layeredGraph.assignWeights( layer );
    layeredGraph.displayWeights( layer );
    layeredGraph.unHighlight( layer );
    layeredGraph.clearMarks( layer );
    layeredGraph.displayMarks( layer );
}

/**
 * saves the current positions if the number of crossings has decreased
 */
function checkCrossings() {
    int crossings = layeredGraph.numberOfCrossings();
    if ( crossings < globals.minCrossings ) {
        globals.minCrossings = crossings;
        globals.minIteration = globals.iteration;
        globals.minPass = globals.pass;
        layeredGraph.savePositions();
    }
}

function upSweep( int numLayers ) {
    for ( int layer = 1; layer < numLayers; layer++ ) {
        layeredGraph.assignWeights( layer, LayeredGraph.Scope.DOWN );
        displayAfterWeightAssignment( layer, LayeredGraph.Scope.DOWN );
        layeredGraph.sortByWeight( layer );
        globals.iteration++;
        checkCrossings();
        displayAfterSort( layer, "up" );
        reset( layer );
    }
}

function downSweep( int numLayers ) {
    for ( int layer = numLayers - 2; layer >= 0; layer-- ) {
        layeredGraph.assignWeights( layer, LayeredGraph.Scope.UP );
        displayAfterWeightAssignment( layer, LayeredGraph.Scope.UP );
        layeredGraph.sortByWeight( layer );
        globals.iteration++;
        checkCrossings();
        displayAfterSort( layer, "down" );
        reset( layer );
    }
}

setDirected( true );

/**
 * Now the barycenter algorithm.
 */

int numLayers = layeredGraph.numberOfLayers();

beginStep();

checkCrossings();
while ( globals.iteration < MAX_ITERATIONS ) {
    /** what follows is a single pass */
    globals.pass++;
    if ( globals.pass % 1000 == 0 )
        System.out.println( "pass = " + globals.pass );
    upSweep( numLayers );
    downSweep( numLayers );
}

layeredGraph.displaySavedPositions();
graph.writeMessage( "min pass = " + globals.minPass
                    + ", min iteration = " + globals.minIteration
                    + ", min crossings = " + globals.minCrossings 
                    );
System.out.println( "min pass = " + globals.minPass
                    + ", min iteration = " + globals.minIteration
                    + ", min crossings = " + globals.minCrossings 
);

//  [Last modified: 2014 07 14 at 20:05:37 GMT]
