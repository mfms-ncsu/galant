package edu.ncsu.csc.Galant.graph.component;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

import edu.ncsu.csc.Galant.GalantException;
import edu.ncsu.csc.Galant.GraphDispatch;
import edu.ncsu.csc.Galant.logging.LogHelper;
import edu.ncsu.csc.Galant.algorithm.Terminate;
import edu.ncsu.csc.Galant.graph.datastructure.GraphElementComparator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract class containing graph element manipulation methods
 *
 * @author Jason Cockrell, Ty Devries, Alex McCabe, Michael Owoc, completely
 * rewritten by Matthias Stallmann
 */
public class GraphElement implements Comparable<GraphElement> {

    /**
     * @todo Put all standard attributes here, even those relevant only to Nodes
     * or only to Edges - they'll be simply ignored if they're not relevant.
     *
     * @todo Add the following standard display attributes. - "fill": fill color
     * for Nodes - "dotted": use dotted lines for edges or node outlines -
     * "dashed": similar to dotted
     */
    public static final String ID = "id";
    public static final String WEIGHT = "weight";
    public static final String LABEL = "label";
    public static final String COLOR = "color";
    public static final String DELETED = "deleted";
    public static final String HIGHLIGHTED = "highlighted";
    public static final String HIDDEN = "hidden";
    public static final String HIDDEN_LABEL = "hiddenLabel";
    public static final String HIDDEN_WEIGHT = "hiddenWeight";

    /**
     * The graph with which this element is associated.
     */
    protected Graph graph;

    protected GraphDispatch dispatch;

    /**
     * The list of states that this element has been in up to this point --
     * essentially the list of all changes.
     */
    protected List<GraphElementState> states;

    /**
     * Create a clean copy, to be used at the start of algorithm execution
     */
    public GraphElement() {
    }

    /**
     * Called (indirectly) in GraphMLParser before the attributes of the element
     * have been processed (see the constructor with AttributeList below)
     */
    public GraphElement(Graph graph) {
        this.dispatch = GraphDispatch.getInstance();
        this.states = new ArrayList<GraphElementState>();
        this.graph = graph;
        try {
            this.addState(new GraphElementState());
        } catch (Terminate t) { // should not happen
            t.printStackTrace();
        }
    }

    /**
     * This constructor is used during parsing. The element is initialized
     * with a single state whose attributes come from the list L
     */
    public GraphElement(Graph graph, AttributeList L) {
        this.dispatch = GraphDispatch.getInstance();
        this.graph = graph;
        this.states = new ArrayList<GraphElementState>();
        this.states.add(new GraphElementState(L));
    }

    /**
     * Resets this element to its original state at the end of an animation.
     *
     * @param graphState the initial state of the graph containing this element
     */
    protected void reset() {
        ArrayList<GraphElementState> initialStates
                = new ArrayList<GraphElementState>();
        for (GraphElementState state : this.states) {
            if (state.getState() > 0) {
                break;
            }
            initialStates.add(state);
        }
        this.states = initialStates;
    }

    /**
     * @return a new state for this element; the new state will be identical to
     * the current (latest one) except that it will be tagged with the current
     * algorithm state if the algorithm is running; subsequent changes to this
     * GraphElement will take place in the new state.
     */
    private GraphElementState newState() throws Terminate {
        dispatch.startStepIfAnimationOrIncrementEditState();
        GraphElementState latest = latestState();
        GraphElementState elementState
            = new GraphElementState(latest);
        return elementState;
    }

    /**
     * @return The last state on the list of states. This is the default for
     * retrieving information about any attribute. If no latest state exists a
     * "blank" one with all attributes = null is returned.
     */
    public GraphElementState latestState() {
        GraphElementState state = null;
        if (states.size() != 0) {
            state = states.get(states.size() - 1);
        } else {
            state = new GraphElementState();
        }
        return state;
    }
    /**
     * This method is vital for retrieving the most recent information about a
     * graph element (node or edge), where most recent is defined relative to a
     * given time stamp, as defined by forward and backward stepping through the
     * animation.
     *
     * @see edu.ncsu.csc.Galant.algorithm.AlgorithmExecutor
     * @param stateNumber the numerical indicator (timestamp) of a state,
     * usually the current display state
     * @return the latest instance of GraphElementState that was created before
     * the given time stamp, or null if the element did not exist before the
     * time stamp.
     */
    public GraphElementState getLatestValidState(int stateNumber) {
        GraphElementState toReturn = null;
        int stateIndex = states.size() - 1;
        while (stateIndex >= 0) {
            GraphElementState state = states.get(stateIndex);
            if (state.getState() <= stateNumber) {
                toReturn = state;
                break;
            }
            stateIndex--;
        }
        return toReturn;
    }

