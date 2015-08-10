/** 
 * euclidian.alg
 *  Implementation of Dijkstra's shortest path algorithm. Interprets
 *  undirected edges as going in both directions if run in undirected
 *  mode. Unlike dijkstra.alg, the edge weights are set initially by the
 *  algorithm, based on Euclidian distances between nodes.
 * $Id: euclidian.alg 106 2015-04-15 13:01:30Z mfms $
 */

/** Required: edge weights defined, node and edge weights visible */

import java.util.PriorityQueue;

double INFINITY = Double.POSITIVE_INFINITY;

Edge [] chosenEdge;
nodesList discovery;
Graph graph;

function decreaseKey( Node v, double newKey ) {
    nodePQ.remove(v);
    v.setWeight(newKey);
    nodePQ.add(v);
}

function double euclidian( Node v, Node w ) {
    return v.getPosition().distance( w.getPosition() );
}

algorithm {
 graph = getGraph();
 chosenEdge = new Edge[ graph.getNodes().size() ];
 beginStep();
 for_nodes(node) {
     node.setWeight( INFINITY );
     nodePQ.add( node);
 }
 for_edges(edge) {
     Node source = edge.getSourceNode();
     Node destination = edge.getDestNode();
     double distance = euclidian( source, destination );
     edge.setWeight( distance );
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
}
//  [Last modified: 2015 04 07 at 12:39:21 GMT]
