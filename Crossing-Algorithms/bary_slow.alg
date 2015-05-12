/**
 * bary_slow.alg
 * An animation of barycenter algorithm for crossing minimization. This
 * version shows the sorting in great detail.
 *
 * Caution: assumes that the graph is paritioned into layers, defined by
 * y-coordinates of the nodes and that all the edges are between nodes that
 * lie on adjacent layers. Edges are directed from lower numbered layers to
 * higher numbered ones. See LayeredGraph.java for more information
 * 
 * $Id: bary_slow.alg 104 2015-03-27 16:23:25Z mfms $
 */

import edu.ncsu.csc.Galant.local.LayeredGraph;

final int NUMBER_OF_PASSES = 4;

final LayeredGraph layeredGraph
    = new LayeredGraph( getGraph() );

/**
 * Assigns weights to nodes based on the average positions of their outgoing
 * neighbors, those on higher numbered layers. Also highlights the nodes and
 * their outgoing edges.
 */
function calculateAveragesOut( List<Node> nodes ) {
    for ( Node x: nodes ) {
        int outdegree = 0;
        int sumOfPositions = 0;
        x.setSelected( true );
        for_outgoing( x, e, y ) {
            sumOfPositions += y.getWeight();
            outdegree++;
            e.setSelected( true );
        }
        x.setWeight( ((double) sumOfPositions) / outdegree );
    }
}

/**
 * Assigns weights to nodes based on the average positions of their incoming
 * neighbors, those on lower numbered layers. Also highlights the nodes and
 * their incoming edges.
 */
function calculateAveragesIn( List<Node> nodes ) {
    for ( Node x: nodes ) {
        int outdegree = 0;
        int sumOfPositions = 0;
        x.setSelected( true );
        for_incoming( x, e, y ) {
            sumOfPositions += y.getWeight();
            outdegree++;
            e.setSelected( true );
        }
        x.setWeight( ((double) sumOfPositions) / outdegree );
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
    /** what follows is a single pass */
    for ( int layer = 1; layer < numLayers - 1; layer++ ) {
        List<Node> nodesOnLayer = layeredGraph.getLayer( layer );
        beginStep();
        calculateAveragesOut( nodesOnLayer );
        endStep();
        layeredGraph.sort( layer );
        layeredGraph.displayPositions( layer );
        beginStep();
        resetWeights( nodesOnLayer );
        graph.writeMessage( "Crossings = " + layeredGraph.numberOfCrossings() );
        endStep();
    }

    for ( int layer = numLayers - 2; layer > 0; layer-- ) {
        List<Node> nodesOnLayer = layeredGraph.getLayer( layer );
        beginStep();
        calculateAveragesIn( nodesOnLayer );
        endStep();
        layeredGraph.sort( layer );
        layeredGraph.displayPositions( layer );
        beginStep();
        resetWeights( nodesOnLayer );
        graph.writeMessage( "Crossings = " + layeredGraph.numberOfCrossings() );
        endStep();
    }
}

//  [Last modified: 2014 07 14 at 20:54:00 GMT]
