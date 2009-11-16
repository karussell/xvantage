package de.pannous.xvantage.core.writing;

import de.pannous.xvantage.core.Binding;
import de.pannous.xvantage.core.ObjectStringTransformer;
import java.io.File;
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

    private String getAliasFromClass(Class clazz) {
        String res = classToString.get(clazz);
        if (res == null)
            return clazz.getName();

        return res;
    }
    public static Writing WRITING_TO_STRING = new Writing() {

        public void toString(Object object, TransformerHandler transformerHandler) throws Exception {
            String str = object.toString();
            transformerHandler.characters(str.toCharArray(), 0, str.length());
        }
    };
    public static Writing CLASS_TO_STRING = new Writing() {

        public void toString(Object object, TransformerHandler transformerHandler) throws Exception {
            String str = ((Class) object).getName();
            transformerHandler.characters(str.toCharArray(), 0, str.length());
        }
    };
    // without collections because they have a special handling: Collection.class.isAssignableFrom(clazz)
    private HashMap<Class, Writing> selectWriteMethodMap = new HashMap<Class, Writing>() {

        {
            put(Byte.class, WRITING_TO_STRING);
            put(byte.class, WRITING_TO_STRING);

            put(Double.class, WRITING_TO_STRING);
            put(double.class, WRITING_TO_STRING);

            put(Float.class, WRITING_TO_STRING);
            put(float.class, WRITING_TO_STRING);

            put(Long.class, WRITING_TO_STRING);
            put(long.class, WRITING_TO_STRING);

            put(Integer.class, WRITING_TO_STRING);
            put(int.class, WRITING_TO_STRING);

            put(Short.class, WRITING_TO_STRING);
            put(short.class, WRITING_TO_STRING);

            put(Boolean.class, WRITING_TO_STRING);
            put(boolean.class, WRITING_TO_STRING);

            put(Character.class, WRITING_TO_STRING);
            put(char.class, WRITING_TO_STRING);

            put(String.class, WRITING_TO_STRING);

            put(BitSet.class, WRITING_TO_STRING);

            put(File.class, WRITING_TO_STRING);
            
            put(Class.class, CLASS_TO_STRING);
        }
    };

    public void putWriting(Class clazz, Writing w) {
        Writing old = selectWriteMethodMap.put(clazz, w);
        if (old != null)
            logger.warning("Overwriting writing:" + old + "(class: " + clazz + ")");
    }

    /**
     * Write an object of a mounted class
     */
    <T> void writePOJO(Binding<T> binding, T mountedObject,
            TransformerHandler transformerHandler) throws Exception {
        writeObject(binding, mountedObject, binding.getClassObject(), binding.getElementName(), transformerHandler);
    }

    /**
     * This method writes the specified object to the transformerHandler as property.
     * It will only write its id if the object is not a primitive nor a collection.
     *
     * @param object the object to be saved
     * @param clazz is necessary to check if the object should be serialized as collections
     * @param elementName the name of the xml element
     * @param transformerHandler
     * @throws SAXException
     */
    void writeObject(Object object, Class clazz,
            String elementName, TransformerHandler transformerHandler) throws Exception {
        writeObject(null, object, clazz, elementName, transformerHandler);
    }

    /**
     * Writes mounted or unmounted object.
     * 
     * @param binding not null if a mounted object
     * @param object the object to write
     * @param clazz the class of the object
     * @param elementName the name of the xml element for this object
     * @param transformerHandler
     * @throws Exception
     */
    public void writeObject(Binding binding, Object object, Class clazz,
            String elementName, TransformerHandler transformerHandler) throws Exception {

        if (object == null && skipNullProperty)
            return;

        atts.clear();
        if (object != null) {
            Class entryClass = object.getClass();
            // print arrayList if list (assignable because of 'extends') but do not add jc='Long' if 'long'
            if (!entryClass.equals(clazz) && !samePrimitive(entryClass, clazz)) {
                atts.addAttribute("", "", javaClass, "", getAliasFromClass(entryClass));
                clazz = entryClass;
            }
        }

        Long id = dataPool.getId(object);
        if (id != null)
            atts.addAttribute("", "", "id", "", Long.toString(id));

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
                Class firstValueClass = null;
                for (Object innerObj : array) {
                    if (firstEntry) {
                        firstEntry = false;
                        firstValueClass = innerObj.getClass();
                        atts.addAttribute("", "", valueClassStr, "", getAliasFromClass(firstValueClass));
                        transformerHandler.startElement("", "", elementName, atts);
                    }
                    writeObject(innerObj, firstValueClass, valueStr, transformerHandler);
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
                Class firstValueClass = null;
                for (Object innerObj : (Iterable) object) {
                    if (firstEntry) {
                        firstEntry = false;
                        firstValueClass = innerObj.getClass();
                        atts.addAttribute("", "", valueClassStr, "", getAliasFromClass(firstValueClass));
                        transformerHandler.startElement("", "", elementName, atts);
                    }
                    writeObject(innerObj, firstValueClass, valueStr, transformerHandler);
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
                Class firstKeyClass = null;
                Class firstValueClass = null;
                for (Object innerObj : map.entrySet()) {
                    Entry entry = (Entry) innerObj;
                    if (firstEntry) {
                        firstEntry = false;
                        firstKeyClass = entry.getKey().getClass();
                        firstValueClass = entry.getValue().getClass();
                        atts.addAttribute("", "", keyClassStr, "", getAliasFromClass(firstKeyClass));
                        atts.addAttribute("", "", valueClassStr, "", getAliasFromClass(firstValueClass));
                        transformerHandler.startElement("", "", elementName, atts);
                    }
                    atts.clear();
                    transformerHandler.startElement("", "", entryStr, atts);
                    writeObject(entry.getKey(), firstKeyClass, keyStr, transformerHandler);
                    writeObject(entry.getValue(), firstValueClass, valueStr, transformerHandler);
                    transformerHandler.endElement("", "", entryStr);
                }
            }
//        } else if(Serializable.class.isAssignableFrom(clazz)) {
//            binding.getWriteObjectMethod().invoke(object, );
        } else if (binding != null) {
            elementName = binding.getElementName();
            transformerHandler.startElement("", "", elementName, atts);
            writeGetterOnly(binding, object, transformerHandler);
        } else if (id != null) {
            // reference to an existing object, so: write id as subnode not as attribute
            // do not put anything as attribute, because the object has to be an
            // instance of a mounted class
            atts.clear();
            transformerHandler.startElement("", "", elementName, atts);
            String str = Long.toString(id);
            transformerHandler.characters(str.toCharArray(), 0, str.length());
        } else if (object == null) {
            // empty value results in empty node
            transformerHandler.startElement("", "", elementName, atts);
        } else {
            // TODO PERFORMANCE use cached bindings for unmounted classes
            // try to write as POJO
            transformerHandler.startElement("", "", elementName, atts);
            writeGetterOnly(new Binding("/unknon", clazz), object, transformerHandler);
        }

        transformerHandler.endElement("", "", elementName);
    }

    private void writeGetterOnly(Binding binding, Object object, TransformerHandler transformerHandler)
            throws Exception {
        atts.clear();
        for (Object obj : binding.getGetterMethods().entrySet()) {
            Entry<String, Method> tmpEntry = (Entry<String, Method>) obj;
            if (binding.shouldIgnore(tmpEntry.getValue().getName()))
                continue;

            Object result = tmpEntry.getValue().invoke(object);
            writeObject(result, tmpEntry.getValue().getReturnType(), tmpEntry.getKey(), transformerHandler);
        }
    }

    public void writeObject(Object object, Class expectedClass, Class entryClass,
            String elementName, TransformerHandler transformerHandler) throws Exception {

        writeObject(object, expectedClass, elementName, transformerHandler);
    }

    /**
     * @return true if long==Long, int==Integer, byte==Byte, float==Float, Double==double
     */
    boolean samePrimitive(Class entryClass, Class clazz) {
        String str = classToString.get(entryClass);
        if (str == null)
            return false;
        return str.equalsIgnoreCase(classToString.get(clazz));
    }
}
