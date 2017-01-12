/**
 * Tests sorting and priority queues on the edges; these should work for
 * nodes as well
 */

algorithm {
  List<Node> nodeList = getNodes();
  beginStep();
  for_nodes( v ) {
    set(v, "other", integer(getString(v, "myInt")));
  }
  endStep();
  sort(nodeList, getIntegerComparator("other"));
  for ( Node v: nodeList ) {
    mark(v);
  }
  sort(nodeList, getIntegerComparator("other", true));
  for ( Node v: nodeList ) {
    color(v, BLUE);
  }

  List<Edge> edgeList = getEdges();
  sort(edgeList, getStringComparator("label"));
  for ( Edge e: edgeList ) {
    color(e, ORANGE);
  }
 EdgePriorityQueue pq = new EdgePriorityQueue(getStringComparator("label"));
  for ( Edge e: edgeList ) {
    color(e, GREEN);
    pq.insert(e);
  }
  while ( ! pq.isEmpty() ) {
    Edge e = pq.removeBest();
    color(e, RED);
  }
}

//  [Last modified: 2017 01 12 at 18:10:48 GMT]
