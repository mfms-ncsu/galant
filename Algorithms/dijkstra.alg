/** dijkstra.alg
 *  Implementation of Dijkstra's shortest path algorithm. Interprets
 *  undirected edges as going in both directions.
 * $Id: dijkstra.alg 74 2014-05-14 01:45:29Z mfms $
 */

/** Required: edge weights defined, node and edge weights visible */

import java.util.PriorityQueue;

final double INFINITY = Double.POSITIVE_INFINITY;

final Edge [] chosenEdge = new Edge[ getNodes().size() ];

function decreaseKey( Node v, double newKey ) {
    nodePQ.remove(v);
    v.setWeight(newKey);
    nodePQ.add(v);
}

beginStep();
for_nodes(node) {
    node.setWeight( INFINITY );
    nodePQ.add( node);
}

// can start at vertex 0 for now, as in dfs, but would be more general to
// allow user to select starting vertex (do for dfs also)
Node v = getNodeById(0);
v.setSelected( true );
v.setWeight( 0 );
endStep();

while ( ! nodePQ.isEmpty() ) {
    v = nodePQ.poll();
    v.setVisited( true );
    v.setSelected( false );
    for_outgoing ( v, e, w ) {
        if ( ! w.isVisited() )  {
           if ( ! w.isSelected() ) w.setSelected( true );
           double distance = v.getWeight() + e.getWeight();
           if ( distance < w.getWeight() ) {
                beginStep();
                e.setSelected( true );
                Edge previous_chosen = chosenEdge[w.getId()];
                if (previous_chosen != null )
                    previous_chosen.setSelected( false );
                decreaseKey( w, distance );
                chosenEdge[w.getId()] = e;
                endStep();
            }
        } // end, neighbor not visited (not in tree); do nothing if node is
          // already in tree
    } // end, adjacency list traversal
} // stop when priority queue is empty

//  [Last modified: 2013 08 03 at 12:39:35 GMT]
