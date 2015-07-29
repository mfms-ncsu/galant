package edu.ncsu.csc.Galant.graph.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import edu.ncsu.csc.Galant.GalantException;
import edu.ncsu.csc.Galant.GraphDispatch;
import edu.ncsu.csc.Galant.logging.LogHelper;

/**
 * Edge graph object. Connects two <code>Node<code>s, and can be directored or undirected.
 * For undirected graphs, "source" and "destination" are meaningless.
 * @author Jason Cockrell, Ty Devries, Alex McCabe, Michael Owoc
 *
 */
public class Edge extends GraphElement implements Comparable<Edge> {
	private List<EdgeState> edgeStates;
    Integer id;
    Node source;
    Node target;
	
    /**
     * When an edge is created during parsing and source, target and id are not known.
     */
	public Edge(GraphState currentState) {
        super(currentState.getGraph(), currentState);
	}

    /**
     * To add an edge while editing: id, source and target are known at the
     * time.
     */
    public Edge(GraphState algorithmState, int id, Node source, Node target) {
        super(algorithmState.getGraph(), algorithmState);
        super.graph = algorithmState.getGraph();
        this.id = id;
        this.source = source;
        this.target = target;
    }

    /**
     * Getters for source and target (destination).
     */
    public Integer getId() {
        return id;
    }

    public Node getSourceNode() {
        return source;
    }

    public Node getTargetNode() {
        return target;
    }

    /** 
     * Careful! To be used only when done parsing.
     */
    void setId(int id) {
        this.id = id;
    }

    /**
     * Makes sure that all the attributes specific to edges are properly
     * initialized.
     */
    public void initializeAfterParsing()
    throws GalantException {
        LogHelper.enterMethod( getClass(), "initializeAfterParsing");
        super.initializeAfterParsing();
        this.id = getInteger("id");
        if ( id == null ) {
            id = super.graph.getNextEdgeId();
        }
        Integer sourceId = super.getInteger("source");
        if ( sourceId == null ) {
             throw new GalantException("Missing attribute source when processing edge " + this.id);
        }
        this.source = super.graph.getNodeById(sourceId);
        if (source == null) {
            throw new GalantException("Source node missing when processing edge " + this.id);
        }
        Integer targetId = super.getInteger("target");
        if ( targetId == null ) {
             throw new GalantException("Missing attribute target when processing edge " + this.id);
        }
        this.target = super.graph.getNodeById(targetId);
        if (target == null) {
            throw new GalantException("Target node missing when processing edge " + this.id);
        }

        // no longer need these to be stored in attribute list -- they're
        // instance variables now
        attributes.remove("id");
        attributes.remove("source");
        attributes.remove("target");
    }

	@Override
	public String toString() {
        // id may not exist for an edge; not really essential
        String idComponent = "";
        if ( super.graph.inputHasEdgeIds() )
            idComponent = "id=\"" + this.id + "\"";
        System.out.println("Edge toString: source = " + source + ", target = " + target);
 		String s = "<edge " + idComponent;
        s += " source=\"" + this.source.getId() + "\"";
        s += " target=\"" + this.target.getId() + "\"";
        s += super.toString();
        s += " />";
		return s;
	}
	
    /**
     * This version is called when the current state of the animation is
     * exported.
     */
    @Override
	public String toString(int state) {
        if ( ! inScope(state) ) {
            return "";
        }
        // id may not exist for an edge; not really essential
        String idComponent = (id == null) ? "" : "id=\"" + this.getId() + "\"";
		String s = "<edge "
            + idComponent
            + " source=\"" + this.source.getId() + "\""
            + " target=\"" + this.target.getId() + "\"";
        s += super.toString(state);
        s += " />";
		return s;
	}

	@Override
	public int compareTo(Edge e) {
        Double thisDouble = new Double( this.getWeight() );
        Double otherDouble = new Double( e.getWeight() );
		return thisDouble.compareTo( otherDouble );
	}
}

//  [Last modified: 2015 07 29 at 01:04:40 GMT]
