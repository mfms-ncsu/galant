/**
 * set_test.alg - test whether incidence/adjacency tests, and by implication,
 * EdgeSet, work correctly in queries. Also test sets directly.
 */

algorithm {
  Node start = getStartNode();
  NodeSet nodeSet = new NodeSet(neighbors(start));
  Set<Node> S = nodeSet; // to test whether conversion to abstract works
  for ( Node v : S ) {
    print(v);
    mark(v);
  }
  // this part tests selection within a set
  Node neighbor = getNode("neighbor of " + id(start) + "?",
                          nodeSet, "not a neighbor");
  highlight(neighbor);
  EdgeSet edgeSet = new EdgeSet(edges(start));
  for ( Edge e: edgeSet ) {
    print(e);
    color(e, BLUE);
  }
  Edge incident = getEdge("edge incident on " + id(start) + "?", edgeSet,
                          "not incident");
  highlight(incident);
}

//  [Last modified: 2017 01 24 at 17:49:28 GMT]
