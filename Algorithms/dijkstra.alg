/**
 *  Implementation of Dijkstra's shortest path algorithm. Interprets
 *  undirected edges as going in both directions.
 */

/** Required: edge weights defined; otherwise they will be based on geometry */

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

algorithm {
    showNodeWeights();
    showEdgeWeights();
    NodePriorityQueue pq = new NodePriorityQueue();
    Edge [] chosenEdge = new Edge[nodeIds()]; 
    beginStep();
    init_edge_costs();
    for_nodes(node) {
        setWeight(node, INFINITY);
        insert(node, pq);
    }
    Node s = getNode("Start node?");
    setWeight(s, 0);
    changeKey(s, pq);
    color(s, BLUE);
    endStep();

    while ( ! empty(pq) ) {
        Node v = removeBest(pq);
        beginStep();
        mark(v);        // nodes are marked when visited
        unhighlight(v); // and highlighted when on the frontier
        if ( chosenEdge[id(v)] != null )
            color(chosenEdge[id(v)], BLUE); // edge turns blue when part of tree
        endStep();
        for_outgoing ( v, e, w ) {
            if ( ! marked(w) )  {
                if ( ! highlighted(w) ) highlight(w);
                double distance = weight(v) + weight(e);
                if ( distance < weight(w) ) {
                    beginStep();
                    Edge previous_chosen = chosenEdge[id(w)];
                    if (previous_chosen != null ) {
                        // change color of edge to yellow to indicate no longer best
                        color(previous_chosen, YELLOW);
                        // green means that this edge represents a change
                        color(e, GREEN);
                    }
                    else
                        // edge is red if it is the first edge to connect v to the tree
                        color(e, RED);
                    setWeight(w, distance);
                    changeKey(w, pq);
                    chosenEdge[id(w)] = e;
                    endStep();
                }
            } // end, neighbor not visited (not in tree); do nothing if node
            // is already in tree
        } // end, adjacency list traversal
    } // stop when priority queue is empty
} // end, algorithm

//  [Last modified: 2021 03 30 at 12:15:20 GMT]
