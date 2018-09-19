package edu.ncsu.csc.Galant.graph.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.ncsu.csc.Galant.GalantException;
import edu.ncsu.csc.Galant.GraphDispatch;
import edu.ncsu.csc.Galant.algorithm.Terminate;
import edu.ncsu.csc.Galant.logging.LogHelper;
import edu.ncsu.csc.Galant.graph.datastructure.EdgeSet;

/**
 * Edge graph object. Connects two <code>Node<code>s, and can be directored or undirected.
 * For undirected graphs, "source" and "target" (destination) are meaningless.
 * @author Jason Cockrell, Ty Devries, Alex McCabe, Michael Owoc, with major
 * modifications by Matthias Stallmann.
 */
public class Edge extends GraphElement {
    Node source;
    Node target;
    /** used internally only or for array indexing, unless edge has an explicit
     * id */
    Integer id;

    /**
     * true if the GraphML representation of this edge had an id
     */
    boolean hasExplicitId = false;

    /**
     * create a blank instance for use when copying state at start of
     * algorithm execution
     */
    public Edge() {
    }
    
    /**
     * When an edge is created during parsing when source, target and id are
     * not yet known.
     */
	public Edge(Graph graph) {
        super(graph);
	}

    /**
     * To add an edge while editing or during algorithm execution: source
     * and target are known at the time.
     */
    public Edge(Graph graph, Node source, Node target) {
        super(graph);
        this.source = source;
        this.target = target;
    }

    public Node getSourceNode() {
        return source;
    }

    public Node getTargetNode() {
        return target;
    }

    public Node getSource() {
        return source;
    }

    public Node getTarget() {
        return target;
    }

    public void setId(int id) { this.id = id; }

    public Integer getId() { return this.id; }

    public boolean hasExplicitId() { return this.hasExplicitId; }

  /**
   * natural syntax for set containment
   */
  public Boolean in(EdgeSet S) { return S.contains(this); }
  
    /**
     * Makes sure that all the attributes specific to edges are properly
     * initialized. The relevant ones are ...
     * - source, target: integer (id's of nodes)
     */
    public void initializeAfterParsing()
        throws GalantException {
        LogHelper.disable();
        LogHelper.logDebug("-> initializeAfterParsing " + this);
        super.initializeAfterParsing();
        // id has already been parsed by GraphElement.initializeAfterParsing()
        Integer graphElementId = getInteger(super.ID);
        String sourceString = getString("source");
        String targetString = getString("target");
        Integer sourceId = Integer.MIN_VALUE;
        Integer targetId = Integer.MIN_VALUE;
        if ( graphElementId != null ) {
            this.id = graphElementId;
            this.hasExplicitId = true;
        }
        if ( sourceString == null )
            throw new GalantException("Missing source for " + this);
        if ( targetString == null )
            throw new GalantException("Missing target for " + this);
        try {
            sourceId = Integer.parseInt(sourceString);
        }
        catch ( NumberFormatException e ) {
            throw new GalantException("Bad source id " + sourceString);
        }
        try {
            targetId = Integer.parseInt(targetString);
        }
        catch ( NumberFormatException e ) {
            throw new GalantException("Bad target id " + targetString);
        }
        this.source = super.graph.getNodeById(sourceId);
        if ( this.source == null ) {
            throw new GalantException("Source node missing when processing edge "
                                      + this);
        }
        this.target = super.graph.getNodeById(targetId);
        if ( this.target == null ) {
            throw new GalantException("Target node missing when processing edge "
                                      + this);
        }
        try { // these attributes are fixed and stored as fields of the edge
              // object
            super.remove("source");
            super.remove("target");
        }
        catch ( Terminate t ) { // should not happen
            t.printStackTrace();
        }
        LogHelper.logDebug(" id = " + id + " explicit = " + hasExplicitId);
        LogHelper.logDebug("<- initializeAfterParsing, edge = "
                               + this);
        LogHelper.restoreState();
    }

	public String xmlString() {
        // id may not exist for an edge; not really essential;
        // inputHasEdgeIds() returns true if they appeared in the input, in
        // which case they should be rendered in the output as the first
        // attribute; edges with non-existent id's need to be given ones
        String idComponent = "";
        if ( super.graph.hasExplicitEdgeIds() ) {
          Integer edgeId = this.id;
          if ( edgeId == null ) edgeId = super.graph.nextEdgeId();
          idComponent = "id=\"" + edgeId + "\"";
        }
 		String s = "<edge " + idComponent;
        // need this to get past here when the edge is first created and this
        // function is used for debugging.
        if ( this.source != null && this.target != null ) {
            s += " source=\"" + this.source.getId() + "\"";
            s += " target=\"" + this.target.getId() + "\"";
        }
        s += super.attributesWithoutId();
        s += " />";
		return s;
	}

    /**
     * This version is called when the current state of the animation is
     * exported.
     */
	public String xmlString(int state) {
        if ( ! inScope(state) ) {
            return "";
        }
		String s = "<edge "
            + " source=\"" + this.source.getId() + "\""
            + " target=\"" + this.target.getId() + "\"";
        s += super.xmlString(state);
        s += " />";
		return s;
	}
        
        public Edge copyEdge(Graph currentGraph) {
        Edge copy = new Edge();
        copy.source=this.source;
        copy.target=this.target;
        copy.id = this.id;
        copy.dispatch=GraphDispatch.getInstance();
        copy.graph = currentGraph;
        ArrayList<GraphElementState> statesCopy=super.copyCurrentState();
        copy.states=statesCopy;
        return copy;
    }

    /**
     * For debugging only
     */
	@Override
	public String toString() {
        String s = "[Edge ";
        s += "(" + this.id + ") ";
        if ( source != null && target != null ) {
            s += source.getId() + "," + target.getId() + " ";
        }
        s += super.attributesWithoutId();
        s += "]";
		return s;
	}
}

//  [Last modified: 2018 09 19 at 18:56:17 GMT]
