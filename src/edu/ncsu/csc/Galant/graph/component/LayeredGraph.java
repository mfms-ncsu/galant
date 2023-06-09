/**
 * @file LayeredGraph.java
 * Can be used as a basis for Galant implementations of a variety of crossing
 * minimization algorithms.
 *
 * It is assumed (for now) that all edges are directed from nodes on layer i
 * to ones on layer i+1 for some i.
 *
 * @todo Come up with a way, perhaps via a compile-time option, to run the
 * heuristics decoupled from Galant displays and GUI's, so that their
 * performance can be evaluated before animations are undertaken. This may
 * also be useful for Galant in general.
 *
 * @todo A lot of this code runs "in parallel" to the built-in implementation
 *       of Layer, LayerInformation, and LayeredGraphNode.
 *       The latter are created during parsing and accessed during display.
 *       However the code in here appears to create a separate LayeredGraph entity,
 *       with arrays to keep track of layers, nodes, and positions.
 *       It would make far more sense to use the attributes of the nodes
 *       directly and take advantage of recent sorting utilities.
 *       This will be a slow transition process, fraught with peril.
 * 
 * @todo There appears to be a bug in adjust_weights. The weight of a node
 * with no neighbors is based on an already assigned weight of the node to
 * the left. Consider iteration 7 with mod_bary on the graph
 * r_100_110_10_1_1_1.graphml: node 77 should have a weight of 10, based on
 * its right neighbor -- its left neighor, 76, has no weight; however, it is
 * given a weight of 10.5, based on both neighbors. This is correct for
 * sequential algorithms (such as mod_bary), so a distinction needs to be
 * made via, for example, a second argument. 
 */

package edu.ncsu.csc.Galant.graph.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.ncsu.csc.Galant.algorithm.Terminate;
import edu.ncsu.csc.Galant.GalantException;
import edu.ncsu.csc.Galant.logging.LogHelper;

/**
 * The class LayeredGraph is used as a basis for Galant implementations of a
 * variety of crossing minimization algorithms. It provides several
 * functionalities:
 * <ol>
 *
 * <li>
 * Methods that translate layers and positions within layers into
 * y-coordinates and x-coordinates, respectively. These coordinates are
 * scaled with respect to the window dimensions: layers are evenly spaced
 * with padding at the top and bottom equal to the interlayer distance. The
 * same scaling principle is applied to each layer.
 *
 * <li>
 * Methods that supply infrastructure for crossing minimization algorithms
 * such as numberOfCrossings, assignWeights and sortByWeight (for barycenter
 * and related heuristics).
 *
 * <li>Methods that separate logical attributes from display attributes. For
 * example, dynamic versions of sifting and barycenter (logically) mark
 * nodes/layers to indicate that they will not be chosen again during the
 * current pass; at various points in the animation it is desirable to
 * display the logical marks in some way; methods such as mark(), unMark(),
 * and displayMarks() accomplish this functionality.
 *
 * <li>Methods that facilitate the highlighting of groups of nodes and edges
 * in the specific context of (layered) crossing minimization algorithms;
 * these include highlight() -- with arguments speficifying a layer and the
 * direction of the edges to be highlighted, unhighlight(),
 *
 * </ol>
 *
 * It is assumed that all edges are directed from nodes on layer i
 * to ones on layer i+1 for some i.
 *
 * Also assumed is that node id's range from 0 to n-1, where n is the number
 * of nodes, and edge id's are in the range 0 to m-1, where m is the number
 * of edges.
 */

public class LayeredGraph extends Graph {

    /**
     * These constants are used to determine both highlighting and assignment
     * of weights to nodes on a layer.
     * LAYER = highlight only the nodes, assign weights based on node positions;
     * UP = highlight nodes and outgoing edges, assign weights based on
     * average position of outgoing neighbors;
     * DOWN = highlight nodes and incoming edges, assign weights based on
     * average position of incoming neighbors;
     * BOTH = highlight nodes and both incoming and outgoing edges, assign
     * weights based on average position of incoming neighbors + average
     * position of outgoing neighbors.
     */
    public enum Scope {
        LAYER, UP, DOWN, BOTH
    };

    private LayerInformation layerInformation;

    private ArrayList<Layer> layers;
    private int[] savedPositionOfNode;

    /**
     * weightOfNode and isMarked are designed to avoid graph state changes
     * during fast versions of barycenter and friends; the weight; default
     * value for weight is the position
     */
    private double[] weightOfNode;
    private boolean[] isMarked;

    /** typically displayed as edge weight */
    private int[] crossingsOfEdge;

    /**
     * Creates a new instance based on node positions in the graph.
     */
    public LayeredGraph() {
        super();
        layers = new ArrayList<Layer>();

        // // record layer and position information for all the nodes
        // for ( LayeredGraphNode u : this.getNodes() ) {
        // // edited by 2021 Galant Team
        // // add a cast to tell program this is really a LayeredGraphNode
        // LayeredGraphNode temp = (LayeredGraphNode) u;
        // int layer = temp.getLayer();
        // int position = temp.getPositionInLayer();
        // addNode(temp, layer, position);
        // positionOfNode[temp.getId()] = position;
        // }

        /*
         * @todo
         * !!! the code below causes null pointer exceptions when nodes are not
         * contiguous. !!!
         * Also true of any other code that sorts by position: for example, the reset
         * method used
         * in barycenter.alg
         * 
         * The source of the problem is ensurePosition() in Layer.java,
         * which adds null nodes in gaps when nodes are not contiguous.
         * As with the LG-Local code, we need to distinguish between the position of a
         * node
         * and its index within the layer.
         */

        /**
         * // sort the nodes on each layer by their position and then set the
         * // horizontal gap between them based on window width
         * for ( Layer theLayer : layers ) {
         * theLayer.sortByPosition();
         * }
         * 
         * // initialize edge crossing counts
         * for ( int layer = 0; layer < layers.size() - 1; layer++ ) {
         * updateCrossingsInChannel(layer);
         * }
         */
    } // constructor ends

