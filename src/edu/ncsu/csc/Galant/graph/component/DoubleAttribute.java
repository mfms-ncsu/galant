package edu.ncsu.csc.Galant.graph.component;

public class DoubleAttribute extends Attribute {
    private Double value;
    public DoubleAttribute(String key, Double value) {
        super(key);
        this.value = value;
    }
    public Double getDoubleValue() { return value; }
    public void set(Double value) { this.value = value; }
    public String toString() {
        return key + "=\"" + value + "\"";
    }
}

//  [Last modified: 2015 07 25 at 22:06:32 GMT]
