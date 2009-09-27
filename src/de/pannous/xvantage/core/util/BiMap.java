package de.pannous.xvantage.core.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class BiMap<V1, V2> implements Map<V1, V2> {

    private Map<V2, V1> getV1 = new HashMap();
    private Map<V1, V2> map = new HashMap();

    public V2 put(V1 v1, V2 v2) {
        getV1.put(v2, v1);
        return map.put(v1, v2);
    }

    public V2 get(Object v1) {
        return map.get(v1);
    }

    public V1 getSecond(V2 v2) {
        return getV1.get(v2);
    }

    public Collection<V2> values() {
        return map.values();
    }

    public Map<V2, V1> getAllSecondToFirst() {
        return getV1;
    }

    public int size() {
        assert getV1.size() == map.size();
        return getV1.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    public V2 remove(Object key) {
        V2 val = map.remove(key);
        getV1.remove(val);
        return val;
    }

    public void putAll(Map<? extends V1, ? extends V2> t) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void clear() {
        map.clear();
        getV1.clear();
    }

    public Set<V1> keySet() {
        return map.keySet();
    }

    public Set<Entry<V1, V2>> entrySet() {
        return map.entrySet();
    }
}
