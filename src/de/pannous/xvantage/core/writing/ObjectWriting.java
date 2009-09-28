package de.pannous.xvantage.core.writing;

import de.pannous.xvantage.core.Binding;
import de.pannous.xvantage.core.ObjectStringTransformer;
import java.lang.reflect.Method;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.transform.sax.TransformerHandler;
import org.xml.sax.SAXException;

/**
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class ObjectWriting extends ObjectStringTransformer {

    private Map<Class, String> classToString = new HashMap<Class, String>() {

        {
            put(Byte.class, "byte");
            put(byte.class, "byte");

            put(Double.class, "double");
            put(double.class, "double");
            put(Float.class, "float");
            put(float.class, "float");

            put(Long.class, "long");
            put(long.class, "long");
            put(Integer.class, "integer");
            put(int.class, "integer");

            put(Character.class, "char");
            put(char.class, "char");

            put(Short.class, "short");
            put(short.class, "short");

            put(Boolean.class, "boolean");
            put(boolean.class, "boolean");

            put(String.class, "string");

            put(BitSet.class, "bitSet");
        }
    };

    private String getAliasFromClass(Class clazz) {
        String res = classToString.get(clazz);
        if (res == null)
            return clazz.getName();

        return res;
    }
    private Writing writingToString = new Writing() {

        public void toString(Object object, TransformerHandler transformerHandler) throws Exception {
            String str = object.toString();
            transformerHandler.characters(str.toCharArray(), 0, str.length());
        }
    };

    // without collections because they have a special handling: Collection.class.isAssignableFrom(clazz)
    private HashMap<Class, Writing> selectWriteMethodMap = new HashMap<Class, Writing>() {

        {
            put(Byte.class, writingToString);
            put(byte.class, writingToString);

            put(Double.class, writingToString);
            put(double.class, writingToString);

            put(Float.class, writingToString);
            put(float.class, writingToString);

            put(Long.class, writingToString);
            put(long.class, writingToString);

            put(Integer.class, writingToString);
            put(int.class, writingToString);

            put(Short.class, writingToString);
            put(short.class, writingToString);

            put(Boolean.class, writingToString);
            put(boolean.class, writingToString);

            put(Character.class, writingToString);
            put(char.class, writingToString);

            put(String.class, writingToString);

            put(BitSet.class, writingToString);
        }
    };

    public void putWriting(Class clazz, Writing w) {
        selectWriteMethodMap.put(clazz, w);
    }

    public <T> void writeObject(Binding<T> binding, T oneObject, TransformerHandler transformerHandler) throws Exception {
        Long id = dataPool.getId(oneObject);
        if (id != null)
            atts.addAttribute("", "", "id", "", Long.toString(id));

        transformerHandler.startElement("", "", binding.getElementName(), atts);
        atts.clear();
        for (Entry<String, Method> tmpEntry : binding.getGetterMethods().entrySet()) {
            if (binding.shouldIgnore(tmpEntry.getValue().getName()))
                continue;

            Object result = tmpEntry.getValue().invoke(oneObject);
            if (result == null && skipNullProperty)
                continue;

            writeObject(result, tmpEntry.getValue().getReturnType(), tmpEntry.getKey(), transformerHandler);
        }

        transformerHandler.endElement("", "", binding.getElementName());
    }

    /**
     * This method writes the specified object to the transformerHandler as property.
     * It will only write its id if object is not a primitive nor a collection.
     *
     * This is different to writeObject, which writes the object with all of its properties.
     * @see #writeObject(de.pannous.xvantage.core.Binding, java.lang.Object, javax.xml.transform.sax.TransformerHandler)
     *
     * @param object the object to be saved
     * @param clazz is necessary to check if the object should be serialized as collections
     * @param elementName the name of the xml element
     * @param transformerHandler
     * @throws SAXException
     */
    public void writeObject(Object object, Class clazz,
            String elementName, TransformerHandler transformerHandler) throws Exception {

        atts.clear();
        Writing writing = selectWriteMethodMap.get(clazz);
        if (writing != null) {
            transformerHandler.startElement("", "", elementName, atts);
            if (object != null)
                writing.toString(object, transformerHandler);
        } else if (clazz.isArray()) {
            // TODO char[] -> ClassCastException
            Object[] array = (Object[]) object;
            int size = array.length;
            if (size == 0) {
                if (skipNullProperty)
                    return;

                transformerHandler.startElement("", "", elementName, atts);
            } else {
                boolean firstEntry = true;
                for (Object innerObj : array) {
                    if (firstEntry) {
                        firstEntry = false;
                        atts.addAttribute("", "", valueClassStr, "", getAliasFromClass(innerObj.getClass()));
                        transformerHandler.startElement("", "", elementName, atts);
                    }
                    writeObject(innerObj, innerObj.getClass(), valueStr, transformerHandler);
                }
            }
        } else if (Collection.class.isAssignableFrom(clazz)) {
            int size = ((Collection) object).size();
            if (size == 0) {
                if (skipNullProperty)
                    return;

                transformerHandler.startElement("", "", elementName, atts);
            } else {
                boolean firstEntry = true;
                for (Object innerObj : (Iterable) object) {
                    if (firstEntry) {
                        firstEntry = false;
                        atts.addAttribute("", "", valueClassStr, "", getAliasFromClass(innerObj.getClass()));
                        transformerHandler.startElement("", "", elementName, atts);
                    }
                    writeObject(innerObj, innerObj.getClass(), valueStr, transformerHandler);
                }
            }
        } else if (Map.class.isAssignableFrom(clazz)) {
            Map map = (Map) object;
            if (map.size() == 0) {
                if (skipNullProperty)
                    return;

                transformerHandler.startElement("", "", elementName, atts);
            } else {
                boolean firstEntry = true;
                for (Object innerObj : map.entrySet()) {
                    Entry entry = (Entry) innerObj;
                    if (firstEntry) {
                        firstEntry = false;
                        atts.addAttribute("", "", valueClassStr, "", getAliasFromClass(entry.getValue().getClass()));
                        atts.addAttribute("", "", keyClassStr, "", getAliasFromClass(entry.getKey().getClass()));
                        transformerHandler.startElement("", "", elementName, atts);
                        atts.clear();
                    }
                    transformerHandler.startElement("", "", entryStr, atts);
                    writeObject(entry.getKey(), entry.getKey().getClass(), keyStr, transformerHandler);
                    writeObject(entry.getValue(), entry.getValue().getClass(), valueStr, transformerHandler);
                    transformerHandler.endElement("", "", entryStr);
                }
            }
        } else {
            Long id = dataPool.getId(object);
            if (id != null) {
                String str = Long.toString(id);
                transformerHandler.startElement("", "", elementName, atts);
                transformerHandler.characters(str.toCharArray(), 0, str.length());
            } else {
                if (object != null) {
                    // try to write as POJO
                    // TODO PERFORMANCE use cached bindings for unmounted classes
                    writeObject(new Binding("/unknown/" + elementName, object.getClass()), object, transformerHandler);
                    return;
                } else {
                    // skip value
                    transformerHandler.startElement("", "", elementName, atts);
                }
            }
        }

        transformerHandler.endElement("", "", elementName);
    }
}
