/**
 * manual_selection.alg - allows a user to select a vertex cover one vertex
 * at a time and keeps track of cardinality
 */

NodeSet cover;
EdgeSet edges_not_covered;
Integer cover_size;

function checkCover() {
    for_edges( e ) {
        show(e);
        highlight(e);
        if ( cover.contains(source(e)) || cover.contains(target(e)) ) {
            unHighlight(e);
        }
    }
}

/**
 * @todo Should be built-in
 */
function NodeSet visibleNeighbors(Node v) {
    NodeSet neighbors = new NodeSet();
    for_adjacent(v, e, w) {
        if ( visible(w) ) neighbors.add(w);
    }
    return neighbors;
}

algorithm {
    /**
     * @todo Awkward to have to do the following step if user has weights
     * invisible during edit mode; the semantics of visibility of labels and
     * weights still need work.
     */
    hideEdgeWeights();
    cover = new NodeSet();
    edges_not_covered = new EdgeSet();
    for_edges( e ) {
        edges_not_covered.add(e);
    }

    while ( edges_not_covered.size() > 0 ) {
        Node v = getNode("Choose a node to add to the cover");
        if ( hidden(v) ) continue;
        beginStep();
        highlight(v);
        cover.add(v);
        for_adjacent(v, e, w) {
            highlight(e);
            edges_not_covered.remove(e);
        }
        mark(v);
        display("cover size = " + cover.size());
        endStep();
        beginStep();
        unhighlight(v);
        hide(v);
        endStep();
    } // while there are uncovered edges
    beginStep();
    showNodes();
    checkCover();
    endStep();
}

//  [Last modified: 2016 09 28 at 19:41:50 GMT]
