/**
 * greedy.alg - implementation of a greedy algorithm for vertex cover: always
 * choose the vertex with largest degree; for now, the weight of each vertex
 * is negative degree so that the priority queue, a min heap, functions
 * correctly
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
        if ( ! cover.contains(source(e)) && ! cover.contains(target(e)) ) {
            highlight(e);
        }
    }
}

algorithm {
    hideEdgeWeights();
    cover = new NodeSet();
    beginStep();
    for_nodes( v ) {
        setWeight(v, -degree(v));
    }
    endStep();
    queueAllNodes();
    int cover_size = 0;
    while ( ! pq.isEmpty() ) {
        Node v = pq.removeMin();
        if ( weight(v) == 0 ) continue;
        highlight(v);
        beginStep();
        for_adjacent(v, e, w) {
            color(e, GREEN);
            setWeight(w, weight(w) + 1);
        }
        endStep();
        beginStep();
        mark(v);
        hide(v);
        cover.add(v);
        cover_size++;
        display("cover size = " + cover_size);
        endStep();
    } // while pq not empty
    beginStep();
    showNodes();
    checkCover();
    endStep();
}

//  [Last modified: 2016 10 08 at 22:49:58 GMT]
