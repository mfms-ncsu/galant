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

int NUMBER_OF_PASSES = 25;

int numLayers = -1;
int minCrossings = Integer.MAX_VALUE;
int edgeCrossings = 0;
int minEdgeCrossings = Integer.MAX_VALUE;
int iteration = 0;
int pass = 0;
int minIteration = 0;
int minPass = 0;
LayeredGraph layeredGraph;

/**
 * writes message about crossings
 */
function displayMessage( LayeredGraph.Scope scope ) {
    int crossings = layeredGraph.numberOfCrossings();
    String bestCrossingsString = "    ";
    String bestBottleneckString = "    ";
    if ( crossings == minCrossings ) bestCrossingsString = " ** ";
    if ( edgeCrossings == minEdgeCrossings ) bestBottleneckString = " ** ";
    String scopeString = " up ";
    if ( scope == LayeredGraph.Scope.DOWN ) scopeString = "down";
    else if ( scope == LayeredGraph.Scope.BOTH ) scopeString = "both"; 
    display("pass ", pass,
            ", iteration ", iteration,
            " ", scopeString,
            ", crossings = ", crossings, bestCrossingsString,
            ", bottleneck = ",  edgeCrossings, bestBottleneckString
            );
}

/**
 * saves the current positions if the number of crossings has decreased
 */
function checkCrossings() {
    int crossings = layeredGraph.numberOfCrossings();
    if ( crossings < minCrossings ) {
        minCrossings = crossings;
        minIteration = iteration;
        layeredGraph.savePositions();
    }
    edgeCrossings = layeredGraph.getMaxEdgeCrossings();
    if ( edgeCrossings < minEdgeCrossings ) {
      minEdgeCrossings = edgeCrossings;
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
        iteration++;
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
        iteration++;
        beginStep();
        displayMessage( scope );
        layeredGraph.displayPositions( layer );
        endStep();
    }
}

algorithm {
  setDirected( true );
  layeredGraph
    = new LayeredGraph( getGraph() );

  /**
   * Now the modified barycenter algorithm.
   */

  numLayers = layeredGraph.numberOfLayers();

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
      iteration++;
      beginStep();
      displayMessage( scope );
      layeredGraph.displayPositions( maxCrossingsLayer );
      endStep();

      upSweep( maxCrossingsLayer + 1, numLayers );
      downSweep( maxCrossingsLayer - 1 );

      maxCrossingsLayer = layeredGraph.getMaxCrossingsLayer();
    }
    pass++;
  } // end, pass

  layeredGraph.displaySavedPositions();
  graph.writeMessage( "min iteration = " + minIteration
                      + ", min crossings = " + minCrossings 
                      );

  System.out.println( "Minimum crossings = " + minCrossings
                      + " at iteration " + minIteration );
}

//  [Last modified: 2017 11 30 at 12:57:39 GMT]
