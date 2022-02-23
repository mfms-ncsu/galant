/**
 * packing1.alg - implementation of a packing algorithm
 *  (to get a lower bound for the dominating set problem)
 * this version chooses the lowest degree vertex at each iteration
 * and eliminates it, all neighbors and all cousins from consideration;
 * a cousin is a distance two neighbor
 */

NodePriorityQueue pq;
NodeSet packing;

/**
 * Inserts all nodes into the queue using degree as weight; the queue is a
 * max heap
 */
function queueAllNodes() {
    /** @todo for some reason, the constructor that allows a priority queue
     * to be initialized from a collection is not recognized. */
    pq = new NodePriorityQueue();
    beginStep();
    for_nodes( v ) {
        setWeight(v, degree(v));
        pq.insert(v);
    }
    endStep();
}

algorithm {
    hideEdgeWeights();
    showNodeWeights();
    packing = new NodeSet();
    queueAllNodes();
    int packing_size = 0;
    while ( ! pq.isEmpty() ) {
        Node v = pq.removeMin();
        if ( isMarked(v) ) continue;
        beginStep();
        color(v, BLUE);
        mark(v);
        hideWeight(v);
        packing.add(v);
        packing_size++;
        display("packing size = " + packing_size);
        endStep();
        for_adjacent(v, e, neighbor) {
            // exclude all neighbors of v
            if ( isMarked(neighbor) ) continue;
            beginStep();
            highlight(e);
            highlight(neighbor);
            hideWeight(neighbor);
            mark(neighbor);
            endStep();
            for_adjacent(neighbor, ne, cousin) {
                // exclude all distance 2 neighbors
                if ( isMarked(cousin) ) continue;
                beginStep();
                highlight(ne);
                highlight(cousin);
                hideWeight(cousin);
                mark(cousin);
                for_adjacent(cousin, ce, second_cousin) {
                    // decrease degree of neighbors of cousins
                    // if they have not been excluded
                    if ( isMarked(second_cousin) ) continue;
                    color(ce, GREEN);
                    if ( pq.contains(second_cousin) ) {
                        pq.changeKey(second_cousin,
                                     weight(second_cousin) - 1);
                    }
                }
                endStep();
                // hide the edges that are no longer relevant
                beginStep();
                for_adjacent(cousin, ce, second_cousin) {
                    if ( marked(second_cousin) ) continue;
                    hide(ce);
                }
                endStep();
            }
        }
        endStep();
    } // while pq not empty
}