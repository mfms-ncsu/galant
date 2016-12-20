/**
 * Tests use of Collections.sort() on the edges
 */
List<Edge> edgeList = getEdges();

Collections.sort( edgeList );

for ( Edge e: edgeList ) {
    e.setSelected( true );
}

//  [Last modified: 2016 11 04 at 17:15:02 GMT]
