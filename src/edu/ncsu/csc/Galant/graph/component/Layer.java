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
    ArrayList<Node> nodes;
    boolean marked;

    public Layer(LayeredGraph graph) {
        super(graph);
        this.marked = false;
        this.graph = graph;
        nodes = new ArrayList<Node>();
    }


    /**
     * Highlights nodes on this layer and, if appropriate, incident edges --
     * see "enum Scope"
     *
     * @todo will need to be fixed; probably via a class Channel
     */
//     public void highlight( LayeredGraph.Scope scope ) throws Terminate {
//         for ( Node v: nodes ) {
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
     * adds new positions numbered layer.nodes.size(), ... , position to the
     * nodes list of the given layer; the new positions are filled with null
     * nodes; can be called even if not needed - it does nothing, safely, in
     * that case
     */
    void ensurePosition( int position ) {
        for ( int i = nodes.size(); i <= position; i++ ) {
            nodes.add( null );
        }
    }

    /**
     * puts node v into the i-th position
     */
    public void addNode( Node v, int i ) {
        ensurePosition( i );
        nodes.set( i, v );
    }

    /**
     * @return the node at the given position on this layer
     */
    public Node getNodeAt( int position ) {
        return nodes.get( position );
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
        for ( Node v: nodes ) {
            v.setVisited( graph.isMarked( v ) );
        }
    }

    /**
     * Removes displayed marks from all nodes on this layer without affecting
     * their logical status.
     */
    public void removeMarks() throws Terminate {
        for ( Node v: nodes ) {
            v.setVisited( false );
        }
    }

    /**
     * logically unmarks all nodes on this layer
     */
    public void clearMarks() {
        for ( Node v: nodes ) {
            graph.unMark( v );
        }
    }

    /**
     * Sets node labels to be blank
     */
    public void clearLabels() throws Terminate {
        for ( Node v: nodes ) {
            v.setLabel("");
        }
    }

    /**
     * Highlights nodes on this layer and, if appropriate, incident edges --
     * see "enum Scope"
     */
    public void highlight( LayeredGraph.Scope scope ) throws Terminate {
        for ( Node v: nodes ) {
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
        for ( Node v: nodes ) {
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
        for ( Node v: nodes ) {
            v.setWeight( graph.getWeight( v ) );
        }
    }

    /**
     * Gives node weights a default value that makes them invisible
     */
    public void clearWeights() throws Terminate {
        for ( Node v: nodes ) {
            v.clearWeight();
        }
    }

    /**
     * @return the list of nodes on this layer
     */
    public List<Node> getNodes() {
        return nodes;
    }

    /**
     * inserts the node currently at position originalPosition so
     * node currently at newPosition, shifting the intervening nodes to the
     * right
     */
    public void insert( int originalPosition, int newPosition ) {
        Node toBeInserted = nodes.remove( originalPosition );
        nodes.add( newPosition, toBeInserted );
        updatePositions();
    }

    /**
     * sorts the nodes by their weight (as assigned by Galant code)
     */
    public void sort() {
        Collections.sort(nodes);
        updatePositions();
    }

    /**
     * sorts node by positions in their layers (as assigned by Galant)
     */
    final Comparator<Node> POSITION_COMPARATOR = new Comparator<Node>() {
        public int compare(Node x, Node y) {
            int state = GraphDispatch.getInstance().getDisplayState();
            //edited by 2021 Galant Team
            //add a cast to ensure x and y here are layeredGraphNode
            return ((LayeredGraphNode) x).getPositionInLayer(state) - ((LayeredGraphNode) y).getPositionInLayer(state);
        }
    };

    public void sortByPosition() {
        Collections.sort( nodes, POSITION_COMPARATOR );
        updatePositions();
    }

    /**
     * sorts nodes by their logical weight (for use with 'fast' versions of
     * barycenter-related algorithms)
     */
    final Comparator<Node> WEIGHT_COMPARATOR = new Comparator<Node>() {
        public int compare( Node x, Node y ) {
            double wx = graph.getWeight( x );
            double wy = graph.getWeight( y );
            if ( wx > wy ) return 1;
            if ( wx < wy ) return -1;
            return 0;
        }
    };

    public void sortByWeight() {
        Collections.sort( nodes, WEIGHT_COMPARATOR );
        updatePositions();
    }

    /**
     * sorts the nodes on this layer by increasing degree
     */
    public void sortByIncreasingDegree() {
        LayeredGraph.sortByIncreasingDegree( nodes );
        updatePositions();
    }

    /**
     * Puts nodes with largest (smallest) degree in the middle and puts
     * subsequent nodes farther toward the outside.
     *
     * @param largestInMiddle if true then the node with largest degree goes
     * in the middle.
     */
    public void middleDegreeSort( boolean largestMiddle ) {
       LayeredGraph.sortByIncreasingDegree( nodes );
       if ( largestMiddle ) Collections.reverse( nodes );
       ArrayList<Node> tempNodeList = new ArrayList<Node>();
       boolean addToFront = true;
       for ( Node node : nodes ) {
           if ( addToFront ) {
               tempNodeList.add( 0, node );
           }
           else {
               tempNodeList.add( tempNodeList.size(), node );
           }
           addToFront = ! addToFront;
       }
       nodes = tempNodeList;
       updatePositions();
    }

    /**
     * Uses the order in the list 'nodes' to update the positions of the
     * nodes in the graph.
     */
    public void updatePositions() {
        int i = 0;
        for ( Node v: nodes ) {
            // sets the position information in the layered graph
            graph.setPosition( v, i );
            i++;
        }
    }

    /**
     * Updates the display based on the order of the nodes on this layer;
     * assumes the only y-coordinate changes occurred for nodes whose
     * positions changed: see previewPositionChanges()
     */
    public void displayPositions() throws Terminate {
        int i = 0;
        for ( Node v: nodes ) {
        	//edited by 2021 Galant Team
            //add a cast to tell program this is really a LayeredGraphNode
        	LayeredGraphNode temp = (LayeredGraphNode) v;
            if ( temp.getPositionInLayer() != i ) {
                temp.setPositionInLayer( i );
            }
            i++;
        }
    }

    public void markPositionChanges() {
        int i = 0;
        for ( Node v: nodes ) {
        	//edited by 2021 Galant Team
            //add a cast to tell program this is really a LayeredGraphNode
        	LayeredGraphNode temp = (LayeredGraphNode) v;
            if ( temp.getPositionInLayer() != i ) {
                graph.mark( temp );
            }
            i++;
        }
    }

    /**
     * Saves positions of all the nodes
     */
    public void savePositions() {
        for ( Node v: nodes ) {
            graph.setSavedPosition( v, graph.getPosition( v ) );
        }
    }

    /**
     * Restores saved positions of all the nodes
     */
    public void restoreSavedPositions() {
        for (Node v: nodes ) {
            graph.setPosition( v, graph.getSavedPosition( v ) );
        }
    }

    /**
     * Updates the display based on previously saved positions
     */
    public void displaySavedPositions() throws Terminate {
        for ( Node v: nodes ) {
            int position = graph.getSavedPosition( v );
            //edited by 2021 Galant Team
            //add a cast to tell program this is really a LayeredGraphNode
        	LayeredGraphNode temp = (LayeredGraphNode) v;
            if ( temp.getPositionInLayer() != position ) {
                temp.setPositionInLayer( position );
            }
        }
    }

    /**
     * Puts node v at position i on the display (does nothing to its logical
     * position)
     */
    public void displayPosition( Node v, int i ) throws Terminate {
    	//edited by 2021 Galant Team
        //add a cast to tell program this is really a LayeredGraphNode
    	LayeredGraphNode temp = (LayeredGraphNode) v;
        if ( temp.getPositionInLayer() != i ) {
            temp.setPositionInLayer( i );
        }
    }

    public String toString() {
        String s = "";
        s += "[";
        for ( Node node : nodes ) {
        	//edited by 2021 Galant Team
            //add a cast to tell program this is really a LayeredGraphNode
        	LayeredGraphNode temp = (LayeredGraphNode) node;
            s += " " + temp.getId() + " " +
                "(" + temp.getLayer() + "," + temp.getPositionInLayer() + "),";
        }
        s += " ]";
        return s;
    }

} // end, class Layer

//  [Last modified: 2021 01 31 at 14:28:34 GMT]
