/**
 * Does a sequence of reductions of a graph based on dominance, i.e., if vw
 * is an edge and the neighbors of v are a subset of the neighbors of w, then
 * there is always a minimum vertex cover that includes w. A special case is
 * a degree-one reduction: v has degree one implies that you can include w
 * and throw away v.
 */

NodePriorityQueue pq;
NodeSet cover;

function queueAllNodes() {
  /** @todo for some reason, the constructor that allows a priority queue
   * to be initialized from a collection is not recognized. */
  pq = new NodePriorityQueue();
  beginStep();
  for_nodes( v ) {
    set(v, "onQ");
    pq.insert(v, degree(v));
  }
  endStep();
}

function checkCover() {
  for_edges( e ) {
    highlight(e);
    if ( cover.contains(source(e)) || cover.contains(target(e)) ) {
      unHighlight(e);
    }
  }
}

algorithm {
  /**
   * @todo Awkward to have to do the following step if user has weights
   * invisible during edit mode; the semantics of visibility of labels and
   * weights still need work.
   */
  showNodeWeights();
  cover = new NodeSet();
  queueAllNodes();
  while ( ! pq.isEmpty() ) {
    //    print("pq = " + pq);
    Node v = pq.removeMin();
    if ( hidden(v) ) continue;
    highlight(v);
    for ( Node w : visibleNeighbors(v) ) {
      Edge e = getEdge(v, w);
      display("checking neighbors: " + id(v) + "," + id(w));
      highlight(e);
      step();
      NodeSet closedNeighborsV = new NodeSet(visibleNeighbors(v));
      closedNeighborsV.add(v);
      NodeSet closedNeighborsW = new NodeSet(visibleNeighbors(w));
      closedNeighborsW.add(w);
      if ( closedNeighborsV.subset(closedNeighborsW) ) {
        display(id(w) + " dominates " + id(v));
        mark(w);
        cover.add(w);
        // inform neighbors that their degree and position in
        // queue has changed
        NodeSet neighborsOfW = new NodeSet(visibleNeighbors(w));
        step();
        for ( Node x : neighborsOfW ) {
          color(x, GREEN);
          setWeight(x, weight(x) - 1);
          if ( x.is("onQ") && weight(x) > 0 )
            pq.decreaseKey(x, weight(x));
          else if ( weight(x) > 0 ) {
            pq.insert(x);
            set(x, "onQ");
          }
          else {
            pq.remove(x);
            clear(x, "onQ");
            hide(x);
          }
        }
        hide(w);
        step();
        for ( Node x : neighborsOfW ) uncolor(x);
        step();
      } // dominance established
      unhighlight(e);
    } // for visible incident edges on v
    unhighlight(v);
    step();
} // while pq not empty
  step();
  showNodes();
  checkCover();
}

//  [Last modified: 2017 01 31 at 16:30:16 GMT]
