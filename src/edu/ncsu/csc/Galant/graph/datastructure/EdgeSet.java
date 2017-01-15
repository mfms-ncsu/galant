/**
 * A set of edges.
 *
 * @todo Tried to use TreeSet so that order would be preserved for lists, but
 * that gave the wrong answer for contains() method. Odd.
 */

package edu.ncsu.csc.Galant.graph.datastructure;

import java.util.Set;
import java.util.AbstractSet;
import java.util.HashSet;
import java.util.Collection;
import java.util.Iterator;

import edu.ncsu.csc.Galant.graph.component.Edge;

public class EdgeSet extends AbstractSet<Edge> {
    private HashSet<Edge> set;
    public EdgeSet() { set = new HashSet<Edge>(); }
    public EdgeSet(Collection<Edge> C) {
        set = new HashSet<Edge>(C);
    }
    /** adds the edge
     * @return true if the edge is not present, false if it is already in
     * the set
     */
    @Override
        public boolean add(Edge e) { return set.add(e); }
    /** removes the edge
     * @return true if it is present, false otherwise
     */
    public boolean remove(Edge e) { return set.remove(e); }
    public boolean contains(Edge e) { return set.contains(e); }
    @Override
        public boolean isEmpty() { return set.isEmpty(); }
    @Override
        public int size() { return set.size(); }
    @Override
        public Iterator<Edge> iterator() { return set.iterator(); }

  @Override
  public String toString() {
    String s = "{";
    for ( Edge e : set ) {
      s += " (" + e.getSource().getId() + "," + e.getTarget().getId() + ")";
    }
    s += " }";
    return s;
  }
}

//  [Last modified: 2017 01 15 at 16:47:15 GMT]
