package edu.ncsu.csc.Galant.graph.component;

import java.util.ArrayList;

/**
 * An AttributeList plays the role of a Map. A list is used so that
 * attributes will appeare in the order they were added rather than
 * alphabetically by key.
 */
public class AttributeList {

    private ArrayList<Attribute> myList;

    public AttributeList() { myList = new ArrayList<Attribute>(); }

    public ArrayList<Attribute> getAttributes() { return myList; }

    /**
     * The getters traverse the list until they find a matching key or return
     * null if they don't.
     */
    public Integer getInteger(String key) {
        for ( Attribute attribute : myList ) {
            if ( attribute.getKey().equals(key) ) {
                return attribute.getIntegerValue();
            }
        }
        return null;
    }

    public Double getDouble(String key) {
        for ( Attribute attribute : myList ) {
            if ( attribute.getKey().equals(key) ) {
                return attribute.getDoubleValue();
            }
        }
        return null;
    }

    public Boolean getBoolean(String key) {
        for ( Attribute attribute : myList ) {
            if ( attribute.getKey().equals(key) ) {
                return attribute.getBooleanValue();
            }
        }
        return null;
    }

    public String getString(String key) {
        for ( Attribute attribute : myList ) {
            if ( attribute.getKey().equals(key) ) {
                return attribute.getStringValue();
            }
        }
        return null;
    }

    /**
     * The following setters set the value of an attribute or, if the
     * attribute is not present, add it to the list. They return true if and
     * only if the attribute was in the list already.
     */
    public boolean set(String key, Integer value) {
        for ( Attribute attribute : myList ) {
            if ( attribute.getKey().equals(key) ) {
                ((IntegerAttribute)attribute).set(value);
                return true;
            }
        }
        add(key, value);
        return false;
    }

    public boolean set(String key, Double value) {
        for ( Attribute attribute : myList ) {
            if ( attribute.getKey().equals(key) ) {
                ((DoubleAttribute)attribute).set(value);
                return true;
            }
        }
        add(key, value);
        return false;
    }

    public boolean set(String key, Boolean value) {
        for ( Attribute attribute : myList ) {
            if ( attribute.getKey().equals(key) ) {
                ((BooleanAttribute)attribute).set(value);
                return true;
            }
        }
        add(key, value);
        return false;
    }

    public boolean set(String key, String value) {
        for ( Attribute attribute : myList ) {
            if ( attribute.getKey().equals(key) ) {
                ((StringAttribute)attribute).set(value);
                return true;
            }
        }
        add(key, value);
        return false;
    }

    /**
     * The following unconditionally add items to the list; if care
     * is not taken there may be duplicate attributes with the same key --
     * only the first of these will matter
     */
    public void add(String key, Integer value) {
        myList.add(new IntegerAttribute(key, value));
    }

    public void add(String key, Double value) {
        myList.add(new DoubleAttribute(key, value));
    }

    public void add(String key, Boolean value) {
        myList.add(new BooleanAttribute(key, value));
    }

    public void add(String key, String value) {
        myList.add(new StringAttribute(key, value));
    }

    /**
     * The following unconditionally insert items at the beginnibg of the
     * list; if care is not taken there may be duplicate attributes with the
     * same key -- only the first of these will matter
     */
    public void push(String key, Integer value) {
        myList.add(0, new IntegerAttribute(key, value));
    }

    public void push(String key, Double value) {
        myList.add(0, new DoubleAttribute(key, value));
    }

    public void push(String key, Boolean value) {
        myList.add(0, new BooleanAttribute(key, value));
    }

    public void push(String key, String value) {
        myList.add(0, new StringAttribute(key, value));
    }

    /**
     * The following method removes an item from the list. It does nothing if
     * there was no item with the given key.
     */
    public void remove(String key) {
        for ( int i = 0; i < myList.size(); i++ ) {
            if ( myList.get(i).getKey().equals(key) ) {
                myList.remove(i);
                return;
            }
        }
    }

    /**
     * @return a deep copy of the list
     */
    public AttributeList duplicate() {
        ArrayList<Attribute> newList = (ArrayList<Attribute>) myList.clone();
        for ( Attribute attribute : newList ) {
            newList.add((Attribute) attribute.clone());
        } 
    }

    // The following does not work; the toString() method for ArrayList
    // always takes over, but that's useful for debugging.
//     public String toString() {
//         StringBuilder builder = new StringBuilder();
//         builder.append(" ");
//         for ( Attribute attribute : myList ) {
//             builder.append( "" + attribute + " " );
//         }
//         return builder.toString();
//     }

}

//  [Last modified: 2015 07 25 at 22:20:19 GMT]
