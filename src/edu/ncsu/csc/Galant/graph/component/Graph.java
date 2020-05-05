/**
 * Provides access to all information about the graph
 */

package edu.ncsu.csc.Galant.graph.component;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Collection;

import edu.ncsu.csc.Galant.GalantException;
import edu.ncsu.csc.Galant.GraphDispatch;
import edu.ncsu.csc.Galant.gui.window.GraphWindow;
import edu.ncsu.csc.Galant.gui.util.EdgeSelectionDialog;
import edu.ncsu.csc.Galant.gui.util.NodeSelectionDialog;
import edu.ncsu.csc.Galant.algorithm.Terminate;
import edu.ncsu.csc.Galant.graph.datastructure.NodeList;
import edu.ncsu.csc.Galant.graph.datastructure.EdgeList;
import edu.ncsu.csc.Galant.graph.datastructure.EdgeSet;
import edu.ncsu.csc.Galant.graph.datastructure.NodeSet;
import edu.ncsu.csc.Galant.logging.LogHelper;

/**
 * Stores all nodes, edges and related information for use in graph algorithms.
 *
 * @author Michael Owoc, Ty Devries (major modifications by Matt Stallmann)
 */
public class Graph{
  public GraphWindow graphWindow;
  private GraphDispatch dispatch;

  private String name;
  private String comment;
  private boolean directed;

  private boolean layered = false;
  private LayerInformation layerInformation;

  private NodeList nodes;

  private TreeMap<Integer, Node> nodeById = new TreeMap<Integer, Node>();

  private EdgeList edges;

  private MessageBanner banner;

  private int editState=0;
  /**
   * Keeps track of an edge selected during algorithm execution.
   */
  private Edge selectedEdge;

  /**
   * Keeps track of a node selected during algorithm execution.
   */
  private Node selectedNode;

  /**
   * @todo should handle this in the same way that GraphElementState is
   * handled; perhaps a graph can be another GraphElement with attributes
   * such as a list of nodes, etc.
   */
  protected List<GraphState> states;

  /**
   * @todo Never clear what this meant. A better name might be startNode,
   * but then there's getStartNode(), which has lots of side effects, and
   * is apparently not used anywhere (probably intended for algorithms that
   * have a start node such as dfs, bfs, and Dijkstra's).
   */
  private Node rootNode;

  /**
   * An integer that can be used as the id of the next edge if id's are not
   * explicit in the input.
   */
  private int nextEdgeId = 0;

  /**
   * true of the graph has edge id's
   */
  private boolean hasExplicitEdgeIds = false;

  /**
   * Default constructor.
   *
   * @todo let the Terminate exception propagate all the way to the top,
   * where it can be caught based on whether or not you're running an
   * algorithm
   */
  public Graph() {
    dispatch = GraphDispatch.getInstance();
    graphWindow = dispatch.getGraphWindow();
    nodes = new NodeList();
    edges = new EdgeList();
    states = new ArrayList<GraphState>();
    try {
      this.addState( new GraphState() );
    }
    catch ( Terminate t ) {     // should not happen
      t.printStackTrace();
    }
    banner = new MessageBanner(this);
  }
  
  public Graph(Graph newGraph)
  {
    
  }
  
public int getEditState()
  {
      return editState;
  }
public void incrementEditState() {
        editState++;
    }

    /**
     * @return true if the edit state was actually decremented, i.e.,
     * originally positive, false if it was already 0
     */
    public boolean decrementEditState() {
        if ( editState > 0 ) {
            editState--;
            return true;
        }
        return false;
    }
    
  /**
   * Resets the graph to its original state at the end of an
   * animation.
   */
  //The following method can be deleted as cloning accomplishes the same task
  public void reset() {
    int initialStateNumber = GraphDispatch.getInstance().getAlgorithmState();

    // first, reset any graph visibility attributes
    // not currently used
    ArrayList<GraphState> initialStates
      = new ArrayList<GraphState>();
    for ( GraphState state : this.states ) {
      if ( state.getState() > 0 ) break;
      initialStates.add(state);
    }
    this.states = initialStates;  

    // get rid of any nodes and edges created by the algorithm; better to
    // do this by gathering the ones that should not be removed and then
    // resetting nodes/edges to be the gathered ones
    NodeList validNodes = new NodeList();
    for ( Node node : this.nodes ) {
      if ( node.inScope(initialStateNumber) ) validNodes.add(node);
    }
    EdgeList validEdges = new EdgeList();
    for ( Edge edge : this.edges ) {
      if ( edge.inScope(initialStateNumber) ) validEdges.add(edge);
    }
    this.nodes = validNodes;
    this.edges = validEdges;

    // then reset the attributes of all nodes and edges
    for ( Node node : this.nodes ) {
      node.reset();
    }
    for ( Edge edge : this.edges ) {
      edge.reset();
    }
    // then reinitialize the message banner
    banner = new MessageBanner(this);
  }

