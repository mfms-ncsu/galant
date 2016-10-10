/**
 * Test whether incidence/adjacency tests, and by implication, EdgeSet, work
 * correctly. Also test sets directly.
 */

algorithm {
    Node start = getStartNode();
    NodeSet nodeSet = new NodeSet(neighbors(start));
    for ( Node v : nodeSet ) {
        print(v);
        mark(v);
    }
    // this part tests selection within a set
    Node neighbor = getNode("neighbor of " + id(start) + "?", nodeSet, "not a neighbor");
    highlight(neighbor);
    EdgeSet edgeSet = new EdgeSet(edges(start));
    for ( Edge e: edgeSet ) {
        print(e);
        color(e, BLUE);
    }
    Edge incident = getEdge("edge incident on " + id(start) + "?", edgeSet,
                            "not incident");
    highlight(incident);
}

//  [Last modified: 2016 10 10 at 21:01:00 GMT]
