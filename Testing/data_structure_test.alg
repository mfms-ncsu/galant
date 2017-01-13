/**
 * Testing of procedural syntax for lists, queues and priority queues
 */

algorithm {
  EdgeSet allEdges = getEdgeSet();
  NodeSet allNodes = getNodeSet();
  EdgeList L = new EdgeList();
  // treat the list as a stack
  for ( Edge e : allEdges ) {
    if ( size(L) > 2 ) break;
    color(e, RED);
    push(e, L);
  }
  while ( ! empty(L) ) {
    Edge e = pop(L);
    color(e, BLUE);
  }
  clearEdgeColors();
  // node queue
  NodeQueue Q = new NodeQueue();
  for ( Node v : allNodes ) {
    if ( size(Q) > 2 ) break;
    color(v, RED);
    put(v, Q);
  }
  while ( ! empty(Q) ) {
    Node v = get(Q);
    color(v, BLUE);
  }
}

//  [Last modified: 2017 01 13 at 18:03:21 GMT]