    /**
     * @return a new state for this graph; the new state will be identical
     * to the current (latest one) except that it will be tagged with the
     * current algorithm state
     */
    private GraphState newState() throws Terminate {
        dispatch.startStepIfAnimationOrIncrementEditState();
        if ( dispatch.isAnimationMode() ) {
            GraphState latest = latestState();
            GraphState state
                = new GraphState(latest);
            return state;
        }
        else { // in edit mode
            GraphState latestValidState = getLatestValidState(getEditState());
            GraphState state
                = new GraphState(latestValidState);
            return state;
        }
    }

    /**
     * creates a deep copy of the graph, i.e., clones graph object completely
     * and is used to store edit state of graph just before algorithm execution
     */
    public Graph copyCurrentState(Graph currentGraph){
      
        Graph copyOfGraph=new Graph(currentGraph);
        copyOfGraph.dispatch = GraphDispatch.getInstance();
        copyOfGraph.graphWindow = dispatch.getGraphWindow();
        copyOfGraph.nodes = new NodeList();
        copyOfGraph.edges = new EdgeList();
        copyOfGraph.states = new ArrayList<GraphState>();
        EdgeList edgeListCopy=new EdgeList();
        NodeList nodeListCopy=new NodeList();
        TreeMap<Integer, Node> nodeByIdMap = new TreeMap<Integer, Node>();
        TreeMap<Integer, Node> copyOfNodeMap=new TreeMap<Integer, Node>(this.nodeById);
        for(int i=0;i<=this.nodes.size()-1;i++)
            {
                Node copiedNode= this.nodes.get(i);
                copiedNode=copiedNode.copyNode(currentGraph);
                nodeListCopy.add(copiedNode);
                copyOfNodeMap.put(copiedNode.getId(), copiedNode);
                nodeByIdMap.put(copiedNode.getId(), copiedNode);
            }
        for(int i=0;i<=this.edges.size()-1;i++)
            {
                Edge originalEdge=this.edges.get(i);
                Integer sourceId=originalEdge.getSourceNode().getId();
                Integer targetId=originalEdge.getTargetNode().getId();
                Node newSource=copyOfNodeMap.get(sourceId);
                Node newTarget=copyOfNodeMap.get(targetId);
                Edge copiedEdge=originalEdge.copyEdge(currentGraph,newSource,newTarget);
                edgeListCopy.add(copiedEdge);
            }
        String nameCopy=this.name;
        String commentCopy=this.comment;
        boolean directedCopy=this.directed;
        boolean layeredCopy=this.layered;
        LayerInformation layerInformationCopy=this.layerInformation;
        TreeMap<Integer, Node> nodeByIdCopy=new TreeMap<Integer, Node>(this.nodeById);
        MessageBanner bannerCopy=this.banner;
        Edge selectedEdgeCopy=this.selectedEdge;
        Node selectedNodeCopy=this.selectedNode;
        List<GraphState> statesCopy=this.states;
        Node rootNodeCopy=this.rootNode;
        int nextEdgeIdCopy=this.nextEdgeId;
        boolean hasExplicitEdgeIdsCopy=this.hasExplicitEdgeIds;
        copyOfGraph.edges=edgeListCopy;
        copyOfGraph.nodes=nodeListCopy;
        copyOfGraph.name=nameCopy;
        copyOfGraph.comment=commentCopy;
        copyOfGraph.directed=directedCopy;
        copyOfGraph.layered=layeredCopy;
        copyOfGraph.layerInformation=layerInformationCopy;
        copyOfGraph.nodeById=nodeByIdMap;
        copyOfGraph.banner=bannerCopy;
        copyOfGraph.selectedEdge=selectedEdgeCopy;
        copyOfGraph.selectedNode=selectedNodeCopy;
        copyOfGraph.states=statesCopy;
        copyOfGraph.rootNode=rootNodeCopy;
        copyOfGraph.nextEdgeId=nextEdgeIdCopy;
        copyOfGraph.hasExplicitEdgeIds=hasExplicitEdgeIdsCopy;
        return copyOfGraph;
    }

   
  /**
   * @return The last state on the list of states. This is the default for
   * retrieving information about any attribute.
   */
  public GraphState latestState() {
    GraphState state = null;
    if ( states.size() != 0 ) {
      state = states.get(states.size() - 1);
    }
    return state;
  }

  /**
   * This method is vital for retrieving the most recent information about
   * a graph, where most recent is defined relative to a given time stamp,
   * as defined by forward and backward stepping through the animation.
   * @see edu.ncsu.csc.Galant.algorithm.AlgorithmExecutor
   * @param stateNumber the numerical indicator (timestamp) of a state,
   * usually the current display state
   * @return the latest instance of GraphState that was created
   * before the given time stamp, or null if the element did not exist
   * before the time stamp.
   */
  public GraphState getLatestValidState(int stateNumber) {
    GraphState toReturn = null;
    int stateIndex = states.size() - 1;
    while ( stateIndex >= 0 ) {
      GraphState state = states.get(stateIndex);
      if ( state.getState() <= stateNumber ) {
        toReturn = state;
        break;
      }
      stateIndex--;
    }
    return toReturn;
  }

