/**
 * shift_bary.alg
 *
 * An animation of an algorithm that applies barycenter sorting, doing a
 * regular barycenter pass in each slab of some specified number of layers;
 * slabs then rotate their bottom layers, mod SLAB_SIZE. Very well suited to
 * parallelism.
 *
 * $Id: shift_bary.alg 104 2015-03-27 16:23:25Z mfms $
 */

import edu.ncsu.csc.Galant.local.LayeredGraph;

final int MAX_ITERATIONS = 40;
/**
 * number of layers on which to apply barycenter concurrently during each
 * iteration;
 *
 * if this algorithm is used in parallel, the number of processors can range
 * from 1 to (number of layers) / SLAB_SIZE
 */
final int SLAB_SIZE = 2;

class Globals {
    int minCrossings = Integer.MAX_VALUE;
    int pass = 0;
    int iteration = 0;
    int minPass = 0;
    int minIteration = 0;
    int numLayers = -1;
}
final Globals globals = new Globals();

beginStep();
final LayeredGraph layeredGraph
    = new LayeredGraph( getGraph() );
layeredGraph.setWeightsToPositions();
graph.writeMessage( "Crossings = " + layeredGraph.numberOfCrossings() );
endStep();

/**
 * writes message about crossings
 */
function displayMessage( int bottom, int layer, String direction ) {
    int crossings = layeredGraph.numberOfCrossings();
    graph.writeMessage( "pass = " + globals.pass
                        + ", direction = " + direction
                        + ", bottom = " + bottom
                        + ", layer = " + layer
                        + ", iteration = " + globals.iteration
                        + ", crossings = " + crossings 
                        );
}

/**
 * displays weights, highlighting, and message; the beginStep that
 * corresponds to the endStep here occurred when weights and highlighting
 * were reset for the previous layer 
 */
function displayAfterWeightAssignment( int bottom, int layer, String direction ) {
    if ( direction.equals( "up   " ) || direction.equals( "both" ) ) {
        layeredGraph.highlight( layer, LayeredGraph.Scope.DOWN );
    }
    if ( direction.equals( "down" ) || direction.equals( "both" ) ) {
        layeredGraph.highlight( layer, LayeredGraph.Scope.UP );
    }
    layeredGraph.displayWeights( layer );
    displayMessage( bottom, layer, direction );
    endStep();
    beginStep();
}

/**
 * displays state of affairs after layer is sorted and begins a step that
 * resets weights and highlighting
 */
function displayAfterSort( int bottom, int layer, String direction ) {
    layeredGraph.displayPositions( layer );
    displayMessage( bottom, layer, direction );
    endStep();
    beginStep();
    layeredGraph.displayWeights( layer );
    layeredGraph.unHighlight( layer );
}

/**
 * saves the current positions if the number of crossings has decreased
 */
function checkCrossings() {
    int crossings = layeredGraph.numberOfCrossings();
    System.out.println( "-> checkCrossings: crossings = "
                        + crossings
                        + ", minCrossings = "
                        + globals.minCrossings );
    if ( crossings < globals.minCrossings ) {
        globals.minCrossings = crossings;
        globals.minIteration = globals.iteration;
        globals.minPass = globals.pass;
        layeredGraph.savePositions();
    }
}

function upSweep( int bottomLayer, int topLayer ) {
    System.out.println( "-> upSweep: bottom = " + bottomLayer
                        + ", top = " + topLayer );
    String direction = "up   ";
    if ( topLayer > globals.numLayers - 1 )
        topLayer = globals.numLayers - 1;
    if ( bottomLayer < 0 )
        bottomLayer = 0;
    for ( int layer = bottomLayer; layer <= topLayer; layer++ ) {
        System.out.println( "  sweeping up: layer = " + layer );
        layeredGraph.assignWeights( layer, LayeredGraph.Scope.DOWN );
        displayAfterWeightAssignment( bottomLayer, layer, direction );
        layeredGraph.sortByWeight( layer );
        checkCrossings();
        layeredGraph.assignWeights( layer );
        layeredGraph.displayWeights( layer );
        displayAfterSort( bottomLayer, layer, direction );
    }
}

function downSweep( int bottomLayer, int topLayer ) {
    System.out.println( "-> downSweep: bottom = " + bottomLayer
                        + ", top = " + topLayer );
    String direction = "down";
    if ( topLayer > globals.numLayers - 1 )
        topLayer = globals.numLayers - 1;
    if ( bottomLayer < 0 )
        bottomLayer = 0;
    for ( int layer = topLayer; layer >= bottomLayer; layer-- ) {
        System.out.println( "  sweeping down: layer = " + layer );
        layeredGraph.assignWeights( layer, LayeredGraph.Scope.UP );
        displayAfterWeightAssignment( bottomLayer, layer, direction );
        layeredGraph.sortByWeight( layer );
        checkCrossings();
        layeredGraph.assignWeights( layer );
        layeredGraph.displayWeights( layer );
        displayAfterSort( bottomLayer, layer, direction );
    }
}

setDirected( true );

/**
 * Now the modified barycenter algorithm.
 */

globals.numLayers = layeredGraph.numberOfLayers();

beginStep();
/**
 * the bottom layer of the first slab during a pass -- can be negative to
 * ensure that we always start at layer 0; upsweep() and downsweep() make the
 * necessary adjustments.
 */
int numberOfSlabs = globals.numLayers / SLAB_SIZE;
int startLayer = 0;
while ( globals.iteration < MAX_ITERATIONS ) {
    /** what follows is a single pass */
    globals.pass++;
    //    if ( globals.pass % 1000 == 0 )
        System.out.println( "pass = " + globals.pass );
    // for each slab, do
    for ( int slabBottom = startLayer; slabBottom < globals.numLayers; slabBottom += SLAB_SIZE ) {
        /**
         * do a barycenter pass in each slab; the layers within a slab range
         * from slabBottom to slabBottom + SLAB_SIZE - 1;
         * if slabBottom &lt; 0 or slabBottom + SLAB_SIZE &gt; numLayers,
         * upSweep and downSweep will make the necessary adjustments.
         */
        upSweep( slabBottom, slabBottom + SLAB_SIZE - 1 );
        downSweep( slabBottom, slabBottom + SLAB_SIZE - 1 );
    } // end of sweeps
    // assumes the slabs do their work in parallel
    globals.iteration += 2 * SLAB_SIZE;
    startLayer = (startLayer - 1) % SLAB_SIZE;
} // end of pass

layeredGraph.displaySavedPositions();
graph.writeMessage( "min pass = " + globals.minPass
                    + ", min iteration = " + globals.minIteration
                    + ", min crossings = " + globals.minCrossings 
                    );

//  [Last modified: 2014 07 28 at 14:38:11 GMT]
