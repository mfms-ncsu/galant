package edu.ncsu.csc.Galant;
/**
 * This is not actual code. Rather, it is a list of items to be considered in
 * future implementations of Galant and its extension to layered graphs.
 *
 * <ul>
 *
 * <li>
 * Attributes of nodes and edges that are not present in the input or set by
 * subsequent setters should be given the value <code>null</code>. Display
 * can substitute default values for, e.g., color.
 * </li>
 *
 * <li>
 * Nodes and edges should have an additional attribute <em>thickness</em>.
 * </li>
 *
 * <li>
 * The x and y coordinate attributes of a Node should stand in for
 * position_in_layer and layer, respectively. The only place where this
 * requires interpretation is in the display code (GraphPanel), where layered
 * graphs are already distinguished from regular ones.
 * </li>
 *
 * <li>
 * However, when a layered graph is exported, the real current x and y
 * positions should be recorded somehow, so that, by removing the layered
 * attribute, a user can move vertices around to see if they can improve the
 * number of crossings. This is an argument for retaining layer and
 * position_in_layer as separate attributes; unless there's a way to specify
 * the type during export.
 * </li>
 *
 * <li>
 * Class LayeredGraph should extend class Graph and augment the latter with
 * an internal class Layer, but this incarnation of LayeredGraph should
 * manipulate only the list of nodes in a layer (not anything having to do
 * with display or algorithm implementation). Because the display used in
 * algorithms sometimes modifies the positions of nodes, a method to resync
 * these based on the list needs to be included. The list is therefore
 * canonical.
 * </li>
 *
 * <li>
 * A class CrossingMinimization can extend LayeredGraph and offer a variety
 * of utilities for crossing minimization algorithms.
 * </li>
 *
 * <li>
 * A class LayeredGraphDisplay can offer methods that are used in the current
 * crossing minimization algorithms for display purposes. Here is where the
 * LayeredGraph date is augmented with a separate positionOfNode array to
 * store the position at which a node is to be displayed. 
 * </li>
 *
 * </ul>
 *
 * $Id: todo_list.java 96 2014-07-18 21:53:45Z mfms $
 */

class todo_list {
}

//  [Last modified: 2014 07 18 at 18:05:10 GMT]
