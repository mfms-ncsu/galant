/** 1-median.alg
 *  Implementation of Goldman's algorithm for 1-median of trees
 */

Double total_weight;          // total weight of all vertices
Integer deg[];                // degree of a node while algorithm in progress
Double wt[];                  // original weight of a node - so it can be
                              // displayed at the end
NodeQueue leaves;             // current leaves of the tree

function initialize_degrees_and_weights() {
    total_weight = 0.0;
    deg = new Integer[nodeIds()];
    leaves = new NodeQueue();
    wt = new Double[nodeIds()];
    for_nodes(v) {
        deg[id(v)] = degree(v);
        if ( deg[id(v)] == 1 ) leaves.enqueue(v);
        wt[id(v)] = weight(v);
        total_weight = total_weight + weight(v);
    }
}

function Node firstVisibleNeighbor(Node v) {
    for_adjacent(v, e, w) {
        if ( isVisible(w) ) return w;
    }
    return null;
}

function restore_weights() {
    for_nodes(v) {
        setWeight(v, wt[id(v)]);
    }
}

algorithm {
    beginStep();
    hideEdgeWeights();
    hideEdgeLabels();
    initialize_degrees_and_weights();
    display("total weight = " + total_weight);
    endStep();
    Node median = null;
    Boolean done = false;
    // exit the following loop if either
    //  - only one node remains (it will also be the sole leaf), or
    //  - a leaf has weight more than half of the total
    while ( true ) {
        if ( leaves.isEmpty() ) {
            error("Graph is not a tree.");
        }
        // merge a leaf x with its neighbor y and let the new weight of y be
        // the sum of the two weights
        Node x = leaves.dequeue();
        if ( leaves.isEmpty() ) {
            display(id(x) + " is the only remaining node");
            median = x; break;
        }
        if ( weight(x) >= total_weight / 2 ) {
            display("weight of node " + id(x)
                    + " is at least half of the total, "
                    + total_weight);
            median = x; break;
        }
        Node y = firstVisibleNeighbor(x);
        beginStep();
        deg[id(y)] = deg[id(y)] - 1;
        setWeight(y, weight(x) + weight(y));
        if ( deg[id(y)] <= 1 ) leaves.enqueue(y);
        hide(x);
        display("node " + id(x) + " merged into " + id(y));
        endStep();
    }
    beginStep();
    showNodes();
    showEdges();
    restore_weights();
    highlight(median);
    endStep();
} // end, algorithm

//  [Last modified: 2016 12 22 at 16:21:47 GMT]
