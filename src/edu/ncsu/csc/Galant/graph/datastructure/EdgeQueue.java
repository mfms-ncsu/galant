/**
 * A queue of edges
 */

package edu.ncsu.csc.Galant.graph.datastructure;

import java.util.ArrayDeque;
import java.util.Collection;

import edu.ncsu.csc.Galant.graph.component.Edge;
import edu.ncsu.csc.Galant.graph.component.GraphElement;
import edu.ncsu.csc.Galant.GalantException;

public class EdgeQueue extends ArrayDeque<Edge> {
  public EdgeQueue() {
    super();
  }
  public EdgeQueue(Collection<Edge> C) {
    super(C);
  }
  public void enqueue(Edge edge) throws GalantException {
    if ( edge == null )
      throw new GalantException("Attempt to add null edge to queue");
      this.offer(edge);
  }
  public Edge dequeue() throws GalantException {
    if ( this.isEmpty() )
      throw new GalantException("Attempt to remove item from empty queue");
    return this.poll();
  }
}
  
//  [Last modified: 2017 01 15 at 21:25:36 GMT]