    /**
     * adds new empty layers numbered layers.size(), ... , layer_number; can be
     * called even if not needed - it does nothing, safely, in that case
     */
    void ensureLayer(int layerNumber) {
        for ( int layer = layers.size(); layer <= layerNumber; layer++ ) {
            layers.add(new Layer(this));
        }
    }

    /**
     * Adds a node to the graph during parsing. The node has already been
     * created.
     */
    public void addNode(final LayeredGraphNode n) {
        LogHelper.enterMethod(getClass(), "addNode: node = " + n);

        super.addNode(n);
        layerInformation.addNode(n);

        LogHelper.exitMethod(getClass(), "addNode( Node )");
    }

    /**
     * @return a list containing the nodes on the i-th layer.
     */
    public List<LayeredGraphNode> getLayer(int layer) {
        return layers.get(layer).getNodesInLayer();
    }

    /**
     * @return the number of nodes on the layer.
     */
    public int getLayerSize(int layer) {
        return getLayer(layer).size();
    }

    /**
     * @return the number of layers
     */
    public int numberOfLayers() {
        return layers.size();
    }

    public int numberOfNodesOnLayer(int layer) {
        return layerInformation.maxPositionInLayer.get(layer);
    }

    public int maxPositionInAnyLayer() {
        return layerInformation.maxPosition;
    }

    /**
     * @return true if verticality matters, i.e., there are layers whose number of
     *         nodes
     *         does not correspond to the maximum
     *         in that case the graph is displayed by taking positions "literally"
     *         instead of centering each layer
     */
    public boolean isVertical() {
        return layerInformation.vertical;
    }

    public void initializeAfterParsing() throws GalantException {
        System.out.println("Layered graph initialization");
        super.initializeAfterParsing();
        savedPositionOfNode = new int[this.nodeIds()];
        weightOfNode = new double[this.nodeIds()];
        isMarked = new boolean[this.nodeIds()];
        crossingsOfEdge = new int[this.edgeIds()];
    }

    public String toString() {
        String s = "";
        for ( int layerNumber = 0; layerNumber < layers.size(); layerNumber++ ) {
            s += "Layer " + layerNumber + " " + layers.get(layerNumber) + "\n";
        }
        return s;
    }

    /********************************************************************/
    // Algorithm-related code begins here
    /********************************************************************/

    /**
     * Displays the node v (as if it were) at position i on its layer. The
     * logical position of node v is not changed.
     */
    public void displayPosition(LayeredGraphNode v, int i) throws Terminate {
        layers.get(v.getLayer()).displayPosition(v, i);
    }

    /**
     * Updates the display to reflect the logical position information of
     * nodes in layer i.
     */
    public void displayPositions(int i) throws Terminate {
        layers.get(i).displayPositions();
    }

    /**
     * Updates the display to reflect the logical position information of
     * all nodes.
     */
    public void displayPositions() throws Terminate {
        for ( int layer = 0; layer < numberOfLayers(); layer++ ) {
            displayPositions(layer);
        }
    }

    /**
     * Changes positions of nodes based on an insertion of a single node
     * either before or after another.
     *
     * @param layer
     *            the layer on which the insertion takes place
     * @param nodeLocation
     *            index of node to be inserted
     * @param insertLocation
     *            index of the new position of the node
     */
    public void insert(int layer, int nodeLocation, int insertLocation) throws Terminate {
        layers.get(layer).insert(nodeLocation, insertLocation);
        // update edges crossings in the relevant channels - should probably
        // be done separately
        if ( layer > 0 ) {
            updateCrossingsInChannel(layer - 1);
        }
        if ( layer < layers.size() - 1 ) {
            updateCrossingsInChannel(layer);
        }
    }

    /**
     * marks nodes on layer i whose positions are about to change
     */
    public void markPositionChanges(int i) {
        layers.get(i).markPositionChanges();
    }

    /**
     * Saves the current (logical) positions for later restoration; useful
     * for remembering positions that minimize crossings.
     */
    public void savePositions() {
        for ( Layer layer : layers ) {
            layer.savePositions();
        }
    }

    /**
     * Restores the previously saved positions.
     */
    public void restoreSavedPositions() throws Terminate {
        for ( Layer layer : layers ) {
            layer.restoreSavedPositions();
        }
    }

    /**
     * Displays the previously saved positions.
     */
    public void displaySavedPositions() throws Terminate {
        for ( Layer layer : layers ) {
            layer.displaySavedPositions();
        }
    }

    /**
     * @return the saved position of node v
     */
    public int getSavedPosition(LayeredGraphNode v) {
        return savedPositionOfNode[v.getId()];
    }

