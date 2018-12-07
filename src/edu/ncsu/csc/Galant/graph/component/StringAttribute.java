package edu.ncsu.csc.Galant.graph.component;

public class StringAttribute extends Attribute implements Cloneable {
    private String value;
    public StringAttribute(String key, String value) {
        super(key);
        this.value = value;
    }
    public String getStringValue() { return value; }
    public void set(String value) { this.value = value; }
    public Attribute clone() { return new StringAttribute(key, value); }
    public String toString() {
        return key + "=\"" + value + "\"";
    }
    public String xmlString() {
        return key + "=\"" + value + "\"";
    }
}

//  [Last modified: 2018 12 07 at 16:15:45 GMT]