  /**
   * Adds the given state to the list of states for this graph. If there is
   * already a state having the same algorithm state (time stamp), there is
   * no need to add another one. Such a situation might arise if there are
   * multiple state changes to this element between a beginStep()/endStep()
   * pair or if no algorithm is running.  If an algorithm is running, this
   * method initiates synchronization with the master thread to indicate
   * that the changes corresponding to the added state are completed
   *
   * @invariant states are always sorted by state number.
   */
  private void addState(GraphState stateToAdd) throws Terminate {
    int stateNumber = stateToAdd.getState();
    boolean found = false;
    for ( int i = states.size() - 1; i >= stateNumber; i-- ) {
      GraphState state = states.get(i);
      if ( state.getState() == stateNumber ) {
        states.set(i, stateToAdd);
        found = true;
        break;
      }
    }
    if ( ! found ) {
      states.add(stateToAdd);
      dispatch.pauseExecutionIfRunning();
    }
  }

  /**
   * sets the selected edge; called from EdgeSelectionDialog
   */
  public void setSelectedEdge(Edge edge) {
    selectedEdge = edge;
  }

  /**
   * @todo A dialog lingers if user terminates algorithm when the dialog
   * window is up. Fixing this would involve some sort of coordination
   * between the dialog (a child of the GraphWindow) and the algorithm.
   */

  /**
   * @param prompt a message displayed in the edge selection dialog popup
   * @return an edge selected via a dialog during algorithm execution
   */
  public Edge getEdge(String prompt) throws Terminate {
    dispatch.initStepIfRunning();
    EdgeSelectionDialog dialog = new EdgeSelectionDialog(prompt);
    dispatch.pauseExecutionIfRunning();
    dialog = null;              // to keep window from lingering when
                                // execution is terminated
    return selectedEdge;
  }

  /**
   * @param prompt a message displayed in the edge selection dialog popup
   * @param restrictedSet the set from which the edge should be selected
   * @param errorMessage the message to be displayed if edge is not in
   * restrictedSet
   * @return a edge selected via a dialog during algorithm execution
   */
  public Edge getEdge(String prompt, EdgeSet restrictedSet, String errorMessage)
  throws Terminate {
    dispatch.initStepIfRunning();
    EdgeSelectionDialog dialog
      = new EdgeSelectionDialog(prompt, restrictedSet, errorMessage);
    dispatch.pauseExecutionIfRunning();
    dialog = null;              // to keep window from lingering when
                                // execution is terminated
    return selectedEdge;
  }

  /**
   * sets the selected node; called from NodeSelectionDialog
   */
  public void setSelectedNode(Node node) {
    selectedNode = node;
  }

  /**
   * @param prompt a message displayed in the node selection dialog popup
   * @return a node selected via a dialog during algorithm execution
   *
   */
  public Node getNode(String prompt) throws Terminate {
    dispatch.initStepIfRunning();
    NodeSelectionDialog dialog = new NodeSelectionDialog(prompt);
    dispatch.pauseExecutionIfRunning();
    dialog = null;              // to keep window from lingering when
                                // execution is terminated
    return selectedNode;
  }


  /**
   * @param prompt a message displayed in the node selection dialog popup
   * @param restrictedSet the set from which the node should be selected
   * @param errorMessage the message to be displayed if node is not in
   * restrictedSet
   * @return a node selected via a dialog during algorithm execution
   */
  public Node getNode(String prompt, NodeSet restrictedSet, String errorMessage)
  throws Terminate {
    dispatch.initStepIfRunning();
    NodeSelectionDialog dialog
      = new NodeSelectionDialog(prompt, restrictedSet, errorMessage);
    dispatch.pauseExecutionIfRunning();
    return selectedNode;
  }

  /**
   * Removes the attribute with the given key from the list and updates
   * state information appropriately.
   */
  public void remove(String key) throws Terminate {
    GraphState newState = newState();
    newState.remove(key);
    addState(newState);
  }

  /************** Boolean attributes ***************/

  public boolean set(String key, Boolean value) throws Terminate {
    GraphState newState = newState();
    boolean found = newState.set(key, value);
    addState(newState);
    return found;
  }

  /**
   * If value is not specified, assume it's boolean and set to true
   */
  public boolean set(String key) throws Terminate {
    return this.set(key, true);
  }

  public void clear(String key) throws Terminate {
    this.remove(key);
  }

  /**
   * For boolean attributes, assume that the absense of an attribute means
   * that it's false.
   */
  public Boolean getBoolean(String key) {
    GraphState state = latestState();
    if ( state == null ) return false;
    return state.getAttributes().getBoolean(key);
  }
  public Boolean getBoolean(int state, String key) {
    GraphState validState = getLatestValidState(state);
    return validState == null ? false : validState.getAttributes().getBoolean(key);
  }

  /**
   * Synonyms (for readability in algorithms)
   */
  public Boolean is(String key) {
    return getBoolean(key);
  }
  public Boolean is(int state, String key) {
    return getBoolean(state, key);
  }

  /************** String attributes ***************/
  public boolean set(String key, String value) throws Terminate {
    GraphState newState = newState();
    boolean found = newState.set(key, value);
    addState(newState);
    return found;
  }
  public String getString(String key) {
    GraphState state = latestState();
    if ( state == null ) return null;
    return state.getAttributes().getString(key);
  }
  public String getString(int state, String key) {
    GraphState validState = getLatestValidState(state);
    return validState == null ? null : validState.getAttributes().getString(key);
  }

