/** 
 * Implementation of breadth-first search. If the graph is undirected, the
 * edges are regarded as being directed both ways.
 */

NodeQueue queue;

algorithm {
  showNodeWeights();
  queue = new NodeQueue();
  beginStep();
  for_nodes( v ) {
    setWeight(v, INFINITY);
  }

  Node start = getStartNode();
  setWeight(start, 0);
  highlight(start);
  put(start, queue);
  endStep();

  while ( ! empty(queue) ) {
    Node next = get(queue);
    beginStep();
    mark(next);

    for_outgoing( next, e, other ) {
      if ( ! highlighted(other) ) {
        highlight(e);
        highlight(other);
        Double distance = weight(next) + 1;
        setWeight(other, distance);
        put(other, queue);
        }
      else color(e, BLUE);
    }
    endStep();
  }
}

//  [Last modified: 2018 03 01 at 13:22:30 GMT]
