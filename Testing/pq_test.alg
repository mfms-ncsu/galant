/**
 * Tests of priority queue functionality for nodes and edges
 */

algorithm {
    NodePriorityQueue npq = new NodePriorityQueue();
    EdgePriorityQueue epq = new EdgePriorityQueue(true);
    for_nodes( v ) {
        npq.insert(v, degree(v));
    }
    for_edges( e ) {
        epq.insert(e);
    }
    while ( ! epq.isEmpty() ) {
        Edge e = epq.removeMax();
        print(e);
    }
}

//  [Last modified: 2016 10 14 at 19:35:02 GMT]
