package edu.ncsu.csc.Galant.graph.component;
import java.awt.Point;
import edu.ncsu.csc.Galant.GraphDispatch; 
import edu.ncsu.csc.Galant.logging.LogHelper;

/**
 * The state of a GraphElement, used akin to a frame in an animation. As the
 * animation advances, the subsequent GraphElementState is evoked to be
 * displayed in the animation window.
 *
 * Based on the NodeState and EdgeState classes:
 * @author Jason Cockrell, Ty Devries, Alex McCabe, Michael Owoc
 *
 * Adapted for a more general, flexible model by Matthias Stallmann
 */
public class GraphElementState {

    /**
     * The sequence number of this state.
     * @todo This is redundant information and easy to lose track of. Either
     * we should access everything by actual state or by state number.
     */
	private int state;

    public int getState() { return state; }
	
    /**
     * The actual algorithm state corresponding to this state.
     *
     * @todo Once threading gets sorted out, this wil refer to an algorithm
     * execution state, or be deprecated if the execution state can be
     * retrieved from its sequence number.
     */
    private GraphState algorithmState;

    public GraphState getAlgorithmState() { return algorithmState; }

    /**
     * Attribute list for the snapshot representing this element state
     */
    private AttributeList attributes;
    public AttributeList getAttributes() { return attributes; }

    /**
     * Constructor for a state of this element during any given algorithm state.
     */
    public GraphElementState(GraphState algorithmState, AttributeList list) {
        this.algorithmState = algorithmState;
        this.state = algorithmState.getState();
        this.attributes = list;
    }

    /**
     * This serves essentially as a copy constructor: creates the new object
     * in a different graph state and copies all the information for the node
     * (state) - except, of course, the state
     */
    public GraphElementState(GraphElementState elementState, GraphState algorithmState) {
        this.algorithmState = algorithmState;
        this.state = algorithmState.getState();
        this.attributes = elementState.getAttributes().duplicate();
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
	public Boolean getBooleanAttribute(String key) {
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
     * Creates a string that can be used to form the "interior" of a GraphML
     * representation of this element.
     */
    public String toString() {
        String s = " ";
        LogHelper.logDebug("GraphElementState toString, attributes = "
                           + attributes.attributes);
        for ( Attribute attribute : attributes.getAttributes() ) {
            s += attribute + " ";
        }
        return s;
    }
}

//  [Last modified: 2015 08 13 at 01:02:59 GMT]
