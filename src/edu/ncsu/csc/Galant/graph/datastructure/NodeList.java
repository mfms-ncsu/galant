/**
 * A list of nodes; inherited directly from ArrayList<Node>.
 */

package edu.ncsu.csc.Galant.graph.datastructure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import edu.ncsu.csc.Galant.graph.component.Node;

public class NodeList extends ArrayList<Node> {
  public NodeList() { super(); }
  public NodeList(Collection<Node> C) { super(C); }

  @Override
  public String toString() {
    String s = "[";
    for ( Node v : this ) {
      s += " " + v.getId();
    }
    s += " ]";
    return s;
  }
}

//  [Last modified: 2017 01 15 at 17:33:09 GMT]
