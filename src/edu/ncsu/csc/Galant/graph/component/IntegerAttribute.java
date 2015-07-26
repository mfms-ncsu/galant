package edu.ncsu.csc.Galant.graph.component;

public class IntegerAttribute extends Attribute {
    private Integer value;
    public IntegerAttribute(String key, Integer value) {
        super(key);
        this.value = value;
    }
    public Integer getIntegerValue() { return value; }
    public void set(Integer value) { this.value = value; }
    public String toString() {
        return key + "=\"" + value + "\"";
    }
}

//  [Last modified: 2015 07 25 at 22:06:21 GMT]
