/**
 * tests the ability to change radius of nodes and thickness of edges and node outlines
 */

 algorithm {
    for_nodes( node ) {
        node.setThickness(7);
        node.setRadius(15);
    }
    for_edges( edge ) {
        edge.setThickness(8);
    }
}
