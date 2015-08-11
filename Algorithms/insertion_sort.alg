/** insertion_sort.alg
 * An animation of insertion sort.
 *
 * Caution: assumes that the graph is a collection of nodes with no edges
 * weight of node = sorting key; this can also be set up with an external
 * filter that transforms a list of numbers into the appropriate graph (with
 * nodes already lined up
 *
 * The already sorted part of the array is 'visited'. The element to be
 * inserted moves about the array -- the element to which it is compared at
 * each step is selected.
 * 
 * $Id: insertion_sort.alg 74 2014-05-14 01:45:29Z mfms $
 */

import edu.ncsu.csc.Galant.GraphDispatch;

/**
 * Distances used when lining up nodes
 */
final int HORIZONTAL_GAP = 100;
final int VERTICAL_GAP = 50;
/**
 * For convenience - to make the algorithm look more transparent.
 */
int [] xCoord;  // x coordinates of nodes
double [] A;    // array elements (as weights of nodes)
Node [] nodes;  // actual nodes
Graph graph;

/**
 * Lines up the nodes in a single horizontal row
 */
function lineUpNodes() {
    int xPosition = HORIZONTAL_GAP;
    int yPosition = 2 * VERTICAL_GAP;
    for ( int i = 0; i < numberOfNodes(); i++ ) { 
        nodes[i] = getNodeById( i );
        xCoord[i] = xPosition;
        A[i] = nodes[i].getWeight();
        nodes[i].setPosition( xPosition, yPosition );
        xPosition += HORIZONTAL_GAP;
    }
}

algorithm {
    movesNodes();

    int n = numberOfNodes();
    A = new double[ n ];
    xCoord = new int[ n ];
    nodes = new Node[ n ];

    beginStep();
    lineUpNodes();
    endStep();

    beginStep();
    Node toInsert = addNode(HORIZONTAL_GAP, VERTICAL_GAP);
    toInsert.highlight();
    nodes[0].mark();

    for ( int i = 1; i < n; i++ ) {
        double x = A[i]; toInsert.setWeight(A[i]);
        toInsert.setX(xCoord[i]);
        endStep();
        int j = i - 1;
        while ( j >= 0 && A[j] > x ) {  
            beginStep();
            toInsert.setX( xCoord[j] );
            nodes[j].highlight();
            endStep(); beginStep();
            A[j+1] = A[j]; nodes[j+1].setWeight(A[j]);
            nodes[j].unMark();
            nodes[j].unHighlight();
            nodes[j+1].mark();
            endStep();
            j = j - 1;
        }
        beginStep();
        A[j+1] = x; nodes[j+1].setWeight(x);
        nodes[j+1].mark();
    }
    graph.deleteNode(toInsert);
    endStep();
}

//  [Last modified: 2015 08 11 at 01:44:33 GMT]