    /**
     * Sets the saved position of node v.
     */
    public void setSavedPosition(LayeredGraphNode v, int positionInLayer) {
        savedPositionOfNode[v.getId()] = positionInLayer;
    }

    /**
     * @return the node on the given layer at the given position
     */
    public LayeredGraphNode getNodeAt(int layerNumber, int position) {
        Layer layer = layers.get(layerNumber);
        return layer.getNodeAt(position);
    }

    /**
     * @return the node to the left of v on the same layer as v
     */
    public LayeredGraphNode getNodeToTheLeft(LayeredGraphNode v) {
        int layer = v.getLayer();
        int index = v.getIndexInLayer();
        if ( index > 0 ) {
            return getNodeAt(layer, index - 1);
        }
        return null;
    }

    /**
     * @return the node to the right of v on the same layer as v
     */
    public LayeredGraphNode getNodeToTheRight(LayeredGraphNode v) {
        int layer = v.getLayer();
        int position = v.getIndexInLayer();
        if ( position < getLayerSize(layer) - 1 ) {
            return getNodeAt(layer, position + 1);
        }
        return null;
    }

    /**
     * Sets the logical weight of node v to the given weight without changing
     * the display.
     */
    public void setWeight(LayeredGraphNode v, double weight) {
        weightOfNode[v.getId()] = weight;
    }

    /**
     * @return the logical weight of node v.
     */
    public double getWeight(LayeredGraphNode v) {
        return weightOfNode[v.getId()];
    }

    private double getUpperAverage(LayeredGraphNode v) {
        // a -1 signals that there are no nodes on the layer above/below
        // on which to base a weight; in these cases, a final adjustment
        // bases the weights on those of the left and right neighbors
        double upperAverage = - 1;
        int outdegree = v.getOutdegree();
        int sumOfPositions = 0;
        for ( Edge e : v.getOutgoingEdges() ) {
            LayeredGraphNode w = (LayeredGraphNode) v.travel(e);
            sumOfPositions += w.getPositionInLayer();
        }
        if ( outdegree > 0 ) {
            upperAverage = ((double) sumOfPositions) / outdegree;
        }
        return upperAverage;
    }

    private double getLowerAverage(LayeredGraphNode v) {
        // a -1 signals that there are no nodes on the layer above/below
        // on which to base a weight; in these cases, a final adjustment
        // bases the weights on those of the left and right neighbors
        double lowerAverage = - 1;
        int indegree = v.getIndegree();
        int sumOfPositions = 0;
        for ( Edge e : v.getIncomingEdges() ) {
            LayeredGraphNode w = (LayeredGraphNode) v.travel(e);
            sumOfPositions += w.getPositionInLayer();
        }
        if ( indegree > 0 ) {
            lowerAverage = ((double) sumOfPositions) / indegree;
        }
        return lowerAverage;
    }

    /**
     * Assign a weight based on the two neighbors for each node that does
     * not have a well-defined weight (indegree or outdegree 0)
     *
     * @param nodeList
     *            the list of nodes on this layer in the current left to
     *            right order
     * @param weightArray
     *            the weights of the corresponding nodes (left to
     *            right) with a -1 for each node that has no weight
     */
    private void adjustWeights(List<LayeredGraphNode> nodeList, double[] weightArray) {
        int length = nodeList.size();
        for ( int i = 0; i < length; i++ ) {
            // do nothing if this node already has a weight
            if ( weightArray[i] >= 0 )
                continue;
            // if both neighbors are present and have weights, take the
            // average of their weights
            if ( i > 0 && i < length - 1
                    && weightArray[i - 1] != - 1 && weightArray[i + 1] != - 1 ) {
                weightArray[i] = (weightArray[i - 1] + weightArray[i + 1]) / 2;
            } else if ( i > 0 && weightArray[i - 1] != - 1 ) {
                // only the left neighbor has a weight
                weightArray[i] = weightArray[i - 1];
            } else if ( i < length - 1 && weightArray[i + 1] != - 1 ) {
                // only the right neighbor has a weight
                weightArray[i] = weightArray[i + 1];
            } else {
                // neither neighbor has a weight; occurs in sequential
                // implementations only if the leftmost node has a right
                // neighbor with no weight; should fix this if simulating
                // parallel algorithms
                weightArray[i] = 0;
            }
        }
    }

    /**
     * Assigns logical weights to nodes on the given layer based on average
     * position of adjacent nodes on neighboring layer(s).
     *
     * @param scope
     *            If this is UP, sorting is based on the higher numbered
     *            layer, if it's DOWN it's based on the lower numbered layer, if
     *            it's
     *            BOTH it will be the average of the two.
     */
    public void assignWeights(int layer, Scope scope) {
        List<LayeredGraphNode> nodeList = layers.get(layer).getNodesInLayer();
        double[] upperWeightArray = new double[nodeList.size()];
        double[] lowerWeightArray = new double[nodeList.size()];
        if ( scope == Scope.UP ) {
            int i = 0;
            for ( LayeredGraphNode v : nodeList ) {
                upperWeightArray[i++] = getUpperAverage(v);
            }
            adjustWeights(nodeList, upperWeightArray);
        } else if ( scope == Scope.DOWN ) {
            int i = 0;
            for ( LayeredGraphNode v : nodeList ) {
                lowerWeightArray[i++] = getLowerAverage(v);
            }
            adjustWeights(nodeList, lowerWeightArray);
        } else { // scope is BOTH
            int i = 0;
            for ( LayeredGraphNode v : nodeList ) {
                double upper = getUpperAverage(v);
                double lower = getLowerAverage(v);
                upperWeightArray[i] = upper >= 0 ? upper : 0;
                lowerWeightArray[i] = lower >= 0 ? lower : 0;
                i++;
            }
        }
        int i = 0;
        for ( LayeredGraphNode v : nodeList ) {
            if ( scope == Scope.DOWN )
                weightOfNode[v.getId()] = lowerWeightArray[i];
            else if ( scope == Scope.UP )
                weightOfNode[v.getId()] = upperWeightArray[i];
            else {
                weightOfNode[v.getId()] = (upperWeightArray[i] + lowerWeightArray[i]) / 2;
            }
            i++;
        }
    }

