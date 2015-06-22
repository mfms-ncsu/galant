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

	private Map<String, Object> attributes;
	
	private GraphState graphCurrentState;
	private List<EdgeState> edgeStates;
	
	public void addEdgeState(EdgeState e){
		edgeStates.add(e);
		graphCurrentState.synchronizedWait();
	}
	
	public Edge(GraphState currentState, int id, Node _source, Node _dest) {
		this.graphCurrentState = currentState;
		edgeStates = new ArrayList<EdgeState>();
		attributes = new HashMap<String, Object>();
		
		EdgeState es = new EdgeState(currentState, id, _source, _dest);
		addEdgeState(es);
	}
	
	public Edge(GraphState currentState, int _id, Node _source, Node _target, boolean _highlighted, double _weight, String _color, String _label) {
		graphCurrentState = currentState;
		edgeStates = new ArrayList<EdgeState>();
		attributes = new HashMap<String, Object>();
		
		EdgeState es = new EdgeState(currentState, _highlighted, _weight, _source, _target, _id, _color, _label, false);
		addEdgeState(es);;
	}

	public boolean inScope() {
		return !(isDeleted());
	}
	
	public boolean inScope(int state)
    {
		return isCreated(state) && !isDeleted(state);
	}
	
	/**
	 * @return true if the edge is highlighted, false otherwise
	 */
	public boolean isSelected() {
		return latestState().isHighlighted();
	}

	/**
	 * @return true if the edge is highlighted, false otherwise
	 */
	public boolean isSelected(int state)
    {
		EdgeState es = getLatestValidState(state);
		return es == null ? false : es.isHighlighted();
		
	}
	
    public boolean isHighlighted() {
        return isSelected();
    }
	
    public boolean isHighlighted(int state)
        throws GalantException
    {
        return isSelected(state);
    }
	
	/**
	 * @param selected toggles highlighting on the edge
	 */
	public void setSelected(boolean selected) {
		EdgeState es = newState();
		es.setHighlighted(selected);
		
		addEdgeState(es);;
	}
	
    public void highlight() {
        setSelected( true );
    }

    public void unHighlight() {
        setSelected( false );
    }

	/**
	 * @return the weight of the edge
	 */
	@Override
	public double getWeight() {
		return latestState().getWeight();
	}
	
	@Override
	public double getWeight(int state) {
        EdgeState es = getLatestValidState(state);
        return es==null ? null : es.getWeight();
    }

	/**
	 * @param weight the weight of the edge
	 */
	@Override
        public void setWeight(double weight)
        {
            EdgeState es = newState();
            es.setWeight(weight);
            addEdgeState(es);;
        }

    /**
     * @return true if the edge has a weight in the current state
     */
    public boolean hasWeight() {
        return latestState().hasWeight();
    }

    /**
     * @return true if this edge had a non-empty weight at the given state
     */
    @Override
        public boolean hasWeight(int state)
        {
            EdgeState es = getLatestValidState(state);
            return es == null ? false : es.hasWeight();
        }

	/**
	 * Postcondition: hasWeight() == false
	 */
	@Override
        public void clearWeight()
        {
            EdgeState es = newState();
            es.clearWeight();
            addEdgeState(es);;
        }

	/**
	 * @return the source node. If the graph is undirected, source and destination nodes are treated similarly.
	 */
	public Node getSourceNode() {
		return latestState().getSource();
	}
	
	public Node getSourceNode(int state)
    {
		EdgeState es = getLatestValidState(state);
		return es==null ? null : es.getSource();
	}

	/**
	 * @param sourceNode the source node to set
	 */
	public void setSourceNode(Node sourceNode) {
		EdgeState es = newState();
		es.setSource(sourceNode);
		addEdgeState(es);;
	}

	/**
	 * @return the destination node. If the graph is undirected, source and destination nodes are treated similarly.
	 */
	public Node getDestNode() {
		return latestState().getDestination();
	}

	public Node getDestNode(int state) 
    {
		EdgeState es = getLatestValidState(state);
		return es==null ? null : es.getDestination();
	}

	/**
	 * @param destNode the destination node to set
	 */
	public void setDestNode(Node destNode) {
		EdgeState es = newState();
		es.setDestination(destNode);
		addEdgeState(es);;
	}

	public Node getOtherEndpoint(Node in) {
		EdgeState es = latestState();
		
		if (es.getSource().equals(in)) {
			return es.getDestination();
		} else if (es.getDestination().equals(in)) {
			return es.getSource();
		} else {
			return null;
		}
	}
	
	/**
	 * @return the unique ID of the edge
	 */
	public int getId() {
		return latestState().getId();
	}
	
	public int getId(int state)
    {
		EdgeState es = getLatestValidState(state);
		return es==null ? null : es.getId();
	}

	/**
	 * @param id the unique ID to set
	 */
	public void setId(int id) {
		EdgeState es = newState();
		es.setId(id);
		addEdgeState(es);;
	}

	/**
	 * @return the color of the edge stored in six-digit hex representation
	 */
	@Override
	public String getColor() {
		return latestState().getColor();
	}
	
	@Override
        public String getColor(int state)
        {
            EdgeState es = getLatestValidState(state);
            return es==null ? null : es.getColor();
        }

	/**
	 * @param color the color of the edge to set, stored in six-digit hex representation
	 */
	@Override
	public void setColor(String color) {
		EdgeState es = newState();
		es.setColor(color);
		addEdgeState(es);;
	}

	/**
	 * @return the label
	 */
	@Override
	public String getLabel() {
		return latestState().getLabel();
	}
	
	@Override
        public String getLabel(int state)
        {
            EdgeState es = getLatestValidState(state);
            return es==null ? null : es.getLabel();
        }

	/**
	 * @param label the label to set
	 */
	@Override
	public void setLabel(String label) {
		EdgeState es = newState();
		es.setLabel(label);
		addEdgeState(es);;
	}

    /**
     * @return true if the edge has a label in the current state
     */
    public boolean hasLabel() {
        return latestState().hasLabel();
    }

    /**
     * @return true if this edge had a non-empty label at the given state
     */
    @Override
        public boolean hasLabel(int state)
        {
            EdgeState es = getLatestValidState(state);
            return es == null ? false : es.hasLabel();
        }

	/**
	 * Postcondition: hasLabel() == false
	 */
	@Override
        public void clearLabel()
        {
            EdgeState es = newState();
            es.clearLabel();
            addEdgeState(es);;
        }
	
	public boolean isCreated(int state)
    {
		EdgeState ns = getLatestValidState(state);
		return ! (ns == null);
	}
	
	public boolean isDeleted() {
		return latestState().isDeleted();
	}
	
	public boolean isDeleted(int state)
    {
		EdgeState es = getLatestValidState(state);
		return es==null ? false : es.isDeleted();
	}

	public void setDeleted(boolean deleted) {
		EdgeState es = newState();
		es.setDeleted(deleted);
		addEdgeState(es);;
	}
	
	public void setStringAttribute(String key, String value) {
		attributes.put(key, value);
	}
	public String getStringAttribute(String key) {
		Object o = attributes.get(key);
		
		if (o != null && String.class.isInstance(o)) {
			return (String) o;
		}
		
		return null;
	}
	
	public void setIntegerAttribute(String key, Integer value) {
		attributes.put(key, value);
	}
	public Integer getIntegerAttribute(String key) {
		Object o = attributes.get(key);
		
		if (o != null && Integer.class.isInstance(o)) {
			return (Integer) o;
		}
		
		return null;
	}
	
	public void setDoubleAttribute(String key, Double value) {
		attributes.put(key, value);
	}
	public Double getDoubleAttribute(String key) {
		Object o = attributes.get(key);
		
		if (o != null && Double.class.isInstance(o)) {
			return (Double) o;
		}
		
		return null;
	}
	
	private EdgeState newState() {
		graphCurrentState.incrementState();
		EdgeState latest = edgeStates.get(edgeStates.size()-1);
		EdgeState es = new EdgeState( latest, this.graphCurrentState );
		
		return es;
	}
	
	private EdgeState latestState() {
		return edgeStates.get(edgeStates.size()-1);
	}
	
    /**
     * This method is vital for retrieving the most recent information about
     * an edge, where most recent is defined relative to a given time stamp,
     * as defined by forward and backward stepping through the animation.
     * @param stateNumber the numerical indicator (timestamp) of a state,
     * usually the current one in the animation
     * @return the latest instance of EdgeState that was created before the
     * given time stamp, or null if the edge did not exist before the time
     * stamp.
     */
	private EdgeState getLatestValidState( int stateNumber ) 
    {
		for ( int i = edgeStates.size() - 1; i >= 0; i-- ) {
			EdgeState es = edgeStates.get(i);
			if ( es.getState() <= stateNumber ) {
				return es;
			}
		}
		
        return null;
	}
	
	@Override
	public String toString() {
		String label = "";
		if (this.getLabel() != null) {
			label = this.getLabel();
		}
		
		Node sNode = this.latestState().getSource();
		Node tNode = this.latestState().getDestination();
		int sourceId = (sNode != null) ? sNode.getId() : -1;
		int targetId = (tNode != null) ? tNode.getId() : -1;
		double weight = this.latestState().getWeight();
		
		String s = "<edge "
				+ "id=\"" + this.getId() 
				+ "\" label=\"" + label  
				+ "\" weight=\"" + weight
				+ "\" source=\"" + sourceId 
				+ "\" target=\"" + targetId  
				+ "\" color=\"" + getColor() + "\" />";
		return s;
	}
	
	public String toString(int state) 
    {
        if ( ! inScope(state) ) {
            return "";
        }
        
        EdgeState es = getLatestValidState(state);

        int sourceId = (es.getSource() != null) ? es.getSource().getId() : -1;
        int targetId = (es.getDestination() != null) ? es.getDestination().getId() : -1;
			
        double weight = es.getWeight();
        String label = "";
        if (es.getLabel() != null) {
            label = this.getLabel();
        }
			
        String s = "<edge id=\"" + es.getId() +
            "\" weight=\"" + weight +
            "\" label=\"" + label + 
            "\" source=\"" + sourceId + 
            "\" target=\"" + targetId +  
            "\" color=\"" + es.getColor() + "\" />";
        return s;
    }

	@Override
	public int compareTo(Edge e) {
        Double thisDouble = new Double( this.getWeight() );
        Double otherDouble = new Double( e.getWeight() );
		return thisDouble.compareTo( otherDouble );
	}
	
	
}

//  [Last modified: 2015 05 20 at 19:40:31 GMT]
