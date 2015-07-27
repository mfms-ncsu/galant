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
        return key + "=\"" + value + "\"";
    }
}

//  [Last modified: 2015 07 27 at 15:56:47 GMT]
