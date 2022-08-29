/**
 * manual_ids.alg - allows a user to select an independent dominating set one vertex
 * at a time and keeps track of cardinality
 */

NodeSet ids;
// nodes not in independent dominating set nor neighbor of ids node
NodeSet available;
Integer ids_size;

/**
 * sets weight of each node to its degree
 */
function initializeWeights() {
    beginStep();
    for ( Node v : getNodes() ) {
        setWeight(v, degree(v));
    }
    endStep();
}

algorithm {
    initializeWeights();
    hideEdgeWeights();
    showNodeWeights();
    ids = new NodeSet();
    available = getNodeSet();
    while ( size(available) > 0 ) {
        Node v = getNode("Choose a node to add to the ids", available, "node no longer available");
        beginStep();
        highlight(v);
        mark(v);
        add(v, ids);
        remove(v, available);
        display("ids size = ", size(ids));
        endStep();
        // we need to be careful to decrease degrees
        // only with respect to neighbors not previously encountered
        NodeSet available_neighbors = new NodeSet();
        beginStep();
        for_adjacent(v, e, w) {
            if ( ! available.contains(w) ) continue;
            highlight(e);
            highlight(w);
            add(w, available_neighbors);
        }
        endStep();
        beginStep();
        for ( Node w : available_neighbors ) {
            hide(w);
            for ( Node x : neighbors(w) ) {
                setWeight(x, weight(x) - 1);
            }
            remove(w, available);
        }
        hide(v);
        endStep();
    } // while there are undominated nodes
    beginStep();
    hideNodeWeights();
    showNodes();
    endStep();
}
