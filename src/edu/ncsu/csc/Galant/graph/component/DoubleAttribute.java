package edu.ncsu.csc.Galant.graph.component;

public class DoubleAttribute extends Attribute implements Cloneable {
    private Double value;
    public DoubleAttribute(String key, Double value) {
        super(key);
        this.value = value;
    }
    public Double getDoubleValue() { return value; }
    public void set(Double value) { this.value = value; }
    public Attribute clone() { return new DoubleAttribute(key, value); }
    public String toString() {
        return key + "=\"" + value + "\"";
    }
}

//  [Last modified: 2015 07 27 at 15:55:50 GMT]
