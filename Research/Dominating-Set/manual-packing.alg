/**
 * manual-packing.alg - just what it says; choose vertices manually
 * since the user might simulate either a static or dynamic version,
 *  labels are used for initial degrees and weights for those that get updated
 * Important:
 *  - when a vertex is packed, it and its neighbors are excluded in the future
 *  - a cousin (distance two neighbor) cannot be used as a packing vertex
 *  - however, it must stay in the graph as a potential neighbor
 * More formally, a valid packing is a set of vertices v_1 , ... , v_k
 *  such that, for any pair i,j, the distance between v_i and v_j is at least 2
 */

/**
 * set up weights and labels
 */
function initialize_packing() {
    for_nodes( v ) {
        setWeight(v, degree(v));
        setLabel(v, degree(v));
    }
}

algorithm {
    beginStep();
    hideEdgeWeights();
    showNodeWeights();
    showNodeLabels();
    initialize_packing();
    endStep();
    int packing_size = 0;
    NodeSet available = getNodeSet();
    while ( ! empty(available) ) {
        Node packed_vertex = getNode("pick a vertex to pack", available, "vertex not available");
        beginStep();
        color(packed_vertex, BLUE);
        mark(packed_vertex);
        hideWeight(packed_vertex);
        hideLabel(packed_vertex);
        packing_size++;
        display("packing size = " + packing_size);
        endStep();

        // first collect unmarked neighbors and cousins in sets
        NodeSet neighbors = neighborSet(packed_vertex);
        EdgeSet neighbor_edges = edgeSet(packed_vertex);
        NodeSet cousins = new NodeSet();
        EdgeSet cousin_edges = new EdgeSet();
        for ( Node neighbor : neighbors ) {
            cousins = union(cousins, neighborSet(neighbor));
            cousin_edges = union(cousin_edges, edgeSet(neighbor));
        }

        // highlight the closed neighborhood of packed_vertex
        beginStep();
        for ( Node neighbor : neighbors ) {
            hideWeight(neighbor);
            hideLabel(neighbor);
            highlight(neighbor);
            mark(neighbor);
            remove(neighbor, available);
        }
        for ( Edge neighbor_edge : neighbor_edges ) {
            highlight(neighbor_edge);
        }
        endStep();

        for ( Node cousin : cousins.difference(neighbors) ) {
            // mark cousins to show that they are not available
            if ( marked(cousin) ) continue;
            beginStep();
            mark(cousin);
            remove(cousin, available);
            hideLabel(cousin);
            hideWeight(cousin);
            endStep();
            // need to be careful not to deal with second cousins
            // that are neighbors or cousins
            beginStep();
            for_adjacent(cousin, sce, second_cousin) {
                if ( isMarked(second_cousin) ) continue;
                if ( neighbors.contains(second_cousin) ) continue;
                if ( cousins.contains(second_cousin) ) continue;
                setWeight(second_cousin, weight(second_cousin) - 1);
            }
            endStep();
        }
    } // while pq not empty
}
