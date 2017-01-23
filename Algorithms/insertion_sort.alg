/** insertion_sort.alg
 * An animation of insertion sort.
 *
 * Caution: assumes that the graph is a collection of nodes with no edges
 * weight of node = sorting key; this can also be set up with an external
 * filter that transforms a list of numbers into the appropriate graph (with
 * nodes already lined up
 *
 * The already sorted part of the array is visited'. The element to be
 * inserted moves about the array -- the element to which it is compared at
 * each step is selected.
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
Integer numNodes;               // number of nodes
int [] xCoord;                  // x coordinates of nodes
double [] A;                    // array elements (as weights of nodes)
Node [] nodes;                  // actual nodes

/**
 * Lines up the nodes in a single horizontal row
 */
function lineUpNodes() {
  // note: we can allocate the arrays to have only as many entries as the
  // number of nodes; their values are filled contiguously
  numNodes = numberOfNodes();
  A = new double[numNodes];
  xCoord = new int[numNodes];
  nodes = new Node[numNodes];

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
  showNodeWeights();

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
    Double x = A[i]; setWeight(toInsert, A[i]);
    setX(toInsert, xCoord[i]);
    endStep();
    Integer j = i - 1;
    while ( j >= 0 ) {  
      // show the comparison; normally the loop condition would include
      // A[j] > x
      beginStep();
      setX(toInsert, xCoord[j]);
      highlight(nodes[j]);
      endStep();
      unhighlight(nodes[j]);
      if ( A[j] <= x ) break;
      beginStep();
      A[j+1] = A[j]; setWeight(nodes[j+1], A[j]);
      unmark(nodes[j]);
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

//  [Last modified: 2017 01 23 at 22:35:03 GMT]
