/**
 * Draws a binary tree that fits into current graph window. First queries the
 * user about the tree height.
 */

/**
 * creates a complete binary tree of height tree_height with root at position
 * (root_x,root_y) and returns the root; local_width is the horizontal
 * distance to the nearest sibling and local_height is the vertical distance
 * to the parent
 */
function Node createTree( int tree_height,
                          int root_x, int root_y,
                          int local_width, int local_height ) {

    beginStep();
    Node root = addNode(root_x, root_y);
    highlight(root);
    endStep();

    if ( tree_height == 0 ) return root;

    Node left = createTree( tree_height - 1,
                            root_x - local_width / 2, root_y + local_height,
                            local_width / 2, local_height );
    Node right = createTree( tree_height - 1,
                             root_x + local_width / 2, root_y + local_height,
                             local_width / 2, local_height );
    beginStep();
    Edge left_edge = addEdge( root, left );
    highlight(left_edge);
    endStep();
    beginStep();
    Edge right_edge = addEdge( root, right );
    highlight(right_edge);
    endStep();
    mark(root);
    return root;
}

/**
 * erases the graph so we start with a clean slate.
 */
function clearGraph() {
    for_nodes( v ) { 
        deleteNode(v);
    }
}

algorithm {
    int tree_height = getInteger("Tree height?");
    int width = windowWidth();
    int height = windowHeight();
    // the + 2 is required to center the tree in the window
	int padding = height / (tree_height + 2);
    beginStep();
    clearGraph();
    endStep();
	Node root = createTree(tree_height, width / 2, padding, width / 2, padding);
}	

//  [Last modified: 2021 01 12 at 18:00:05 GMT]
