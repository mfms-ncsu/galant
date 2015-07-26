package edu.ncsu.csc.Galant.graph.component;

public class StringAttribute extends Attribute {
    private String value;
    public StringAttribute(String key, String value) {
        super(key);
        this.value = value;
    }
    public String getStringValue() { return value; }
    public void set(String value) { this.value = value; }
    public String toString() {
        return key + "=\"" + value + "\"";
    }
}

//  [Last modified: 2015 07 25 at 22:07:00 GMT]
