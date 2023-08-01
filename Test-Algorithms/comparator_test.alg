/**
 * comparator_test.alg - tests sorting and priority queues on both nodes and
 * edges; requires use of the file for_comparator_test.graphml and also tests
 * conversion of attributes; requires use of the file
 * for_comparator_test.graphml in 0-Test_Graphs and also tests conversion of
 * attributes; expected behavior on the for_comparator_test graph:
    - marks nodes in the order 3,1,2,0 based on myInt attribute
      (increasing values)
    - colors the nodes blue in reverse order (they are sorted using a
      reversed comparator)
    - colors edges orange in increasing lexicographic order of their
      labels (based on use of sort
    - colors edges green in the same order (based on a priority
      queue)
    - changes label of the edge (0,1) to "abc"
      using changeStringKey
    - colors edges again, with the edge (0,1) colored first
 */

algorithm {
  showEdgeLabels();
  NodeList nodeList = getNodes();
  beginStep();
  // have to convert the string attribute myInt from the GraphML file (all
  // attributes are treated as strings by the parser) to an integer
  // attribute; can't use the same name unless you store all of them first
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

  EdgeList edgeList = getEdges();
  sort(edgeList, getStringComparator("label"));
  for ( Edge e: edgeList ) {
    color(e, ORANGE);
  }
  EdgePriorityQueue pq = new EdgePriorityQueue(getStringComparator("label"));
  for ( Edge e: edgeList ) {
    color(e, GREEN);
    insert(e, pq);
  }
  pq.changeStringKey(edgeList.get(1), "abc");
  while ( ! empty(pq) ) {
    Edge e = removeBest(pq);
    color(e, RED);
  }
}

//  [Last modified: 2017 01 24 at 19:23:03 GMT]
