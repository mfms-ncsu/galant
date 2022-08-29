/**
 * packing1d.alg - implementation of a packing algorithm
 *  (to get a lower bound for the dominating set problem)
 * this version chooses the lowest degree vertex at each iteration
 * and eliminates it, all neighbors and all cousins from consideration;
 * a cousin is a distance two neighbor
 * it is dynamic in the sense that it updates degrees based on
 * edges from cousins
 */

NodePriorityQueue pq;

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
    queueAllNodes();
    int packing_size = 0;
    while ( ! pq.isEmpty() ) {
        Node packed_vertex = pq.removeMin();
        if ( isMarked(packed_vertex) ) continue;
        beginStep();
        color(packed_vertex, BLUE);
        mark(packed_vertex);
        hideWeight(packed_vertex);
        packing_size++;
        display("packing size = " + packing_size);
        endStep();

        // first collect unmarked neighbors and cousins in sets
        NodeSet neighbors = new NodeSet();
        NodeSet cousins = new NodeSet();
        EdgeSet neighbor_edges = new EdgeSet();
        EdgeSet cousin_edges = new EdgeSet();
        for_adjacent(packed_vertex, e, neighbor) {
            // exclude all neighbors of v
            if ( marked(neighbor) ) continue;
            add(neighbor, neighbors);
            add(e, neighbor_edges);
            for_adjacent(neighbor, ce, cousin) {
                // exclude all distance 2 neighbors
                if ( marked(cousin) ) continue;
                add(cousin, cousins);
                add(ce, cousin_edges);
            }
        }

        // highlight neighbors and cousins and relevant edges
        beginStep();
        for ( Node to_be_highlighted : union(neighbors, cousins) ) {
            hideWeight(to_be_highlighted);
            highlight(to_be_highlighted);
        }
        for ( Edge to_be_highlighted : union(neighbor_edges, cousin_edges) ) {
            highlight(to_be_highlighted);
        }
        endStep();

        // use cousins that are not neighbors to decrease degrees of 'second cousins'
        for ( Node cousin : cousins.difference(neighbors) ) {
            // need to be careful not to deal with second cousins
            // that are neighbors or cousins
            for_adjacent(cousin, sce, second_cousin) {
                // decrease degree of neighbors of cousins
                if ( isMarked(second_cousin) ) continue;
                if ( neighbors.contains(second_cousin) ) continue;
                if ( cousins.contains(second_cousin) ) continue;
                beginStep();
                color(sce, GREEN);
                pq.changeKey(second_cousin, weight(second_cousin) - 1);
                endStep();
            }
            // hide the edges to second cousins - no longer relevant
            beginStep();
            for_adjacent(cousin, sce, second_cousin) {
                if ( isMarked(second_cousin) ) continue;
                if ( neighbors.contains(second_cousin) ) continue;
                if ( cousins.contains(second_cousin) ) continue;
                hide(sce);
            }
            endStep();
        }
        // now exclude all neighbors and cousins by marking them
        beginStep();
        for ( Node not_available : union(neighbors, cousins) ) {
            mark( not_available );
        }
        endStep(); 
    } // while pq not empty
}
