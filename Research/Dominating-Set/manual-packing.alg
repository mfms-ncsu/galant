/**
 * manual-packing.alg - just what it says; choose vertices manually
 * since the user might simulate either a static or dynamic version,
 *  labels are used for initial degrees and weights for those that get updated
 */

/**
 * set up weights and labels
 */
function initialize_packing() {
    beginStep();
    for_nodes( v ) {
        setWeight(v, degree(v));
        setLabel(v, degree(v));
    }
    endStep();
}

algorithm {
    hideEdgeWeights();
    showNodeWeights();
    showNodeLabels();
    initialize_packing();
    int packing_size = 0;
    NodeSet available = getNodeSet();
    while ( ! empty(available) ) {
        Node packed_vertex = getNode("pick a vertex to pack", available, "vertex not available");
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
        for ( Node unavailable : union(neighbors, cousins) ) {
            hideWeight(unavailable);
            hideLabel(unavailable);
            highlight(unavailable);
        }
        for ( Edge to_be_highlighted : union(neighbor_edges, cousin_edges) ) {
            highlight(to_be_highlighted);
        }
        endStep();

        for ( Node cousin : cousins.difference(neighbors) ) {
            // need to be careful not to deal with second cousins
            // that are neighbors or cousins
            // hide the edges to second cousins - no longer relevant
            beginStep();
            for_adjacent(cousin, sce, second_cousin) {
                if ( isMarked(second_cousin) ) continue;
                if ( neighbors.contains(second_cousin) ) continue;
                if ( cousins.contains(second_cousin) ) continue;
                hide(sce);
                setWeight(second_cousin, weight(second_cousin) - 1);
            }
            endStep();
        }
        // now exclude all neighbors and cousins by marking them
        // and removing them from available set
        beginStep();
        for ( Node not_available : union(neighbors, cousins) ) {
            mark( not_available );
            remove(not_available, available);
        }
        endStep(); 
    } // while pq not empty
}
