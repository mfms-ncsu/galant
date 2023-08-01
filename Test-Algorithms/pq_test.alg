/**
 * pq_test.alg - tests basic priority queue functionality for nodes and edges
 * using object-oriented syntax and both a min-heap and a max-heap
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

//  [Last modified: 2017 01 24 at 17:47:23 GMT]
