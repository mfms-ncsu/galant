package edu.ncsu.csc.Galant.graph.component;
import java.awt.Point;
import edu.ncsu.csc.Galant.GraphDispatch; 
import edu.ncsu.csc.Galant.logging.LogHelper;

/**
 * The state of a Graph, used akin to a frame in an animation. As the
 * animation advances, the subsequent GraphState is evoked to be
 * displayed in the animation window.
 *
 * @author Matthias Stallmann
 */
public class GraphState {

    /**
     * The sequence number (algorithm state) of this state.
     */
	private int state;

    public int getState() { return state; }
	
    /**
     * Attribute list for the snapshot representing this  state
     */
    protected AttributeList attributes;
    public AttributeList getAttributes() { return attributes; }

    private GraphDispatch dispatch;

    /**
     * Constructor used during parsing and editing, when no attributes are
     * known yet.
     */
    public GraphState() {
        this.dispatch = GraphDispatch.getInstance();
        this.state = dispatch.getAlgorithmState();
        this.attributes = new AttributeList();
    }

    /**
     * This serves essentially as a copy constructor: creates the new object
     * in a different algorithm state and copies all the information for the
     * node (state) - except, of course, the state
     */
    public GraphState(GraphState State) {
        this.dispatch = GraphDispatch.getInstance();
        this.state = dispatch.getAlgorithmState();
        this.attributes = State.getAttributes().duplicate();
    }

    /************** Integer attributes ***************/
	public boolean set(String key, Integer value) {
        return attributes.set(key, value);
	}
	public Integer getIntegerAttribute(String key) {
		return attributes.getInteger(key);
	}

    /************** Double attributes ***************/
	public boolean set(String key, Double value) {
        return attributes.set(key, value);
	}
	public Double getDoubleAttribute(String key) {
		return attributes.getDouble(key);
	}

    /************** Boolean attributes ***************/
	public boolean set(String key, Boolean value) {
        return attributes.set(key, value);
	}
    public boolean set(String key) {
        return attributes.set(key, true);
    }
	public Boolean getBooleanAttribute(String key) {
		return attributes.getBoolean(key);
	}
    public Boolean is(String key) {
        return attributes.getBoolean(key);
    }

    /************** String attributes ***************/
	public boolean set(String key, String value) {
        return attributes.set(key, value);
	}
	public String getStringAttribute(String key) {
		return attributes.getString(key);
	}

    /**
     * Removes the attribute with the given key from the list and updates
     * state information appropriately.
     */
    public void remove(String key) {
        attributes.remove(key);
    }
 
    /**
     * Creates a string that can be used to form the GraphML representation
     * of the graph as it exists in this state (relevant during animation only)
     * @todo needs to be implemented properly
     */
    public String xmlString() {
        String s = " ";
        for ( Attribute attribute : attributes.getAttributes() ) {
            s += attribute + " ";
        }
        return s;
    }

    public String toString() {
        String s = "{graphState ";
        s += "" + state + " ";
        s += attributes.getAttributes();
        s += "}";
        return s;
    }
}

//  [Last modified: 2016 10 17 at 12:52:59 GMT]
