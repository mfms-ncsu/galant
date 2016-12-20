/**
 * Test whether hiding and showing works correctly
 */

algorithm {
    Node aNode = getNode("node to hide");
    hide(aNode);
    Edge anEdge = getEdge("edge to hide");
    hide(anEdge);
    for ( Node n: visibleNodes() ) {
        highlight(n);
    }
    for ( Edge e: visibleEdges() ) {
        highlight(e);
    }
    clearNodeHighlighting();
    clearEdgeHighlighting();
    Node otherNode = getNode("node to check");
    print("visible edges of " + otherNode);
    for ( Edge e: visibleEdges(otherNode) ) {
        print(e);
        color(e, GREEN);
    }
    print("visible in edges of " + otherNode);
    for ( Edge e: visibleInEdges(otherNode) ) {
        print(e);
        color(e, BLUE);
    }
    print("visible out edges of " + otherNode);
    for ( Edge e: visibleOutEdges(otherNode) ) {
        print(e);
        color(e, ORANGE);
    }
    print("visible neighbors of " + otherNode);
    for ( Node n: visibleNeighbors(otherNode) ) {
        print(n);
        color(n, GREEN);
    }
}

//  [Last modified: 2016 12 20 at 21:59:56 GMT]
