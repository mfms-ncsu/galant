package edu.ncsu.csc.Galant.graph.component;

/**
 * An AttributeList plays the role of a Map. A list is used so that
 * attributes will appeare in the order they were added rather than
 * alphabetically by key.
 */
public class AttributeList {

    /**
     * A generic item in an AttributeList. Usually only one of the four
     * possible getters other than getKey() will return a non-null value.
     */
    class Attribute {
        protected String key;
        public Attribute(String key, Object value) { this.key = key; }
        public String getKey() { return key; } 
        public Integer getIntegerValue() { return null; }
        public Double getDoubleValue() { return null; }
        public Boolean getBooleanValue() { return null; } 
        public String getStringValue() { return null; }
    }

    class IntegerAttribute extends Attribute {
        private Integer value;
        public IntegerAttribute(String key, Integer value) {
            super(key);
            this.value = value;
        }
        public Integer getIntegerValue() { return value; }
        public void set(Integer value) { this.value = value; }
    }

    public class DoubleAttribute extends Attribute {
        private Double value;
        public DoubleAttribute(String key, Double value) {
            super(key);
            this.value = value;
        }
        public Double getDoubleValue() { return value; }
        public void set(Integer value) { this.value = value; }
    }

    public class BooleanAttribute extends Attribute {
        private Boolean value;
        public BooleanAttribute(String key, Boolean value) {
            super(key);
            this.value = value;
        }
        public Boolean getBooleanValue() { return value; }
        public void set(Integer value) { this.value = value; }
    }

    public class StringAttribute extends Attribute {
        private String value;
        public StringAttribute(String key, String value) {
            super(key);
            this.value = value;
        }
        public String getStringValue() { return value; }
        public void set(Integer value) { this.value = value; }
    }

    private ArrayList<Attribute> myList;

    public AttributeList() { myList = new ArrayList<Attribute>(); }

    /**
     * The getters traverse the list until they find a matching key or return
     * null if they don't.
     */
    public Integer getIntegerValue(String key) {
        for ( Attribute attribute : myList ) {
            if ( attribute.getKey().equals(key) ) {
                return attribute.getIntegerValue();
            }
        }
        return null;
    }

    public Double getDoubleValue(String key) {
        for ( Attribute attribute : myList ) {
            if ( attribute.getKey().equals(key) ) {
                return attribute.getDoubleValue();
            }
        }
        return null;
    }

    public Boolean getBooleanValue(String key) {
        for ( Attribute attribute : myList ) {
            if ( attribute.getKey().equals(key) ) {
                return attribute.getBooleanValue();
            }
        }
        return null;
    }

    public String getStringValue(String key) {
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
        addInteger(key, value);
        return false;
    }

    public boolean set(String key, Double value) {
        for ( Attribute attribute : myList ) {
            if ( attribute.getKey().equals(key) ) {
                ((DoubleAttribute)attribute).set(value);
                return true;
            }
        }
        addDouble(key, value);
        return false;
    }

    public boolean set(String key, Boolean value) {
        for ( Attribute attribute : myList ) {
            if ( attribute.getKey().equals(key) ) {
                ((BooleanAttribute)attribute).set(value);
                return true;
            }
        }
        addBoolean(key, value);
        return false;
    }

    public boolean set(String key, String value) {
        for ( Attribute attribute : myList ) {
            if ( attribute.getKey().equals(key) ) {
                ((StringAttribute)attribute).set(value);
                return true;
            }
        }
        addString(key, value);
        return false;
    }

    /**
     * The following unconditionally add items to the list; if care
     * is not taken there may be duplicate attributes with the same key --
     * only the first of these will matter
     */
    public void addInteger(String key, Integer value) {
        myList.add(new IntegerAttribute(key, value));
    }

    public void addDouble(String key, Double value) {
        myList.add(new DoubleAttribute(key, value));
    }

    public void addBoolean(String key, Boolean value) {
        myList.add(new BooleanAttribute(key, value));
    }

    public void addString(String key, String value) {
        myList.add(new StringAttribute(key, value));
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

}

//  [Last modified: 2015 07 23 at 21:38:12 GMT]
