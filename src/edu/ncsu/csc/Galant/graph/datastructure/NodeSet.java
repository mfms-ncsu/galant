/**
 * A set of nodes.
 *
 * @todo Tried to use TreeSet so that order would be preserved for lists, but
 * that gave the wrong answer for contains() method. Odd.
 */

package edu.ncsu.csc.Galant.graph.datastructure;

import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.util.Iterator;

import edu.ncsu.csc.Galant.graph.component.Node;

public class NodeSet extends HashSet<Node> {
  public NodeSet() { super(); }
  public NodeSet(Collection<Node> C) {
    super(C);
  }

  public NodeSet union(NodeSet other) {
    NodeSet theUnion = new NodeSet(this);
    for ( Node node : other ) {
      theUnion.add(node);
    }
    return theUnion;
  }

  public NodeSet intersection(NodeSet other) {
    NodeSet theIntersection = new NodeSet();
    for ( Node node : other ) {
      if ( this.contains(node) ) {
        theIntersection.add(node);
      }
    }
    return theIntersection;
  }

  public NodeSet difference(NodeSet other) {
    NodeSet theDifference = new NodeSet();
    for ( Node node : this ) {
      if ( ! other.contains(node) ) {
        theDifference.add(node);
      }
    }
    return theDifference;
  }
  
  public NodeSet symmetricDifference(NodeSet other) {
    NodeSet theDifference = new NodeSet();
    for ( Node node : this ) {
      if ( ! other.contains(node) ) {
        theDifference.add(node);
      }
    }
    for ( Node node : other ) {
      if ( ! this.contains(node) ) {
        theDifference.add(node);
      }
    }
    return theDifference;
  }

  /**
   * @return true if this set is a subset of other
   */
  public Boolean subset(NodeSet other) {
    for ( Node node : this ) {
      if ( ! other.contains(node) ) {
        return false;
      }
    }
    return true;
  }
  
  @Override
  public String toString() {
    String s = "{";
    for ( Node v : this ) {
      s += " " + v.getId();
    }
    s += " }";
    return s;
  }
}

//  [Last modified: 2017 01 20 at 20:35:25 GMT]