  /**
   * Methods that cause labels and weights to be shown or hidden during
   * algorithm execution. Typically, an algorithm will declare its intent
   * at the beginning.
   */

  /**
   * displays node labels if show is true, hides them if show is false
   * also toggles the button in the graph window if appropriate
   */
  public void showNodeLabels(boolean show) {
    graphWindow.showNodeLabels(show);
  }
  /**
   * displays node weights if show is true, hides them if show is false
   * also toggles the button in the graph window if appropriate
   */
  public void showNodeWeights(Boolean show) {
    graphWindow.showNodeWeights(show);
  }
  /**
   * displays edge labels if show is true, hides them if show is false
   * also toggles the button in the graph window if appropriate
   */
  public void showEdgeLabels(boolean show) {
    graphWindow.showEdgeLabels(show);
  }
  /**
   * displays edge weights if show is true, hides them if show is false
   * also toggles the button in the graph window if appropriate
   */
  public void showEdgeWeights(Boolean show) {
    graphWindow.showEdgeWeights(show);
  }

  /**
   * Individually hide Node/Edge labels or weights; has no effect if they
   * are already hidden via, e.g., hideNodeLabels()
   */
  public void hideAllNodeLabels() throws Terminate {
    for ( Node node : nodes ) {
      node.hideLabel();
    }
  }
  public void hideAllEdgeLabels() throws Terminate {
    for ( Edge edge : edges ) {
      edge.hideLabel();
    }
  }
  public void hideAllNodeWeights() throws Terminate {
    for ( Node node : nodes ) {
      node.hideWeight();
    }
  }
  public void hideAllEdgeWeights() throws Terminate {
    for ( Edge edge : edges ) {
      edge.hideWeight();
    }
  }

  /**
   * Undo hiding of individual Node/Edge labels or weights; has no effect
   * if they are already hidden via, e.g., hideNodeLabels()
   */
  public void showAllNodeLabels() throws Terminate {
    for ( Node node : nodes ) {
      node.showLabel();
    }
  }
  public void showAllEdgeLabels() throws Terminate {
    for ( Edge edge : edges ) {
      edge.showLabel();
    }
  }
  public void showAllNodeWeights() throws Terminate {
    for ( Node node : nodes ) {
      node.showWeight();
    }
  }
  public void showAllEdgeWeights() throws Terminate {
    for ( Edge edge : edges ) {
      edge.showWeight();
    }
  }

  /**
   * @return the set of visible nodes
   */
  public NodeList visibleNodes() {
    NodeList nodeList = new NodeList();
    for ( Node node : nodes ) {
      if ( ! node.isHidden() ) {
        nodeList.add(node);
      }
    }
    return nodeList;
  }

  /**
   * @return the set of visible edges
   */
  public EdgeList visibleEdges() {
    EdgeList edgeList = new EdgeList();
    for ( Edge edge : edges ) {
      if ( ! edge.isHidden() ) {
        edgeList.add(edge);
      }
    }
    return edgeList;
  }

  /**
   * Shows all Nodes that have been hidden individually
   */
  public void showNodes() throws Terminate {
    for ( Node node : nodes ) {
      node.show();
    }
  }

  /**
   * Shows all edges that have been hidden individually
   */
  public void showEdges() throws Terminate {
    for ( Edge edge : edges ) {
      edge.show();
    }
  }

  /**
   * The following are used to do blanket clearing of attributes
   */
  public void clearNodeMarks() throws Terminate {
    for ( Node node : nodes ) node.unmark();
  }
  public void clearNodeHighlighting() throws Terminate {
    for ( Node node : nodes ) node.unHighlight();
  }
  public void clearEdgeHighlighting() throws Terminate {
    for ( Edge edge : edges ) edge.unHighlight();
  }
  public void clearNodeLabels() throws Terminate {
    for ( Node node : nodes ) node.clearLabel();
  }
  public void clearEdgeLabels() throws Terminate {
    for ( Edge edge : edges ) edge.clearLabel();
  }
  public void clearNodeWeights() throws Terminate {
    for ( Node node : nodes ) node.clearWeight();
  }
  public void clearEdgeWeights() throws Terminate {
    for ( Edge edge : edges ) edge.clearWeight();
  }
  public void clearAllNode(String attribute) throws Terminate {
    for ( Node node : nodes ) node.clear(attribute);
  }
  public void clearAllEdge(String attribute) throws Terminate {
    for ( Edge edge : edges ) edge.clear(attribute);
  }

  /** Graph methods that are independent of state */

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public String getComment() {
    return comment;
  }

  public void setLayered(boolean layered) {
    this.layered = layered;
    if ( layerInformation == null ) {
      layerInformation = new LayerInformation();
    }
  }

  public boolean isLayered() {
    return layered;
  }

  public boolean isVertical() {
    if ( layered ) {
      return layerInformation.vertical;
    }
    else return false;
  }

  /**
   * Changes the contents of the current message banner
   */
  public void writeMessage(String message) throws Terminate {
    banner.set(message);
  }

