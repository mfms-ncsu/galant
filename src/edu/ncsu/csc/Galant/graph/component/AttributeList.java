package edu.ncsu.csc.Galant.graph.component;

import java.util.ArrayList;

/**
 * An AttributeList plays the role of a Map. A list is used so that
 * attributes will appeare in the order they were added rather than
 * alphabetically by key.
 */
public class AttributeList implements Cloneable{

    protected ArrayList<Attribute> attributes;

    public AttributeList() { attributes = new ArrayList<Attribute>(); }

    /**
     * The purpose of this method is to allow the outside world to retrieve
     * the attributes as a list so that an iterator can be applied. There's
     * probably a more elegant solution, but ...
     */
    public ArrayList<Attribute> getAttributes() { return attributes; }

    @Override
    public AttributeList clone() throws CloneNotSupportedException
    {
        AttributeList clonedAttributeList=(AttributeList)super.clone();
        ArrayList<Attribute> attributesList=this.attributes;
        ArrayList<Attribute> copyOfAttributeList=new ArrayList<Attribute>();
        for(Attribute attribute:attributesList)
        {
            Attribute copyOfAttribute=attribute.clone();
            copyOfAttributeList.add(copyOfAttribute);
            clonedAttributeList.attributes=copyOfAttributeList;
        }
        return clonedAttributeList;
    }
    /**
     * The getters traverse the list until they find a matching key or return
     * null if they don't.
     *
     * @todo if the key matches, should check that the attribute has the
     * right type and think about throwing an exception
     */
    public Integer getInteger(String key) {
        for ( Attribute attribute : attributes ) {
            if ( attribute.getKey().equals(key) ) {
                return attribute.getIntegerValue();
            }
        }
        return null;
    }

    public Double getDouble(String key) {
        for ( Attribute attribute : attributes ) {
            if ( attribute.getKey().equals(key) ) {
                return attribute.getDoubleValue();
            }
        }
        return null;
    }

    public Boolean getBoolean(String key) {
        for ( Attribute attribute : attributes ) {
            if ( attribute.getKey().equals(key) ) {
                return attribute.getBooleanValue();
            }
        }
        // if missing, return a default, safe value
        return false;
    }

    public String getString(String key) {
        for ( Attribute attribute : attributes ) {
            if ( attribute.getKey().equals(key) ) {
                return attribute.getStringValue();
            }
        }
        return null;
    }

    /**
     * The following setters replace an attribute in the list if one with the
     * same key already exists (regardless of the class) and add one if none
     * exists.  They return true if and only if the attribute was in the list
     * already.
     */
  public boolean set(String key, Integer value) {
    for ( int i = 0; i < attributes.size(); i++ ) {
      if ( attributes.get(i).getKey().equals(key) ) {
        attributes.set(i, new IntegerAttribute(key, value));
        return true;
      }
    }
    attributes.add(new IntegerAttribute(key, value));
    return false;
  }

  public boolean set(String key, Double value) {
    for ( int i = 0; i < attributes.size(); i++ ) {
      if ( attributes.get(i).getKey().equals(key) ) {
        attributes.set(i, new DoubleAttribute(key, value));
        return true;
      }
    }
    attributes.add(new DoubleAttribute(key, value));
    return false;
  }

  public boolean set(String key, Boolean value) {
    for ( int i = 0; i < attributes.size(); i++ ) {
      if ( attributes.get(i).getKey().equals(key) ) {
        attributes.set(i, new BooleanAttribute(key, value));
        return true;
      }
    }
    attributes.add(new BooleanAttribute(key, value));
    return false;
  }

  public boolean set(String key, String value) {
    for ( int i = 0; i < attributes.size(); i++ ) {
      if ( attributes.get(i).getKey().equals(key) ) {
        attributes.set(i, new StringAttribute(key, value));
        return true;
      }
    }
    attributes.add(new StringAttribute(key, value));
    return false;
  }

    /**
     * The following method removes an item from the list. It does nothing if
     * there was no item with the given key.
     * @return true if the attribute was present
     */
    public boolean remove(String key) {
        for ( int i = 0; i < attributes.size(); i++ ) {
            if ( attributes.get(i).getKey().equals(key) ) {
                attributes.remove(i);
                return true;
            }
        }
        return false;
    }

    /**
     * @return a copy of the list; the attributes themselves are not copied
     * since new copies are created by the setters - see above.
     * This is effectively a clone() without the annoyance of having to catch
     * a CloneNotSupported exception or having to cast.
     */
    public AttributeList duplicate() {
      AttributeList newList = new AttributeList();
      for ( Attribute attribute : this.attributes ) {
        newList.attributes.add(attribute);
      }
      return newList;
    }

    // The following does not work; the toString() method for ArrayList
    // always takes over, but that's useful for debugging.
//     public String toString() {
//         StringBuilder builder = new StringBuilder();
//         builder.append(" ");
//         for ( Attribute attribute : attributes ) {
//             builder.append( "" + attribute + " " );
//         }
//         return builder.toString();
//     }

}

//  [Last modified: 2018 08 27 at 20:44:20 GMT]
