/**
 * Does a sequence of reductions of a graph based on dominance, i.e., if vw
 * is an edge and the neighbors of v are a subset of the neighbors of w, then
 * there is always a minimum vertex cover that includes w. A special case is
 * a degree-one reduction: v has degree one implies that you can include w
 * and throw away v.
 */

NodePriorityQueue pq;

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

/** @todo this should be easier when neighbors are sets rather than lists
 * and we return them as NodeSet objects */
function boolean subset(List<Node> M, List<Node> N) {
    for ( Node v : M ) {
        print("checking" + v);
        print("contained? " + N.contains(v));
        if ( ! N.contains(v) ) {
            return false;
        }
    }
    return true;
}

algorithm {
    beginStep();
    for_nodes( v ) {
        setWeight(v, degree(v));
    }
    endStep();

    boolean reduced = false;
    do {
        reduced = false;
        queueAllNodes();
        while ( ! pq.isEmpty() ) {
            Node v = pq.removeMin();
            mark(v);
            for_adjacent(v, e, w) {
                beginStep(); highlight(e); highlight(w); endStep();
                List<Node> closedNeighborsV = neighbors(v);
                closedNeighborsV.add(v);
                List<Node> closedNeighborsW = neighbors(w);
                closedNeighborsW.add(w);
                if ( subset(closedNeighborsV, closedNeighborsW ) ) {
                    print("subset");
                    // getting rid of w; inform neighbors that their degree
                    // and position in queue has changed
                    for_adjacent(w, f, x) {
                        highlight(x);
                        setWeight(x, weight(x) - 1);
                        if ( x.is("onQ") )
                            pq.decreaseKey(x, weight(x));
                        else { 
                            pq.add(x);
                            set(x, "onQ");
                        }
                        unhighlight(x);
                    }
                    deleteNode(w);
                    reduced = true;
                }
            }
        }
    } while ( reduced );
}

//  [Last modified: 2016 09 11 at 21:51:59 GMT]