  /**
   * @param state the algorithm state for the desired message
   * @return the current message banner
   */
  public String getMessage(int state) {
    return banner.get(state);
  }

  /**
   * @return the number of <code>Node</code>s in the current <code>Graph</code>
   */
  public int numberOfNodes() {
    int count = 0;
    for ( Node n : nodes ) {
      if ( n.inScope() )
        count++;
    }
    return count;
  }

  /**
   * @return the largest id of any node + 1; this should be used when
   * allocating an array of nodes, as there is no longer a guarantee that
   * id's start at 0 and are contiguous.
   */
  public int nodeIds() {
    int maxId = 0;
    for ( Node currentNode : nodes ) {
      if ( currentNode.inScope() && currentNode.getId() > maxId )
        maxId = currentNode.getId();
    }
    return maxId + 1;
  }

  /**
   * @return the number of <code>Edge</code>s in the current <code>Graph</code>
   */
  public int numberOfEdges() {
    int count = 0;
    for ( Edge n : edges ) {
      if ( n.inScope() )
        count++;
    }
    return count;
  }

  /**
   * @return the largest id of any edge + 1; this should be used when
   * allocating an array of edges; unlike the case of nodes, it should not
   * really be needed -- edge id's are assigned contiguously; we provide it
   * to avoid confusion.
   */
  public int edgeIds() {
    int maxId = 0;
    for ( Edge currentEdge : edges ) {
      if ( currentEdge.inScope()
           && currentEdge.getId() > maxId )
        maxId = currentEdge.getId();
    }
    return maxId + 1;
  }

  /**
   * @return true if the graph is directed, false otherwise
   */
  public boolean isDirected() {
    return directed;
  }

  /**
   * @param directed true if setting the graph to directed, false if undirected
   */
  public void setDirected(boolean directed) {
    this.directed = directed;
  }

  /**
   * @return all nodes in the graph; inScope() without the 'state' argument
   * only checks to see if a node has been deleted.
   */
  public NodeList getNodes() {
    NodeList retNodes = new NodeList();
    for ( Node v : this.nodes ) {
      if ( v.inScope() ) {
        retNodes.add(v);
      }
    }
    return retNodes;
  }

  /**
   * @return all nodes in the graph that exist in the given state.
   */
  public NodeList getNodes(int state)
  {
    NodeList retNodes = new NodeList();
    for ( Node n : this.nodes ) {
      if ( n.inScope(state) ) {
        retNodes.add(n);
      }
    }
    return retNodes;
  }

    /**
     * @return a list of all nodes in the graph, even ones that have been deleted
     * used when drawing, so that we can see the effect of undoing deletions
     */
    public NodeList getAllNodes() {
        NodeList retNodes = new NodeList();
        for ( Node v : this.nodes ) {
            retNodes.add(v);
        }
        return retNodes;
    }

  /**
   * @param nodes new set of nodes to be added to the graph
   */
  public void setNodes(Collection<Node> nodes) {
    this.nodes = new NodeList(nodes);
  }

  /**
   * @return all edges as a list
   */
  public EdgeList getEdges()
  {
    EdgeList retEdges = new EdgeList();
    for ( Edge e : this.edges ) {
      if ( e.inScope() ) {
        retEdges.add(e);
      }
    }
    return retEdges;
  }

  /**
   * @return all edges at the current algorithm state
   */
  public EdgeList getEdges(int state)
  {
    EdgeList retEdges = new EdgeList();
    for ( Edge e : this.edges ) {
      if ( e.inScope(state) ) {
        retEdges.add(e);
      }
    }
    return retEdges;
  }

    /**
     * @return a list of all edges in the graph, even ones that have been deleted
     * used when drawing, so that we can see the effect of undoing deletions
     */
    public EdgeList getAllEdges() {
        EdgeList retEdges = new EdgeList();
        for ( Edge e : this.edges ) {
            retEdges.add(e);
        }
        return retEdges;
    }

  /**
   * @return the edges as a set
   */
  public EdgeSet getEdgeSet() {
    EdgeSet retEdges = new EdgeSet();
    for ( Edge e : this.edges ) {
      if ( e.inScope() ) {
        retEdges.add(e);
      }
    }
    return retEdges;
  }

  /**
   * @return the nodes as a set
   */
  public NodeSet getNodeSet() {
    NodeSet retNodes = new NodeSet();
    for ( Node v : this.nodes ) {
      if ( v.inScope() ) {
        retNodes.add(v);
      }
    }
    return retNodes;
  }

  /**
   * @return all edges at the current algorithm state as a set
   */
  public EdgeSet getEdgeSet(int state) {
    EdgeSet retEdges = new EdgeSet();
    for ( Edge e : this.edges ) {
      if ( e.inScope(state) ) {
        retEdges.add(e);
      }
    }
    return retEdges;
  }

  /**
   * @return all nodes at the current algorithm state as a set
   */
  public NodeSet getNodeSet(int state) {
    NodeSet retNodes = new NodeSet();
    for ( Node v : this.nodes ) {
      if ( v.inScope(state) ) {
        retNodes.add(v);
      }
    }
    return retNodes;
  }

