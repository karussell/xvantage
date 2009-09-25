package de.pannous.xvantage.core.util;

import java.util.Map.Entry;

/**
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class MapEntry<K, V> implements Entry<K, V> {

    private K key;
    private V value;

    public MapEntry(K k, V v) {
        key = k;
        value = v;
    }

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public V getValue() {
        return value;
    }

    public V setValue(V value) {
        V old = this.value;
        this.value = value;
        return old;
    }

    @Override
    public String toString() {
        return "key:" + key + " value:" + value;
    }
}
