/**
 * Draws a binary tree based on current graph window width and height.
 *
 * $Id: binary_tree.alg 65 2014-04-04 21:19:54Z mfms $
 */

import java.awt.Point;

final int TREE_HEIGHT = 4;
int width = windowWidth();
int height = windowHeight();

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
    Node root = addNode();
    root.setPosition( new Point( root_x, root_y ) );
    root.setSelected( true );
    endStep();

    if ( tree_height == 1 ) return root;

    Node left = createTree( tree_height - 1,
                            root_x - local_width / 2, root_y + local_height,
                            local_width / 2, local_height );
    Node right = createTree( tree_height - 1,
                             root_x + local_width / 2, root_y + local_height,
                             local_width / 2, local_height );
    beginStep();
    addEdge( root, left );
    addEdge( root, right );
    // Edge left_edge = addEdge( root, left );
    // Edge right_edge = addEdge( root, right );
    // left_edge.setSelected( true );
    // right_edge.setSelected( true );
    root.setVisited( true );
    endStep();
    return root;
}

int padding = height / (TREE_HEIGHT + 1);
Node root = createTree( TREE_HEIGHT, width / 2, padding, width / 2, padding );

//  [Last modified: 2014 04 04 at 21:19:10 GMT]
