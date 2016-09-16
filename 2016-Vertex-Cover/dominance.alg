/**
 * Does a sequence of reductions of a graph based on dominance, i.e., if vw
 * is an edge and the neighbors of v are a subset of the neighbors of w, then
 * there is always a minimum vertex cover that includes w. A special case is
 * a degree-one reduction: v has degree one implies that you can include w
 * and throw away v.
 */

NodePriorityQueue pq;
NodeSet cover;

function queueAllNodes() {
    /** @todo for some reason, the constructor that allows a priority queue
     * to be initialized from a collection is not recognized. */
    pq = new NodePriorityQueue();
    beginStep();
    for_nodes( v ) {
        set(v, "onQ");
        pq.add(v);
    }
    endStep();
}

function checkCover() {
    for_edges( e ) {
        highlight(e);
        if ( cover.contains(source(e)) || cover.contains(target(e)) ) {
            unHighlight(e);
        }
    }
}

/**
 * @todo Consider making standard set operations built-in
 */
function boolean subset(NodeSet M, NodeSet N) {
    for ( Node v : M ) {
        if ( ! N.contains(v) ) {
            return false;
        }
    }
    return true;
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
    beginStep();
    for_nodes( v ) {
        setWeight(v, degree(v));
    }
    endStep();

    queueAllNodes();
    while ( ! pq.isEmpty() ) {
        Node v = pq.removeMin();
        if ( hidden(v) ) continue;
        highlight(v);
        /**
         * @todo Need built-in way to traverse only visible neighbors
         */
        for_adjacent(v, e, w) {
            if ( visible(w) ) {
                highlight(e);
                NodeSet closedNeighborsV = visibleNeighbors(v);
                closedNeighborsV.add(v);
                NodeSet closedNeighborsW = visibleNeighbors(w);
                closedNeighborsW.add(w);
                if ( subset(closedNeighborsV, closedNeighborsW ) ) {
//                     print(w + " dominates " + v);
                    mark(w);
                    cover.add(w);
                    // inform neighbors that their degree and position in
                    // queue has changed
                    NodeSet neighborsOfW = visibleNeighbors(w);
                    beginStep();
                    for ( Node x : neighborsOfW ) {
                        color(x, GREEN);
                        setWeight(x, weight(x) - 1);
                        if ( x.is("onQ") && weight(x) > 0 )
                            pq.decreaseKey(x, weight(x));
                        else if ( weight(x) > 0 ) { 
                            pq.add(x);
                            set(x, "onQ");
                        }
                        else {
                            pq.remove(x);
                            clear(x, "onQ");
                            hide(x);
                        }
                    }
                    hide(w);
                    endStep();
                    beginStep();
                    for ( Node x : neighborsOfW ) uncolor(x);
                    endStep();
                } // dominance established
                unhighlight(e);
            } // w is visible
        } // for adjacent to v
        unhighlight(v);
    } // while pq not empty
    beginStep();
    showNodes();
    checkCover();
    endStep();
}

//  [Last modified: 2016 09 16 at 18:45:22 GMT]
