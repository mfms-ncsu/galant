/** bubble_sort.alg
 * An animation of bubble sort.
 *
 * Caution: assumes that the graph is a collection of nodes with no edges
 * weight of node = sorting key
 * this is the simplest version of bubble sort; makes a pass over the whole
 * list each iteration
 */

/**
 * Distances used when lining up nodes
 */
int HORIZONTAL_GAP = 100;
int VERTICAL_GAP = 100;
int TOP_GAP = 2 * VERTICAL_GAP;
Node [] nodes;

/**
 * Lines up the nodes in a single horizontal row
 */
function lineUpNodes() {
    int xPosition = HORIZONTAL_GAP;
    int yPosition = TOP_GAP;
    int i = 0;
    for_nodes( n ) {
        nodes[i] = n;
        setPosition(n, xPosition, yPosition);
        xPosition += HORIZONTAL_GAP;
        i++;
    }
}

/**
 * swaps the positions of the nodes x and y, assuming they both exist
 */
function exchange(Node n1, Node n2) {
    // The following instructions are for an earlier attempt that assumed
    // nodes to be connected in a path. This would require edge
    // additions and deletions, which could get complicated.
    //
    // w - d -> x - e -> x - f -> z
    // remove: w --> x, x --> y, y --> z
    // (move x and y to new positions)
    // add: w --> y; x --> z; y --> x
    // return x --> z

    Double w1 = weight(n1);
    Double w2 = weight(n2);
    Integer p1Y = getY(n1);
    Integer p2Y = getY(n2);
    beginStep();
    setY(n1, p1Y + VERTICAL_GAP);
    setY(n2, p2Y + VERTICAL_GAP);
    mark(n1);
    mark(n2);
    endStep();
    beginStep(); 
    setWeight(n1, w2);
    setWeight(n2, w1);
    endStep(); 
    beginStep();
    unmark(n1);
    unmark(n2);
    setY(n1, p1Y);
    setY(n2, p2Y);
    endStep();
}

algorithm {
    movesNodes();
    showNodeWeights();

    nodes = new Node[numberOfNodes()];
    beginStep();
    lineUpNodes();
    endStep();

    Boolean sorted = false;

    while ( ! sorted ) {
        sorted = true;
        for ( int i = 0; i < numberOfNodes() - 1; i++ ) {
            Node n_i = nodes[i];
            Node n_i_plus_1 = nodes[i+1];
            beginStep();
            highlight(n_i);
            highlight(n_i_plus_1);
            endStep();
            if ( weight(n_i_plus_1) < weight(n_i) ) {
                exchange( n_i, n_i_plus_1 );
                sorted = false;
            }
            beginStep(); 
            unhighlight(n_i);
            unhighlight(n_i_plus_1);
            endStep(); 
        }
    }
} 

//  [Last modified: 2017 01 23 at 22:35:39 GMT]
