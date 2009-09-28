package de.pannous.xvantage.core;

import org.xml.sax.helpers.AttributesImpl;

/**
 * This class transforms between objects (primitives, collections) and string.
 * Support only for some collections were provided.
 * The dataPool is necessary to get the id of a none primitive object as property.
 * E.g. task1.persons Then several the ids of the persons of task1 will be necessary.
 * Otherwise it will use an internal counter to keep track of those references.
 * 
 * An object should be created only once and then initialized with a new dataPool
 * an every read/write.
 * 
 * @see Xvantage
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public abstract class ObjectStringTransformer {

    protected final String valueClassStr = "valueClass";
    protected final String keyClassStr = "keyClass";
    protected final String valueStr = "value";
    protected final String keyStr = "key";
    protected final String entryStr = "entry";
    protected DataPool dataPool;
    protected AttributesImpl atts = new AttributesImpl();
    protected boolean skipNullProperty = false;

    public void init(DataPool dataPool) {
        this.dataPool = dataPool;
    }

    public DataPool getDataPool() {
        return dataPool;
    }

    public void setSkipNullProperty(boolean skip) {
        this.skipNullProperty = skip;
    }
}

