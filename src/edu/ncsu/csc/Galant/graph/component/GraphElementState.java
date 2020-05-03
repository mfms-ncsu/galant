package edu.ncsu.csc.Galant.graph.component;
import java.awt.Point;
import edu.ncsu.csc.Galant.GraphDispatch;
import edu.ncsu.csc.Galant.logging.LogHelper;
import java.util.ArrayList;
import java.util.List;

/**
 * The state of a GraphElement, used akin to a frame in an animation. As the
 * animation advances, the subsequent GraphElementState is evoked to be
 * displayed in the animation window.
 *
 * Based on the NodeState and EdgeState classes:
 * @author Jason Cockrell, Ty Devries, Alex McCabe, Michael Owoc
 *
 * Adapted for a more general, flexible model by Matthias Stallmann
 *
 * @todo It did not occur to me at the time, but now that all of the standard
 * attributes are instances of classes (Integer, Double, Boolean) rather than
 * primitive types, the AttributeList can be reserved for user-defined
 * attributes. When a standard attribute is null, it can then simply not be
 * added to a saved graphml file.
 *
 * @todo Another idea: store all the user-defined attributes as String's. The
 * setters can do the automatic conversion using the ' "" + ' incantation, or
 * a simple test for Booleans (where it's about presence or absence
 * anyhow). The getters can do parsing.
 */
public class GraphElementState {

    /**
     * The sequence number (algorithm state) of this state.
     */
	private int state;

    public int getState() { return state; }

    /**
     * Attribute list for the snapshot representing this element state
     */
    protected AttributeList attributes;
    public AttributeList getAttributes() { return attributes; }

    private GraphDispatch dispatch;

    /**
     * Constructor used during parsing and editing, when no attributes are
     * known yet.
     */
    public GraphElementState() {
        this.dispatch = GraphDispatch.getInstance();
        this.state = dispatch.getAlgorithmState();
        this.attributes = new AttributeList();
    }

    /**
     * This serves essentially as a copy constructor: creates the new object
     * in a different algorithm state and copies all the information for the
     * node (state) - except, of course, the state
     */
    public GraphElementState(GraphElementState elementState) {
        this.dispatch = GraphDispatch.getInstance();
        this.state = dispatch.getAlgorithmState();
        this.attributes = elementState.getAttributes().duplicate();
    }

    /**
     * The setters below have two additional features:
     *   - they remove an attribute from the list if the given value is null
     *   - they return true if the attribute was present prior to the call
     */

    /************** Integer attributes ***************/
	public boolean set(String key, Integer value) {
        if ( value == null ) {
            return remove(key);
        }
        return attributes.set(key, value);
	}
	public Integer getIntegerAttribute(String key) {
		return attributes.getInteger(key);
	}

    /************** Double attributes ***************/
	public boolean set(String key, Double value) {
        if ( value == null ) {
            return remove(key);
        }
        return attributes.set(key, value);
	}
	public Double getDoubleAttribute(String key) {
		return attributes.getDouble(key);
	}

    /************** Boolean attributes ***************/
	public boolean set(String key, Boolean value) {
        if ( value == null ) {
            return remove(key);
        }
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
        if ( value == null ) {
            return remove(key);
        }
        return attributes.set(key, value);
	}
	public String getStringAttribute(String key) {
		return attributes.getString(key);
	}

    /**
     * Removes the attribute with the given key from the list and updates
     * state information appropriately.
     * @return true if the attribute was present before the call
     */
    public boolean remove(String key) {
        return attributes.remove(key);
    }

    /**
     * Creates a string that can be used to form the "interior" of a GraphML
     * representation of this element.
     */
    public String xmlString() {
        String s = " ";
        for ( Attribute attribute : attributes.getAttributes() ) {
            s += attribute + " ";
        }
        return s;
    }

    /**
     * Like xmlString(), except that it omits the "x" and "y" attributes; to
     * be used in cases where these attributes are superceded by the
     * corresponding fixed ones of a Node.
     */
    public String attributesWithoutPosition() {
        String s = " ";
        for ( Attribute attribute : attributes.getAttributes() ) {
            if ( ! attribute.getKey().equals("x")
                 && ! attribute.getKey().equals("y") ) {
                s += attribute + " ";
            }
        }
        return s;
    }

    /**
     * Like xmlString(), except that it omits the "id" attribute; to be used
     * in cases where the id is optional, as is the case with an Edge
     */
    public String attributesWithoutId() {
        String s = " ";
        for ( Attribute attribute : attributes.getAttributes() ) {
            if ( ! attribute.getKey().equals("id") ) {
                s += attribute + " ";
            }
        }
        return s;
    }

    public String toString() {
        String s = "{elementState ";
        s += "" + state + " ";
        s += attributes.getAttributes();
        s += "}";
        return s;
    }
}

//  [Last modified: 2018 08 31 at 14:41:01 GMT]
