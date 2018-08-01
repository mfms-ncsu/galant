package edu.ncsu.csc.Galant.gui.window;

public class GuiAlgorithmDescription {
  /** 
   * There is no code in this file. Purpose is only to describe the current
   * algorithms for reacting to mouse events and drawing.
   *
   * <dl>
   * <dt>Mouse events:</dt>
   * <dd>note these should be encapsulated in a separate class that
   * implements either MouseListener or MouseAdapter; actually, the current
   * code has separate MouseMotionListener and MouseListener -- these can be
   * combined. Communication with the "outside world" takes two basic forms:
   * <ol>
   * <li>requests to repaint the panel and requests to update the text version of
   * the graph. For now, all of the following are disabled during animations
   * except for movement of nodes when the algorithm does not specify that it
   * will do node movement.</li>
   *
   * <li>
   * The mouse events are intercepted in GraphWindow.java but information about
   * the objects (nodes/edges) present at the mouse location are retrieved
   * using methods of panels/GraphPanel.java</li>
   </ol>
   </dd>
   </dl>

   <dl>
   <dt>mouse pressed</dt> 
   <dd>only used when it occurs at the beginning of a node moving or
   edge creation operation:<br>
   remember prevNode = the last node that was selected by the press
   inform the panel of the node currently pressed
   defer action until a mouse release
   </dd>

   <dt>mouse released</dt>
   <dd>
   there are several cases to consider, all end with
   updating the (text) editor and repainting the panel:
   <ul>
   <li> mouse being dragged (can only happen at the end of a node move):
   reinitialize all dragging information
   wipe out memory of prevNode
   </li>


   <li>The remaining cases can only occur if we're not in animation mode.
   <ul>

   <li> mode is <code>CREATE NODE</code> and haven't clicked on existing node:
   add a node at the given position
   select the added node (for adding weight, color, etc., and inform
   panel and editor)</li>
   <li> mode is <code>CREATE EDGE</code>, the click was on a node, and
   there was a prevNode:
   <ol>
   <li>create edge from prevNode to clicked node</li>
   <li>select the added edge (inform panel and editor)</li>
   <li>inform the panel that no node is currently selected as source</li>
   <li>inform the panel that we're no longer tracking an edge</li>
   </ol>
   </li>
   <li>mode is DELETE and a node/edge was clicked:
   <ol>
   <li>remove it</li>
   <li>unselect it (inform panel and editor)</li>
   </li>reposition all nodes if in spring drawing mode</li>
   </li>if an edge was clicked</li>
   </ol>
   </ul>
   </dd>

   <dt>mouse dragged</dt>
   <dd> make note of the fact that a node (the current selected one) is being
   dragged and record its current position; you're not creating an edge.</dd>

   <dt>mouse moved</dt>
   <dd>only relevant if an edge is being created; record (track)
   its current endpoint so that the partial edge is drawn correctly when
   repainting</dd>
   </dl>
  **/

  public GuiAlgorithmDescription() {}
}

//  [Last modified: 2018 08 01 at 14:32:50 GMT]
