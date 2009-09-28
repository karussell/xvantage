package de.pannous.xvantage.core.impl;

import de.pannous.xvantage.core.DataPool;
import de.pannous.xvantage.core.util.BiMap;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class DefaultDataPool implements DataPool {

    private Map<Class, Map<Long, Object>> objects = new HashMap<Class, Map<Long, Object>>();

    public <T> Map<Long, T> getData(Class<T> clazz) {
        if (clazz == null)
            return null;

        BiMap<Long, Object> map = (BiMap<Long, Object>) objects.get(clazz);
        if (map == null) {
            map = new BiMap();
            objects.put(clazz, map);
        }
        return (BiMap<Long, T>) map;
    }

    public Long getId(Object object) {
        if (object == null)
            return null;

        BiMap<Long, Object> map = (BiMap<Long, Object>) getData(object.getClass());
        if (map != null)
            return map.getSecond(object);

        return null;
    }

    @Override
    public String toString() {
        return objects.entrySet().toString();
    }
}

