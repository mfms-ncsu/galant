/**
 * A priority queue of nodes. This datatype has some built-in
 * flexibility. When creating the queue, user can decide whether to have it
 * be a max heap or a min heap (the latter is the default) and whether to use
 * a different attribute instead of the default - weight.
 */

package edu.ncsu.csc.Galant.graph.container;

import java.util.PriorityQueue;
import java.util.Collections;
import java.util.Iterator;
import java.util.Comparator;

import edu.ncsu.csc.Galant.graph.component.Edge;
import edu.ncsu.csc.Galant.graph.component.GraphElement;
import edu.ncsu.csc.Galant.GalantException;
import edu.ncsu.csc.Galant.algorithm.Terminate;

public class EdgePriorityQueue extends PriorityQueue<Edge> {
    // used only because max heap needs this parameter
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
     * creates a min heap whose keys are based on edge weight 
     */
    public EdgePriorityQueue() {
        super();
    }

    /**
     * creates a max heap whose keys are based on edge weight 
     * @param isMax ignored - needed only to distinguish
     */
    public EdgePriorityQueue(boolean isMax) {
        super(INITIAL_SIZE, Collections.reverseOrder());
        this.isMaxHeap = true;
    }

    /**
     * creates a min heap whose keys are based on the given attribute, which
     * must have Double values
     *
     * @todo This does not work because the super constructor call is not
     * allowed to reference attribute; maybe we have to resort to composition
     * rather than inheritance if we want this functionality
     */
//     public EdgePriorityQueue(String sttribute) {
//         super(INITIAL_SIZE, GraphElement.getComparator(attribute));
//         this.attribute = attribute;
//     }

  /**
   * creates a heap using the given comparator
   */
  public EdgePriorityQueue(Comparator<GraphElement> C) {
    super(INITIAL_SIZE, C);
  }
  
    /**
     * @return the maximum or minimum item on the queue, depending on how the
     * queue was initialized (isMax argument in the constructor); null if the
     * queue is empty
     */
    public Edge removeBest() throws GalantException {
        Edge e = this.poll();
        if ( e == null )
            throw new GalantException("attempt to remove item from empty queue");
        return e;
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
     * @return the minimum item on the queue; an exception if this is a max heap
     */
    public Edge removeMax() throws GalantException {
        if ( ! isMaxHeap )
            throw new GalantException("attempt to removeMax from min heap");
        return removeBest();
    }

    /**
     * adds an item, assumes it has a value for the desired attribute, throws
     * an exception if not
     * @return true if e was not already in the queue
     */
    public boolean insert(Edge e) throws GalantException {
        if ( e == null )
            throw new GalantException("attempt to add null edge to priority queue");
        if ( e.getDouble(attribute) == null )
            throw new GalantException("edge " + e.getId() + " has no attribute "
                                      + attribute + " when attempting to add to"
                                      + " priority queue");
        return super.add(e);
    }

    /**
     * adds an item and gives it a key for the attribute
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
     * Changes the value of e's attribute and the position of e in the queue
     * to reflect the change
     */
    public void changeKey(Edge e, double key) throws GalantException, Terminate {
        this.remove(e);
        e.set(attribute, key);
        this.insert(e);
    }

    public void decreaseKey(Edge e, double key) throws GalantException, Terminate {
        changeKey(e, key);
    }
}

//  [Last modified: 2017 01 11 at 21:03:53 GMT]
