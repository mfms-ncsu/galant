/**
 * A list of edges; inherited directly from ArrayList<Edge>.
 */

package edu.ncsu.csc.Galant.graph.datastructure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import edu.ncsu.csc.Galant.graph.component.Edge;

public class EdgeList extends ArrayList<Edge> {
  public EdgeList() { super(); }
  public EdgeList(Collection<Edge> C) { super(C); }

  @Override
  public String toString() {
    String s = "[";
    for ( Edge e : this ) {
      s += " (" + e.getSource().getId() + "," + e.getTarget().getId() + ")";
    }
    s += " ]";
    return s;
  }
}

//  [Last modified: 2017 01 15 at 17:34:49 GMT]
