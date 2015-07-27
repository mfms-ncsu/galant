package edu.ncsu.csc.Galant.graph.component;

/**
 * A generic item in an AttributeList. Usually only one of the four
 * possible getters other than getKey() will return a non-null value.
 */
public abstract class Attribute implements Cloneable {
    protected String key;
    public Attribute(String key) { this.key = key; }
    public String getKey() { return key; } 
    public Integer getIntegerValue() { return null; }
    public Double getDoubleValue() { return null; }
    public Boolean getBooleanValue() { return null; } 
    public String getStringValue() { return null; }
    public abstract Attribute clone();
    public abstract String toString();
}

//  [Last modified: 2015 07 27 at 16:05:48 GMT]
