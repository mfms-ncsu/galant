/** dijkstra.alg
 *  Implementation of Dijkstra's shortest path algorithm. Interprets
 *  undirected edges as going in both directions.
 * $Id: dijkstra.alg 74 2014-05-14 01:45:29Z mfms $
 */

/** Required: edge weights defined, node and edge weights visible */

algorithm {
    NodePriorityQueue pq = new NodePriorityQueue();
    Edge [] chosenEdge = new Edge[nodeIds()]; 
    beginStep();
    for_nodes(node) {
        node.setWeight(INFINITY);
        pq.add(node);
    }
    Node v = getStartNode();
    v.setSelected(true);
    v.setWeight(0);
    endStep();

    while ( ! pq.isEmpty() ) {
        v = pq.removeMin();
        v.setVisited(true);
        v.setSelected(false);
        for_outgoing ( v, e, w ) {
            if ( ! w.isVisited() )  {
                if ( ! w.isSelected() ) w.setSelected(true);
                double distance = v.getWeight() + e.getWeight();
                if ( distance < w.getWeight() ) {
                    beginStep();
                    e.setSelected(true);
                    Edge previous_chosen = chosenEdge[id(w)];
                    if (previous_chosen != null )
                        previous_chosen.setSelected(false);
                    pq.decreaseKey(w, distance);
                    chosenEdge[id(w)] = e;
                    endStep();
                }
            } // end, neighbor not visited (not in tree); do nothing if node
              // is already in tree
        } // end, adjacency list traversal
    } // stop when priority queue is empty
} // end, algorithm

//  [Last modified: 2016 02 09 at 22:03:59 GMT]
