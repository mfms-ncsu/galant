/**
 * Testing of procedural syntax for lists, queues and priority queues
 */

algorithm {
  // testing compiler on type conversions
  List<Edge> L1 = new EdgeList();
  EdgeList L2 = new EdgeList(getEdges());
  NodeList L3 = new NodeList(getNodeSet());
  NodeSet S = new NodeSet(getNodes());

  print(L1);
  print(L2);
  print(L3);
  print(S);

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

//  [Last modified: 2017 01 14 at 22:43:25 GMT]
