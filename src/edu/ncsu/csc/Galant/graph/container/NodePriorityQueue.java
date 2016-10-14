/**
 * A priority queue of nodes. This datatype has some built-in
 * flexibility. When creating the queue, user can decide whether to have it
 * be a max heap or a min heap (the latter is the default) and whether to use
 * a different attribute instead of the default - weight.
 *
 * UNDER CONSTRUCTION
 */

package edu.ncsu.csc.Galant.graph.container;

import java.util.PriorityQueue;
import java.util.Collections;
import java.util.Iterator;

import edu.ncsu.csc.Galant.graph.component.Node;
import edu.ncsu.csc.Galant.graph.component.GraphElement;
import edu.ncsu.csc.Galant.GalantException;
import edu.ncsu.csc.Galant.algorithm.Terminate;

public class NodePriorityQueue extends PriorityQueue<Node> {
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
     * creates a min heap whose keys are based on node weight 
     */
    public NodePriorityQueue() {
        super();
    }

    /**
     * creates a max heap whose keys are based on node weight 
     * @param isMax ignored - needed only to distinguish
     */
    public NodePriorityQueue(boolean isMax) {
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
//     public NodePriorityQueue(String sttribute) {
//         super(INITIAL_SIZE, GraphElement.getComparator(attribute));
//         this.attribute = attribute;
//     }

    /**
     * @return the maximum or minimum item on the queue, depending on how the
     * queue was initialized (isMax argument in the constructor); null if the
     * queue is empty
     */
    public Node removeBest() throws GalantException {
        Node v = this.poll();
        if ( v == null )
            throw new GalantException("attempt to remove item from empty queue");
        return v;
    }

    /**
     * @return the minimum item on the queue; an exception if this is a max heap
     */
    public Node removeMin() throws GalantException {
        if ( isMaxHeap )
            throw new GalantException("attempt to removeMin from max heap");
        return removeBest();
    }

    /**
     * @return the minimum item on the queue; an exception if this is a max heap
     */
    public Node removeMax() throws GalantException {
        if ( ! isMaxHeap )
            throw new GalantException("attempt to removeMax from min heap");
        return removeBest();
    }

    /**
     * adds an item, assumes it has a value for the desired attribute, throws
     * an exception if not
     * @return true if v was not already in the queue
     */
    public boolean insert(Node v) throws GalantException {
        if ( v == null )
            throw new GalantException("attempt to add null node to priority queue");
        if ( v.getDouble(attribute) == null )
            throw new GalantException("node " + v.getId() + " has no attribute "
                                      + attribute + " when attempting to add to"
                                      + " priority queue");
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
     * Changes the value of v's attribute and the position of v in the queue
     * to reflect the change
     */
    public void changeKey(Node v, Double key) throws GalantException, Terminate {
        this.remove(v);
        v.set(attribute, key);
        this.insert(v);
    }

    public void decreaseKey(Node v, Double key) throws GalantException, Terminate {
        changeKey(v, key);
    }
}

//  [Last modified: 2016 10 14 at 17:25:42 GMT]