  /**
   * Replaces the current <code>Edge</code>s.
   * @param edges new set of edges to be added to the graph
   */
  public void setEdges(Collection<Edge> edges) {
    this.edges = new EdgeList(edges);
  }

  /**
   * marks the edge e as deleted without physically removing it.
   */
  public void deleteEdge(Edge e) throws Terminate {
    e.setDeleted(true);
  }

  /**
   * @return an edge with the given source and target if one exists; throws
   * an exception otherwise; if the graph is directed, source and target
   * must match.
   */
  public Edge getEdge(Node source, Node target) throws GalantException {
    List<Edge> incidenceList
      = source.getOutgoingEdges();
    for ( Edge e : incidenceList ) {
      if ( source.travel(e) == target ) {
        return e;
      }
    }
    throw new GalantException("no edge with source " + source.getId()
                              + " and target " + target.getId() + " exists");
  }

  /**
   * Deletes an edge with the given source and target if one exists; throws
   * an exception otherwise; if the graph is directed, source and target
   * must match. This is called only during algorithm execution. Not clear
   * how it would be used since we can hide edges.
   */
  public void deleteEdge(Node source, Node target) throws Terminate, GalantException {
    Edge edge = getEdge(source, target);
    deleteEdge(edge);
  }

    /**
     * Deletes (but does not permanently remove) the specified
     * <code>Node</code> from the <code>Graph</code>; simply marks it and its
     * incident edges deleted
     */
    public void deleteNode(Node n) throws Terminate {
        n.setDeleted(true);
        dispatch.setAtomic(true);
        for ( Edge e : n.getEdges() ) {
            e.setDeleted(true);
        }
        dispatch.setAtomic(false);
    }

  /**
   * @return the root node
   */
  public Node getRootNode()
  throws GalantException
  {
    if ( rootNode == null ) {
      throw new GalantException("no root node has been set"
                                + "\n - in getRootNode");
    }
    return rootNode;
  }

  /**
   * @param rootNode the new root node
   */
  public void setRootNode(Node rootNode) {
    this.rootNode = rootNode;
  }

  /**
   * @returns true if a node with the given id exists
   */
  public boolean nodeIdExists(Integer id) {
    return nodeById.containsKey(id);
  }

  /**
   * Returns the Node in the graph represented by the given unique ID.
   *
   * @param id
   * @return the specified Node if it exists, null otherwise
   */
  public Node getNodeById(int id)
  throws GalantException
  {
    if ( this.nodes.size() == 0 ) {
      throw new GalantException("Empty graph"
                                + "\n - in getNodeById");
    }

    if ( ! nodeById.containsKey(id) ) {
      throw new GalantException("No node with id = "
                                + id
                                + " exists"
                                + "\n - in getNodeById");
    }

    Node n = nodeById.get(id);

    if ( n.isDeleted() ) {
      throw new GalantException("Node has been deleted, id = "
                                + id
                                + "\n - in getNodeById");
    }

    return n;
  }

  /**
   * @return true if the input had explicit edge id's as determined by the
   * fact that none had to be assigned internally -- the assumption being
   * that either all or none of the edges have explicit id's.
   */
  public boolean hasExplicitEdgeIds() {
    return hasExplicitEdgeIds;
  }

  /**
   * @return A node that can be used as a starting node by an algorithm
   * that requires it
   */
  public Node getStartNode() throws GalantException {
    if ( this.nodes.size() == 0 ) {
      throw new GalantException("Empty graph in getStartNode.");
    }
    return this.nodes.get(0);
  }


  /**
   * Adds a new <code>Node</code> to the <code>Graph</code>
   * @param x the x coordinate of the new node
   * @param y the y coordinate of the new node
   * @return the added <code>Node</code>; called only during editing
   */
  public Node addInitialNode(Integer x, Integer y) {
    LogHelper.enterMethod(getClass(), "addInitialNode(), x = " + x + ", y = " + y);
    Integer newId = nextNodeId();
    Node n = new Node(this, newId, x, y);
    nodes.add(n);
    nodeById.put(newId, n);

    if ( this.rootNode == null ) {
      this.rootNode = n;
    }

    // seems like we need an addState call as is the case with state
    // changes in GraphElement.java

    LogHelper.exitMethod(getClass(), "addInitialNode() " + n);
    return n;
  }

  /**
   * Adds a new <code>Node</code> to the <code>Graph</code>
   * @param x the x coordinate of the new node
   * @param y the y coordinate of the new node
   * @return the added <code>Node</code>; called only during algorithm
   * execution; the assumption here is that the algorithm has to "know" the
   * position of the node it is adding. The only difference from the above
   * is that an algorithm step is initiated if appropriate
   */
  public Node addNode(Integer x, Integer y) throws Terminate {
    LogHelper.enterMethod(getClass(), "addNode(), x = " + x + ", y = " + y);
    dispatch.startStepIfAnimationOrIncrementEditState();
    Integer newId = nextNodeId();
    Node n = new Node(this, newId, x, y);
    nodes.add(n);
    nodeById.put(newId, n);

    // probably not needed but couldn't hurt; maybe the algorithm
    // constructs a tree and then traverses it
    if ( this.rootNode == null ) {
      this.rootNode = n;
    }

    /**
     * @todo do we need a pauseExecution here? possibly not because a new
     * state is added to the created node
     */

    LogHelper.exitMethod(getClass(), "addNode() " + n);
    return n;
  }

