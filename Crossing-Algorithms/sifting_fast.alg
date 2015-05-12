/**
 * @file sifting_fast.alg
 * @author Matthias Stallmann
 * @date 2014/3/25
 * A version of sifting that displays only when the number of crossings has
 * decreased
 *
 * Caution: assumes that the graph is paritioned into layers, defined by
 * y-coordinates of the nodes and that all the edges are between nodes that
 * lie on adjacent layers. Edges are directed from lower numbered layers to
 * higher numbered ones. See LayeredGraph.java for more information
 * 
 * $Id: sifting_fast.alg 104 2015-03-27 16:23:25Z mfms $
 */

import edu.ncsu.csc.Galant.local.LayeredGraph;

class GlobalVariables {
    public int iteration;
    public int pass;
}

final GlobalVariables globals = new GlobalVariables();
final LayeredGraph layeredGraph = new LayeredGraph( getGraph() );

final int NUMBER_OF_PASSES = 1000;

setDirected( true );

function displayCrossings( Node x, boolean increasing ) {
    beginStep();
    layeredGraph.displayPositions();
    layeredGraph.displayMarks();
    x.setSelected( true );
    for_adjacent( x, e, y ) {
        e.setSelected( true );
    }
    int crossings = layeredGraph.numberOfCrossings();
    graph.writeMessage( "pass = " + globals.pass
                        + ", increasing = " + increasing
                        + ", iteration = " + globals.iteration
                        + ", crossings = " + crossings );
    endStep();
    beginStep();
    x.setSelected( false );
    for_adjacent( x, e, y ) {
        e.setSelected( false );
    }
    endStep();
}

function sift( Node x ) {
    globals.iteration++;
    int pos = layeredGraph.getPosition( x );
    int layer = layeredGraph.getLayer( x );
    int layerSize = layeredGraph.getLayerSize( layer );
    int [] diff = new int[ layerSize ];
    // compute the incremental effect of swapping x with each of the nodes on
    // its layer; diff[i] records the difference in number of crossings that
    // would result if x were swapped with the node at position i
    for ( int i = 0; i < layerSize; i++ ) {
        Node y = layeredGraph.getNodeAt( layer, i );
        if ( y != x ) {
            diff[i] = layeredGraph.getCrossings( y, x )
                - layeredGraph.getCrossings( x, y );
        }
        else diff[i] = 0;
    }

    // compute the minimum prefix sum and its position in the diff array
    // bias the decision in favor of maximum distance from the current
    // position; this does consistently better in preliminary experiments,
    // possibly because it's good to cycle through a lot of possible
    // configurations
    int prefix_sum = 0;
    int min_prefix_sum = 0;
    int min_position = -1;
    int max_distance = pos + 1;
    // INVARIANT: prefix_sum = crossings(i) - crossings(-1), where
    // crossings(i) represents the number of crossings that would result if x
    // is moved between the nodes originally at positions i and i+1 (at the
    // far left if i = -1)
    for ( int i = 0; i < layerSize; i++ ) {
      prefix_sum += diff[i];
      if( prefix_sum < min_prefix_sum 
          || ( prefix_sum == min_prefix_sum
               && Math.abs( i - pos ) > max_distance ) ) {
          min_prefix_sum = prefix_sum;
          min_position = i;
          max_distance = Math.abs( i - pos );
      }
    }
    // if min_position is i, then the node belongs between the nodes at
    // positions i and i + 1 (following the design in the Min-Crossings
    // software); caution: we need to take into account the fact the insert()
    // method removes the node before inserting it into its new position

    if ( min_position < pos ) {
        layeredGraph.insert( layer, pos, min_position + 1 );
    }
    else {
        layeredGraph.insert( layer, pos, min_position );
    }
    layeredGraph.mark( x );
}

/**
 * @return true if the number of crossings has decreased
 */
function boolean siftIncreasing( List<Node> nodes ) {
    int initial_crossings = layeredGraph.numberOfCrossings();
    for ( int i = 0; i < nodes.size(); i++ ) {
        int previousCrossings = layeredGraph.numberOfCrossings();
        sift( nodes.get( i ) );
        int currentCrossings = layeredGraph.numberOfCrossings();
        if ( currentCrossings < previousCrossings ) {
            displayCrossings( nodes.get(i), true );
        }
    }
    int crossings = layeredGraph.numberOfCrossings();
    if ( crossings < initial_crossings ) return true;
    return false;
}

/**
 * @return true if the number of crossings has decreased
 */
function boolean siftDecreasing( List<Node> nodes ) {
    int initial_crossings = layeredGraph.numberOfCrossings();
    for ( int i = nodes.size() - 1; i >= 0; i-- ) {
        int previousCrossings = layeredGraph.numberOfCrossings();
        sift( nodes.get( i ) );
        int currentCrossings = layeredGraph.numberOfCrossings();
        if ( currentCrossings < previousCrossings ) {
            displayCrossings( nodes.get(i), false );
        }
    }
    int crossings = layeredGraph.numberOfCrossings();
    if ( crossings < initial_crossings ) return true;
    return false;
}

List<Node> nodes = graph.getNodes();
layeredGraph.sortByIncreasingDegree( nodes );
globals.iteration = 0;
displayCrossings( getNodeById(0), true );
for ( globals.pass = 1; globals.pass <= NUMBER_OF_PASSES; globals.pass++ ) {
    if ( globals.pass % 100 == 0 ) System.out.println( "pass = " + globals.pass );
   layeredGraph.clearMarks();
    boolean improved = siftDecreasing( nodes );
    if ( ! improved ) {
        layeredGraph.clearMarks();
        improved = siftIncreasing( nodes );
    }
}

//  [Last modified: 2014 03 28 at 20:24:14 GMT]
