/**
 * bfs.alg
 * Implementation of breadth-first search. Undirected edges are regarded as
 * being directed both ways.
 * $Id: bfs.alg 47 2014-03-10 16:11:00Z mfms $
 */

/** Required: node weights visible */

final double INFINITY = Double.POSITIVE_INFINITY;

beginStep();
for_nodes( n ) {
    n.setWeight( INFINITY );
}

Node start = getNodeById( 0 );
start.setWeight( 0 );
nodeQ.offer( start );
endStep();

while ( ! nodeQ.isEmpty()) {
    Node n = nodeQ.poll();
    n.setVisited( true );

    for_adjacent ( n, e, head ) {
        if ( ! head.isSelected() ) {
            beginStep();
            e.setSelected( true );
            head.setSelected( true );
            double distance = n.getWeight() + 1;
            head.setWeight( distance );
            nodeQ.offer( head );
            endStep();
        }
    }
}

/*
beginStep();
for_nodes ( n ) {
    n.setSelected( false );
    n.setVisited( false );
    n.setWeight( INFINITY );
}

for_edges ( e ) {
    e.setSelected( false );
}
endStep();
*/

//  [Last modified: 2013 07 15 at 14:22:11 GMT]
