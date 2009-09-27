package de.pannous.xvantage.core.util.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class ObjectWithCollections {

    private String name;
    private Set<String> stringSet = new HashSet<String>();
    private Map<Integer, String> stringMap = new HashMap<Integer, String>();
    private Long id;
    private String stringArray[] = {"str1", "str2"};
    private Collection<String> stringCollection = new ArrayList<String>();

    private ObjectWithCollections() {
    }

    public ObjectWithCollections(String n) {
        setName(n);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Map<Integer, String> getStringMap() {
        return stringMap;
    }

    public void setStringMap(Map<Integer, String> stringMap) {
        this.stringMap = stringMap;
    }

    public Set<String> getStringSet() {
        return stringSet;
    }

    public void setStringSet(Set<String> stringSet) {
        this.stringSet = stringSet;
    }

    public String[] getStringArray() {
        return stringArray;
    }

    public void setStringArray(String[] stringArray) {
        this.stringArray = stringArray;
    }

    public Collection<String> getStringCollection() {
        return stringCollection;
    }

    public void setStringCollection(Collection<String> stringCollection) {
        this.stringCollection = stringCollection;
    }

    @Override
    public String toString() {
        return "name:" + name;
    }
}
