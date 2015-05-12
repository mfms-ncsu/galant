package edu.ncsu.csc.Galant.graph.component;

/**
 * The state of an <code>Edge</code>, used akin to a frame in an animation. As the animation
 * advances, the subsequent <code>EdgeState</code> is evoked to be displayed in
 * the animation window.
 * @author Jason Cockrell, Ty Devries, Alex McCabe, Michael Owoc
 *
 */
public class EdgeState {

	private int state;
	
	private boolean highlighted;
	private double weight;
	
	private Node source;
	private Node destination;
	
	private int id;
	private String color;
	private String label;
	
	private boolean deleted = false;
	
    /** 
     * Creates a new edge (state) using the given graph state
     */
    public EdgeState( EdgeState es, GraphState s ) {
        this.state = s.getState();
        this.highlighted = es.highlighted;
        this.source = es.source;
        this.destination = es.destination;
        this.id = es.id;
        this.weight = es.weight;
        this.color = es.color;
        this.label = es.label;
        this.deleted = es.deleted;
    }

	public EdgeState(GraphState s) {
		this.color = "#000000";
		state = s.getState();
	}
	
	public EdgeState(GraphState s, boolean _highlighted, double _weight, Node _source, Node _dest, int _id, String _color, String _label, boolean deleted) {
		this(s.getState(), _highlighted, _weight, _source, _dest, _id, _color, _label, deleted);
	}
	
	public EdgeState(int s, boolean _highlighted, double _weight, Node _source, Node _dest, int _id, String _color, String _label, boolean deleted) {
		this.state = s;
		
		this.highlighted = _highlighted;
		this.weight = _weight;
		this.source = _source;
		this.destination = _dest;
		this.id = _id;
		this.color = _color;
		this.label = _label;
		this.deleted = deleted;
	}
	
	public EdgeState(GraphState s, int id, Node _source, Node _dest) {
		this.state = s.getState();
		
		this.highlighted = false;
		this.weight = 0;
		this.source = _source;
		this.destination = _dest;
		this.id = id;
		this.color = "#000000";
		this.label = null;
	}
	
	
	public boolean isHighlighted() {
		return highlighted;
	}
	public void setHighlighted(boolean highlighted) {
		this.highlighted = highlighted;
	}
	public double getWeight() {
		return weight;
	}
	public void setWeight(double weight) {
		this.weight = weight;
	}
    public boolean hasWeight() {
        Double weightAsObject = weight;
        return ! weightAsObject.equals( Graph.NOT_A_WEIGHT );
    }
    public void clearWeight() {
        weight = Graph.NOT_A_WEIGHT;
    }
	public Node getSource() {
		return source;
	}
	public void setSource(Node source) {
		this.source = source;
	}
	public Node getDestination() {
		return destination;
	}
	public void setDestination(Node destination) {
		this.destination = destination;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getColor() {
		return color;
	}
	public void setColor(String color) {
		this.color = color;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
    public boolean hasLabel() {
        return label != null && ! label.equals( Graph.NOT_A_LABEL );
    }
    public void clearLabel() {
        label = Graph.NOT_A_LABEL;
    }
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	public boolean isDeleted() {
		return deleted;
	}
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
	
}

//  [Last modified: 2014 07 08 at 19:18:37 GMT]
