package edu.ncsu.csc.Galant.graph.component;

import java.util.List;
import java.util.ArrayList;

import edu.ncsu.csc.Galant.GalantException;
import edu.ncsu.csc.Galant.GraphDispatch;
import edu.ncsu.csc.Galant.logging.LogHelper;

/**
 * Abstract class containing graph element manipulation methods
 * @author Jason Cockrell, Ty Devries, Alex McCabe, Michael Owoc,
 *         completely rewritten by Matthias Stallmann
 */
public class GraphElement {

    public static final String WEIGHT = "weight";
    public static final String LABEL = "label";
    public static final String COLOR = "color";
    public static final String DELETED = "deleted";
    public static final String HIGHLIGHTED = "highlighted";
    
    /**
     * The graph with which this element is associated.
     */
    protected Graph graph;

    /**
     * The state of the graph corresponding to the most recent state of this
     * element. Other elements may have changed since the last change of this
     * one, but the state of the graph changes only as the result of an
     * algorithm step.
     *
     * @todo In future we may want an algorithm state independent of a graph
     * state.
     */
	protected GraphState algorithmState;

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
    public GraphElement(Graph graph, GraphState algorithmState) {
        LogHelper.logDebug("-> GraphElement, state = " + algorithmState.getState());
        states = new ArrayList<GraphElementState>();
        this.graph = graph;
        this.algorithmState = algorithmState;
        this.addState(new GraphElementState(algorithmState));
        LogHelper.logDebug("<- GraphElement, element = " + this);
    }

