package edu.ncsu.csc.Galant.graph.component;

import edu.ncsu.csc.Galant.GalantException;

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
public abstract class GraphElement {

    public static final String WEIGHT = "weight";
    public static final String LABEL = "label";
    public static final String COLOR = "color";
    
    private AttributeList attributes;
	private GraphState graphCurrentState;
	private List<ElementState> states;

    public GraphElement() {
        attributes = new AttributeList();
        states = new ArrayList<ElementState>;
    }

    /**
     * @return a new state for this element; the new state will be identical
     * to the current (latest one) except that it will be tagged with the
     * current graph state number; subsequent changes to this GraphElement
     * will take place in the new state.
     */
    private ElementState newState() {
		graphCurrentState.incrementState();
		ElementState latest = latestState();
		ElementState elementState
            = new ElementState(latest, this.graphCurrentState);
		
        LogHelper.logDebug( "newState (elememt) = " + elementState );
		return elementState;
    }

    /**
     * adds the given state to the list of states for this element and
     * prompts synchronization with the master thread to indicate that the
     * changes corresponding to the added state are completed (contingent on
     * whether we're in the middle of a step -- if locked, then addState will
     * not result in synchronization);
     */
	public void addState(ElementState elementState) {
		states.add(elementState);
        // the pauseExecution call will be replaced by something different
        // when threading is sorted out
		graphCurrentState.pauseExecution();
	}

	public boolean setAttribute(String key, Integer value) {
        ElementState newState = newState();
        return newState.setAttribute(key, value);
	}
	public Integer getIntegerAttribute(String key) {
		return latestState().getAttributes().getInteger(key);
	}
	public Integer getIntegerAttribute(int state, String key) {
        validState = getLatestValidState(state);
		return validState == null ? null : validState.getAttributes().getInteger(key);
	}


	public boolean setAttribute(String key, Double value) {
        ElementState newState = newState();
        return newState.setAttribute(key, value);
	}
	public Double getDoubleAttribute(String key) {
		return attributes.getDouble(key);
	}
	public Double getDoubleAttribute(int state, String key) {
        validState = getLatestValidState(state);
		return validState == null ? null : validState.getAttributes().getDouble(key);
	}

	public boolean setBooleanAttribute(String key, Boolean value) {
        ElementState newState = newState();
        return newState.setAttribute(key, value);
	}
	public Boolean getBooleanAttribute(String key) {
		return latestState().getAttributes().getBoolean(key);
	}
	public Boolean getBooleanAttribute(int state, String key) {
        validState = getLatestValidState(state);
		return validState == null ? null : validState.getAttributes().getBoolean(key);
	}

	public boolean setStringAttribute(String key, String value) {
        ElementState newState = newState();
        return newState.setAttribute(key, value);
	}
	public String getStringAttribute(String key) {
		return latestState().getAttributes().getString(key);
	}
	public String getStringAttribute(int state, String key) {
        validState = getLatestValidState(state);
		return validState == null ? null : validState.getAttributes().getString(key);
	}

    public void removeAttribute(String key) {
        ElementState newState = newState();
        newState.removeAttribute(key);
    }
	
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
        setDoubleAttribute(WEIGHT, weight);
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
        setStringAttribute(LABEL, label);
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
        setStringAttribute(COLOR, color);
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
	
}

//  [Last modified: 2015 07 23 at 21:50:21 GMT]
