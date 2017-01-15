/**
 * A set of edges.
 *
 * @todo Tried to use TreeSet so that order would be preserved for lists, but
 * that gave the wrong answer for contains() method. Odd.
 */

package edu.ncsu.csc.Galant.graph.datastructure;

import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.util.Iterator;

import edu.ncsu.csc.Galant.graph.component.Edge;

public class EdgeSet extends HashSet<Edge> {
  public EdgeSet() { super(); }
  public EdgeSet(Collection<Edge> C) {
    super(C);
  }

  public EdgeSet union(EdgeSet other) {
    EdgeSet theUnion = new EdgeSet(this);
    for ( Edge edge : other ) {
      theUnion.add(edge);
    }
    return theUnion;
  }

  public EdgeSet intersection(EdgeSet other) {
    EdgeSet theIntersection = new EdgeSet();
    for ( Edge edge : other ) {
      if ( this.contains(edge) ) {
        theIntersection.add(edge);
      }
    }
    return theIntersection;
  }

  public EdgeSet difference(EdgeSet other) {
    EdgeSet theDifference = new EdgeSet();
    for ( Edge edge : this ) {
      if ( ! other.contains(edge) ) {
        theDifference.add(edge);
      }
    }
    return theDifference;
  }
  
  public EdgeSet symmetricDifference(EdgeSet other) {
    EdgeSet theDifference = new EdgeSet();
    for ( Edge edge : this ) {
      if ( ! other.contains(edge) ) {
        theDifference.add(edge);
      }
    }
    for ( Edge edge : other ) {
      if ( ! this.contains(edge) ) {
        theDifference.add(edge);
      }
    }
    return theDifference;
  }
  
  @Override
  public String toString() {
    String s = "{";
    for ( Edge e : this ) {
      s += " (" + e.getSource().getId() + "," + e.getTarget().getId() + ")";
    }
    s += " }";
    return s;
  }
}

//  [Last modified: 2017 01 15 at 22:39:15 GMT]
