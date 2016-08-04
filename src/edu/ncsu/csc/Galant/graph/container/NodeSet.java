/**
 * A set of nodes.
 *
 * @todo Tried to use TreeSet so that order would be preserved for lists, but
 * that gave the wrong answer for contains() method. Odd.
 */

package edu.ncsu.csc.Galant.graph.container;

import java.util.Set;
import java.util.AbstractSet;
import java.util.HashSet;
import java.util.Collection;
import java.util.Iterator;

import edu.ncsu.csc.Galant.graph.component.Node;

public class NodeSet extends AbstractSet<Node> {
    private HashSet<Node> set;
    public NodeSet() { set = new HashSet<Node>(); }
    public NodeSet(Collection<Node> C) {
        set = new HashSet<Node>(C);
    }
    /** adds the node
     * @return true if the node is not present, false if it is already in
     * the set
     */
    @Override
        public boolean add(Node e) { return set.add(e); }
    /** removes the node
     * @return true if it is present, false otherwise
     */
    public boolean remove(Node e) { return set.remove(e); }
    public boolean contains(Node e) { return set.contains(e); }
    @Override
        public boolean isEmpty() { return set.isEmpty(); }
    @Override
        public int size() { return set.size(); }
    @Override
        public Iterator<Node> iterator() { return set.iterator(); }
}

//  [Last modified: 2016 08 04 at 14:57:04 GMT]
