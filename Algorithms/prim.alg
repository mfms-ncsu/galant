/** 
 *  prim.alg
 *  Implementation of Prim's minimum spanning tree algorithm. Makes edges
 *  undirected initially
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
  setDirected(false);
  showNodeWeights();
  showEdgeWeights();
  int total_weight = 0;
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
    if ( v != s )
      total_weight += weight(chosenEdge[id(v)]);
    display("weight so far = ", total_weight);
    mark(v);        // nodes are marked when visited
    unhighlight(v); // and highlighted when on the frontier
    for_outgoing ( v, e, w ) {
      if ( ! marked(w) )  {
        if ( ! highlighted(w) ) highlight(w);
        if ( weight(e) < weight(w) ) {
          beginStep();
          Edge previous_chosen = chosenEdge[id(w)];
          if (previous_chosen != null ) {
            unhighlight(previous_chosen);
            color(previous_chosen, YELLOW);
            color(e, BLUE);
          }
          // have to do this because highlight trumps color
          else
            highlight(e);
          setWeight(w, weight(e));
          changeKey(w, pq);
          chosenEdge[id(w)] = e;
          endStep();
        }
      } // end, neighbor not visited (not in tree); do nothing if node
      // is already in tree
    } // end, adjacency list traversal
  } // stop when priority queue is empty
  display("total weight of MST = ", total_weight);
} // end, algorithm

//  [Last modified: 2017 10 12 at 16:55:31 GMT]
