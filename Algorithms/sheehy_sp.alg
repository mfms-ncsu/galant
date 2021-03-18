/**
 *  Implementation of Dijkstra's shortest path algorithm. Interprets
 *  undirected edges as going in both directions.
 *
 * This version, based on an idea by Don Sheehy, puts edges rather than
 * vertices on a priority queue.
 * Because the distance from start vertex to the other endpoint of the edge
 * does not change, there is no need for a decreaseKey() operation.
 *
 * Two features of this implementation are
 *  - lazy deletion: edges to vertices already in the tree are ignored when
 * removed from the queue
 *  - a dummy edge from the start vertex to itself; it will be the first edge removed
 */

/** If edge weights are not defined, they will be based on geometry */

/**
 * @return the scaled Euclidian distance between v and w
 */
function double euclidian(Node v, Node w) {
    double MAX_DISTANCE = 10.0;
    double SCALE_FACTOR = MAX_DISTANCE / diagonalLength();
    return distance(v, w) * SCALE_FACTOR;
}

function init_edge_costs() {
    for_edges(edge) {
        if ( ! hasWeight(edge) ) {
            double distance = euclidian(source(edge), target(edge));
            setWeight(edge, distance);
        }
    }
}

/**
 * colors edge e with the given color unless e is blue (part of the tree);
 * this is needed when the graph is undirected
 * also, if c is yellow, hide the weight
 */
function conditional_color(Edge e, String c) {
    if ( ! hasColor(e) || ! color(e).equals(BLUE) ) {
        color(e, c);
        if ( c.equals(YELLOW) )
            hideWeight(e);
    }
}

algorithm {
    showEdgeWeights();
    EdgePriorityQueue pending_edges = new EdgePriorityQueue();
    /** need to keep track of destination for any edge on the queue */
    beginStep();
    init_edge_costs();
    Node s = getNode("Start vertex?");
    Edge dummy_edge = addEdge(s, s);
    setWeight(dummy_edge, 0);
    color(dummy_edge, RED);
    insert(dummy_edge, pending_edges);
    endStep();

    while ( ! empty(pending_edges) ) {
        Edge best = removeMin(pending_edges);
        // choose vertex not already in tree, if any
        Node other_end = marked(source(best)) ? target(best) : source(best);
        beginStep();
        display("best edge has destination ", id(other_end));
        if ( marked(other_end) ) {
            display("destination ", id(other_end), " already in tree");
            conditional_color(best, YELLOW);
            endStep();
            continue;
        }
        color(best, BLUE);      // edge turns blue when part of tree
        mark(other_end);        // nodes are marked when visited
        setWeight(other_end, weight(best));
        showWeight(other_end);
        endStep();
        // the edges incident on other_end can now be queued, distance to
        // other_end will not change
        for_outgoing ( other_end, e, w ) {
            beginStep();
            // edges on the frontier turn red unless already in the tree
            // (the additional test is needed for undirected graphs only
            display("next incident edge: ", id(other_end), ", ", id(w));
            if ( ! marked(w) ) {
                double distance = weight(other_end) + weight(e);
                setWeight(e, distance);
                insert(e, pending_edges);
                conditional_color(e, RED);
            } // w not already visited
            else {
                display("destination ", id(w), " already in tree");
                conditional_color(e, YELLOW);
            }
            endStep();
        } // end, for outgoing edges from endpoint
    } // stop when priority queue is empty
} // end, algorithm

//  [Last modified: 2021 03 13 at 21:51:52 GMT]