  /**
   * Adds a node to the graph during parsing. The node has already been
   * created.
   */
  public void addNode(Node n) {
    LogHelper.enterMethod(getClass(), "addNode: node = " + n);

    /**
     * @todo subclass method for layered graphs
     */
    if ( layered ) {
      layerInformation.addNode(n);
    }

    nodes.add(n);
    nodeById.put(n.getId(), n);

    if ( this.rootNode == null ) {
      this.rootNode = n;
    }

    LogHelper.exitMethod(getClass(), "addNode( Node )");
  }

  /**
   * This variant is used any time new edge has been created. Its purpose
   * is integrate the edge into the graph.
   */
  public void addEdge(Edge edge) {
    LogHelper.disable();
    LogHelper.enterMethod(getClass(), "addEdge " + edge);
    // during parsing we need to know if the edge had an explicit id in
    // its GraphML representation
    if ( edge.hasExplicitId() ) this.hasExplicitEdgeIds = true;
    edge.getSourceNode().addEdge(edge);
    edge.getTargetNode().addEdge(edge);
    edges.add(edge);
    LogHelper.exitMethod(getClass(), "addEdge, hasExplicitEdgeIds = "
                         + hasExplicitEdgeIds);
    LogHelper.restoreState();
  }

  /**
   * Adds a new edge to the graph with the specified source and
   * target. Starts an algorithm step if appropriate.
   * Note: for undirected graphs, "source" and "target" are meaningless.
   * @return the added edge
   *
   * This variant is used during algorithm execution if the actual nodes
   * are known.
   */
  public Edge addEdge(Node source, Node target) throws Terminate {
    dispatch.startStepIfAnimationOrIncrementEditState();
    Edge e = new Edge(this, source, target);
    addEdge(e);
    return e;
  }

  /**
   * Adds a new edge to the graph with the specified source and
   * target. Starts an algorithm step if appropriate.
   * @param sourceId the ID of the source <code>Node</code>
   * @param targetId the ID of the target <code>Node</code>
   *
   * Note: for undirected graphs, "source" and "target" are meaningless.
   * @return the added edge
   *
   * This variant is used during algorithm execution when only the node
   * id's are known.
   */
  public Edge addEdge(int sourceId, int targetId)
  throws Terminate, GalantException {
    return addEdge(getNodeById(sourceId), getNodeById(targetId));
  }

  /**
   * Adds a new edge to the graph with the specified source and
   * target.
   * Note: for undirected graphs, "source" and "target" are meaningless.
   * @return the added edge
   *
   * This variant is used only during editing.
   */
  public Edge addInitialEdge(Node source, Node target) {
    Edge e = new Edge(this, source, target);
    addEdge(e);
    return e;
  }

  /**
   * Removes the specified <code>Edge</code> from the <code>Graph</code>
   * @param e the <code>Edge</code> to remove; this version actually gets
   * rid of the edge rather than marking it deleted
   */
  public void removeEdge(Edge e) {
    LogHelper.enterMethod(getClass(), "removeEdge " + e);
    edges.remove(e);

    Node source = e.getSourceNode();
    source.getIncidentEdges().remove(e);
    Node target = e.getTargetNode();
    target.getIncidentEdges().remove(e);
    LogHelper.exitMethod(getClass(), "removeEdge");
  }

  /**
   * Removes the edge with the specified source and target; throws an
   * exception if none exists; source and target must match if the graph is
   * directed
   */
  public void removeEdge(Node source, Node target) throws GalantException {
    Edge edge = getEdge(source, target);
    removeEdge(edge);
  }

  /**
   * Removes the specified <code>Node</code> from the <code>Graph</code>
   * @param n the <code>Node</code> to remove; this one actually removes
   * the node and its incident edges rather than just marking it deleted
   */
  public void removeNode(Node n) {
    List<Edge> n_edges = n.getIncidentEdges();
    LogHelper.enterMethod( getClass(), "removeNode " + n + ", deg = " + n_edges.size() );

    for ( Edge e : n_edges ) {
      removeEdge(e);
    }

    nodes.remove(n);
    LogHelper.exitMethod(getClass(), "removeNode");
  }

  /**
   * @return an integer ID for the next <code>Node</code> to be
   * added. This will always be the largest id so far + 1
   */
  private int nextNodeId() {
    LogHelper.enterMethod(getClass(), "nextNodeId");
    int id = 0;
    if ( ! nodeById.isEmpty() )
      id = nodeById.lastKey() + 1;
    LogHelper.exitMethod(getClass(), "nextNodeId, id = " + id);
    return id;
  }

  /**
   * @return the number of layers if this is a layered graph
   */
  public int numberOfLayers() {
    return layerInformation.numberOfLayers;
  }

  /**
   * @return the number of nodes on layer i
   */
  public int numberOfNodesOnLayer(int i) {
    return layerInformation.layerSize.get(i);
  }

  /**
   * @return the maximum position of a node on layer i
   */
  public int maxPositionInLayer(int i) {
    return layerInformation.maxPositionInLayer.get(i);
  }

