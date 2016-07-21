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

/**
 * Lines up the nodes in a single horizontal row
 */
function lineUpNodes() {
    int xPosition = HORIZONTAL_GAP;
    int yPosition = 2 * VERTICAL_GAP;
    int i = 0;
    for_nodes ( n ) { 
        nodes[i] = n;
        xCoord[i] = xPosition;
        A[i] = weight(n);
        setPosition(n, xPosition, yPosition);
        xPosition += HORIZONTAL_GAP;
        i++;
    }
}

algorithm {
    movesNodes();
    A = new double[nodeIds()];
    xCoord = new int[nodeIds()];
    nodes = new Node[nodeIds()];
    int numNodes = numberOfNodes();

    beginStep();
    lineUpNodes();
    endStep();

    beginStep();
    Node toInsert = addNode(xCoord[1], VERTICAL_GAP);
    highlight(toInsert);
    mark(nodes[0]);
    endStep();

    for ( int i = 1; i < numNodes; i++ ) {
        beginStep();
        double x = A[i]; setWeight(toInsert, A[i]);
        setX(toInsert, xCoord[i]);
        endStep();
        int j = i - 1;
        while ( j >= 0 && A[j] > x ) {  
            beginStep();
            setX(toInsert, xCoord[j]);
            highlight(nodes[j]);
            endStep(); beginStep();
            A[j+1] = A[j]; setWeight(nodes[j+1], A[j]);
            unMark(nodes[j]);
            unHighlight(nodes[j]);
            mark(nodes[j+1]);
            endStep();
            j = j - 1;
        }
        beginStep();
        A[j+1] = x; setWeight(nodes[j+1], x);
        mark(nodes[j+1]);
        endStep();
    }
    deleteNode(toInsert);
}

//  [Last modified: 2016 07 21 at 13:21:53 GMT]
