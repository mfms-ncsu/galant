/**
 * There is no code in this file. Purpose is only to describe the current
 * algorithms for reacting to mouse events and drawing.
 *
 * $Id: gui_algorithm_description.java 102 2015-03-02 22:00:43Z mfms $
 */

/* Mouse events: note these should be encapsulated in a separate class that
 * implements either MouseListener or MouseAdapter; actually, the current
 * code has separate MouseMotionListener and MouseListener -- these can be
 * combined. Communication with the "outside world" takes two basic forms:
 * requests to repaint the panel and requests to update the text version of
 * the graph. For now, all of the following are disabled during animations
 * except for movement of nodes when the algorithm does not specify that it
 * will do node movement.
 *
 * The mouse events are intercepted in GraphWindow.java but information about
 * the objects (nodes/edges) present at the mouse location are retrieved
 * using methods of panels/GraphPanel.java

   mouse pressed -- only used when it occurs at the beginning of a node moving or
   edge creation operation:
      remember prevNode = the last node that was selected by the press
      inform the panel of the node currently pressed
      defer action until a mouse release

   mouse released -- there are several cases to consider, all end with
   updating the (text) editor and repainting the panel:
     - mouse being dragged (can only happen at the end of a node move):
          reinitialize all dragging information
          wipe out memory of prevNode
     The remaining cases can only occur if we're not in animation mode.
     - mode is CREATE NODE and haven't clicked on existing node:
          add a node at the given position
          select the added node (for adding weight, color, etc., and inform
          panel and editor)
     - mode is CREATE EDGE, the click was on a node, and there was a prevNode:
          create edge from prevNode to clicked node
          select the added edge (inform panel and editor)
          inform the panel that no node is currently selected as source
          inform the panel that we're no longer tracking an edge
     - mode is DELETE and a node/edge was clicked:
          remove it
          unselect it (inform panel and editor)
          reposition all nodes if in spring drawing mode
          if an edge was clicked

     mouse dragged -- note that a node (the current selected one) is being
     dragged and record its current position; you're not creating an edge.

     mouse moved -- only relevant if an edge is being created; record (track)
     its current endpoint so that the partial edge is drawn correctly when
     repainting
 */

//  [Last modified: 2016 06 23 at 19:28:53 GMT]

/**
 * @TODO allow user to repaint the panel during animation
 *
 */
