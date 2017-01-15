/**
 * Testing of procedural syntax for lists, queues and priority queues
 */

algorithm {
  // testing compiler on type conversions
  List<Edge> EL = new EdgeList();
  EdgeList allEdges = getEdges();
  NodeList NL = new NodeList(getNodeSet());
  NodeSet allNodes = new NodeSet(getNodes());
  EdgeSet ES = new EdgeSet(getEdges());
  EdgeQueue EQ = new EdgeQueue(getEdgeSet());
  
  print(EL);
  print(allEdges);
  print(NL);
  print(allNodes);
  print(ES);

  EdgeList L = new EdgeList();
  // treat the list as a stack
  for ( Edge e : allEdges ) {
    if ( size(L) > 2 ) break;
    color(e, RED);
    push(e, L);
  }
  // use opportunity to test set operations
  EdgeSet ES1 = new EdgeSet();
  for ( Edge e : ES ) {
    if ( size(ES1) > 2 ) break;
    add(e, ES1);
  }
  print("ES1 = " + ES1);
  EdgeSet ES2 = new EdgeSet(L);
  print("ES2 = " + ES2);
  print("union = " + union(ES1, ES2));
  print("intersection = " + intersection(ES1, ES2));
  print("ES1 - ES2 = " + difference(ES1, ES2));
  print("ES1 delta ES2 = " + symmetricDifference(ES1, ES2));
  // now do the popping
  while ( ! empty(L) ) {
    Edge e = pop(L);
    color(e, BLUE);
  }
  clearEdgeColors();
  // edge queue
  EdgeQueue EQ2 = new EdgeQueue();
  for ( Edge e : EQ ) {
    if ( size(EQ2) > 2 ) break;
    color(e, RED);
    put(e, EQ2);
  }
  while ( ! empty(EQ2) ) {
    Edge e = get(EQ2);
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

//  [Last modified: 2017 01 15 at 22:57:40 GMT]
