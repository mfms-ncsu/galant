/**
 * alt_bary_slow.alg
 * An animation of an algorithm that applies barycenter sorting, alternating
 * between even numbered and odd numbered layers. Very well suited to
 * parallelism. This is the slow version: all movement during sorting is shown.
 *
 * $Id: alt_bary_slow.alg 104 2015-03-27 16:23:25Z mfms $
 */

import edu.ncsu.csc.Galant.local.LayeredGraph;

final int NUMBER_OF_PASSES = 4;

final LayeredGraph layeredGraph
    = new LayeredGraph( getGraph() );

/**
 * Assigns weights to nodes based on the average positions of their 
 * neighbors: outgoing if no incoming ones exist, incoming if no outgoing
 * ones exist, the average of the two if both exist
 */
function calculateWeights( List<Node> nodes ) {
    for ( Node x: nodes ) {
        x.setSelected( true );

        // first look at outgoing edges
        double outgoingWeight = 0;
        int sumOfPositions = 0;
        for_outgoing( x, e, y ) {
            sumOfPositions += y.getWeight();
            e.setSelected( true );
        }
        int outdegree = x.getOutdegree();
        if ( outdegree == 0 ) outgoingWeight = 0;
        else outgoingWeight = ((double) sumOfPositions) / outdegree;
        x.setWeight( outgoingWeight );

        // then look at incoming edges
        double incomingWeight = 0;
        sumOfPositions = 0;
        for_incoming( x, e, y ) {
            sumOfPositions += y.getWeight();
            e.setSelected( true );
        }
        int indegree = x.getIndegree();
        if ( indegree == 0 ) incomingWeight = 0;
        else incomingWeight = ((double) sumOfPositions) / indegree;
        x.setWeight( incomingWeight );

        if ( indegree == 0 )
            x.setWeight( outgoingWeight );
        else if ( outdegree == 0 )
            x.setWeight( incomingWeight );
        else
            x.setWeight( incomingWeight + outgoingWeight / 2 );
    }
}

/**
 * Sets weights of the nodes to be their positions and undoes highlighting
 * of nodes and their incident edges
 */
function resetWeights( List<Node> nodes ) {
    for ( Node v: nodes ) {
        v.setWeight( layeredGraph.getPosition( v ) );
        v.setSelected( false );
        for_adjacent( v, e, w ) {
            e.setSelected( false );
        }
    }
}

setDirected( true );

/**
 * Now the barycenter algorithm.
 */

int numLayers = layeredGraph.numberOfLayers();

graph.writeMessage( "Crossings = " + layeredGraph.numberOfCrossings() );

/** @todo use setNodeWeights() elsewhere */
beginStep();
layeredGraph.setNodeWeights();
endStep();

for ( int pass = 0; pass < NUMBER_OF_PASSES; pass++ ) {
    // first, reorder based on odd-numbered layers
    for ( int layer = 1; layer < numLayers; layer += 2 ) {
        List<Node> nodesOnLayer = layeredGraph.getLayer( layer );
        beginStep();
        calculateWeights( nodesOnLayer );
        endStep();
        layeredGraph.sort( layer );
        layeredGraph.displayPositions( layer );
        beginStep();
        resetWeights( nodesOnLayer );
        graph.writeMessage( "Crossings = " + layeredGraph.numberOfCrossings() );
        endStep();
    }

    for ( int layer = 0; layer < numLayers; layer += 2 ) {
        List<Node> nodesOnLayer = layeredGraph.getLayer( layer );
        beginStep();
        calculateWeights( nodesOnLayer );
        endStep();
        layeredGraph.sort( layer );
        layeredGraph.displayPositions( layer );
        beginStep();
        resetWeights( nodesOnLayer );
        graph.writeMessage( "Crossings = " + layeredGraph.numberOfCrossings() );
        endStep();
    }
}

//  [Last modified: 2014 07 14 at 20:55:36 GMT]