    /**
     * Sorts nodes on the given layer by their logical weights.
     */
    public void sortByWeight(int layer) throws Terminate {
        layers.get(layer).sortByWeight();
    }

    /**
     * Sets weights of all nodes on layer i to 0
     */
    public void resetNodeWeights(int i) throws Terminate {
        for ( LayeredGraphNode v : getLayer(i) ) {
            v.setWeight(0.0);
        }
    }

    /**
     * Sets all node weights to 0.
     */
    public void resetNodeWeights() throws Terminate {
        for ( int layer = 0; layer < numberOfLayers(); layer++ ) {
            resetNodeWeights(layer);
        }
    }

    /**
     * Gives nodes on layer i default weights that make them invisible
     */
    public void clearNodeWeights(int i) throws Terminate {
        layers.get(i).clearWeights();
    }

    /**
     * Sets weights of all nodes on layer i to their positions.
     */
    public void setWeightsToPositions(int i) {
        for ( LayeredGraphNode v : getLayer(i) ) {
            weightOfNode[v.getId()] = v.getPositionInLayer();
        }
    }

    /**
     * Sets the weights of all nodes to be their positions.
     */
    public void setWeightsToPositions() {
        for ( int layer = 0; layer < numberOfLayers(); layer++ ) {
            setWeightsToPositions(layer);
        }
    }

    /**
     * Displays the weights of nodes on the given layer.
     */
    public void displayWeights(int layer) throws Terminate {
        layers.get(layer).displayWeights();
    }

    /**
     * Displays the weights of all nodes.
     */
    public void displayWeights() throws Terminate {
        for ( int layer = 0; layer < numberOfLayers(); layer++ ) {
            displayWeights(layer);
        }
    }

    /**
     * Sorts the list of nodes on layer i by their <em>displayed</em> weights.
     */
    public void sort(int i) throws Terminate {
        layers.get(i).sort();
    }

    /**
     * Makes all node labels on layer i blank
     */
    public void clearNodeLabels(int i) throws Terminate {
        layers.get(i).clearLabels();
    }

    /**
     * Logically marks node v.
     */
    public void mark(LayeredGraphNode v) {
        isMarked[v.getId()] = true;
    }

    /**
     * Logically undoes any logical mark on node v.
     */
    public void unMark(LayeredGraphNode v) {
        isMarked[v.getId()] = false;
    }

    /**
     * @return true if node v is marked.
     */
    public boolean isMarked(LayeredGraphNode v) {
        return isMarked[v.getId()];
    }

    /**
     * Removes the logical marks from all nodes on layer i.
     */
    public void clearMarks(int i) {
        layers.get(i).clearMarks();
    }

    /**
     * Displays all the current logical marks on layer i.
     */
    public void displayMarks(int i) throws Terminate {
        layers.get(i).displayMarks();
    }

    /**
     * Removes all displayed marks from layer i without affecting their
     * logical status.
     */
    public void removeMarks(int i) throws Terminate {
        layers.get(i).removeMarks();
    }

    /**
     * Logically marks layer i: used in the animation of a dynamic version of
     * the barycenter heuristic.
     */
    public void markLayer(int i) {
        layers.get(i).mark();
    }

    /**
     * Logically undoes the marking of layer i: used in the animation of a
     * dynamic version of the barycenter heuristic.
     */
    public void unMarkLayer(int i) {
        layers.get(i).unMark();
    }

    /**
     * Logically undoes the marking of all layers: used in the animation of a
     * dynamic version of the barycenter heuristic.
     */
    public void clearLayerMarks() {
        for ( int i = 0; i < layers.size(); i++ ) {
            layers.get(i).unMark();
        }
    }

    /**
     * Displays the marked layers: all of the nodes on each marked layer are
     * shown as marked.
     */
    public void displayLayerMarks() throws Terminate {
        for ( int i = 0; i < layers.size(); i++ ) {
            layers.get(i).displayMarks();
        }
    }

    /**
     * highlights the edges incident on node v.
     */
    public void highlightEdges(LayeredGraphNode v) throws Terminate {
        for ( Edge e : v.getIncidentEdges() ) {
            e.setSelected(true);
        }
    }

    /**
     * Undoes highlighting of the edges incident on node v.
     */
    public void unHighlightEdges(LayeredGraphNode v) throws Terminate {
        for ( Edge e : v.getIncidentEdges() ) {
            e.setSelected(false);
        }
    }

