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
    return getPosition(v).distance(getPosition(w));
}

function init_edge_costs() {
    for_edges(edge) {
        double distance = euclidian(source(edge), target(edge));
        setWeight(edge, distance);
    }
}

algorithm {
  showNodeWeights();
  NodePriorityQueue pq = new NodePriorityQueue();
  Edge [] chosenEdge = new Edge[nodeIds()];

  beginStep();
  init_edge_costs();

  for_nodes(node) {
    setWeight(node, INFINITY);
    pq.add(node);
  }
  Node s = getNode("Start vertex?");
  color(s, RED);
  Node t = getNode("Terminal vertex?");
  color(t, BLUE);
  pq.decreaseKey(s, euclidian(s, t));
  endStep();

  while ( ! pq.isEmpty() ) {
    Node v = pq.removeMin();
    beginStep();
    mark(v);
    highlight(v);
    if ( chosenEdge[id(v)] != null )
      color(chosenEdge[id(v)], BLUE);
    endStep();
    if ( v == t ) break;
    for_outgoing ( v, e, w ) {
      if ( ! isMarked(w) )  {
        if ( w != t && ! highlighted(w) ) highlight(w);
        double distance = weight(v) + weight(e) + euclidian(w, t) - euclidian(v, t);
        if ( distance < weight(w) ) {
          beginStep();
          Edge previous_chosen = chosenEdge[id(w)];
          if (previous_chosen != null ) {
            color(previous_chosen, YELLOW);
            color(e, GREEN);
          }
          // have to do this because highlight trumps color
          else
            color(e, RED);
          pq.decreaseKey( w, distance );
          chosenEdge[id(w)] = e;
          endStep();
        }
      } // end, neighbor not visited (not in tree); do nothing if node is
      // already in tree
    } // end, adjacency list traversal
  } // stop when priority queue is empty

  // trace path back from t
  beginStep();
  Node currentNode = t;
  Edge currentEdge = chosenEdge[id(t)];
  while ( currentEdge != null ) {
    unhighlight(currentNode);
    color(currentNode, BLACK);
    unhighlight(currentEdge);
    color(currentEdge, BLACK);
    currentNode = otherEnd(currentEdge, currentNode);
    currentEdge = chosenEdge[id(currentNode)];
  }
  unhighlight(s);
  color(s, BLACK);
}

//  [Last modified: 2017 10 27 at 12:17:22 GMT]