    /**
     * Adds the given state to the list of states for this element. If there is
     * already a state having the same algorithm state (time stamp), there is no
     * need to add another one. Such a situation might arise if there are
     * multiple state changes to this element between a beginStep()/endStep()
     * pair or if no algorithm is running. If an algorithm is running, this
     * method initiates synchronization with the master thread to indicate that
     * the changes corresponding to the added state are completed
     *
     * @invariant states are always sorted by state number.
     */
    private void addState(GraphElementState stateToAdd) throws Terminate {
        int stateNumber = stateToAdd.getState();
        boolean found = false;
        for (int i = states.size() - 1; i >= stateNumber; i--) {
            GraphElementState state = states.get(i);
            if (state.getState() == stateNumber) {
                states.set(i, stateToAdd);
                found = true;
                break;
            }
        }
        if (!found) {
            states.add(stateToAdd);
            dispatch.pauseExecutionIfRunning();
        }
    }

    public ArrayList<GraphElementState> copyCurrentState() {
        GraphElementState currentState
            = new GraphElementState(this.getLatestValidState(this.graph.getEditState()));
        ArrayList<GraphElementState> statesCopy = new ArrayList<GraphElementState>();
        /**
         * @todo use a linked list and link current state after the
         * latestValidState for current edit state of the graph the new current
         * state will need a state number corresponding to the graph edit state
         * + 1
         */
        statesCopy.add(currentState);
        return statesCopy;
    }

    /**
     * @todo Would be nice to have methods hasInteger(String key), etc., so that
     * animator can say ge.hasInteger("attribute"), e.g.
     */
    /**
     * ************ Integer attributes **************
     */
    public boolean set(String key, Integer value) throws Terminate {
        GraphElementState newState = newState();
        boolean found = newState.set(key, value);
        addState(newState);
        return found;
    }

    public Integer getInteger(String key) {
        GraphElementState state = latestState();
        return state.getAttributes().getInteger(key);
    }

    public Integer getInteger(int state, String key) {
        GraphElementState validState = getLatestValidState(state);
        return validState == null ? null : validState.getAttributes().getInteger(key);
    }

    /**
     * ************ Double attributes **************
     */
    public boolean set(String key, Double value) throws Terminate {
        GraphElementState newState = newState();
        boolean found = newState.set(key, value);
        addState(newState);
        return found;
    }

    public Double getDouble(String key) {
        GraphElementState state = latestState();
        return state.getAttributes().getDouble(key);
    }

    public Double getDouble(int state, String key) {
        GraphElementState validState = getLatestValidState(state);
        return validState == null ? null : validState.getAttributes().getDouble(key);
    }

    /**
     * ************ Boolean attributes **************
     */
    public boolean set(String key, Boolean value) throws Terminate {
        GraphElementState newState = newState();
        boolean found = newState.set(key, value);
        addState(newState);
        return found;
    }

    /**
     * If value is not specified, assume it's boolean and set to true
     */
    public boolean set(String key) throws Terminate {
        return this.set(key, true);
    }

    public void clear(String key) throws Terminate {
        this.remove(key);
    }

    /**
     * For boolean attributes, assume that the absense of an attribute means
     * that it's false.
     */
    public Boolean getBoolean(String key) {
        GraphElementState state = latestState();
        return state.getAttributes().getBoolean(key);
    }

    public Boolean getBoolean(int state, String key) {
        GraphElementState validState = getLatestValidState(state);
        if (validState == null) {
            return false;
        }
        if (validState.getAttributes() == null) {
            return false;
        }
        return validState.getAttributes().getBoolean(key);
    }

    /**
     * Synonyms (for readability in algorithms)
     */
    public Boolean is(String key) {
        return getBoolean(key);
    }

    public Boolean is(int state, String key) {
        return getBoolean(state, key);
    }

    /**
     * ************ String attributes **************
     */
    public boolean set(String key, String value) throws Terminate {
        GraphElementState newState = newState();
        boolean found = newState.set(key, value);
        addState(newState);
        return found;
    }

