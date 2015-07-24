package edu.ncsu.csc.Galant.graph.component;
import java.awt.Point;
import edu.ncsu.csc.Galant.GraphDispatch; 

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
    private AttributeList myList;
    public AttributeList getAttributeList() { return myList; }

    /**
     * Constructor for a state of this element during any given algorithm state.
     */
    public GraphElementState(GraphState algorithmState, AttributeList list) {
        this.algorithmState = algorithmState;
        this.myList = list;
    }

    /**
     * This serves essentially as a copy constructor: creates the new object
     * in a different graph state and copies all the information for the node
     * (state) - except, of course, the state
     */
    public GraphElementState(GraphElementState elementState, GraphState algorithmState) {
        this.algorithmState = algorithmState;
        this.state = algorithmState.getState();
        this.myList = elementState.getAttributeList();
    }

    /************** Integer attributes ***************/
	public boolean setAttribute(String key, Integer value) {
        return myList.set(key, value);
	}
	public Integer getIntegerAttribute(String key) {
		return myList.getInteger(key);
	}

    /************** Double attributes ***************/
	public boolean setAttribute(String key, Double value) {
        return myList.set(key, value);
	}
	public Double getDoubleAttribute(String key) {
		return myList.getDouble(key);
	}

    /************** Boolean attributes ***************/
	public boolean setAttribute(String key, Boolean value) {
        return myList.set(key, value);
	}
	public Boolean getBooleanAttribute(String key) {
		return myList.getBoolean(key);
	}

    /************** String attributes ***************/
	public boolean setAttribute(String key, String value) {
        return myList.set(key, value);
	}
	public String getStringAttribute(String key) {
		return myList.getString(key);
	}

    /**
     * Removes the attribute with the given key from the list and updates
     * state information appropriately.
     */
    public void removeAttribute(String key) {
        myList.remove(key);
    }
}
 
//  [Last modified: 2015 07 24 at 21:55:45 GMT]
