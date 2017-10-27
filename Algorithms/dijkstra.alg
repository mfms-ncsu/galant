/** dijkstra.alg
 *  Implementation of Dijkstra's shortest path algorithm. Interprets
 *  undirected edges as going in both directions.
 */

/** Required: edge weights defined; otherwise they will be based on geometry */

function double euclidian(Node v, Node w) {
    return getPosition(v).distance(getPosition(w));
}

function init_edge_costs() {
  for_edges(edge) {
    if ( ! hasWeight(edge) ) {
      double distance = euclidian(source(edge), target(edge));
      setWeight(edge, distance);
    }
  }
}
algorithm {
  showNodeWeights();
  showEdgeWeights();
  NodePriorityQueue pq = new NodePriorityQueue();
  Edge [] chosenEdge = new Edge[nodeIds()]; 
  beginStep();
  init_edge_costs();
  for_nodes(node) {
    setWeight(node, INFINITY);
    insert(node, pq);
  }
  Node s = getNode("Start vertex?");
  setWeight(s, 0);
  changeKey(s, pq);
  color(s, BLUE);
  endStep();

  while ( ! empty(pq) ) {
    Node v = removeBest(pq);
    beginStep();
    mark(v);        // nodes are marked when visited
    unhighlight(v); // and highlighted when on the frontier
    if ( chosenEdge[id(v)] != null )
      color(chosenEdge[id(v)], BLUE);
    endStep();
    for_outgoing ( v, e, w ) {
      if ( ! marked(w) )  {
        if ( ! highlighted(w) ) highlight(w);
        double distance = weight(v) + weight(e);
        if ( distance < weight(w) ) {
          beginStep();
          Edge previous_chosen = chosenEdge[id(w)];
          if (previous_chosen != null ) {
            color(previous_chosen, YELLOW);
            color(e, GREEN);
          }
          else
            color(e, RED);
          setWeight(w, distance);
          changeKey(w, pq);
          chosenEdge[id(w)] = e;
          endStep();
        }
      } // end, neighbor not visited (not in tree); do nothing if node
      // is already in tree
    } // end, adjacency list traversal
  } // stop when priority queue is empty
} // end, algorithm

//  [Last modified: 2017 10 27 at 11:35:59 GMT]
