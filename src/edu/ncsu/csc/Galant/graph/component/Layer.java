/**
 * Keeps track of position and display information for nodes on a single
 * layer. The <em>logical</em> order of nodes on a layer is determined by the
 * ArrayList <code>nodes</code>, while the display order is determined by
 * setting the positionInLayer attribute of a node. These two are brought in
 * sync by displayPositions.
 *
 * @todo not clear that this class is currently in use; the layered
 * graph algorithms appear to import the LayeredGraph class and use it
 * independent of anything in core Galant functionality; this issue
 * will be resolved once there's a new way of handling positions
 */

package edu.ncsu.csc.Galant.graph.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import edu.ncsu.csc.Galant.algorithm.Terminate;
import edu.ncsu.csc.Galant.GraphDispatch;
import edu.ncsu.csc.Galant.logging.LogHelper;

public class Layer extends GraphElement {
    LayeredGraph graph;
    ArrayList<LayeredGraphNode> nodes;
    boolean marked;

    public Layer(LayeredGraph graph) {
        super(graph);
        this.marked = false;
        this.graph = graph;
        nodes = new ArrayList<LayeredGraphNode>();
    }


    /**
     * Highlights nodes on this layer and, if appropriate, incident edges --
     * see "enum Scope"
     *
     * @todo will need to be fixed; probably via a class Channel
     */
//     public void highlight( LayeredGraph.Scope scope ) throws Terminate {
//         for ( LayeredGraphNode v: nodes ) {
//             v.setSelected( true );
//             if ( scope == LayeredGraph.Scope.UP
//                  || scope == LayeredGraph.Scope.BOTH ) {
//                 for ( Edge e: v.getOutgoingEdges() ) {
//                     e.setSelected( true );
//                 }
//             }
//             if ( scope == LayeredGraph.Scope.DOWN
//                  || scope == LayeredGraph.Scope.BOTH ) {
//                 for ( Edge e: v.getIncomingEdges() ) {
//                     e.setSelected( true );
//                 }
//             }
//         }
//    
    /**
     * sets the indexInLayer property of a node
     */
    public void setIndex(LayeredGraphNode v, int index) throws Terminate {
        v.set("indexInLayer", index);
    }

    /**
     * @return the indexInLayer property of a node
     */
    public int getIndex(LayeredGraphNode v) {
        return v.getInteger("indexInLayer");
    }

    

    /**
     * @return the node at the given index on this layer
     */
    public LayeredGraphNode getNodeAt(int index) {
        return nodes.get(index);
    }


    /**
     * @return true if this layer is marked (for mod_bary)
     */
    public boolean isMarked() {
        return marked;
    }

    public void mark() {
        marked = true;
    }

    public void unMark() {
        marked = false;
    }

    /**
     * Displays the marked/unmarked state of all nodes on this layer
     */
    public void displayMarks() throws Terminate {
        for ( LayeredGraphNode v: nodes ) {
            v.setVisited( graph.isMarked( v ) );
        }
    }

    /**
     * Removes displayed marks from all nodes on this layer without affecting
     * their logical status.
     */
    public void removeMarks() throws Terminate {
        for ( LayeredGraphNode v: nodes ) {
            v.setVisited( false );
        }
    }

    /**
     * logically unmarks all nodes on this layer
     */
    public void clearMarks() {
        for ( LayeredGraphNode v: nodes ) {
            graph.unMark( v );
        }
    }

    /**
     * Sets node labels to be blank
     */
    public void clearLabels() throws Terminate {
        for ( LayeredGraphNode v: nodes ) {
            v.setLabel("");
        }
    }

    /**
     * Highlights nodes on this layer and, if appropriate, incident edges --
     * see "enum Scope"
     */
    public void highlight( LayeredGraph.Scope scope ) throws Terminate {
        for ( LayeredGraphNode v: nodes ) {
            v.setSelected( true );
            if ( scope == LayeredGraph.Scope.UP
                 || scope == LayeredGraph.Scope.BOTH ) {
                for ( Edge e: v.getOutgoingEdges() ) {
                    e.setSelected( true );
                }
            }
            if ( scope == LayeredGraph.Scope.DOWN
                 || scope == LayeredGraph.Scope.BOTH ) {
                for ( Edge e: v.getIncomingEdges() ) {
                    e.setSelected( true );
                }
            }
        }
    }

    /**
     * Highlights the nodes between the two given positions, inclusive (used
     * to highlight an insertion)
     */
    public void highlightNodes( int positionOne, int positionTwo ) throws Terminate {
        for ( int i = positionOne; i <= positionTwo; i++ ) {
            nodes.get(i).setSelected( true );
        }
    }

    /**
     * undoes highlighting for the nodes and any edges incident on this layer
     */
    public void unHighlight() throws Terminate {
        for ( LayeredGraphNode v: nodes ) {
            v.setSelected( false );
            for ( Edge e: v.getIncidentEdges() ) {
                e.setSelected( false );
            }
        }
    }


    /**
     * Displays logical weights assigned to the nodes
     */
    public void displayWeights() throws Terminate {
        for ( LayeredGraphNode v: nodes ) {
            v.setWeight( graph.getWeight( v ) );
        }
    }

    /**
     * Gives node weights a default value that makes them invisible
     */
    public void clearWeights() throws Terminate {
        for ( LayeredGraphNode v: nodes ) {
            v.clearWeight();
        }
    }