    /**
     * Highlights nodes on layer i.
     * 
     * @param scope
     *            determines which incident edges, if any, should be
     *            highlighted: if scope is UP, the edges pointing to the
     *            higher-numbered
     *            layers are highlighted; if DOWN, those to the lower-numbered
     *            layer; if
     *            both, all incident edges; if LAYER, the nodes only.
     */
    public void highlight(int i, Scope scope) throws Terminate {
        layers.get(i).highlight(scope);
    }

    /**
     * Undoes any highlighting of nodes on layer i and their incident edges.
     */
    public void unHighlight(int i) throws Terminate {
        layers.get(i).unHighlight();
    }

    /**
     * Undoes highlighting for all nodes and edges.
     */
    public void unHighlight() throws Terminate {
        for ( int layer = 0; layer < numberOfLayers(); layer++ ) {
            unHighlight(layer);
        }
    }

    /**
     * Highlights the nodes between the two given positions, inclusive; used,
     * for example, to highlight an insertion.
     */
    public void highlightNodes(int layer, int positionOne, int positionTwo)
            throws Terminate {
        layers.get(layer).highlightNodes(positionOne, positionTwo);
    }

    /**
     * @return total number of crossings based on current logical positions.
     */
    public int numberOfCrossings() {
        int crossings = 0;
        for ( int layer = 0; layer < numberOfLayers() - 1; layer++ ) {
            crossings += crossingsBetweenLayers(layer);
        }
        return crossings;
    }

    /**
     * @return the number of edges that cross edge e, based on current
     *         logical positions.
     */
    public int getCrossings(Edge e) {
        return crossingsOfEdge[e.getId()];
    }

    /**
     * @return the number of crossings that arise among the edges incident to
     *         and y if x is to the left of y. Assumes that both are on the same
     *         layer.
     */
    public int getCrossings(LayeredGraphNode leftNode, LayeredGraphNode rightNode) {
        int layer = leftNode.getLayer();
        int crossings = 0;
        // compute crossings on upward edges (if any)
        if ( layer < numberOfLayers() - 1 ) {
            List<Edge> upEdges = createUpEdgeArray(leftNode, rightNode);
            List<Integer> destination_positions = getDestinationPositions(upEdges);
            crossings += countInversions(destination_positions);
        }

        // compute crossings on downward edges (if any)
        if ( layer > 0 ) {
            List<Edge> upEdges = createDownEdgeArray(leftNode, rightNode);
            List<Integer> source_positions = getSourcePositions(upEdges);
            crossings += countInversions(source_positions);
        }
        return crossings;
    }

    /**
     * @return number of crossings among edges between layer and layer + 1.
     *         Uses O(|E|+|C|) algorithm from "Simple and efficient bilayer cross
     *         counting," W. Barth, M. Juenger, P. Mutzel, in JGAA, 2004.
     */
    public int crossingsBetweenLayers(int layer) {
        List<LayeredGraphNode> sourceNodes = layers.get(layer).getNodesInLayer();
        // create an array of the positions of nodes at the other ends of
        // outgoing edges from the source; for each source node, sort these
        // by position
        ArrayList<Integer> targetPositions = new ArrayList<Integer>();
        for ( LayeredGraphNode v : sourceNodes ) {
            ArrayList<Integer> localTargetPositions = new ArrayList<Integer>();
            for ( Edge e : v.getOutgoingEdges() ) {
                LayeredGraphNode w = (LayeredGraphNode) v.travel(e);
                localTargetPositions.add(w.getPositionInLayer());
            }
            Collections.sort(localTargetPositions);
            targetPositions.addAll(localTargetPositions);
        }
        int inversions = countInversions(targetPositions);
        return inversions;
    }

    /**
     * @return the number of crossings for the edges incident on nodes of the
     *         given layer.
     */
    int getLayerCrossings(int layer) {
        int crossings = 0;
        for ( LayeredGraphNode v : getLayer(layer) ) {
            for ( Edge e : v.getIncidentEdges() ) {
                crossings += crossingsOfEdge[e.getId()];
            }
        }
        return crossings;
    }

    /**
     * @return the index of the (logically) unmarked layer with the maximum
     *         number of crossings or -1 if all layers are marked.
     */
    public int getMaxCrossingsLayer() {
        updateEdgeCrossings();
        int maxCrossings = - 1;
        int maxLayer = - 1;
        for ( int i = 0; i < layers.size(); i++ ) {
            if ( layers.get(i).isMarked() )
                continue;
            int currentCrossings = getLayerCrossings(i);
            if ( currentCrossings > maxCrossings ) {
                maxCrossings = currentCrossings;
                maxLayer = i;
            }
        }
        return maxLayer;
    }

    /**
     * @return the maximum, over all edges e of the graph, of the number of
     *         edges crossing e
     */
    public int getMaxEdgeCrossings() {
        int maxCrossings = Integer.MIN_VALUE;
        for ( Edge e : this.getEdges() ) {
            if ( crossingsOfEdge[e.getId()] > maxCrossings ) {
                maxCrossings = crossingsOfEdge[e.getId()];
            }
        }
        return maxCrossings;
    }

