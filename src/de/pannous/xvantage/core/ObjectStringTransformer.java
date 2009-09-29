package de.pannous.xvantage.core;

import de.pannous.xvantage.core.util.BiMap;
import java.util.BitSet;
import java.util.logging.Logger;
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
    protected Logger logger = Logger.getLogger(getClass().getName());
    // avoid long class names for primitive types
    protected BiMap<Class, String> classToString = new BiMap<Class, String>();

    public ObjectStringTransformer() {
        putAlias(Byte.class, "Byte");
        putAlias(byte.class, "byte");

        putAlias(Double.class, "Double");
        putAlias(double.class, "double");
        putAlias(Float.class, "Float");
        putAlias(float.class, "float");

        putAlias(Long.class, "Long");
        putAlias(long.class, "long");
        putAlias(Integer.class, "Integer");
        putAlias(int.class, "int");

        putAlias(Character.class, "Character");
        putAlias(char.class, "char");

        putAlias(Short.class, "Short");
        putAlias(short.class, "short");

        putAlias(Boolean.class, "Boolean");
        putAlias(boolean.class, "boolean");

        putAlias(String.class, "String");

        putAlias(BitSet.class, "BitSet");
    }

    public void putAlias(Class clazz, String str) {
        String old = classToString.put(clazz, str);
        if (old != null)
            logger.warning("Overwriting alias:" + old + "(class: " + clazz + ")");
    }

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

