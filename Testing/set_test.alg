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
}

//  [Last modified: 2016 08 04 at 14:39:22 GMT]