    /**
     * @return the edge with the most crossings among those that still have
     *         one (logically) unmarked endpoint or null if all edges have both
     *         endpoints marked.
     */
    public Edge getMaxCrossingsEdge() {
        Edge maxEdge = null;
        int maxCrossings = Integer.MIN_VALUE;
        for ( Edge e : this.getEdges() ) {
            if ( (! isMarked[e.getSourceNode().getId()]
                    || ! isMarked[e.getTargetNode().getId()])
                    && crossingsOfEdge[e.getId()] > maxCrossings ) {
                maxEdge = e;
                maxCrossings = crossingsOfEdge[e.getId()];
            }
        }
        return maxEdge;
    }

    static int indexOfLastReturned = 0;

    /**
     * @return unmarked edge with the most crossings, as with
     *         getMaxCrossingsEdge().
     * @param roundRobin
     *            if true, start the search one index beyond that of
     *            the last edge returned <em>by this method</em>
     */
    public Edge getMaxCrossingsEdge(boolean roundRobin) {
        if ( ! roundRobin )
            return getMaxCrossingsEdge();
        int maxEdgeIndex = - 1;
        Edge maxEdge = null;
        int maxCrossings = Integer.MIN_VALUE;
        List<Edge> edgeList = this.getEdges();
        int i = indexOfLastReturned + 1;
        i = i % edgeList.size();
        while ( i != indexOfLastReturned ) {
            Edge e = edgeList.get(i);
            if ( (! isMarked[e.getSourceNode().getId()]
                    || ! isMarked[e.getTargetNode().getId()])
                    && crossingsOfEdge[e.getId()] > maxCrossings ) {
                maxEdgeIndex = i;
                maxEdge = e;
                maxCrossings = crossingsOfEdge[e.getId()];
            }
            i = (i + 1) % edgeList.size();
        }
        if ( maxEdgeIndex >= 0 ) {
            indexOfLastReturned = maxEdgeIndex;
        }
        return maxEdge;
    }

    public static final Comparator<LayeredGraphNode> DEGREE_COMPARATOR = new Comparator<LayeredGraphNode>() {
        public int compare(LayeredGraphNode x, LayeredGraphNode y) {
            return x.getDegree()
                    - y.getDegree();
        }
    };

    /**
     * Sorts nodes by their degree (for use in sifting).
     */
    public static void sortByIncreasingDegree(List<LayeredGraphNode> nodes) {
        Collections.sort(nodes, DEGREE_COMPARATOR);
    }

    /**
     * Sorts nodes on each layer by increasing degree -- for use in
     * middleDegreeSort() and its reversed version
     */
    public void sortLayersByIncreasingDegree() throws Terminate {
        for ( Layer layer : layers ) {
            layer.sortByIncreasingDegree();
        }
    }

    /**
     * On each layer: puts nodes with largest (smallest) degree in the middle
     * and puts subsequent nodes farther toward the outside.
     *
     * @param largestInMiddle
     *            if true then the node with largest degree goes
     *            in the middle.
     */
    public void middleDegreeSort(boolean largestInMiddle) throws Terminate {
        for ( Layer layer : layers ) {
            layer.middleDegreeSort(largestInMiddle);
        }
    }

    /**
     * Updates crossings for edges when two edges form an inversion. Used by
     * the maximum crossings edge heuristic reported by Stallmann in JEA (2012).
     *
     * @param diff
     *            indicates whether to increment the number of crossings for
     *            the edges (+1) or decrement them (-1)
     */
    void updateEdgeCrossings(Edge e, Edge f, int diff) {
        crossingsOfEdge[e.getId()] += diff;
        crossingsOfEdge[f.getId()] += diff;
    }

    /**
     * Updates the number of crossings for each edge in edgeList based on the
     * inversions in the positions of the heads of the edges. Used by
     * the maximum crossings edge heuristic reported by Stallmann in JEA (2012).
     *
     * @param diff
     *            indicates whether to increment the crossing counts (+1) or
     *            decrement them (-1) each time there is an inversion
     */
    void updateUpperEdgeCrossings(List<Edge> edgeList, int diff) {
        // use insertion sort
        for ( int i = 1; i < edgeList.size(); i++ ) {
            Edge toBeInserted = edgeList.get(i);
            LayeredGraphNode thisTarget = (LayeredGraphNode) toBeInserted.getTargetNode();
            int j = i - 1;
            while ( j >= 0 ) {
                LayeredGraphNode otherTarget = (LayeredGraphNode) edgeList.get(j)
                        .getTargetNode();
                if ( otherTarget.getPositionInLayer() <= thisTarget.getPositionInLayer() )
                    break;
                updateEdgeCrossings(edgeList.get(j), toBeInserted, diff);
                edgeList.set(j + 1, edgeList.get(j));
                j--;
            }
            edgeList.set(j + 1, toBeInserted);
        }
    }

    /**
     * Updates the number of crossings for each edge in edgeList based on the
     * inversions in the positions of the tails of the edges. Used by the
     * maximum crossings edge heuristic reported by Stallmann in JEA (2012).
     *
     * @param diff
     *            indicates whether to increment the crossing counts (+1) or
     *            decrement them (-1) each time there is an inversion
     */
    void updateLowerEdgeCrossings(List<Edge> edgeList, int diff) {
        // use insertion sort
        for ( int i = 1; i < edgeList.size(); i++ ) {
            Edge toBeInserted = edgeList.get(i);
            LayeredGraphNode thisTarget = (LayeredGraphNode) toBeInserted.getSourceNode();
            int j = i - 1;
            while ( j >= 0 ) {
                LayeredGraphNode otherTarget = (LayeredGraphNode) edgeList.get(j)
                        .getSourceNode();
                if ( otherTarget.getPositionInLayer() <= thisTarget.getPositionInLayer() )
                    break;
                updateEdgeCrossings(edgeList.get(j), toBeInserted, diff);
                edgeList.set(j + 1, edgeList.get(j));
                j--;
            }
            edgeList.set(j + 1, toBeInserted);
        }
    }

