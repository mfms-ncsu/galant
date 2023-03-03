/**
 * Draws a binary tree that fits into current graph window. First queries the
 * user about the tree height.
 */

/**
 * standardized node separation
 */
int NODE_SEPARATION = 50;
int PADDING = 50;

/**
 * creates a complete binary tree of height tree_height with root at position
 * (root_x,root_y) and returns the root; local_width is the horizontal
 * distance to the nearest sibling and local_height is the vertical distance
 * to the parent
 */
function Node createTree(int tree_height, int width,
                         int root_x, int root_y) {
    print("x = ", root_x, ", y = ", root_y);
    beginStep();
    Node root = addNode(root_x + PADDING, root_y + PADDING);
    highlight(root);
    endStep();

    if ( tree_height == 0 ) return root;

    Node left = createTree(tree_height - 1, width / 2,
                           root_x - width / 2, root_y + NODE_SEPARATION);
    Node right = createTree(tree_height - 1, width / 2,
                            root_x + width / 2, root_y + NODE_SEPARATION);
    beginStep();
    Edge left_edge = addEdge(root, left);
    highlight(left_edge);
    endStep();
    beginStep();
    Edge right_edge = addEdge(root, right);
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
    movesNodes();
    int tree_height = getInteger("Tree height?");
    int width = (1 << tree_height) * NODE_SEPARATION; // 2^height * separation
    int height = tree_height + 1;
    beginStep();
    clearGraph();
    endStep();
    Node root = createTree(tree_height, width / 2, width / 2, NODE_SEPARATION);
}	
