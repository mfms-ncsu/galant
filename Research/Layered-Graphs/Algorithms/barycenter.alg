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

int MAX_ITERATIONS = 200;

int minCrossings = Integer.MAX_VALUE;
int edgeCrossings = 0;
int minEdgeCrossings = Integer.MAX_VALUE;
int iteration = 0;
int pass = 0;
int minIteration = 0;
int minPass = 0;
LayeredGraph layeredGraph;

/**
 * writes a message about current number of crossings, along with context information
 */
function displayMessage( int layer, String sweepDirection ) {
    int crossings = layeredGraph.numberOfCrossings();
    String bestCrossingsString = "    ";
    String bestBottleneckString = "    ";
    if ( crossings == minCrossings ) bestCrossingsString = " ** ";
    if ( edgeCrossings == minEdgeCrossings ) bestBottleneckString = " ** ";
    display( "pass ", pass,
             ", iteration ", iteration,
             ", layer ", layer,
             ", direction ", sweepDirection,
             ", crossings = ", crossings, bestCrossingsString,
             ", bottleneck = ",  edgeCrossings, bestBottleneckString
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
    layeredGraph.resetNodeWeights( layer );
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
    if ( crossings < minCrossings ) {
        minCrossings = crossings;
        minIteration = iteration;
        minPass = pass;
        layeredGraph.savePositions();
    }
    edgeCrossings = layeredGraph.getMaxEdgeCrossings();
    if ( edgeCrossings < minEdgeCrossings ) {
      minEdgeCrossings = edgeCrossings;
    }
}

function upSweep( int numLayers ) {
    for ( int layer = 1; layer < numLayers; layer++ ) {
        layeredGraph.assignWeights( layer, LayeredGraph.Scope.DOWN );
        displayAfterWeightAssignment( layer, LayeredGraph.Scope.DOWN );
        layeredGraph.sortByWeight( layer );
        iteration++;
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
        iteration++;
        checkCrossings();
        displayAfterSort( layer, "down" );
        reset( layer );
    }
}

/**
 * Now the barycenter algorithm.
 */

algorithm {
    movesNodes();
    setDirected( true );

    layeredGraph
        = new LayeredGraph( getGraph() );

    beginStep();
    layeredGraph.setWeightsToPositions();
    display( "Crossings = " + layeredGraph.numberOfCrossings() );
    endStep();

    int numLayers = layeredGraph.numberOfLayers();
    
    beginStep();

    checkCrossings();
    while ( iteration < MAX_ITERATIONS ) {
        /** what follows is a single pass */
        pass++;
        if ( pass % 1000 == 0 )
            print( "pass = " + pass );
        upSweep( numLayers );
        downSweep( numLayers );
    }

    layeredGraph.displaySavedPositions();
    display( "min pass = " + minPass
                        + ", min iteration = " + minIteration
                        + ", min crossings = " + minCrossings 
                        );
    print( "min pass = " + minPass
           + ", min iteration = " + minIteration
           + ", min crossings = " + minCrossings 
           );
}

//  [Last modified: 2021 05 26 at 15:49:41 GMT]
