/** dijkstra.alg
 *  Implementation of Dijkstra's shortest path algorithm. Interprets
 *  undirected edges as going in both directions.
 * $Id: dijkstra.alg 74 2014-05-14 01:45:29Z mfms $
 */

/** Required: edge weights defined, node and edge weights visible */

import java.util.PriorityQueue;

final double INFINITY = Double.POSITIVE_INFINITY;

Edge [] chosenEdge;
Graph graph;
NodePriorityQueue pq;

function decreaseKey( Node v, double newKey ) {
    pq.remove(v);
    v.setWeight(newKey);
    pq.add(v);
}

algorithm {
graph = getGraph();
pq = new NodePriorityQueue();
chosenEdge = new Edge[ graph.getNodes().size() ]; 
beginStep();
for_nodes(node) {
    node.setWeight( INFINITY );
    pq.add( node);
}
//initializationComplete();
// can start at vertex 0 for now, as in dfs, but would be more general to
// allow user to select starting vertex (do for dfs also)
Node v = getNodeById(0);
v.setSelected( true );
v.setWeight( 0 );
endStep();

while ( ! pq.isEmpty() ) {
    v = pq.poll();
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
}
//  [Last modified: 2015 07 10 at 18:38:30 GMT]
