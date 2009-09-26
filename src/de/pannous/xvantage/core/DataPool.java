package de.pannous.xvantage.core;

import de.pannous.xvantage.core.util.BiMap;
import java.util.Map;

/**
 * A pool where we store all objects which should be serialized
 * 
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public interface DataPool {

    Map<Class, BiMap<Long, Object>> getAll();

    <T> BiMap<Long, T> getData(Class<T> clazz);
}