    /**
     * @return an ArrayList of the outgoing edges of the two nodes with those
     *         from leftNode appearing before those from rightNode
     */
    List<Edge> createUpEdgeArray(LayeredGraphNode leftNode, LayeredGraphNode rightNode) {
        ArrayList<Edge> outgoingEdges = new ArrayList<Edge>();
        List<Edge> leftNodeEdges = leftNode.getOutgoingEdges();
        List<Edge> rightNodeEdges = rightNode.getOutgoingEdges();
        sortByTargetPosition(leftNodeEdges);
        sortByTargetPosition(rightNodeEdges);
        outgoingEdges.addAll(leftNodeEdges);
        outgoingEdges.addAll(rightNodeEdges);
        return outgoingEdges;
    }

    /**
     * @return an ArrayList of the outgoing edges of the two nodes with those
     *         from leftNode appearing before those from rightNode
     */
    List<Edge> createDownEdgeArray(LayeredGraphNode leftNode,
            LayeredGraphNode rightNode) {
        ArrayList<Edge> incomingEdges = new ArrayList<Edge>();
        List<Edge> leftNodeEdges = leftNode.getIncomingEdges();
        List<Edge> rightNodeEdges = rightNode.getIncomingEdges();
        sortBySourcePosition(leftNodeEdges);
        sortBySourcePosition(rightNodeEdges);
        incomingEdges.addAll(leftNodeEdges);
        incomingEdges.addAll(rightNodeEdges);
        return incomingEdges;
    }

    /**
     * Change counts based on crossings when left_node appears to the left and
     * right node to the right.
     * 
     * @param diff
     *            +1 to increase crossing counts, -1 to decrease
     *            - used by mce heuristic only
     */
    void change_crossings(LayeredGraphNode leftNode, LayeredGraphNode rightNode,
            int diff) {
        int layer = leftNode.getLayer();
        int numberOfLayers = layers.size();
        // update crossings on upward edges (if any)
        if ( layer < numberOfLayers - 1 ) {
            List<Edge> upEdges = createUpEdgeArray(leftNode, rightNode);
            updateUpperEdgeCrossings(upEdges, diff);
        }

        // update crossings on downward edges (if any)
        if ( layer > 0 ) {
            List<Edge> downEdges = createDownEdgeArray(leftNode, rightNode);
            updateLowerEdgeCrossings(downEdges, diff);
        }
    }

    /**
     * Swaps nodes x and y and updates the edge crossings for their incident
     * edges (the only ones that should be affected). Assumes that x and y
     * are on the same layer and that x is initially to the left of y. Used by
     * the maximum crossings edge heuristic reported by Stallmann in JEA (2012).
     *
     * @return the number of crossings for the edge that has the most
     *         crossings after the swap (among those involved in the swap).
     */
    public int bottleneckSwap(LayeredGraphNode left_node, LayeredGraphNode right_node) {
        change_crossings(left_node, right_node, - 1);
        change_crossings(right_node, left_node, + 1);
        // now find the maximum number of crossings among the edges
        // incident on the two nodes.
        List<Edge> incidentEdges = left_node.getIncidentEdges();
        incidentEdges.addAll(right_node.getIncidentEdges());
        int maxCrossings = Integer.MIN_VALUE;
        for ( Edge e : incidentEdges ) {
            int id = e.getId();
            if ( crossingsOfEdge[id] > maxCrossings ) {
                maxCrossings = crossingsOfEdge[id];
            }
        }
        incidentEdges = null; // for gc
        return maxCrossings;
    }

    /**
     * Updates the crossings for each edge in a channel.
     *
     * @param sourceLayer
     *            the layer containing the source nodes for all the
     *            edges whose number of crossings will be updated.
     */
    void updateCrossingsInChannel(int sourceLayer) {
        ArrayList<Edge> channelEdges = new ArrayList<Edge>();
        for ( LayeredGraphNode v : getLayer(sourceLayer) ) {
            List<Edge> outgoingEdges = v.getOutgoingEdges();
            sortByTargetPosition(outgoingEdges);
            for ( Edge e : outgoingEdges ) {
                crossingsOfEdge[e.getId()] = 0;
                insertAndUpdateCrossings(e, channelEdges);
            }
            outgoingEdges = null; // for gc
        }
        channelEdges = null; // for gc
    }

    /**
     * Updates all edge crossings
     */
    public void updateEdgeCrossings() {
        for ( int i = 0; i < layers.size() - 1; i++ ) {
            updateCrossingsInChannel(i);
        }
    }

    /**
     * Sorts the edges in the list by their destination positions
     */
    void sortByTargetPosition(List<Edge> edgeList) {
        for ( int i = 1; i < edgeList.size(); i++ ) {
            Edge e = edgeList.remove(i);
            LayeredGraphNode thisTarget = (LayeredGraphNode) e.getTargetNode();
            int j = i - 1;
            int targetPosition = thisTarget.getPositionInLayer();
            while ( j >= 0 ) {
                LayeredGraphNode otherTarget = (LayeredGraphNode) edgeList.get(j)
                        .getTargetNode();
                if ( targetPosition >= otherTarget.getPositionInLayer() )
                    break;
                j--;
            }
            edgeList.add(j + 1, e);
        }
    }

