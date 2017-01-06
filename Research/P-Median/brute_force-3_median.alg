/**
 * Computes and displays the 1-median, the two 2-medians and the three
 * 3-medians of a tree. These are appropriately labeled and given colors.
 * Edges are assumed to be weighted. If not, they are automatically given
 * weights of 1.0
 */

/**
 * The following are global because they are reinitialized in different ways
 * when medians of subtrees are computed.
 */
Double total_weight;          // total weight of all vertices in (sub)-tree
Integer deg[];                // degree during 1-median algorithm
Double wt[];                  // current weight during 1-median algorithm
NodeQueue leaves;             // current leaves during 1-median algorithm
Node one_median;              // the one_median
Node first_two_median;        // one of the two-medians
Node second_two_median;       // the other two-median
/** the three three-medians */
Node first_three_median;
Node second_three_median;
Node third_three_median;
/** cut edges for the three-medians */
Edge first_cut_edge;
Edge second_cut_edge;

/**
 * Initializes edge weights in case they are missing or 0
 * and performs initialization for the one-median computation for the whole
 * tree.
 */
function initialize_one_median() {
    for_edges ( e ) {
        if ( hasNoWeight(e) || weight(e) == 0 ) {
            setWeight(e, 1);
        }
    }
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
        if ( visible(e) ) return w;
    }
    return null;
}

/**
 * @return the one-median of the current tree, as initialized by either
 * initialize_one_median (true one_median) or init_with_dfs (subtree medians)
 */
function Node compute_one_median() {
    Boolean done = false;
    Node median = null;
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
    }
    return median;
}

/**
 * visits nodes reachable from v without traversing cut edges and sets the
 * appropriate array values, queueing up leaves in the process, as with the
 * initialization for the one_median algorithm
 */
function dfs(Node v) {
    display("dfs(" + id(v) + ")");
    mark(v);
    deg[id(v)] = visibleDegree(v);
    if ( deg[id(v)] <= 1 ) leaves.enqueue(v);
    wt[id(v)] = weight(v);
    total_weight = total_weight + weight(v);
    for ( Node w : visibleNeighbors(v) ) {
        if ( ! marked(w) ) {
            dfs(w);
        }
    }
}

function display_degrees() {
    for_nodes( v ) {
        label(v, deg[id(v)]);
    }
}

/**
 * Performs initialization for the one_median algorithm by visiting only the
 * nodes reachable from the root without traversing the cut edge(s). Assumes
 * that the root is at one end of a cut edge and that the cut edges are
 * hidden.
 */
function init_with_dfs(Node root) {
    leaves = new NodeQueue();
    total_weight = 0.0;
    deg = new Integer[nodeIds()];
    wt = new Double[nodeIds()];
    dfs(root);
    display_degrees();
}

/**
 * @return the median of the subtree rooted at root assuming that the edge is
 * missing
 */
function Node subtree_median(Node root) {
    init_with_dfs(root);
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
    display("Computing cost, median = " + id(median));
    Double total_cost = 0.0;
    NodeQueue pathFound = new NodeQueue();
    Double distance[] = new Double[nodeIds()];
    mark(median);
    distance[id(median)] = 0.0;
    pathFound.enqueue(median);
    while ( ! pathFound.isEmpty() ) {
        Node v = pathFound.dequeue();
        Double cost = distance[id(v)] * weight(v);
        setLabel(v, cost);
        total_cost = total_cost + cost;
        for_adjacent(v, e, w) {
            if ( isVisible(e) && ! marked(w) ) {
                distance[id(w)] = distance[id(v)] + weight(e);
                mark(w);
                pathFound.enqueue(w);
            }
        }
    }
    display("total cost = " + total_cost);
    return total_cost;
}

function show_two_median(Node two_median) {
    if ( two_median == one_median ) {
        color(two_median, VIOLET);
        label(two_median, "Mm");
    }
    else {
        color(two_median, RED);
        label(two_median, "m");
    }
}

/**
 * computes and displays the one-median of the whole tree
 * @return the one-median
 */
function compute_and_display_one_median() {
    initialize_one_median();
    one_median = compute_one_median();
    color(one_median, BLUE);
    label(one_median, "M");
    Double cost = compute_cost(one_median);
    display("Cost of one-median = " + cost);
}

