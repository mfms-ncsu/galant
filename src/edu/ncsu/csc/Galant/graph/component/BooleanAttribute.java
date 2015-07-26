package edu.ncsu.csc.Galant.graph.component;

public class BooleanAttribute extends Attribute {
    private Boolean value;
    public BooleanAttribute(String key, Boolean value) {
        super(key);
        this.value = value;
    }
    public Boolean getBooleanValue() { return value; }
    public void set(Boolean value) { this.value = value; }
    public String toString() {
        return key + "=\"" + value + "\"";
    }
}

//  [Last modified: 2015 07 25 at 22:06:45 GMT]
