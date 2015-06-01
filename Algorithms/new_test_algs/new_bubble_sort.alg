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
final int HORIZONTAL_GAP = 100;
final int VERTICAL_GAP = 100;
final int TOP_GAP = 2 * VERTICAL_GAP;

/**
 * Lines up the nodes in a single horizontal row
 */
new_function lineUpNodes() {
    int xPosition = HORIZONTAL_GAP;
    int yPosition = TOP_GAP;
    for_nodes( n ) {
        n.setPosition( xPosition, yPosition );
        xPosition += HORIZONTAL_GAP;
    }
}

/**
 * swaps the positions of the nodes x and y, assuming they both exist
 */
new_function exchange( Node n1, Node n2 ) {
    // The following instructions are for an earlier attempt that assumed
    // nodes to be connected in a path. This would require that edges
    // additions and deletions could be done in an algorithm context (which
    // might actually be true):
    //
    // w - d -> x - e -> x - f -> z
    // remove: w --> x, x --> y, y --> z
    // (move x and y to new positions)
    // add: w --> y; x --> z; y --> x
    // return x --> z

    double w1 = n1.getWeight();
    double w2 = n2.getWeight();
    int p1Y = n1.getY();
    int p2Y = n2.getY();
    beginStep();
    n1.setY( p1Y + VERTICAL_GAP );
    n2.setY( p2Y + VERTICAL_GAP );
    n1.setVisited( true );
    n2.setVisited( true );
    endStep();
    beginStep(); 
    n1.setWeight( w2 );
    n2.setWeight( w1 );
    endStep(); 
    beginStep();
    n1.setVisited( false );
    n2.setVisited( false );
    n1.setY( p1Y );
    n2.setY( p2Y );
    endStep();
}

algorithm {
    beginStep();
    lineUpNodes();
    endStep();

    int N = getNodes().size();

    boolean sorted = false;

    while ( ! sorted ) {
        sorted = true;
        for ( int i = 0; i < N - 1; i++ ) {
            Node n_i = getNodeById( i );
            Node n_i_plus_1 = getNodeById( i + 1 );  
            beginStep();
            n_i.setSelected( true );
            n_i_plus_1.setSelected( true );
           endStep();
            if ( n_i_plus_1.getWeight() < n_i.getWeight() ) {
                exchange( n_i, n_i_plus_1 );
                sorted = false;
            }
            beginStep(); 
            n_i.setSelected( false );
            n_i_plus_1.setSelected( false );
            endStep(); 
      }
    } 
}    
//  [Last modified: 2013 08 05 at 15:20:01 GMT]
