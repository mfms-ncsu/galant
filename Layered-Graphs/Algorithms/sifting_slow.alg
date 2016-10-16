/**
 * @file sifting.alg
 * @author Matthias Stallmann
 * @date 2014/3/16 An animation of the sifting algorithm for crossing
 * minimization. Code is taken directly from the C implementation in my
 * crossing minimization software.
 *
 * Caution: assumes that the graph is paritioned into layers, defined by
 * y-coordinates of the nodes and that all the edges are between nodes that
 * lie on adjacent layers. Edges are directed from lower numbered layers to
 * higher numbered ones. See LayeredGraph.java for more information
 * 
 * $Id: sifting_slow.alg 104 2015-03-27 16:23:25Z mfms $
 */

import edu.ncsu.csc.Galant.local.LayeredGraph;

class GlobalVariables {
    public int iteration;
}

final GlobalVariables globals = new GlobalVariables();

beginStep();
final LayeredGraph layeredGraph = new LayeredGraph( getGraph() );
endStep();

final int NUMBER_OF_PASSES = 1;

function sift( Node x ) {
    globals.iteration++;
    int layer = layeredGraph.getLayer( x );
    int layerSize = layeredGraph.getLayerSize( layer );

    beginStep();
    layeredGraph.mark( x );
    layeredGraph.displayMarks();
    x.setSelected( true );
    endStep();

    int xPosition = layeredGraph.getPosition( x );
    int previousXPosition = xPosition;
    int previousXWeight = 0;

    int [] diff = new int[ layerSize ];
    // compute the incremental effect of swapping x with each of the nodes on
    // its layer; diff[i] records the difference in number of crossings that
    // would result if x were swapped with the node at position i
    for ( int i = 0; i < layerSize; i++ ) {
         if ( i != xPosition ) {
             endStep(); beginStep();
             Node y = layeredGraph.getNodeAt( layer, i );
             int crossingsXbeforeY = layeredGraph.getCrossings( x, y );
             int crossingsXafterY = layeredGraph.getCrossings( y, x );
             graph.writeMessage( "Crossings when sift node and one at position "
                                 + i + " are in original positions" );
             layeredGraph.highlightEdges( x );
             layeredGraph.highlightEdges( y );
             layeredGraph.displayPosition( x, xPosition );
             layeredGraph.displayPosition( y, i );
             if ( i < xPosition ) {
                 x.setWeight( crossingsXafterY );
             }
             else { // y after x
                 x.setWeight( crossingsXbeforeY );
             }
             endStep();
             beginStep();
             graph.writeMessage( "Crossings when swapping with node at position " + i );

             layeredGraph.displayPosition( x, i );
             layeredGraph.displayPosition( y, xPosition );
             if ( i < xPosition ) {
                 x.setWeight( crossingsXbeforeY );
             }
             else { // y after x
                 x.setWeight( crossingsXafterY );
             }
             endStep(); beginStep();
             diff[i] = crossingsXafterY - crossingsXbeforeY;
             layeredGraph.displayPosition( x, xPosition );
             layeredGraph.displayPosition( y, i );
             graph.writeMessage( "Difference in crossings when swapping with node at position " + i );
             y.setWeight( diff[i] );
             y.setLabel( "D" );
             x.clearWeight();
             layeredGraph.unHighlightEdges( x );
             layeredGraph.unHighlightEdges( y );
             endStep(); beginStep();
         }
         else {
             diff[i] = 0;
             graph.writeMessage( "position " + i + " is original position of sift node" ); 
         }
    }
    endStep(); beginStep();
    layeredGraph.clearNodeLabels( layer );

    // compute the minimum prefix sum and its position in the diff array
    // bias the decision in favor of maximum distance from the current
    // position; this does consistently better in preliminary experiments,
    // possibly because it's good to cycle through a lot of possible
    // configurations
    int prefixSum = 0;
    int minPrefixSum = Integer.MAX_VALUE;
    int minPosition = -1;
    Node nodeAtMinPosition = null;
    int maxDistance = xPosition + 1;
    // INVARIANT: prefixSum = crossings(i) - crossings(-1), where
    // crossings(i) represents the number of crossings that would result if x
    // is moved between the nodes originally at positions i and i+1 (at the
    // far left if i = -1)
    x.setSelected( false );     // to avoid confusion with min position
    for ( int i = 0; i < layerSize; i++ ) {
        endStep(); beginStep();
        graph.writeMessage( "Prefix sum of differences up to position " + i
                            + "; min is highlighted" );

        prefixSum += diff[i];
        Node currentNode = layeredGraph.getNodeAt( layer, i );
        currentNode.setWeight( prefixSum );
        currentNode.setLabel( "P" );
        if( prefixSum < minPrefixSum 
            || ( prefixSum == minPrefixSum
                 && Math.abs( i - xPosition ) > maxDistance ) ) {
            if ( nodeAtMinPosition != null ) {
                nodeAtMinPosition.setSelected( false );
            }
            minPrefixSum = prefixSum; minPosition = i;
            maxDistance = Math.abs( i - xPosition );
            currentNode.setSelected( true );
            nodeAtMinPosition = currentNode;
        }
    }
    endStep(); beginStep();
    nodeAtMinPosition.setSelected( false );

    // if minPosition is i, then the node belongs between the nodes at
    // positions i and i + 1 (following the design in the Min-Crossings
    // software); caution: we need to take into account the fact the insert()
    // method removes the node before inserting it into its new position

    if ( minPosition < xPosition ) {
        layeredGraph.insert( layer, xPosition, minPosition + 1 );
    }
    else {
        layeredGraph.insert( layer, xPosition, minPosition );
    }
    layeredGraph.displayMarks();
    layeredGraph.clearNodeWeights( layer );
    layeredGraph.clearNodeLabels( layer );
    layeredGraph.displayPositions( layer );
    graph.writeMessage( "Crossings = " + layeredGraph.numberOfCrossings()
                        + ", iteration = " + globals.iteration );
}

/**
 * @return true if the number of crossings has decreased
 */
function boolean siftIncreasing( List<Node> nodes ) {
    int initial_crossings = layeredGraph.numberOfCrossings();
    for ( int i = 0; i < nodes.size(); i++ ) {
        sift( nodes.get( i ) );
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
        sift( nodes.get( i ) );
    }
    int crossings = layeredGraph.numberOfCrossings();
    if ( crossings < initial_crossings ) return true;
    return false;
}

setDirected( true );

List<Node> nodes = graph.getNodes();
layeredGraph.sortByIncreasingDegree( nodes );
globals.iteration = 0;
for ( int pass = 1; pass <= NUMBER_OF_PASSES; pass++ ) {
    beginStep();
    for_nodes( v ) { 
        v.unMark();
    }
    endStep();
    int initial_crossings = layeredGraph.numberOfCrossings();
    graph.writeMessage( "start pass " + pass 
                        + " crossings = " + initial_crossings );
    boolean improved = siftDecreasing( nodes );
    if ( ! improved ) {
        beginStep();
        for_nodes( v ) { 
            v.unMark();
        }
        endStep();
        improved = siftIncreasing( nodes );
    }
}

//  [Last modified: 2014 07 14 at 20:03:51 GMT]