    /**
     * @return a new state for this element; the new state will be identical
     * to the current (latest one) except that it will be tagged with the
     * current graph state number -- after an incrementState(); subsequent
     * changes to this GraphElement will take place in the new state.
     *
     * @todo if this is called during parsing or editing mode, no state
     * change is necessary, so this should just return the current one and
     * not do an incrementState()
     */
    private GraphElementState newState() {
		algorithmState.incrementState();
		GraphElementState latest = latestState();
		GraphElementState elementState
            = new GraphElementState(latest, this.algorithmState);
		
        LogHelper.logDebug( "newState (element) = " + elementState.getState() );
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
     *
     * @todo Once newState() is fixed appropriately, this method should do
     * nothing in parsing or edit mode
     */
	private void addState(GraphElementState stateToAdd) {
        LogHelper.enterMethod(getClass(), "addState, state number  = "
                              + stateToAdd.getState());
        int stateNumber = stateToAdd.getState();
        boolean found = false;
        for ( int i = states.size() - 1; i >= stateNumber; i-- ) {
            GraphElementState state = states.get(i);
            LogHelper.logDebug("addState loop, i = " + i + ", state(i) = " + state.getState());
            if ( state.getState() == stateNumber ) {
                states.set(i, stateToAdd);
                found = true;
                break;
            }
        }
        if ( ! found ) {
            states.add(stateToAdd);
            if (  GraphDispatch.getInstance().isAnimationMode() ) {
                stateToAdd.getAlgorithmState().pauseExecution();
            }
        }
        LogHelper.exitMethod(getClass(), "addState, found = " + found);
	}

    /************** Integer attributes ***************/
	public boolean set(String key, Integer value) {
        GraphElementState newState = newState();
        boolean found = newState.set(key, value);
        addState(newState);
        return found;
	}
	public Integer getInteger(String key) {
		return latestState().getAttributes().getInteger(key);
	}
	public Integer getInteger(int state, String key) {
        GraphElementState validState = getLatestValidState(state);
		return validState == null ? null : validState.getAttributes().getInteger(key);
	}


    /************** Double attributes ***************/
	public boolean set(String key, Double value) {
        GraphElementState newState = newState();
        boolean found = newState.set(key, value);
        addState(newState);
        return found;
	}
	public Double getDouble(String key) {
		return latestState().getAttributes().getDouble(key);
	}
	public Double getDouble(int state, String key) {
        GraphElementState validState = getLatestValidState(state);
		return validState == null ? null : validState.getAttributes().getDouble(key);
	}

    /************** Boolean attributes ***************/
	public boolean set(String key, Boolean value) {
        GraphElementState newState = newState();
        boolean found = newState.set(key, value);
        addState(newState);
        return found;
	}

    /**
     * For boolean attributes, assume that the absense of an attribute means
     * that it's false.
     */
	public Boolean getBoolean(String key) {
        Boolean value = latestState().getAttributes().getBoolean(key);
        if ( value == null ) return false;
        return value;
	}
	public Boolean getBoolean(int state, String key) {
        GraphElementState validState = getLatestValidState(state);
        if ( validState == null ) return false;
        Boolean value = validState.getAttributes().getBoolean(key);
        if ( value == null ) return false;
		return value;
	}

    /************** String attributes ***************/
	public boolean set(String key, String value) {
        GraphElementState newState = newState();
        boolean found = newState.set(key, value);
        addState(newState);
        return found;
	}
	public String getString(String key) {
		return latestState().getAttributes().getString(key);
	}
	public String getString(int state, String key) {
        GraphElementState validState = getLatestValidState(state);
		return validState == null ? null : validState.getAttributes().getString(key);
	}

    /**
     * Removes the attribute with the given key from the list and updates
     * state information appropriately.
     */
    public void remove(String key) {
        GraphElementState newState = newState();
        newState.remove(key);
        addState(newState);
    }
	
    public boolean isDeleted() {
        return getBoolean(DELETED);
    }
    public boolean isDeleted(int state) {
        return getBoolean(state, DELETED);
    }
    /**
     * @param true iff this element is to be deleted in the current state.
     */
    public void setDeleted(boolean deleted) {
        if (deleted) {
            set(DELETED, true);
        }
        else remove(DELETED);
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
        return getDouble(WEIGHT);
    }
	public Double getWeight(int state) {
        return getDouble(state, WEIGHT);
    }

    /**
     * Need to convert the argument to a double because it eventually
     * converts to Double -- see cleanupAfterParsing()
     */
	public void setWeight(double weight) {
        set(WEIGHT, (double) weight);
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
        remove(WEIGHT);
    }
	
    /**************************** labels *************************/
	public String getLabel() {
        return getString(LABEL);
    }
	public String getLabel(int state) {
        return getString(state, LABEL);
    }

	public void setLabel(String label) {
        set(LABEL, label);
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
        remove(LABEL);
    }
	
    /**************************** colors *************************/
	public String getColor() {
        return getString(COLOR);
    }
	public String getColor(int state) {
        return getString(state, COLOR);
    }

	public void setColor(String color) {
        set(COLOR, color);
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
        remove(COLOR);
    }

    /**************************** highlighting ***********************/
    /**
     * In case it matters, setSelected simply changes the value of the
     * HIGHLIGHTED attribute (if it's already there) while unHighlight
     * actually removes the entry corresponding to that attribute. The effect
     * is the same, but the nature of the list traversal might not be.
     */
	public boolean isSelected() {
        return getBoolean(HIGHLIGHTED);
    }
	public Boolean isSelected(int state) {
        return getBoolean(state, HIGHLIGHTED);
    }
	public void setSelected(Boolean highlighted) {
        set(HIGHLIGHTED, highlighted);
    }
	public boolean isHighlighted() {
        return getBoolean(HIGHLIGHTED);
    }
	public Boolean isHighlighted(int state) {
        return getBoolean(state, HIGHLIGHTED);
    }
	public void highlight() {
        set(HIGHLIGHTED, true);
    }
	public void unHighlight() {
        remove(HIGHLIGHTED);
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
        Double weight = getDouble(WEIGHT);
        if ( getWeight() == null ) {
            Integer weightAsInteger = getInteger(WEIGHT);
            if ( weightAsInteger != null ) {
                set(WEIGHT, (double) weightAsInteger);
            }
        }
    }

    /**
     * Creates a string that can be used to form the "interior" of a GraphML
     * representation of the attributes associated with this state.
     */
    public String toString() {
        return latestState().toString();
    }

    /**
     * Like toString(), except that it omits the "x" and "y" attributes; to
     * be used in cases where these attributes are superceded by the
     * corresponding fixed ones of a Node.
     */
    public String attributesWithoutPosition() {
        return latestState().attributesWithoutPosition();
    }

    /**
     * Like toString(), except that it omits the "id" attribute; to be used
     * in cases where the id is optional, as is the case with an Edge
     */
    public String attributesWithoutId() {
        return latestState().attributesWithoutId();
    }

    /**
     * Same as the unparameterized version except that the attributes are
     * ones of the latest valid state. This is used when exporting the state
     * of the graph in the middle of execution.
     */
    public String toString(int state) {
        return getLatestValidState(state).toString();
    }
}

//  [Last modified: 2015 09 28 at 01:35:39 GMT]
