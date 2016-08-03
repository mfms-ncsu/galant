/**
 * Test whether incidence/adjacency tests, and by implication, EdgeSet, work
 * correctly.
 */

algorithm {
    Node start = getStartNode();
    for_edges ( e ) {
        if ( isIncident(e, start) ) highlight(e);
    }
}

//  [Last modified: 2016 08 03 at 20:30:53 GMT]
