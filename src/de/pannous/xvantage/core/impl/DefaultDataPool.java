package de.pannous.xvantage.core.impl;

import de.pannous.xvantage.core.DataPool;
import de.pannous.xvantage.core.util.BiMap;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class DefaultDataPool implements DataPool {

    private Map<Class, BiMap<Long, Object>> objects = new HashMap<Class, BiMap<Long, Object>>();

    public Map<Class, BiMap<Long, Object>> getAll() {
        return objects;
    }

    public <T> BiMap<Long, T> getData(Class<T> clazz) {
        if(clazz == null)
            return null;
        
        BiMap<Long, Object> map = objects.get(clazz);
        if (map == null) {
            map = new BiMap();
            objects.put(clazz, map);
        }
        return (BiMap<Long, T>) map;
    }

    @Override
    public String toString() {
        return objects.toString();
    }
}

