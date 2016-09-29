/**
 * Test whether incidence/adjacency tests, and by implication, EdgeSet, work
 * correctly. Also test sets directly.
 */

algorithm {
    Node start = getStartNode();
    for_edges ( e ) {
        print(e);
        if ( isIncident(e, start) ) highlight(e);
    }
    NodeSet nodeSet = new NodeSet(neighbors(start));
    for ( Node v : nodeSet ) {
        print(v);
        mark(v);
    }
    // this part tests selection within a set
    Node neighbor = getNode("neighbor of " + id(start) + "?", nodeSet, "not a neighbor");
    highlight(neighbor);
}

//  [Last modified: 2016 09 29 at 16:16:41 GMT]
