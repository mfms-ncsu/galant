package edu.ncsu.csc.Galant.graph.component;

import java.util.List;
import java.util.ArrayList;

import edu.ncsu.csc.Galant.GalantException;
import edu.ncsu.csc.Galant.logging.LogHelper;

/**
 * Abstract class containing graph element manipulation methods
 * @author Jason Cockrell, Ty Devries, Alex McCabe, Michael Owoc
 *
 * @todo Instead of having specific attributes fixed, how about setting up
 * attributes as a map from name (String) to Object, where the latter can be
 * instanceof Boolean, Integer, Double, or String. Converting a Node
 * or Edge to a String for output is simple -- we just need to invoke
 * instanceOf to determine the type of the object and call the right
 * toString() method, implicitly or explicitly. Parsing is slightly more
 * complicated:
 *   - first determine whether the name matches one of the standard ones and
 *     interpret appropriately, as is done in the parser code already
 *   - otherwise, go through the possibilities in the following order:
 *     Integer, Double, Boolean, String   
 */
public class GraphElement {

    public static final String WEIGHT = "weight";
    public static final String LABEL = "label";
    public static final String COLOR = "color";
    public static final String DELETED = "deleted";
    public static final String HIGHLIGHTED = "highlighted";
    
    private AttributeList attributes;

    /**
     * The state of the graph corresponding to the most recent state of this
     * element. Other elements may have changed since the last change of this
     * one, but the state of the graph changes only as the result of an
     * algorithm step.
     *
     * @todo In future we may want an algorithm state independent of a graph
     * state.
     */
	private GraphState algorithmState;

    /**
     * The list of states that this element has been in up to this point --
     * essentially the list of all changes.
     */
	protected List<GraphElementState> states;

    /**
     * Constructor to be used during parsing; all additional information is
     * filled in by initializeAfterParsing(). The algorithm state is
     * initialized elsewhere, currently in Graph.
     */
    public GraphElement(GraphState algorithmState) {
        attributes = new AttributeList();
        states = new ArrayList<GraphElementState>();
        this.algorithmState = algorithmState;
    }

    /**
     * @return a new state for this element; the new state will be identical
     * to the current (latest one) except that it will be tagged with the
     * current graph state number; subsequent changes to this GraphElement
     * will take place in the new state.
     */
    private GraphElementState newState() {
		algorithmState.incrementState();
		GraphElementState latest = latestState();
		GraphElementState elementState
            = new GraphElementState(latest, this.algorithmState);
		
        LogHelper.logDebug( "newState (elememt) = " + elementState );
		return elementState;
    }

    /**
     * @return The last state on the list of states. This is the default for
     * retrieving information about any attribute.
     */
    public GraphElementState latestState() {
        return states.get(states.size() - 1); 
    }

    /**
     * This method is vital for retrieving the most recent information about
     * a graph element (node or edge), where most recent is defined relative
     * to a given time stamp, as defined by forward and backward stepping
     * through the animation.
     * @param stateNumber the numerical indicator (timestamp) of a state,
     * usually the current one in the animation
     * @return the latest instance of GraphElementState that was created before the
     * given time stamp, or null if the element did not exist before the time
     * stamp.
     */
	public GraphElementState getLatestValidState(int stateNumber)
    {
		for ( int i = states.size() - 1; i >= 0; i-- ) {
			GraphElementState state = states.get(i);
			if ( state.getState() <= stateNumber ) {
				return state;
			}
		}
        return null;
	}
	
	/**
     * Adds the given state to the list of states for this element. If there
     * is already a state having the same time stamp, there is no need to add
     * another one. Such a situation might arise if there are multiple state
     * changes to this element between a beginStep()/endStep() pair. Also
     * prompts synchronization with the master thread to indicate that the
     * changes corresponding to the added state are completed (contingent on
     * whether we're in the middle of a step -- if locked, then addState will
     * not result in synchronization);
     *
     * @invariant states are always sorted by state number.
     */
	private void addState(GraphElementState stateToAdd) {
        int stateNumber = stateToAdd.getState();
		for ( int i = states.size() - 1; i >= stateNumber; i-- ) {
			GraphElementState state = states.get(i);
			if ( state.getState() == stateNumber ) {
				states.set(i, stateToAdd);
				return;
			}
		}
		states.add(stateToAdd);
		stateToAdd.getAlgorithmState().pauseExecution();
	}

    /************** Integer attributes ***************/
	public boolean setAttribute(String key, Integer value) {
        GraphElementState newState = newState();
        boolean found = newState.setAttribute(key, value);
        addState(newState);
        return found;
	}
	public Integer getIntegerAttribute(String key) {
		return latestState().getAttributeList().getInteger(key);
	}
	public Integer getIntegerAttribute(int state, String key) {
        GraphElementState validState = getLatestValidState(state);
		return validState == null ? null : validState.getAttributeList().getInteger(key);
	}


    /************** Double attributes ***************/
	public boolean setAttribute(String key, Double value) {
        GraphElementState newState = newState();
        boolean found = newState.setAttribute(key, value);
        addState(newState);
        return found;
	}
	public Double getDoubleAttribute(String key) {
		return latestState().getAttributeList().getDouble(key);
	}
	public Double getDoubleAttribute(int state, String key) {
        GraphElementState validState = getLatestValidState(state);
		return validState == null ? null : validState.getAttributeList().getDouble(key);
	}

