package de.pannous.xvantage.core;

import java.util.Map;

/**
 *
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public interface DataPool {

    Map<Class, Map<Long, Object>> getAll();

    <T> Map<Long, T> getData(Class<T> clazz);
}
