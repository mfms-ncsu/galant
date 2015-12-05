package edu.ncsu.csc.Galant.graph.component;

public class BooleanAttribute extends Attribute implements Cloneable {
    private Boolean value;
    public BooleanAttribute(String key, Boolean value) {
        super(key);
        this.value = value;
    }
    public Boolean getBooleanValue() { return value; }
    public void set(Boolean value) { this.value = value; }
    public Attribute clone() { return new BooleanAttribute(key, value); }
    public String toString() {
        return key + "=\"" + value + "\"";
    }
}

//  [Last modified: 2015 12 05 at 18:07:28 GMT]
