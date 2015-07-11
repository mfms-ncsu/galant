/**
 * bfs.alg
 * Implementation of breadth-first search. Undirected edges are regarded as
 * being directed both ways.
 * $Id: bfs.alg 47 2014-03-10 16:11:00Z mfms $
 */

/** Required: node weights visible */

final double INFINITY = Double.POSITIVE_INFINITY;

algorithm {
 beginStep();
 for_nodes( n ) {
     n.setWeight( INFINITY );
 }

 Node start = getNodeById( 0 );
 start.setWeight( 0 );
 start.setSelected( true );
 nodeQ.offer( start );
 endStep();

 while ( ! nodeQ.isEmpty()) {
     Node n = nodeQ.poll();
     n.setVisited( true );

     for_outgoing( n, e, head ) {
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
}

//  [Last modified: 2015 07 11 at 15:32:27 GMT]
