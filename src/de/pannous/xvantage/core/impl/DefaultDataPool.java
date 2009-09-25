package de.pannous.xvantage.core.impl;

import de.pannous.xvantage.core.DataPool;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class DefaultDataPool implements DataPool {

    private Map<Class, Map<Long, Object>> objects = new HashMap<Class, Map<Long, Object>>();

    public Map<Class, Map<Long, Object>> getAll() {
        return objects;
    }

    public <T> Map<Long, T> getData(Class<T> clazz) {
        Map<Long, Object> map = objects.get(clazz);
        if (map == null) {
            map = new HashMap();
            objects.put(clazz, map);
        }
        return (Map<Long, T>) map;
    }
}