    public String getString(String key) {
        GraphElementState state = latestState();
        return state.getAttributes().getString(key);
    }

    public String getString(int state, String key) {
        GraphElementState validState = getLatestValidState(state);
        return validState == null ? null : validState.getAttributes().getString(key);
    }

    /**
     * Removes the attribute with the given key from the list and updates state
     * information appropriately.
     */
    public void remove(String key) throws Terminate {
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
    public void setDeleted(boolean deleted) throws Terminate {
        if (deleted) {
            set(DELETED, true);
        } else {
            remove(DELETED);
        }
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
     * @return true if the element has been created but has not been deleted.
     */
    public boolean inScope() {
        return ! isDeleted();
    }

    public boolean inScope(int state) {
        return isCreated(state) && ! isDeleted(state);
    }

    /**
     * @return true if this element is (logically) hidden in the current
     * algorithm state - useful for algorithm to "know" which nodes/edges are
     * visible and which are not
     */
    public Boolean isHidden() {
        return getBoolean(HIDDEN);
    }

    /**
     * @return true if this element is hidden, i.e., will not be drawn on the
     * graph panel.
     */
    public Boolean isHidden(int state) {
        return getBoolean(state, HIDDEN);
    }

    public void hide() throws Terminate {
        set(HIDDEN);
    }

    public void show() throws Terminate {
        clear(HIDDEN);
    }

    /**
     * ************************** weights *************************
     */
    public Double getWeight() {
        return getDouble(WEIGHT);
    }

    public Double getWeight(int state) {
        return getDouble(state, WEIGHT);
    }

    /**
     * Need to convert the argument to a double because it eventually converts
     * to Double -- see initializeAfterParsing()
     */
    public void setWeight(double weight) throws Terminate {
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
    public void clearWeight() throws Terminate {
        remove(WEIGHT);
    }

    /**
     * @return true if the weight of this element is hidden, i.e., will not be
     * drawn on the graph panel.
     */
    public Boolean weightIsHidden(int state) {
        return getBoolean(state, HIDDEN_WEIGHT);
    }

    public void hideWeight() throws Terminate {
        set(HIDDEN_WEIGHT);
    }

    public void showWeight() throws Terminate {
        clear(HIDDEN_WEIGHT);
    }

    /**
     * ************************** labels ************************
     */
    public String getLabel() {
        return getString(LABEL);
    }

    public String getLabel(int state) {
        return getString(state, LABEL);
    }

    public void setLabel(String label) throws Terminate {
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
    public void clearLabel() throws Terminate {
        remove(LABEL);
    }

    /**
     * @return true if the label of this element is hidden, i.e., will not be
     * drawn on the graph panel.
     */
    public Boolean labelIsHidden(int state) {
        return getBoolean(state, HIDDEN_LABEL);
    }

    public void hideLabel() throws Terminate {
        set(HIDDEN_LABEL);
    }

    public void showLabel() throws Terminate {
        clear(HIDDEN_LABEL);
    }

    /**
     * ************************** colors ************************
     */
    public String getColor() {
        return getString(COLOR);
    }

    public String getColor(int state) {
        return getString(state, COLOR);
    }

    public void setColor(String color) throws Terminate {
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
    public void clearColor() throws Terminate {
        remove(COLOR);
    }

    /**
     * ************************** highlighting **********************
     */
    /**
     * In case it matters, setSelected simply changes the value of the
     * HIGHLIGHTED attribute (if it's already there) while unHighlight actually
     * removes the entry corresponding to that attribute. The effect is the
     * same, but the nature of the list traversal might not be.
     */
    public boolean isSelected() {
        return getBoolean(HIGHLIGHTED);
    }

    public Boolean isSelected(int state) {
        return getBoolean(state, HIGHLIGHTED);
    }

    public void setSelected(Boolean highlighted) throws Terminate {
        set(HIGHLIGHTED, highlighted);
    }

    public boolean isHighlighted() {
        return getBoolean(HIGHLIGHTED);
    }

    public Boolean isHighlighted(int state) {
        return getBoolean(state, HIGHLIGHTED);
    }

    public void highlight() throws Terminate {
        set(HIGHLIGHTED, true);
    }

    public void unHighlight() throws Terminate {
        remove(HIGHLIGHTED);
    }

    // alternate spelling
    public void unhighlight() throws Terminate {
        remove(HIGHLIGHTED);
    }

    /**
     * Parses specific attributes that are not to be stored internally as
     * strings. This allows the GraphMLParser to create each element without
     * knowing its attributes, then collect them in order of appearance, and
     * finally postprocess so that the essential ones are initialized properly.
     * Also establishes the initial state for this element.
     *
     * Relevant attributes for all graph elements are ... - id: integer -
     * weight: double - highlighted: boolean - deleted: boolean (not clear if
     * this ever becomes an issue; not implemented)
     *
     * Attributes for nodes are ... - x, y: integer - layer, positionInLayer:
     * integer - marked: boolean
     *
     * Attributes for edges are ... - source, target: integer (id's of nodes)
     */
    /**
     * @todo The "right" way to do this, if reading of exported files
     * mid-animation is to be fully supported, is as follows.
     *
     * for each attribute (key, value) in the list of attributes do [note that
     * value will be a string at this point] if key is a standard attribute,
     * handle it appropriately otherwise, try to parse value using the following
     * order of attempts: Integer Double Boolean and then default to String
     * attributes particular to Node or Edge may need to be dealt with first.
     */
    public void initializeAfterParsing(AttributeList L) throws GalantException {
        String weightString = null;
        String highlightString = null;
        String hiddenString = null;
        for (int i = 0; i < L.attributes.size(); i++) {
            Attribute attributeOfNode = L.attributes.get(i);
            if (attributeOfNode.key.equals("weight")) {
                String attributeValue = attributeOfNode.getStringValue();
                weightString = attributeValue;
            } else if (attributeOfNode.key.equals("highlighted")) {
                String attributeValue = attributeOfNode.getStringValue();
                highlightString = attributeValue;
            } else if (attributeOfNode.key.equals("hidden")) {
                String attributeValue = attributeOfNode.getStringValue();
                hiddenString = attributeValue;
            }
        } 
        if (weightString != null) {
            Double weight = Double.NaN;
            try {
                weight = Double.parseDouble(weightString);
            } catch (NumberFormatException e) {
                throw new GalantException("Bad weight " + weightString);
            }
            L.remove(WEIGHT);
            L.set(WEIGHT, weight);
        }

        if (highlightString != null) {
            Boolean highlighted = Boolean.parseBoolean(highlightString);
            L.remove(HIGHLIGHTED);
            if (highlighted) {
                L.set(HIGHLIGHTED, highlighted);
            }
        }

        /**
         * @todo need to do something like this for other standard
         * attributes such as HIDDEN_LABEL, HIDDEN_WEIGHT, DELETED in order
         * to avoid errors when reading files exported during animations.
         */

        if (hiddenString != null) {
            Boolean hidden = Boolean.parseBoolean(hiddenString);
            L.remove(HIDDEN);
            if (hidden) {
                L.set(HIDDEN, hidden);
            }
        }
    }

    /**
     * Creates a string that can be used to form the "interior" of a GraphML
     * representation of the attributes associated with this state.
     */
    public String xmlString() {
        GraphElementState elementState = latestState();
        return elementState.xmlString();
    }

    /**
     * Like xmlString(), except that it omits the "x" and "y" attributes; to be
     * used in cases where these attributes are superceded by the corresponding
     * fixed ones of a Node.
     */
    public String attributesWithoutPosition() {
        GraphElementState elementState = latestState();
        return elementState.attributesWithoutPosition();
    }

    /**
     * Like xmlString(), except that it omits the "id" attribute; to be used in
     * cases where the id is optional, as is the case with an Edge
     */
    public String attributesWithoutId() {
        GraphElementState elementState = latestState();
        return elementState.attributesWithoutId();
    }

    /**
     * Same as the unparameterized version except that the attributes are ones
     * of the latest valid state. This is used when exporting the state of the
     * graph in the middle of execution.
     */
    public String xmlString(int state) {
        GraphElementState elementState = getLatestValidState(state);
        if (elementState == null) {
            return "";
        }
        return elementState.xmlString();
    }

    /**
     * Same as the unparameterized version except that the attributes are ones
     * of the latest valid state. This is used when exporting the state of the
     * graph in the middle of execution.
     */
    public String attributesWithoutPosition(int state) {
        GraphElementState elementState = getLatestValidState(state);
        if (elementState == null) {
            return "";
        }
        return elementState.attributesWithoutPosition();
    }

    /**
     * Same as the unparameterized version except that the attributes are ones
     * of the latest valid state. This is used when exporting the state of the
     * graph in the middle of execution.
     */
    public String attributesWithoutId(int state) {
        GraphElementState elementState = getLatestValidState(state);
        if (elementState == null) {
            return "";
        }
        return elementState.attributesWithoutId();
    }

    public int compareTo(GraphElement other) {
        Double thisDouble = new Double(this.getWeight());
        Double otherDouble = new Double(other.getWeight());
        return thisDouble.compareTo(otherDouble);
    }

    /**
     * @return a comparator that compares two graph elements based on the
     * designated attribute; the attribute must have a Double value
     */
    public static GraphElementComparator getDoubleComparator(String attribute) {
        return new GraphElementComparator(attribute, false) {
            public int compare(GraphElement ge_1, GraphElement ge_2) {
                Double value_1 = ge_1.getDouble(attribute);
                Double value_2 = ge_2.getDouble(attribute);
                if (value_1 > value_2) {
                    return 1;
                } else if (value_2 > value_1) {
                    return -1;
                } else {
                    return 0;
                }
            }
        };
    }

    /**
     * @return a comparator that compares two graph elements based on the
     * designated attribute; the attribute must have an Integer value
     */
    public static GraphElementComparator getIntegerComparator(String attribute) {
        return new GraphElementComparator(attribute, false) {
            @Override
            public int compare(GraphElement ge_1, GraphElement ge_2) {
                Integer value_1 = ge_1.getInteger(attribute);
                Integer value_2 = ge_2.getInteger(attribute);
                if (value_1 > value_2) {
                    return 1;
                } else if (value_2 > value_1) {
                    return -1;
                } else {
                    return 0;
                }
            }
        };
    }

    /**
     * @return a comparator that compares two graph elements based on the
     * designated attribute; the attribute must have an Integer value
     */
    public static GraphElementComparator getStringComparator(String attribute) {
        return new GraphElementComparator(attribute, false) {
            @Override
            public int compare(GraphElement ge_1, GraphElement ge_2) {
                String value_1 = ge_1.getString(attribute);
                String value_2 = ge_2.getString(attribute);
                return value_1.compareTo(value_2);
            }
        };
    }

    /**
     * @return a comparator that compares two graph elements based on the
     * designated attribute; the attribute must have a Double value
     * @param reverse use the reverse of the natural order if true
     */
    public static GraphElementComparator getDoubleComparator(String attribute,
            boolean reverse) {
        return new GraphElementComparator(attribute, reverse) {
            public int compare(GraphElement ge_1, GraphElement ge_2) {
                Double value_1 = ge_1.getDouble(attribute);
                Double value_2 = ge_2.getDouble(attribute);
                if (reverse) {
                    return value_2.compareTo(value_1);
                } else {
                    return value_1.compareTo(value_2);
                }
            }
        };
    }

    /**
     * @return a comparator that compares two graph elements based on the
     * designated attribute; the attribute must have an Integer value
     * @param reverse use the reverse of the natural order if true
     */
    public static GraphElementComparator getIntegerComparator(String attribute,
            boolean reverse) {
        return new GraphElementComparator(attribute, reverse) {
            public int compare(GraphElement ge_1, GraphElement ge_2) {
                Integer value_1 = ge_1.getInteger(attribute);
                Integer value_2 = ge_2.getInteger(attribute);
                if (reverse) {
                    return value_2.compareTo(value_1);
                } else {
                    return value_1.compareTo(value_2);
                }
            }
        };
    }

    /**
     * @return a comparator that compares two graph elements based on the
     * designated attribute; the attribute must have a String value
     * @param reverse use the reverse of the natural order if true
     */
    public static GraphElementComparator getStringComparator(String attribute,
            boolean reverse) {
        return new GraphElementComparator(attribute, reverse) {
            public int compare(GraphElement ge_1, GraphElement ge_2) {
                String value_1 = ge_1.getString(attribute);
                String value_2 = ge_2.getString(attribute);
                if (reverse) {
                    return value_2.compareTo(value_1);
                } else {
                    return value_1.compareTo(value_2);
                }
            }
        };
    }

}

//  [Last modified: 2021 01 07 at 16:54:20 GMT]
