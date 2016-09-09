Double total_weight;          // total weight of all vertices in (sub)-tree
Integer deg[];                // degree during 1-median algorithm
Double wt[];                  // current weight during 1-median algorithm
NodeQueue leaves;             // current leaves during 1-median algorithm

function initialize_one_median() {
    leaves = new NodeQueue();
    total_weight = 0.0;
    deg = new Integer[nodeIds()];
    wt = new Double[nodeIds()];
    for_nodes(v) {
        deg[id(v)] = degree(v);
        if ( deg[id(v)] <= 1 ) leaves.enqueue(v);
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

function Node one_median() {
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
        if ( leaves.isEmpty() || wt[id(x)] >= total_weight / 2 ) {
            median = x; break;
        }
        Node y = firstVisibleNeighbor(x);
        deg[id(y)] = deg[id(y)] - 1;
        wt[id(y)] = wt[id(x)] + wt[id(y)];
        if ( deg[id(y)] == 1 ) leaves.enqueue(y);
        hide(x);
    }
    showNodes();
    showEdges();
    return median;
}

/**
 * @param parent the node from which this function was initiated; not a
 * candidate for a recursive call
 * @return the weight of the subtree rooted at root
 */
function Double recursive_weight(Node root, Node parent) {
    Double weight = weight(root);
    for_adjacent(root, e, child) {
        if ( child != parent ) {
            weight = weight + recursive_weight(child, root);
        }
    }
    return weight;
}

/**
 * sets the weight of each edge incident on the median to the total weight of
 * the corresponding subtree
 */
function compute_subtree_weights(Node median) {
    for_adjacent(median, e, subtree_root) {
        Double weight = recursive_weight(subtree_root, median);
        setWeight(e, weight);
    }
}

/**
 * visits nodes reachable from the root (one edge has been blocked at this
 * point) and sets the appropriate array values, queueing up leaves in the
 * process, as with the initialization for the one_median algorithm
 */
function dfs(Node v) {
    mark(v);
    deg[id(v)] = degree(v);
    if ( deg[id(v)] <= 1 ) leaves.enqueue(v);
    wt[id(v)] = weight(v);
    total_weight = total_weight + weight(v);
    for_adjacent(v, e, w) {
        if ( ! marked(w) ) {
            dfs(w);
        }
    }
}

/**
 * Performs initialization for the one_median algorithm by visiting only the
 * nodes reachable from the root without traversing the edge.
 */
function init_with_dfs(Node root, Edge edge) {
    leaves = new NodeQueue();
    total_weight = 0.0;
    deg = new Integer[nodeIds()];
    wt = new Double[nodeIds()];
    clearNodeMarks();
    mark(otherEnd(root, edge));
    dfs(root);
    deg[id(root)] = deg[id(root)] - 1;
    if ( deg[id(root)] == 1 ) leaves.enqueue(root);
}

/**
 * @return the median of the subtree rooted at root assuming that the edge is
 * missing
 */
function Node side_median(Node root, Edge edge) {
    init_with_dfs(root, edge);
    clearNodeMarks();
    hide(otherEnd(root, edge));
    return one_median();
}

/**
 * Computes and displays (as label) the weight of each node times its
 * distance from the median. The breadth first search does not traverse
 * hidden edges.
 *
 * @return total cost of the tree with respect to the median
 */
function Double compute_cost(Node median) {
    clearMarks();
    Double total_cost = 0.0;
    NodeQueue pathFound = new NodeQueue();
    Integer distance[] = new Integer[nodeIds()];
    mark(median);
    distance[id(median)] = 0;
    pathFound.enqueue(median);
    while ( ! pathFound.isEmpty() ) {
        Node v = pathFound.dequeue();
        Double cost = distance[id(v)] * weight(v);
        setLabel(v, cost);
        total_cost = total_cost + cost;
        for_adjacent(v, e, w) {
            if ( isVisible(e) && ! marked(w) ) {
                distance[id(w)] = distance[id(v)] + 1;
                mark(w);
                pathFound.enqueue(w);
            }
        }
    }
    return total_cost;
}

algorithm {
    beginStep();
    initialize_one_median();
    Node median = one_median();
    color(median, BLUE);
    compute_subtree_weights(median);
    endStep();
    Double min_total_cost = Infinity;
    Edge min_cut_edge = null;
    Node min_first_median = null;
    Node min_second_median = null;
    for_edges ( edge  ) {
        highlight( edge );
        beginStep();
        Node first_side_median = side_median(source(edge), edge);
        if ( first_side_median == median ) color(first_side_median, VIOLET);
        else highlight(first_side_median);
        hide(edge);     // need to hide after one_median, which unhides edges
        Double first_side_cost = compute_cost(first_side_median);
        display("first median is " + id(first_side_median)
                + ", cost = " + first_side_cost);
        endStep();

        beginStep();
        Node second_side_median = side_median(target(edge), edge);
        if ( second_side_median == median ) color(second_side_median, VIOLET);
        else highlight(second_side_median);
        hide(edge);
        Double second_side_cost = compute_cost(second_side_median);
        Double total_cost = first_side_cost + second_side_cost;
        display("first median is " + id(first_side_median)
                + ", cost = " + first_side_cost
                + ", second median is " + id(second_side_median)
                + ", cost = " + second_side_cost
                + ", total = " + total_cost);
        endStep();

        if ( total_cost < min_total_cost ) {
            min_total_cost = total_cost;
            min_cut_edge = edge;
            min_first_median = first_side_median;
            min_second_median = second_side_median;
        }

        beginStep();
        display("");
        clearMarks();
        clearNodeLabels();
        clearNodeHighlighting();
        showEdges();
        color(median, BLUE);
        unhighlight(edge);
        endStep();
    }
    beginStep();
    display("Minimum of cost two-median solution = " + min_total_cost);
    highlight(min_cut_edge);
    highlight(min_first_median);
    highlight(min_second_median);
    endStep();
}

//  [Last modified: 2016 06 30 at 20:00:43 GMT]
