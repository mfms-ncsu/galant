/**
 * A priority queue of edges. This datatype has some built-in
 * flexibility. When creating the queue, user can decide whether to have it
 * be a max heap or a min heap (the latter is the default) and whether to use
 * a different attribute instead of the default - weight.
 */

package edu.ncsu.csc.Galant.graph.datastructure;

import java.util.PriorityQueue;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import edu.ncsu.csc.Galant.graph.component.Edge;
import edu.ncsu.csc.Galant.graph.component.GraphElement;
import edu.ncsu.csc.Galant.GalantException;
import edu.ncsu.csc.Galant.algorithm.Terminate;

public class EdgePriorityQueue extends PriorityQueue<Edge> {
  // used only because a priority queue with a specific comparator needs this parameter
  static final int INITIAL_SIZE = 16;

  /**
   * The attribute used for sorting
   */
  private String attribute = GraphElement.WEIGHT;

  /**
   * true if the priority queue is a max heap
   */
  boolean isMaxHeap = false;

  /**
   * creates a min-heap whose keys are based on edge weight 
   */
  public EdgePriorityQueue() {
    super();
  }

  /**
   * creates a min-heap whose keys are based on edge weight from the elements
   * in collection C
   */
  public EdgePriorityQueue(Collection<Edge> C) {
    super(C);
  }

  /**
   * creates a max heap whose keys are based on edge weight 
   * @param isMax true if this will be a max-heap
   */
  public EdgePriorityQueue(boolean isMax) {
    super(INITIAL_SIZE, Collections.reverseOrder());
    this.isMaxHeap = true;
  }

  /**
   * creates a heap using the given comparator; GraphElementComparator
   * objects have embedded information about attributes and reversal.
   */
  public EdgePriorityQueue(GraphElementComparator C) {
    super(INITIAL_SIZE, C);
    this.attribute = C.attribute;
    this.isMaxHeap = C.reverse;
  }
  
  /**
   * @return the maximum or minimum item on the queue, depending on how the
   * queue was initialized (isMax argument in the constructor); null if the
   * queue is empty
   */
  public Edge removeBest() throws GalantException {
    if ( this.isEmpty() )
      throw new GalantException("Attempt to remove item from empty queue");
    return this.poll();
  }

  /**
   * @return the maximum or minimum item on the queue, depending on how the
   * queue was initialized (isMax argument in the constructor); an
   * exception occurs if the queue is empty; the item is not removed
   */
  public Edge best() throws GalantException {
    if ( this.isEmpty() )
      throw new GalantException("Attempt to get item from empty queue");
    return this.peek();
  }
  
  /**
   * @return the minimum item on the queue; an exception if this is a max heap
   */
  public Edge removeMin() throws GalantException {
    if ( isMaxHeap )
      throw new GalantException("attempt to removeMin from max heap");
    return removeBest();
  }

  /**
   * @return the minimum item on the queue; an exception if this is a max
   * heap; don't remove the item
   */
  public Edge min() throws GalantException {
    if ( isMaxHeap )
      throw new GalantException("Attempt to return min from max heap");
    return best();
  }

  /**
   * @return the minimum item on the queue; an exception if this is a max heap
   */
  public Edge removeMax() throws GalantException {
    if ( ! isMaxHeap )
      throw new GalantException("attempt to removeMax from min heap");
    return removeBest();
  }

  /**
   * @return the maximum item on the queue; an exception if this is a min
   * heap; don't remove the item
   */
  public Edge max() throws GalantException {
    if ( ! isMaxHeap )
      throw new GalantException("Attempt to get max from min heap");
    return best();
  }

  /**
   * adds an item, assumes it has a value for the desired attribute, throws
   * an exception if not
   * @return true if e was not already in the queue
   */
  public boolean insert(Edge e) throws GalantException {
    if ( e == null )
      throw new GalantException("attempt to add null edge to priority queue");
    return super.add(e);
  }

  /**
   * adds an item and gives it a key for the attribute (works only for
   * numerical attributes
   * @return true if e was not already in the queue
   */
  public boolean insert(Edge e, double key) throws GalantException, Terminate {
    if ( e == null )
      throw new GalantException("attempt to add null edge to priority queue");
    e.set(attribute, key);
    return super.add(e);
  }

  /**
   * Changes the position of e in the queue to reflect a change in value of
   * the attribute (the latter is done externally)
   */
  public void changeKey(Edge e) throws GalantException {
    this.remove(e);
    this.insert(e);
  }

  /**
   * Changes the value of edge's attribute (works only for Double attributes)
   * and the position of edge in the queue to reflect the change
   */
  public void changeKey(Edge edge, Double key) throws GalantException, Terminate {
    this.remove(edge);
    edge.set(attribute, key);
    this.insert(edge);
  }

  /**
   * Changes the value of edge's attribute (works only for Double attributes)
   * and the position of edge in the queue to reflect the change
   */
  public void changeDoubleKey(Edge edge, Double key) throws GalantException, Terminate {
    this.remove(edge);
    edge.set(attribute, key);
    this.insert(edge);
  }

  /**
   * Changes the value of edge's attribute (this one works for String
   * attributes) and the position of edge in the queue to reflect the change
   */
  public void changeStringKey(Edge edge, String key) throws GalantException, Terminate {
    this.remove(edge);
    edge.set(attribute, key);
    this.insert(edge);
  }

  /**
   * Changes the value of edge's attribute (this one works for Integer
   * attributes) and the position of edge in the queue to reflect the change
   */
  public void changeIntegerKey(Edge edge, Integer key) throws GalantException, Terminate {
    this.remove(edge);
    edge.set(attribute, key);
    this.insert(edge);
  }

  public void decreaseKey(Edge e, double key) throws GalantException, Terminate {
    changeKey(e, key);
  }

  public String toString() {
    String string = "[pq: ";
    for ( Edge e : this ) {
      if ( e == null ) string += " null";
      else string += " (" + e.getSource().getId() +
             "," + e.getTarget().getId() + ")";
    }
    return string + " ]";
  }
}

//  [Last modified: 2017 01 20 at 02:13:33 GMT]