    /************** Boolean attributes ***************/
	public boolean setAttribute(String key, Boolean value) {
        GraphElementState newState = newState();
        boolean found = newState.setAttribute(key, value);
        addState(newState);
        return found;
	}
	public Boolean getBooleanAttribute(String key) {
		return latestState().getAttributeList().getBoolean(key);
	}
	public Boolean getBooleanAttribute(int state, String key) {
        GraphElementState validState = getLatestValidState(state);
		return validState == null ? null : validState.getAttributeList().getBoolean(key);
	}

    /************** String attributes ***************/
	public boolean setAttribute(String key, String value) {
        GraphElementState newState = newState();
        boolean found = newState.setAttribute(key, value);
        addState(newState);
        return found;
	}
	public String getStringAttribute(String key) {
		return latestState().getAttributeList().getString(key);
	}
	public String getStringAttribute(int state, String key) {
        GraphElementState validState = getLatestValidState(state);
		return validState == null ? null : validState.getAttributeList().getString(key);
	}

    /**
     * Removes the attribute with the given key from the list and updates
     * state information appropriately.
     */
    public void removeAttribute(String key) {
        GraphElementState newState = newState();
        newState.removeAttribute(key);
        addState(newState);
    }
	
    public boolean isDeleted() {
        return getBooleanAttribute(DELETED);
    }
    public boolean isDeleted(int state) {
        return getBooleanAttribute(state, DELETED);
    }
    /**
     * @param true iff this element is to be deleted in the current state.
     */
    public void setDeleted(boolean deleted) {
        if (deleted) {
            setAttribute(DELETED, true);
        }
        else removeAttribute(DELETED);
    }

    /**
     * @return true if this element existed in the latest state prior to the
     * given one.
     */
    public boolean isCreated(int state) {
        GraphElementState creationState = getLatestValidState(state);
        return creationState == null ? false : true;
    }

    /**
     * @return true if the element has been created but has not been
     * deleted.
     */
	public boolean inScope() {
		return ! isDeleted();
	}
	
	public boolean inScope(int state) {
		return isCreated(state) && ! isDeleted(state);
	}
	

    /**************************** weights **************************/
	public Double getWeight() {
        return getDoubleAttribute(WEIGHT);
    }
	public Double getWeight(int state) {
        return getDoubleAttribute(state, WEIGHT);
    }

	public void setWeight(Double weight) {
        setAttribute(WEIGHT, weight);
    }

    public boolean hasWeight() {
        return getWeight() != null;
    }
    public boolean hasWeight(int state) {
        return getWeight(state) != null;
    }
    /**
     * removes the weight from the property list
     */
    public void clearWeight() {
        removeAttribute(WEIGHT);
    }
	
    /**************************** labels *************************/
	public String getLabel() {
        return getStringAttribute(LABEL);
    }
	public String getLabel(int state) {
        return getStringAttribute(state, LABEL);
    }

	public void setLabel(String label) {
        setAttribute(LABEL, label);
    }

    public boolean hasLabel() {
        return getLabel() != null;
    }
    public boolean hasLabel(int state) {
        return getLabel(state) != null;
    }
    /**
     * removes the label from the property list
     */
    public void clearLabel() {
        removeAttribute(LABEL);
    }
	
    /**************************** colors *************************/
	public String getColor() {
        return getStringAttribute(COLOR);
    }
	public String getColor(int state) {
        return getStringAttribute(state, COLOR);
    }

	public void setColor(String color) {
        setAttribute(COLOR, color);
    }

    public boolean hasColor() {
        return getColor() != null;
    }
    public boolean hasColor(int state) {
        return getColor(state) != null;
    }
    /**
     * removes the color from the property list
     */
    public void clearColor() {
        removeAttribute(COLOR);
    }

    /**************************** highlighting ***********************/
    /**
     * In case it matters, setSelected simply changes the value of the
     * HIGHLIGHTED attribute (if it's already there) while unHighlight
     * actually removes the entry corresponding to that attribute. The effect
     * is the same, but the nature of the list traversal might not be.
     */
	public boolean isSelected() {
        return getBooleanAttribute(HIGHLIGHTED);
    }
	public Boolean isSelected(int state) {
        return getBooleanAttribute(state, HIGHLIGHTED);
    }
	public void setSelected(Boolean highlighted) {
        setAttribute(HIGHLIGHTED, highlighted);
    }
	public boolean isHighlighted() {
        return getBooleanAttribute(HIGHLIGHTED);
    }
	public Boolean isHighlighted(int state) {
        return getBooleanAttribute(state, HIGHLIGHTED);
    }
	public void highlight() {
        setAttribute(HIGHLIGHTED, true);
    }
	public void unHighlight() {
        removeAttribute(HIGHLIGHTED);
    }

    /**
     * Cleans up specific attributes and initializes important information in
     * an element-specific way. This allows the GraphMLParser to create each
     * element without knowing its attributes, then collect them in order of
     * appearance, and finally postprocess so that the essential ones are
     * initialized properly. Also establishes the initial state for this
     * element. 
     */
    public void initializeAfterParsing() throws GalantException {
        // the only attribute that may cause trouble is the weight, which
        // should be stored as a double but might show up as an integer in
        // the GraphML representation
        Double weight = getDoubleAttribute(WEIGHT);
        if ( getWeight() == null ) {
            Integer weightAsInteger = getIntegerAttribute(WEIGHT);
            if ( weightAsInteger != null ) {
                setAttribute(WEIGHT, (double) weightAsInteger);
            }
        }
    }

    /**
     * Creates a string that can be used to form the "interior" of a GraphML
     * representation of this element.
     */
    public String toString() {
        String s = " "; 
        for ( Attribute attribute : attributes.getAttributes() ) {
            s += attribute + " ";
        }
        return s;
    }
}

//  [Last modified: 2015 07 25 at 22:22:58 GMT]
