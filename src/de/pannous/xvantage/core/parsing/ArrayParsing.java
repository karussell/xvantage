package de.pannous.xvantage.core.parsing;

import de.pannous.xvantage.core.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Node;

/**
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class ArrayParsing implements Parsing {

    private Class ct;
    private ObjectStringTransformer transformer;

    public ArrayParsing(ObjectStringTransformer transformer) {
        this.transformer = transformer;
    }

    public void setComponentType(Class clazz) {
        ct = clazz;
    }

    public Object parse(Node node) {
        List arrayList = new ArrayList();
        transformer.fillCollection(arrayList, node);
        Object array[] = (Object[]) Array.newInstance(ct, arrayList.size());
        return arrayList.toArray(array);
    }
};
