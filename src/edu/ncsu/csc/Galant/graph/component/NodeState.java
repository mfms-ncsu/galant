package edu.ncsu.csc.Galant.graph.component;
import java.awt.*;              // for Point
import edu.ncsu.csc.Galant.GraphDispatch; 

/**
 * The state of an <code>Node</code>, used akin to a frame in an animation. As the animation
 * advances, the subsequent <code>NodeState</code> is evoked to be displayed in
 * the animation window.
 * @author Jason Cockrell, Ty Devries, Alex McCabe, Michael Owoc
 *
 */
public class NodeState {

	private int state;
	
	private boolean selected;
	private boolean visited;
			
	private int id;	
	private double weight = 0;
	
	private String color;			
	private String label;
    private Point position;

    /** for layered graphs; should probably use inheritance */
    private int layer;
    private int positionInLayer;
	
	private boolean deleted = false;
	
    /**
     * This serves as essentially a copy constructor: creates the new object
     * in a different graph state and copies all the
     * information for the node (state) - except, of course, the state
     */
    public NodeState( NodeState ns, GraphState s ) {
        this.state = s.getState();
        this.selected = ns.selected;
        this.visited = ns.visited;
        this.id = ns.id;
        this.weight = ns.weight;
        this.color = ns.color;
        this.label = ns.label;
        this.position = ns.position;
        this.layer = ns.layer;
        this.positionInLayer = ns.positionInLayer;
        this.deleted = ns.deleted;
    }

	public NodeState(GraphState s, int id) {
		this.state = s.getState();
		
		this.visited = false;
		this.selected = false;
		
		this.color = "#000000";
		this.id = id;
		this.label = null;
	}
	
    /**
     * Sets up the various attributes of a node (the work is done by the
     * second version below.
     *
     * (mfms: added the position attribute to allow for movement of nodes as
     * part of an algorithm)
     */
	public NodeState(GraphState s, boolean _highlighted, boolean _visited, int _id, double _weight, String _color, String _label, Point _position) {
		this(s.getState(), _highlighted, _visited, _id, _weight, _color, _label,_position, false);
	}

	public NodeState( int s,
                      boolean _highlighted,
                      boolean _visited,
                      int _id,
                      double _weight,
                      String _color,
                      String _label,
                      Point _position,
                      boolean _deleted
                      ) {
		this.state = s;
		
		this.selected = _highlighted;
		this.visited = _visited;
		this.id = _id;
		this.weight = _weight;
		this.color = _color;
		this.label = _label;
        this.position = _position;
		
		this.deleted = _deleted;
	}
	
    /**
     * Constructor for layered graphs; nodes are never added or deleted in
     * this context so there is no need for a separate constructor for the
     * deleted attribute.
     *
     * @todo LayeredGraph should extend Graph, but this may get complicated
     * because you'd need NodeInLayeredGraph
     */
	public NodeState( GraphState s,
                      boolean _highlighted,
                      boolean _visited,
                      int _id,
                      double _weight,
                      String _color,
                      String _label,
                      int _layer,
                      int _positionInLayer
                      ) {
		this.state = s.getState();
		
		this.selected = _highlighted;
		this.visited = _visited;
		this.id = _id;
		this.weight = _weight;
		this.color = _color;
		this.label = _label;
        this.layer = _layer;
        this.positionInLayer = _positionInLayer; 
		
		this.deleted = false;
	}

	public boolean isSelected() {
		return selected;
	}
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	public boolean isVisited() {
		return visited;
	}
	public void setVisited(boolean visited) {
		this.visited = visited;
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
	public Point getPosition() {
		return position;
	}
	public void setPosition(Point position) {
		this.position = position;
	}
    public void setX( int x ) {
        this.position = new Point( x, this.position.y );
    }
    public void setY( int y ) {
        this.position = new Point( this.position.x, y );
    }
    public int getLayer() {
        return layer;
    }
    public int getPositionInLayer() {
        return positionInLayer;
    }
	public void setLayer( int layer ) {
		this.layer = layer;
	}
	public void setPositionInLayer( int positionInLayer ) {
		this.positionInLayer = positionInLayer;
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
	
    public String toString() {
        String s = "[ "
            + this.getState()
            + ", id=" + this.getId()
            + ", weight=" + this.getWeight()
            + ", label=" + label
            + ", position=" + this.getPosition()
            + ", color=" + this.getColor();
        
        if ( GraphDispatch.getInstance().getWorkingGraph().isLayered() )
            s += ", layer=" + this.getLayer()
                + ", positionInLayer=" + this.getPositionInLayer();
        s += " ]";
        return s;
    }
	
}

//  [Last modified: 2015 05 26 at 15:20:46 GMT]
