/**
 * euclidian.alg
 *  Implementation of Dijkstra's shortest path algorithm. Interprets
 *  undirected edges as going in both directions if run in undirected
 *  mode. Unlike dijkstra.alg, the edge weights are set initially by the
 *  algorithm, based on Euclidian distances between nodes.
 */

/** Required: node and edge weights visible */

import java.util.PriorityQueue;

double INFINITY = Double.POSITIVE_INFINITY;

function double euclidian(Node v, Node w) {
    return v.getPosition().distance(w.getPosition());
}

function init_edge_costs() {
    for_edges(edge) {
        double distance = euclidian(source(edge), target(edge));
        setWeight(edge, distance);
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
    pq.add(node);
  }
  Node s = getNode("Start vertex?");
  Node t = getNode("Terminal vertex?");
  highlight(s);
  pq.decreaseKey(s, euclidian(s, t));
  color(t, BLUE);
  endStep();

  while ( ! pq.isEmpty() ) {
    Node v = pq.removeMin();
    mark(v);
    if ( v == t ) break;
    highlight(v);
    for_outgoing ( v, e, w ) {
      if ( ! isMarked(w) )  {
        if ( w != t ) highlight(w);
        double distance = weight(v) + weight(e) + euclidian(w, t) - euclidian(v, t);
        if ( distance < weight(w) ) {
          beginStep();
          highlight(e);
          Edge previous_chosen = chosenEdge[id(w)];
          if (previous_chosen != null )
            unhighlight(previous_chosen);
          pq.decreaseKey( w, distance );
          chosenEdge[id(w)] = e;
          endStep();
        }
      } // end, neighbor not visited (not in tree); do nothing if node is
      // already in tree
    } // end, adjacency list traversal
  } // stop when priority queue is empty
}

//  [Last modified: 2017 03 30 at 23:45:45 GMT]