/**
 * computes and displays the two two-medians
 * @param one_median the one median, used to decide how to display the two
 * medians
 */
function compute_and_display_two_medians() {
    Double min_total_cost = Infinity;
    Edge min_cut_edge = null;
    for_edges ( edge  ) {
        hide(edge);
        clearMarks();
        Node first_subtree_median = subtree_median(source(edge));
        Node second_subtree_median = subtree_median(target(edge));
        clearMarks();
        Double first_subtree_cost = compute_cost(first_subtree_median);
        Double second_subtree_cost = compute_cost(second_subtree_median);
        Double total_cost = first_subtree_cost + second_subtree_cost;
        display("total cost, both sides = " + total_cost);
        if ( total_cost < min_total_cost ) {
            min_total_cost = total_cost;
            min_cut_edge = edge;
            first_two_median = first_subtree_median;
            second_two_median = second_subtree_median;
        }
        clearNodeHighlighting();
        show(edge);
    }
    highlight(min_cut_edge);
    label(one_median, label(one_median) + ":M");
    hide(min_cut_edge);
    clearMarks();
    compute_cost(first_two_median);
    compute_cost(second_two_median);
    clearMarks();
    show(min_cut_edge);
    show_two_median(first_two_median);
    show_two_median(second_two_median);
    display("Minimum of cost two-median solution = " + min_total_cost);
}

function compute_three_medians() {
    Double min_total_cost = Infinity;
    // try out every possible pair of edges
    for_edges( e ) {
        for_edges( f ) {
            if ( id(f) > id(e) ) {
                hide(e); hide(f);
                Node first_subtree_median = subtree_median(source(e));
                display("first_subtree_median is " + id(first_subtree_median));
                Node second_subtree_median = subtree_median(target(e));
                display("second_subtree_median is " + id(second_subtree_median));
                Node root = source(f);
                if ( marked(root) ) root = target(f);
                Node third_subtree_median = subtree_median(root);
                display("third_subtree_median is " + id(third_subtree_median));
                clearMarks();
                Double first_subtree_cost = compute_cost(first_subtree_median);
                Double second_subtree_cost = compute_cost(second_subtree_median);
                Double third_subtree_cost = compute_cost(third_subtree_median);
                Double total_cost = first_subtree_cost
                    + second_subtree_cost + third_subtree_cost;
                display("total cost of three subtrees is " + total_cost);
                if ( total_cost < min_total_cost ) {
                    min_total_cost = total_cost;
                    first_three_median = first_subtree_median;
                    second_three_median = second_subtree_median;
                    third_three_median = third_subtree_median;
                    first_cut_edge = e;
                    second_cut_edge = f;
                }
                clearMarks();
                show(e); show(f);
            }
        }
    }
}

/**
 * uses color and node labels to show the three medians and diplays them and
 * their costs on the message banner
 * @param cost the computed cost of the optimum three-median solution
 */
function display_three_medians(Double cost) {
    color(first_cut_edge, GREEN);
    color(second_cut_edge, GREEN);
    display("one median = " + id(one_median)
            + "; two_medians = " + id(first_two_median) + ","
            + id(second_two_median)
            + "; three_medians = " + id(first_three_median) + ","
            + id(second_three_median) + "," + id(third_three_median)
            + " . Cost with three medians = " + cost);
    for_nodes( v ) {
        if ( v == one_median ) {
          label(v, label(v) + ":M");
            color(v, BLUE);
        }
        if ( v == first_two_median || v == second_two_median ) {
            label(v, label(v) + ":m");
            color(v, RED);
        }
        if ( v == first_three_median
             || v == second_three_median
             || v == third_three_median ) {
            label(v, label(v) + ":u");
            color(v, GREEN);
        }
    }
}

algorithm {
    step();
    compute_and_display_one_median();
    clearMarks();
    step();
    compute_and_display_two_medians();
    step();
    clearNodeColors();
    clearEdgeHighlighting();
    clearMarks();
    compute_three_medians();
    hide(first_cut_edge); hide(second_cut_edge);
    Double cost = compute_cost(first_three_median);
    cost = cost + compute_cost(second_three_median);
    cost = cost + compute_cost(third_three_median);
    clearMarks();
    show(first_cut_edge); show(second_cut_edge);
    display_three_medians(cost);
}

//  [Last modified: 2017 01 05 at 17:03:15 GMT]