    /**
     * Sorts the edges in the list by their source positions
     *
     * @todo this should go away when focus is on channels
     */
    void sortBySourcePosition(List<Edge> edgeList) {
        for ( int i = 1; i < edgeList.size(); i++ ) {
            Edge e = edgeList.remove(i);
            LayeredGraphNode thisTarget = (LayeredGraphNode) e.getSourceNode();
            int j = i - 1;
            int targetPosition = thisTarget.getPositionInLayer();
            while ( j >= 0 ) {
                LayeredGraphNode otherTarget = (LayeredGraphNode) edgeList.get(j)
                        .getSourceNode();
                if ( targetPosition >= otherTarget.getPositionInLayer() )
                    break;
                j--;
            }
            edgeList.add(j + 1, e);
        }
    }

    /**
     * Inserts an edge into a list of edges, incrementing the crossing count
     * for every inversion
     *
     * @param e
     *            the edge to be inserted, presumed to have crossing count of 0
     * @param edgeList
     *            a list of edges sorted by the positions of their
     *            destination nodes; the sorted order is maintained after the
     *            insertion
     */
    void insertAndUpdateCrossings(Edge e, List<Edge> edgeList) {
        int index = edgeList.size() - 1;
        LayeredGraphNode thisTarget = (LayeredGraphNode) e.getTargetNode();
        int targetPosition = thisTarget.getPositionInLayer();
        while ( index >= 0 ) {
            LayeredGraphNode otherTarget = (LayeredGraphNode) edgeList.get(index)
                    .getTargetNode();
            if ( targetPosition >= otherTarget.getPositionInLayer() )
                break;
            crossingsOfEdge[e.getId()]++;
            crossingsOfEdge[edgeList.get(index).getId()]++;
            index--;
        }
        edgeList.add(index + 1, e);
    }

    /**
     * Sets the weight of each edge to be the number of crossings. Used for
     * the slow version of the maximum crossings edge heuristic, as reported
     * by Stallmann (JEA, 2012)
     */
    public void setEdgeWeights()
            throws Terminate {
        for ( Edge e : this.getEdges() ) {
            e.setWeight((double) crossingsOfEdge[e.getId()]);
        }
    }

    /**
     * @return a list of the positions of all the sources of the edges;
     *         the list will be in the same order as the edges
     */
    List<Integer> getSourcePositions(List<Edge> edges) {
        List<Integer> positions = new ArrayList<Integer>();
        for ( Edge e : edges ) {
            LayeredGraphNode source = (LayeredGraphNode) e.getSourceNode();
            positions.add(source.getPositionInLayer());
        }
        return positions;
    }

    /**
     * @return a list of the positions of all the sources of the edges;
     *         the list will be in the same order as the edges
     */
    List<Integer> getDestinationPositions(List<Edge> edges) {
        List<Integer> positions = new ArrayList<Integer>();
        for ( Edge e : edges ) {
            LayeredGraphNode source = (LayeredGraphNode) e.getTargetNode();
            positions.add(source.getPositionInLayer());
        }
        return positions;
    }

    /**
     * @return the number of inversions in 'integers'
     */
    int countInversions(List<Integer> integers) {
        int inversions = 0;
        for ( int i = 1; i < integers.size(); i++ ) {
            int x = integers.get(i);
            int j = i - 1;
            while ( j >= 0 && x < integers.get(j) ) {
                integers.set(j + 1, integers.get(j));
                inversions++;
                j--;
            }
            integers.set(j + 1, x);
        }
        return inversions;
    }

    /**
     * *** The methods below are related to verticality ***
     */

    /**
     * 
     * @param e
     *            an edge
     * @return the nonverticality of edge e
     */
    public Integer nonverticality(Edge e) {
        int up_position = ((LayeredGraphNode) e.getSourceNode()).getPositionInLayer();
        int down_position = ((LayeredGraphNode) e.getTargetNode()).getPositionInLayer();
        int diff = up_position - down_position;
        return diff * diff;
    }

    public Integer nonverticality() {
        int total = 0;
        for ( Edge e : this.getEdges() ) {
            total += nonverticality(e);
        }
        return total;
    }

    /**
     * @param e
     *            the edge whose nonverticality will be calculated
     *            Computes the nonverticality of e and sets e's weight accordingly
     */
    public void setNonverticality(Edge e) throws Terminate {
        e.setWeight(nonverticality(e));
    }

    /**
     * @param v
     *            the node whose edges will have their nonverticalities set as
     *            weights
     */
    public void setNonverticalities(LayeredGraphNode v) throws Terminate {
        // !!! caution - getEdges returns even edges that have been deleted;
        // not a problem for layered graphs since these are never edited.
        for ( Edge e : v.getEdges() ) {
            setNonverticality(e);
        }
    }

    /**
     * sets nonverticalities of all edges as weights
     */
    public void setNonverticalities() throws Terminate {
        for ( Edge e : this.getEdges() ) {
            setNonverticality(e);
        }
    }
} // end, class LayeredGraph