    /**
     * @return the list of nodes on this layer
     */
    public List<LayeredGraphNode> getNodesInLayer() {
        return nodes;
    }

    /**
     * inserts the node currently at position originalPosition so
     * node currently at newPosition, shifting the intervening nodes to the
     * right
     */
    public void insert( int originalPosition, int newPosition ) throws Terminate {
        LayeredGraphNode toBeInserted = nodes.remove( originalPosition );
        nodes.add( newPosition, toBeInserted );
        updateIndexes();
    }

    /**
     * sorts the nodes by their weight (as assigned by Galant code)
     */
    public void sort() throws Terminate {
        Collections.sort(nodes);
        updateIndexes();
    }

    /**
     * sorts node by positions in their layers (as assigned by Galant)
     */
    final Comparator<LayeredGraphNode> POSITION_COMPARATOR = new Comparator<LayeredGraphNode>() {
        public int compare(LayeredGraphNode x, LayeredGraphNode y) {
            int state = GraphDispatch.getInstance().getDisplayState();
            //edited by 2021 Galant Team
            //add a cast to ensure x and y here are layeredGraphNode
            return x.getPositionInLayer(state) - y.getPositionInLayer(state);
        }
    };

    public void sortByPosition() throws Terminate {
        Collections.sort(nodes, POSITION_COMPARATOR);
        updateIndexes();
    }

    /**
     * sorts nodes by their logical weight (for use with 'fast' versions of
     * barycenter-related algorithms)
     */
    final Comparator<LayeredGraphNode> WEIGHT_COMPARATOR = new Comparator<LayeredGraphNode>() {
        public int compare( LayeredGraphNode x, LayeredGraphNode y ) {
            double wx = graph.getWeight( x );
            double wy = graph.getWeight( y );
            if ( wx > wy ) return 1;
            if ( wx < wy ) return -1;
            return 0;
        }
    };

    public void sortByWeight() throws Terminate {
        Collections.sort( nodes, WEIGHT_COMPARATOR );
        updateIndexes();
    }

    /**
     * sorts the nodes on this layer by increasing degree
     */
    public void sortByIncreasingDegree() throws Terminate {
        LayeredGraph.sortByIncreasingDegree(nodes);
        updateIndexes();
    }

    /**
     * Puts nodes with largest (smallest) degree in the middle and puts
     * subsequent nodes farther toward the outside.
     *
     * @param largestInMiddle if true then the node with largest degree goes
     * in the middle.
     */
    public void middleDegreeSort( boolean largestMiddle ) throws Terminate {
       LayeredGraph.sortByIncreasingDegree( nodes );
       if ( largestMiddle ) Collections.reverse( nodes );
       ArrayList<LayeredGraphNode> tempNodeList = new ArrayList<LayeredGraphNode>();
       boolean addToFront = true;
       for ( LayeredGraphNode node : nodes ) {
           if ( addToFront ) {
               tempNodeList.add( 0, node );
           }
           else {
               tempNodeList.add( tempNodeList.size(), node );
           }
           addToFront = ! addToFront;
       }
       nodes = tempNodeList;
       updateIndexes();
    }

    /**
     * Uses the order in the list 'nodes' to update the indexes of the
     * nodes in the graph, making them consistent
     */
    public void updateIndexes() throws Terminate {
        int currentIndex = 0;
        for ( LayeredGraphNode v: nodes ) {
            v.setIndexInLayer(currentIndex++);
        }
    }

    /**
     * Updates the display based on the order of the nodes on this layer;
     * assumes the only y-coordinate changes occurred for nodes whose
     * positions changed: see previewPositionChanges()
     */
    public void displayPositions() throws Terminate {
        int i = 0;
        for ( LayeredGraphNode node : nodes ) {
            if ( node.getPositionInLayer() != i ) {
                node.setPositionInLayer( i );
            }
            i++;
        }
    }

    public void markPositionChanges() {
        int i = 0;
        for ( LayeredGraphNode node : nodes ) {
        	//edited by 2021 Galant Team
            //add a cast to tell program this is really a LayeredGraphNode
            if ( node.getPositionInLayer() != i ) {
                graph.mark( node );
            }
            i++;
        }
    }

    /**
     * Saves positions of all the nodes
     */
    public void savePositions() {
        for ( LayeredGraphNode v: nodes ) {
            graph.setSavedPosition(v, v.getPositionInLayer());
        }
    }

    /**
     * Restores saved positions of all the nodes
     */
    public void restoreSavedPositions() throws Terminate {
        for ( LayeredGraphNode node : nodes ) {
            node.setPositionInLayer( graph.getSavedPosition(node) );
        }
    }

    /**
     * Updates the display based on previously saved positions
     */
    public void displaySavedPositions() throws Terminate {
        for ( LayeredGraphNode node : nodes ) {
            int position = graph.getSavedPosition(node);
            if ( node.getPositionInLayer() != position ) {
                node.setPositionInLayer( position );
            }
        }
    }

    /**
     * Puts the node at ithe position on the display (does nothing to its logical
     * position)
     */
    public void displayPosition(LayeredGraphNode node, int position) throws Terminate {
        if ( node.getPositionInLayer() != position ) {
            node.setPositionInLayer(position);
        }
    }

    public String toString() {
        String s = "";
        s += "[";
        for ( LayeredGraphNode node : nodes ) {
            s += " " + node.getId() + " " +
                "(" + node.getLayer() + "," + node.getPositionInLayer() + "),";
        }
        s += " ]";
        return s;
    }

} // end, class Layer
