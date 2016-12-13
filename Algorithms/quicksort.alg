/** quicksort.alg
 * An animation of Quicksort
 * @author Hayden Fuss, Solomon Yeh, Jordan Connor
 * @date 2015/4/28
 * Submitted for Project 3 in CSC 316
 *
 * Caution: assumes that the graph is a collection of nodes with no edges
 * weight of node = sorting key;
 * Inital position of the Nodes does not affect output
 *
 * This Quicksort implementation chooses the median index of the list
 * as the pivot, and can handle an arbitrary sized array, but
 * visual fidelity may fall on larger indices
 */

/** Horizontal gap between Nodes */
final int HORIZONTAL_GAP = 60;
/** Leftmost x-coordinate */
final int X_OFFSET = 200;
/** Vertical gap between Nodes */
final int VERTICAL_GAP = 50;


/** number of elements to be sorted (number of nodes) */
int num_elements;
/**  initial unsorted coordinates of the Nodes */
int [] xCoord;
/** Color of various recursive levels in Quicksort */
final String[] colors = {BLACK, GREEN, BLUE, MAGENTA, CYAN, ORANGE, TEAL, VIOLET};

/**
 * Lines up the nodes in a single horizontal row
 */
function lineUpNodes() {
    int xPosition = X_OFFSET;
    int yPosition = 2 * VERTICAL_GAP;
    int i = 0;
    for ( Node node : getNodes() ) {
        xCoord[i] = xPosition;
        setPosition(node, xPosition, yPosition);
        xPosition += HORIZONTAL_GAP;
        i++;
    }
}

/**
 * Recursive quick sort function for a list of nodes
 * @param nodes List of nodes to be sorted
 * @param color_index Curren "level" of color
 *        this maps to the id of the colors array
 * @return the sorted list by weight
 */
function List<Node> Quicksort(List<Node> nodes, int color_index) {
   if(color_index >= colors.length) {
      color_index = 0;
   }

   if(nodes.size() <= 1) {
      return nodes;
   }

   //Get the pivot
   //For now get middle

   final Node pivot = nodes.get(nodes.size()/2);

   //Set pivot color
   beginStep();
   highlight(pivot);
   mark(pivot);
   endStep();

   //Partition defitions
   List<Node> sortedNodes = new ArrayList<Node>();
   List<Node> lowerNodes = new ArrayList<Node>();
   List<Node> upperNodes = new ArrayList<Node>();

   //Now handle new positions of nodes!
   int centerX = getX(pivot);
   int centerY = getY(pivot);
   int newY = centerY + VERTICAL_GAP;

   //Compares
   int newXU = 1;
   int newXL = 1;
   for(Node node : nodes) {
      double pivotWeight = weight(pivot);
      double curWeight = weight(node);

      if(node == pivot) {
         continue;
      }
      else if(curWeight < pivotWeight) {
         lowerNodes.add(node);
         Point pos = new Point (centerX - HORIZONTAL_GAP * newXL, newY);

         //Illustrate what node we are on
         beginStep();
         highlight(node);
         setX(pivot, getX(node));
         setY(pivot, getY(node) - VERTICAL_GAP);
         endStep();

         //Illustrate the move
         beginStep();
         setPosition(node, pos);
         color(node, colors[color_index + 1]);
         endStep();
         beginStep();
         unhighlight(node);
         endStep();
         newXL++;
      }
      else {
         upperNodes.add(node);
         Point pos = new Point (centerX + newXU * HORIZONTAL_GAP, newY);

         //Illustrate what node we are on
         beginStep();
         highlight(node);
         setX(pivot, getX(node));
         setY(pivot, getY(node) - VERTICAL_GAP);
         endStep();

         //Illustrate the move
         beginStep();
         setPosition(node, pos);
         color(node, colors[color_index + 1]);
         endStep();
         beginStep();
         unhighlight(node);
         endStep();
         newXU++;
      }
   }

   beginStep();
   pivot.setX(centerX);
   pivot.setY(centerY);
   endStep();

   //Recursive sorts
   if( ! lowerNodes.isEmpty() ) {
      lowerNodes = Quicksort(lowerNodes, color_index + 1);
   }
   if( ! upperNodes.isEmpty() ) {
      upperNodes = Quicksort(upperNodes, color_index + 1);
   }

   //Now add the nodes into the sorted list
   if( ! lowerNodes.isEmpty()) {
      for ( Node node : lowerNodes ) {
         sortedNodes.add(node);
      }
   }
   sortedNodes.add(pivot);
   if( ! upperNodes.isEmpty() ) {
      for ( Node node : upperNodes ) {
         sortedNodes.add(node);
      }
   }

   //Now move the nodes back to the center level!
   bool leftOfPivot = true;

   int upperCount = 1;
   int lowerCount = lowerNodes.size();
   for ( Node node : sortedNodes ) {

      //Pivot stays the same
      if ( node == pivot ) {
         leftOfPivot = false;
         continue;
      }

      //Illustrate what node we are on
      beginStep();
      highlight(node);
      endStep();

      //Illustrate the move
      beginStep();
      if ( leftOfPivot ) {
          setX(node, centerX - lowerCount * HORIZONTAL_GAP);
          lowerCount--;
      }
      else {
          setX(node, centerX + upperCount * HORIZONTAL_GAP);
          upperCount++;
      }

      node.setY(centerY);
      endStep();

      //Unhighlight
      beginStep();
      unhighlight(node);
      color(node, colors[color_index]);
      endStep();
   }

   beginStep();
   unhighlight(pivot);
   unmark(pivot);
   endStep();
   return sortedNodes;
}

algorithm {
    movesNodes();
  //Line up the nodes
    beginStep();
    xCoord = new int[numberOfNodes()];
    lineUpNodes();
    endStep();

    //Sort them (note: can't use 'sort' -- it's a Galant macro
    Quicksort(getNodes(), 0);
}

//  [Last modified: 2016 12 13 at 20:20:56 GMT]
