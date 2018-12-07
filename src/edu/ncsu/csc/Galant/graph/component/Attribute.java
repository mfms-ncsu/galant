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
    public abstract String xmlString();
}

//  [Last modified: 2018 12 07 at 16:23:57 GMT]
