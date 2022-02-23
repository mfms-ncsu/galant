/**
 * A priority queue of nodes. This datatype has some built-in
 * flexibility. When creating the queue, user can decide whether to have it
 * be a max heap or a min heap (the latter is the default) and whether to use
 * a different attribute instead of the default - weight.
 */

package edu.ncsu.csc.Galant.graph.datastructure;

import java.util.PriorityQueue;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Comparator;

import edu.ncsu.csc.Galant.graph.component.Node;
import edu.ncsu.csc.Galant.graph.component.GraphElement;
import edu.ncsu.csc.Galant.GalantException;
import edu.ncsu.csc.Galant.algorithm.Terminate;

public class NodePriorityQueue extends PriorityQueue<Node> {
  // used only because a priority queue with a specific comparator needs this
  // parameter
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
   * creates a min-heap whose keys are based on node weight
   */
  public NodePriorityQueue() {
    super();
  }

  /**
   * creates a min-heap whose keys are based on node weight from the elements
   * in collection C
   */
  public NodePriorityQueue(Collection<Node> C) {
    super(C);
  }

  /**
   * creates a max heap whose keys are based on node weight
   *
   * @param isMax
   *          true if this will be a max-heap
   */
  public NodePriorityQueue(boolean isMax) {
    super(INITIAL_SIZE, Collections.reverseOrder());
    this.isMaxHeap = true;
  }

  /**
   * creates a heap using the given comparator; GraphElementComparator
   * objects have embedded information about attributes and reversal.
   */
  public NodePriorityQueue(GraphElementComparator C) {
    super(INITIAL_SIZE, C);
    this.attribute = C.attribute;
    this.isMaxHeap = C.reverse;
  }

  /**
   * @return the maximum or minimum item on the queue, depending on how the
   *         queue was initialized (isMax argument in the constructor); an
   *         exception occurs if the queue is empty; remove the item as well
   */
  public Node removeBest() throws GalantException {
    if ( this.isEmpty() )
      throw new GalantException("Attempt to remove item from empty queue");
    return this.poll();
  }

  /**
   * @return the maximum or minimum item on the queue, depending on how the
   *         queue was initialized (isMax argument in the constructor); an
   *         exception occurs if the queue is empty; the item is not removed
   */
  public Node best() throws GalantException {
    if ( this.isEmpty() )
      throw new GalantException("Attempt to get item from empty queue");
    return this.peek();
  }

  /**
   * @return the minimum item on the queue; an exception if this is a max
   *         heap; remove the item as well
   */
  public Node removeMin() throws GalantException {
    if ( isMaxHeap )
      throw new GalantException("Attempt to removeMin from max heap");
    return removeBest();
  }

  /**
   * @return the minimum item on the queue; an exception if this is a max
   *         heap; don't remove the item
   */
  public Node min() throws GalantException {
    if ( isMaxHeap )
      throw new GalantException("Attempt to return min from max heap");
    return best();
  }

  /**
   * @return the maximum item on the queue; an exception if this is a min
   *         heap; remove the item as well
   */
  public Node removeMax() throws GalantException {
    if ( ! isMaxHeap )
      throw new GalantException("Attempt to removeMax from min heap");
    return removeBest();
  }

  /**
   * @return the maximum item on the queue; an exception if this is a min
   *         heap; don't remove the item
   */
  public Node max() throws GalantException {
    if ( ! isMaxHeap )
      throw new GalantException("Attempt to get max from min heap");
    return best();
  }

  /**
   * adds an item, assumes it has a value for the desired attribute, throws
   * an exception if not
   *
   * @return true if v was not already in the queue
   */
  public boolean insert(Node v) throws GalantException {
    if ( v == null )
      throw new GalantException("Attempt to add null node to priority queue");
    return super.add(v);
  }

  /**
   * adds an item and gives it a key for the attribute
   *
   * @return true if v was not already in the queue
   */
  public boolean insert(Node v, double key) throws GalantException, Terminate {
    if ( v == null )
      throw new GalantException("Attempt to add null node to priority queue");
    v.set(attribute, key);
    return super.add(v);
  }

  /**
   * Changes the position of v in the queue to reflect a change in value of
   * the attribute (the latter is done externally)
   */
  public void changeKey(Node v) throws GalantException {
    this.remove(v);
    this.insert(v);
  }

  /**
   * Changes the value of v's attribute (works only for Double attributes)
   * and the position of v in the queue to reflect the change
   */
  public void changeKey(Node v, Double key) throws GalantException, Terminate {
    this.remove(v);
    v.set(attribute, key);
    this.insert(v);
  }

  /**
   * Changes the value of v's attribute (works only for Double attributes)
   * and the position of v in the queue to reflect the change
   */
  public void changeDoubleKey(Node v, Double key) throws GalantException, Terminate {
    this.remove(v);
    v.set(attribute, key);
    this.insert(v);
  }

  /**
   * Changes the value of e's attribute (this one works for String attributes)
   * and the position of e in the queue to reflect the change
   */
  public void changeStringKey(Node v, String key) throws GalantException, Terminate {
    this.remove(v);
    v.set(attribute, key);
    this.insert(v);
  }

  /**
   * Changes the value of e's attribute (this one works for Integer attributes)
   * and the position of e in the queue to reflect the change
   */
  public void changeIntegerKey(Node v, Integer key) throws GalantException, Terminate {
    this.remove(v);
    v.set(attribute, key);
    this.insert(v);
  }

  public void decreaseKey(Node v, double key) throws GalantException, Terminate {
    changeKey(v, key);
  }

  public String toString() {
    String string = "[pq:";
    for ( Node v : this ) {
      if ( v == null )
        string += " null";
      else
        string += " " + v.getId();
    }
    return string + " ]";
  }
}