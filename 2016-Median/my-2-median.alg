/**
 * Quadratic implementation of potentially linear two-median algorithm.
 *
 * @todo the algorithm does not work on t-20-13, among perhaps others,
 * probably because one of the initial two-medians is the one_median; it may
 * be possible to still operate in linear time if we try out all edges
 * indcident on the one_median as cut edges. 
 *
 * @todo add ability to see the incremental costs of shifting cut edge
 * without changing the medians.
 */

Double total_weight;          // total weight of all vertices in (sub)-tree
                              // -- used when calculating a 1-median
Double cost[];                // cost of subtree assuming root is the median
Edge parent_edge[];           // edge to parent of a node, direction wrt 1-median
Integer deg[];                // degree during 1-median algorithm
Double wt[];                  // current weight during 1-median algorithm
NodeQueue leaves;             // current leaves during 1-median algorithm
// the following are global for convenience, so they don't have to be
// returned from the function that computes them
Node one_median;
Node first_median;
Node second_median;
// the following are global variables that keep track of information about
// the minimum cut edge
Double min_total_cost;
Edge min_cut_edge;
Node min_first_median;
Node min_second_median;

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

function Node compute_one_median() {
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
 * Side effects:
 *  - the weight of parent_edge is set to total weight of the subtree
 *  - the label of root (and cost[id(root)]) is set to the cost of the
 * subtree assuming that root is used as the median
 *  - sets the parent of each adjacent node (except parent of root) to be the root
 * @return the weight of the subtree rooted at root
 */
function Double recursive_weight(Node root) {
    Double weight = weight(root);
    Double total_cost = 0.0;
    for_adjacent(root, e, child) {
        if ( e != parent_edge[id(root)] ) {
            parent_edge[id(child)] = e; 
            Double subtree_weight = recursive_weight(child);
            weight = weight + subtree_weight;
            total_cost = total_cost + cost[id(child)] + subtree_weight;
        }
    }
    setWeight(parent_edge[id(root)], weight);
    setLabel(root, total_cost);
    cost[id(root)] = total_cost;
    return weight;
}

/**
 * sets the weight of each edge incident on the median to the total weight of
 * the corresponding subtree
 * @return the edge with the largest resulting weight
 */
function Edge compute_subtree_weights(Node root) {
    Double max_weight = 0.0;
    Edge max_weight_edge = null;
    for_adjacent(root, e, subtree_root) {
        parent_edge[id(subtree_root)] = e;
        Double weight = recursive_weight(subtree_root);
        setWeight(e, weight);
        if ( weight > max_weight ) {
            max_weight = weight;
            max_weight_edge = e;
        }
    }
    return max_weight_edge;
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
    clearMarks();
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
    clearMarks();
    hide(otherEnd(root, edge));
    return compute_one_median();
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


/**
 * computes two medians using the given edge as a cut edge
 * @param root the root of the subtree in which the first median should be located
 */
function Double compute_two_medians(Edge cut_edge, Node root) {
    highlight(cut_edge);
    beginStep();
    first_median = side_median(root, cut_edge);
    if ( first_median == one_median ) color(first_median, VIOLET);
    else color(first_median, RED);
    hide(cut_edge);     // need to hide after one_median, which unhides edges
    Double first_side_cost = compute_cost(first_median);

    second_median = side_median(otherEnd(cut_edge, root), cut_edge);
    if ( second_median == one_median ) color(second_median, TEAL);
    else color(second_median,GREEN);
    hide(cut_edge);
    Double second_side_cost = compute_cost(second_median);
    Double total_cost = first_side_cost + second_side_cost;
    display("first median is " + id(first_median)
            + ", cost = " + first_side_cost
            + ", second median is " + id(second_median)
            + ", cost = " + second_side_cost
            + ", total = " + total_cost);
    endStep();
    return total_cost;
}

/**
 * cleans up the display after compute_two_medians
 */
function cleanup(Edge edge) {
    beginStep();
    display("");
    showEdges();
    uncolor(first_median);
    uncolor(second_median);
    color(one_median, BLUE);
    clearMarks();
    unhighlight(edge);
    endStep();
}

/**
 * tries out all cut edges between start and the one_median; this version is
 * inefficient - no smart updates along the lines explained below
 */
function explore_path(Node start) {
    Node node = start;
    Edge edge = parent_edge[id(node)];
    while ( node != one_median ) {
        Double current_cost = compute_two_medians(edge, node);
        if ( current_cost < min_total_cost ) {
            min_total_cost = current_cost;
            min_first_median = first_median;
            min_second_median = second_median;
            min_cut_edge = edge;
        }
        cleanup(edge);
        node = otherEnd(edge, node);
        edge = parent_edge[id(node)];
    }
}

algorithm {
    cost = new Double[nodeIds()];
    parent_edge = new Edge[nodeIds()];
    min_total_cost = Infinity;
    min_cut_edge = null;
    min_first_median = null;
    min_second_median = null;
    beginStep();
    initialize_one_median();
    one_median = compute_one_median();
    color(one_median, BLUE);
    Edge max_subtree_edge = compute_subtree_weights(one_median);
    endStep();
    
    // first, use edge to a largest weight subtree wrt median as cut edge
    // and compute the two medians
    Double min_cost = compute_two_medians(max_subtree_edge,
                                          otherEnd(max_subtree_edge, one_median));
    cleanup(max_subtree_edge);
    // save the second median so that we can get back to it after computing
    // medians along the path from first_median to median
    Node initial_second_median = second_median;

    // claim: the cut edge that gives min total cost is along the path P
    // between the two medians

    // explore the two segments of P: one between the (initial) first_median
    // and one_median and the other between the initial_second_median and
    // one_median
    explore_path(first_median);
    explore_path(initial_second_median);

    beginStep();
    display("Min cost solution: first median is " + id(min_first_median)
            + ", second median is " + id(min_second_median)
            + ", total cost = " + min_total_cost);
    highlight(min_cut_edge);
    if ( min_first_median == one_median )
        color(min_first_median, TEAL);
    else
        color(min_first_median, GREEN);
    if ( min_second_median == one_median )
        color(min_second_median, VIOLET);
    else
        color(min_second_median, RED);
    endStep();

    // let y have child x and parent z on P and let C_1, C_2 be the
    // costs of the two subtrees if edge xy is cut and m_1 is the median on
    // the side with x, m_2 on the side with y
    // let c[y] = sum of cost[v] for all children v of y except x

    // to update the cost if we move the cut to yz and keep the medians the
    // same (assuming y != m_2);
    //   C_1 = C_1 + c[y] + weight(y) + (d(m_1,y) + 1) * (wt[y] - wt[x])
    //   C_2 = C_2 - c[y] - weight(y) - (d(m_2,y) + 1) * (wt[y] - wt[x])
    // If distances are nonuniform, there is a d(m_i,y) factor in front of
    // weight(y) and the "+ 1's" are replaced with d(y,t), where t is the
    // root of a subtree with respect to y.
    
    // to update cost, moving from m_1 to m', the next node on the path,
    // while keeping cut edge the same, i.e., yz
    //   C_1 = C_1 + wt[m_1] + weight(m_1) - weight(m') - (wt[y] - wt[m_1])
    // and to move m_2 to m" (there may be several choices)
    //   C_2 = C_2 - wt[m"] - weight(m") + weight(m_2) + wt[m_2] - wt[m"]

    // The w[x] and c[x] values are computed bottom up as follows. Suppose x
    // has children y_1,...,y_k, whose values have been computed. Then
    //    w[x] = weight(x) + sum_i w[y_i]
    //    c[x] = sum_i c[y_i] + sum_i w[y_i]
    // If edges have nonuniform distances, the terms in the last sum are
    // multiplied by d(x,y_i)
}

//  [Last modified: 2016 06 30 at 20:08:41 GMT]
