package de.pannous.xvantage.core.util.test;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class ObjectWithPolymorph {

    private String name;
    private ObjectWithPolymorph object;
    private Collection collection = new ArrayList();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Collection getCollection() {
        return collection;
    }

    public void setCollection(Collection collection) {
        this.collection = collection;
    }

    public ObjectWithPolymorph getObject() {
        return object;
    }

    public void setObject(ObjectWithPolymorph object) {
        this.object = object;
    }
}
