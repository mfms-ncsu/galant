package edu.ncsu.csc.Galant.graph.component;

public class IntegerAttribute extends Attribute implements Cloneable {
    private Integer value;
    public IntegerAttribute(String key, Integer value) {
        super(key);
        this.value = value;
    }
    public Integer getIntegerValue() { return value; }
    public void set(Integer value) { this.value = value; }
    public Attribute clone() { return new IntegerAttribute(key, value); }
    public String toString() {
        return key + "=" + value;
    }
    public String xmlString() {
        return key + "=\"" + value + "\"";
    }
}

//  [Last modified: 2018 12 07 at 16:16:14 GMT]