  /**
   * @return the maximum position of any node
   */
  public int maxPositionInAnyLayer() {
    return layerInformation.maxPosition;
  }

  private GraphLayout savedLayout;

  /**
   * Call the repositioning algorithm
   * Currently the only supported algorithm is Force directed repositioning
   * @param boost is the extent to which degree of a node will cause it to
   * repel other nodes - repulsive force is multiplied by degree raised to
   * the boost power.
   *
   * @see edu.ncsu.csc.Galant.graph.component.GraphLayout#forceDirected()
   */
  public void smartReposition(Double boost) {
    savedLayout = new GraphLayout(this);
    GraphLayout layoutToBeRepositioned = new GraphLayout(this);
    layoutToBeRepositioned.forceDirected(boost);
    layoutToBeRepositioned.usePositions();
  }

  /**
   * same as smartReposition(boost) but uses the default boost, as built
   * into the force-directed algorithm.
   */
  public void smartReposition() {
    smartReposition(null);
  }

  /**
   * Restores node positions as they were before the last smartReposition()
   * @todo maintain an another instance of GraphLayout to do this
   */
  public void undoReposition() {
    if ( savedLayout != null ) {
      savedLayout.usePositions();
    }
  }

  /**
   * Assigns edge ids if necessary; could be used for other housekeeping in
   * the future
   */
  public void initializeAfterParsing() throws GalantException {
    // first process the edges that have id's, so there's no conflict later
//    for ( Edge edge : this.edges ) {
//      if ( edge.hasExplicitId() ) {
//        if ( this.edgeById.containsKey( edge.getId() ) ) {
//          throw new GalantException("Duplicate id: "
//                                    + edge.getId()
//                                    + " when processing edge "
//                                    + edge);
//        }
//        this.hasExplicitEdgeIds = true;
//        this.edgeById.put(edge.getId(), edge);
//      }
//    }
    // then those that don't
    for ( Edge edge : this.edges ) {
//      if ( ! edge.hasExplicitId() ) {
//        edge.setId( nextEdgeId() );
//        this.edgeById.put(edge.getId(), edge);
//      }
    }
    if ( layered ) layerInformation.initializeAfterParsing();
  }

  /**
   * Returns a valid graphml representation of the graph; for use when no
   * algorithm is running
   */
  public String xmlString() {
    LogHelper.disable();
    LogHelper.enterMethod(getClass(), "xmlString");
    String s = "";
    s += "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n";
    s += "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" \n";
    s += "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n";
    s += "xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns \n";
    s += "http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">\n";
    s += " <graph ";
    if ( name != null )
      s += " name=\"" + name + "\"";
    if ( comment != null )
      s += " comment=\"" + comment + "\"";
    if ( this.isLayered() ) {
      s += " type=\"layered\"";
    }
    s += " edgedefault=\"" + (this.isDirected() ? "directed" : "undirected") + "\"";
    s += ">\n";
    for ( Node n : this.nodes ) {
      if ( ! n.inScope() ) continue;
      s += "  " + n.xmlString() + "\n";
    }
    for ( Edge e : this.edges ) {
      if ( ! e.inScope() ) continue;
      s += "  " + e.xmlString() + "\n";
    }
    s += " </graph>";
    s += "</graphml>";
    LogHelper.exitMethod(getClass(), "xmlString");
    LogHelper.restoreState();
    return s;
  }

  /**
   * Returns a valid graphml representation of the graph; for use when you
   * want to export the current state of a running algorithm.
   */
  public String xmlString(int state) {
    LogHelper.disable();
    LogHelper.enterMethod(getClass(), "xmlString(" + state + ")");
    String s = "";
    s += "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n";
    s += "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" \n";
    s += "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n";
    s += "xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns \n";
    s += "http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">\n";
    s += " <graph ";
    if ( name != null )
      s += " name=\"" + name + "\"";
    if ( comment != null )
      s += " comment=\"" + comment + "\"";
    if ( this.isLayered() ) {
      s += " type=\"layered\"";
    }
    s += " edgedefault=\"" + (this.isDirected() ? "directed" : "undirected") + "\"";             //
                                                                                                 // directed/undirected
    s += ">\n";
    for ( Node n : this.nodes ) {
      LogHelper.logDebug( "  writing xml string for node with id " + n.getId() );
      LogHelper.logDebug( "  writing xml string for node " + n);
      if ( ! n.inScope(state) ) continue;
      String sN = n.xmlString(state);
      if ( ! sN.trim().isEmpty() )
        s += "  " + sN + "\n";
    }
    for ( Edge e : this.edges ) {
      LogHelper.logDebug("writing xml string for edge " + e);
      if ( ! e.inScope(state) ) continue;
      String sE = e.xmlString(state);
      if ( ! sE.trim().isEmpty() )
        s += "  " + sE + "\n";
    }
    s += " </graph>";
    s += "</graphml>";
    LogHelper.exitMethod(getClass(), "xmlString(" + state + ")");
    LogHelper.restoreState();
    return s;
  }
}

// [Last modified: 2020 05 04 at 20:13:04 GMT]
