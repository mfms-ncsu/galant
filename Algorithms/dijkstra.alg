/** dijkstra.alg
 *  Implementation of Dijkstra's shortest path algorithm. Interprets
 *  undirected edges as going in both directions.
 */

/** Required: edge weights defined */

algorithm {
  showNodeWeights();
  showEdgeWeights();
  NodePriorityQueue pq = new NodePriorityQueue();
  Edge [] chosenEdge = new Edge[nodeIds()]; 
  beginStep();
  for_nodes(node) {
    setWeight(node, INFINITY);
    insert(node, pq);
  }
  Node v = getStartNode();
  setWeight(v, 0);
  endStep();

  while ( ! empty(pq) ) {
    v = removeBest(pq);
    mark(v);        // nodes are marked when visited
    unhighlight(v); // and highlighted when on the frontier
    for_outgoing ( v, e, w ) {
      if ( ! marked(w) )  {
        if ( ! highlighted(w) ) highlight(w);
        double distance = weight(v) + weight(e);
        if ( distance < weight(w) ) {
          beginStep();
          highlight(e);
          Edge previous_chosen = chosenEdge[id(w)];
          if (previous_chosen != null )
            unhighlight(previous_chosen);
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

//  [Last modified: 2017 01 17 at 19:16:29 GMT]
