/**
 * A queue of nodes
 */

package edu.ncsu.csc.Galant.graph.datastructure;

import java.util.ArrayDeque;
import java.util.Collection;

import edu.ncsu.csc.Galant.graph.component.Node;
import edu.ncsu.csc.Galant.graph.component.GraphElement;
import edu.ncsu.csc.Galant.GalantException;

public class NodeQueue extends ArrayDeque<Node> {
  public NodeQueue() {
    super();
  }
  public NodeQueue(Collection<Node> C) {
    super(C);
  }
  public void enqueue(Node node) throws GalantException {
    if ( node == null )
      throw new GalantException("Attempt to add null node to queue");
      this.offer(node);
  }
  public Node dequeue() throws GalantException {
    if ( this.isEmpty() )
      throw new GalantException("Attempt to remove item from empty queue");
    return this.poll();
  }
}

//  [Last modified: 2017 01 15 at 21:23:55 GMT]
