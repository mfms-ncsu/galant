package edu.ncsu.csc.Galant.graph.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import edu.ncsu.csc.Galant.GalantException;

/**
 * Edge graph object. Connects two <code>Node<code>s, and can be directored or undirected.
 * For undirected graphs, "source" and "destination" are meaningless.
 * @author Jason Cockrell, Ty Devries, Alex McCabe, Michael Owoc
 *
 */
public class Edge extends GraphElement implements Comparable<Edge> {
	private List<EdgeState> edgeStates;
    Integer Id;
    Node sourceNode;
    Node targetNode;
	
    /**
     * To add a node while editing: id is the next available one as
     * determined by the graph.
     */
    public Edge(GraphState algorithmState, Integer id, Node sourceNode, Node targetNode) {
        super(algorithmState.getGraph(), algorithmState);
        this.id = id;
        this.sourceNode = sourceNode;
        this.targetNode = targetNode;
    }

    /**
     * Getters for source and target (destination).
     */
    public Integer getId() {
        return id;
    }

    public Node getSourceNode() {
        return sourceNode;
    }

    public Node getTargetNode() {
        return targetNode;
    }

    /**
     * Makes sure that all the attributes specific to edges are properly
     * initialized.
     */
    public void initializeAfterParsing()
    throws GalantException {
        LogHelper.enterMethod( getClass(), "initializeAfterParsing: " + this );
        super.initializeAfterParsing();
        this.id = getInteger("id");
        this.source = getInteger("source");
        if (source == null) {
            throw new GalantException("Missing or malformed source: " + source 
                                           + " when processing edge " + this);
        }
        this.target = getInteger("target");
        if (source == null) {
            throw new GalantException("Missing or malformed source: " + source 
                                           + " when processing edge " + this);
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
        String idComponent = (id == null) ? "" : "id=\"" + this.getId();
		String s = "<edge "
            + idComponent
            + "\" source=\"" + this.sourceNode
            + "\" target=\"" + this.targetNode;
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
		String s = "<edge "
            + idComponent
            + "\" source=\"" + this.sourceNode
            + "\" target=\"" + this.targetNode;
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

//  [Last modified: 2015 07 27 at 01:27:35 GMT]
