/**
 * bfs.alg
 * Implementation of breadth-first search. Undirected edges are regarded as
 * being directed both ways.
 * $Id: bfs.alg 47 2014-03-10 16:11:00Z mfms $
 */

/** Required: node weights visible */

final double INFINITY = Double.POSITIVE_INFINITY;
Graph graph;
NodeQueue queue;

algorithm {
 graph = getGraph();
 queue = new NodeQueue();
 beginStep();
 for_nodes( n ) {
     n.setWeight( INFINITY );
 }

 Node start = getNodeById( 0 );
 start.setWeight( 0 );
 start.setSelected( true );
 queue.offer( start );
 endStep();

 while ( ! queue.isEmpty()) {
     System.out.println("polling ...");
     Node n = queue.poll();
     System.out.println("node = " + n);
     n.setVisited( true );

     for_outgoing( n, e, head ) {
         System.out.println( "head = " + head + ", n = " + n );
         if ( ! head.isSelected() ) {
             System.out.println("not selected");
             beginStep();
             e.setSelected( true );
             head.setSelected( true );
             double distance = n.getWeight() + 1;
             head.setWeight( distance );
             queue.offer( head );
             endStep();
         }
     }
 }
}

//  [Last modified: 2015 07 10 at 19:35:59 GMT]
