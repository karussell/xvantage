package de.pannous.xvantage.core;

import java.util.Map;

/**
 * A pool where we store all objects which should be serialized
 * 
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public interface DataPool {

    /**
     * @return all objects of the specified clazz
     */
    <T> Map<Long, T> getData(Class<T> clazz);

    /**
     * @return the id of the specfied object
     */
    Long getId(Object object);
}
