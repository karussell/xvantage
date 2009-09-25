package de.pannous.xvantage.core;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.xml.transform.sax.TransformerHandler;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * This class transforms between primitive objects and string.
 * Additional support for some collection was provided.
 * Should be only once and then configured.
 * 
 * @see Xvantage
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class ObjectStringTransformer {

    private final static String valueClassStr = "valueClass";
    private final static String keyClassStr = "keyClass";
    private final static String valueStr = "value";
    private final static String keyStr = "key";
    private final static String entryStr = "entry";
    private Class<? extends Map> defaultMapImpl = HashMap.class;
    private Class<? extends Set> defaultSetImpl = HashSet.class;
    private Class<? extends List> defaultListImpl = ArrayList.class;

    public ObjectStringTransformer() {
        
    }

    public Class<? extends List> getDefaultListImpl() {
        return defaultListImpl;
    }

    public void setDefaultListImpl(Class<? extends List> defaultListImpl) {
        this.defaultListImpl = defaultListImpl;
    }

    public Class<? extends Map> getDefaultMapImpl() {
        return defaultMapImpl;
    }

    public void setDefaultMapImpl(Class<? extends Map> defaultMapImpl) {
        this.defaultMapImpl = defaultMapImpl;
    }

    public Class<? extends Set> getDefaultSetImpl() {
        return defaultSetImpl;
    }

    public void setDefaultSetImpl(Class<? extends Set> defaultSetImpl) {
        this.defaultSetImpl = defaultSetImpl;
    }
    private HashMap<Class, Integer> selectMethodMap = new HashMap<Class, Integer>() {

        {
            put(Byte.class, 0);
            put(byte.class, 0);

            put(Double.class, 1);
            put(double.class, 1);

            put(Float.class, 2);
            put(float.class, 2);

            put(Long.class, 3);
            put(long.class, 3);

            put(Integer.class, 4);
            put(int.class, 4);

            put(Short.class, 5);
            put(short.class, 5);

            put(Boolean.class, 6);
            put(boolean.class, 6);

            put(Character.class, 7);
            put(char.class, 7);

            put(Map.class, 108);
            put(HashMap.class, 108);

            put(Set.class, 109);
            put(HashSet.class, 109);

            put(ArrayList.class, 110);

            put(LinkedHashSet.class, 111);

            put(LinkedHashMap.class, 112);

            put(LinkedList.class, 113);
        }
    };

    public Object parsePrimitiveOrCollection(Class tmpClazz, Node node) {
        int result = getClassAsInteger(tmpClazz);

        switch (result) {
            case 0:
                return (Byte) Byte.parseByte(node.getTextContent());
            case 1:
                return (Double) Double.parseDouble(node.getTextContent());
            case 2:
                return (Float) Float.parseFloat(node.getTextContent());
            case 3:
                return (Long) Long.parseLong(node.getTextContent());
            case 4:
                return (Integer) Integer.parseInt(node.getTextContent());
            case 5:
                return (Short) Short.parseShort(node.getTextContent());
            case 6:
                return (Boolean) Boolean.parseBoolean(node.getTextContent());
            case 7:
                String str = node.getTextContent();
                if (str.length() > 0)
                    return str.charAt(0);
                return str;

            case 108:
                Map map;
                try {
                    map = defaultMapImpl.newInstance();
                } catch (Exception ex) {
                    map = new HashMap();
                }
                fillMap(map, node);
                return map;

            case 109:
                Set set;
                try {
                    set = defaultSetImpl.newInstance();
                } catch (Exception ex) {
                    set = new HashSet();
                }
                fillCollection(set, node);
                return set;

            case 110:
                List list;
                try {
                    list = defaultListImpl.newInstance();
                } catch (Exception ex) {
                    list = new ArrayList();
                }
                fillCollection(list, node);
                return list;

            case 111:
                Set linkedSet = new LinkedHashSet();
                fillCollection(linkedSet, node);
                return linkedSet;

            case 112:
                Map linkedMap = new LinkedHashMap();
                fillMap(linkedMap, node);
                return linkedMap;

            case 113:
                List linkedList = new LinkedList();
                fillCollection(linkedList, node);
                return linkedList;

            case 114:
                List arrayList = new ArrayList();
                fillCollection(arrayList, node);
                Object array[] = (Object[]) Array.newInstance(tmpClazz.getComponentType(), arrayList.size());
                return arrayList.toArray(array);

            default:
                return node.getTextContent();
        }
    }

    private void fillCollection(Collection coll, Node node) {
        try {
            Element root = (Element) node;
            String valC = root.getAttribute(valueClassStr).trim();
            if (valC.length() == 0)
                return;

            Class valueType = getPrimitiveClass(valC);

            NodeList list = root.getChildNodes();
            for (int ii = 0; ii < list.getLength(); ii++) {
                Node tmpNode = list.item(ii);
                if (tmpNode.getNodeType() != Node.ELEMENT_NODE)
                    continue;

                coll.add(parsePrimitiveOrCollection(valueType, tmpNode));
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void fillMap(Map map, Node node) {
        try {
            Element root = (Element) node;
            String valC = root.getAttribute(valueClassStr).trim();
            String keyC = root.getAttribute(keyClassStr).trim();
            if (valC.length() == 0 || keyC.length() == 0)
                return;

            Class valueType = getPrimitiveClass(valC);
            Class keyType = getPrimitiveClass(keyC);

            NodeList entryNodes = root.getChildNodes();
            for (int ii = 0; ii < entryNodes.getLength(); ii++) {
                Node tmpNode = entryNodes.item(ii);
                if (tmpNode.getNodeType() != Node.ELEMENT_NODE)
                    continue;

                NodeList keyAndValueNodes = tmpNode.getChildNodes();
                Node keyNode = null;
                Node valueNode = null;
                for (int jj = 0; jj < keyAndValueNodes.getLength(); jj++) {
                    Node keyOrValue = keyAndValueNodes.item(jj);
                    if (keyOrValue.getNodeType() != Node.ELEMENT_NODE)
                        continue;

                    if (keyStr.equals(keyOrValue.getNodeName())) {
                        keyNode = keyOrValue;
                    } else if (valueStr.equals(keyOrValue.getNodeName())) {
                        valueNode = keyOrValue;
                    }

                    if (valueNode != null && keyNode != null)
                        break;
                }

                if (valueNode != null && keyNode != null)
                    map.put(parsePrimitiveOrCollection(keyType, keyNode), parsePrimitiveOrCollection(valueType, valueNode));
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    private Map<String, Class> stringToPrimitiveClasses = new HashMap<String, Class>() {

        {
            put("byte", Byte.class);

            put("double", Double.class);
            put("float", Float.class);

            put("long", Long.class);
            put("integer", Integer.class);

            put("char", Character.class);

            put("short", Short.class);

            put("boolean", Boolean.class);

            put("string", String.class);
        }
    };
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
        }
    };

    private Class getPrimitiveClass(String attribute) throws ClassNotFoundException {
        Class res = stringToPrimitiveClasses.get(attribute.toLowerCase());
        if (res == null)
            return Class.forName(attribute);

        return res;
    }

    private String getStringFromClass(Class clazz) {
        String res = classToString.get(clazz);
        if (res == null)
            return clazz.getSimpleName();

        return res;
    }
    private AttributesImpl atts = new AttributesImpl();

    /**
     *
     * @param object the object to be saved
     * @param clazz is necessary for collections
     * @param elementName the name of the xml element
     * @param transformerHandler
     * @throws SAXException
     */
    public void writePrimitiveOrCollection(Object object, Class clazz,
            String elementName, TransformerHandler transformerHandler) throws SAXException {

        atts.clear();

        if (clazz.isArray()) {
            Object[] array = (Object[]) object;
            int size = array.length;
            if (size == 0) {
                atts.addAttribute("", "", valueClassStr, "", getStringFromClass(String.class));
                transformerHandler.startElement("", "", elementName, atts);
            } else {
                boolean firstEntry = true;
                for (Object innerObj : array) {
                    if (firstEntry) {
                        firstEntry = false;
                        atts.addAttribute("", "", valueClassStr, "", getStringFromClass(innerObj.getClass()));
                        transformerHandler.startElement("", "", elementName, atts);
                    }
                    writePrimitiveOrCollection(innerObj, innerObj.getClass(), valueStr, transformerHandler);
                }
            }
        } else if (Collection.class.isAssignableFrom(clazz)) {
            int size = ((Collection) object).size();
            if (size == 0) {
                atts.addAttribute("", "", valueClassStr, "", getStringFromClass(String.class));
                transformerHandler.startElement("", "", elementName, atts);
            } else {
                boolean firstEntry = true;
                for (Object innerObj : (Iterable) object) {
                    if (firstEntry) {
                        firstEntry = false;
                        atts.addAttribute("", "", valueClassStr, "", getStringFromClass(innerObj.getClass()));
                        transformerHandler.startElement("", "", elementName, atts);
                    }
                    writePrimitiveOrCollection(innerObj, innerObj.getClass(), valueStr, transformerHandler);
                }
            }
        } else if (Map.class.isAssignableFrom(clazz)) {
            Map map = (Map) object;
            if (map.size() == 0) {
                atts.addAttribute("", "", valueClassStr, "", getStringFromClass(String.class));
                transformerHandler.startElement("", "", elementName, atts);
            } else {
                boolean firstEntry = true;
                for (Object innerObj : map.entrySet()) {
                    Entry entry = (Entry) innerObj;
                    if (firstEntry) {
                        firstEntry = false;
                        atts.addAttribute("", "", valueClassStr, "", getStringFromClass(entry.getValue().getClass()));
                        atts.addAttribute("", "", keyClassStr, "", getStringFromClass(entry.getKey().getClass()));
                        transformerHandler.startElement("", "", elementName, atts);
                        atts.clear();
                    }
                    transformerHandler.startElement("", "", entryStr, atts);
                    writePrimitiveOrCollection(entry.getKey(), entry.getKey().getClass(), keyStr, transformerHandler);
                    writePrimitiveOrCollection(entry.getValue(), entry.getValue().getClass(), valueStr, transformerHandler);
                    transformerHandler.endElement("", "", entryStr);
                }
            }
        } else {
            String str = object == null ? "" : object.toString();
            transformerHandler.startElement("", "", elementName, atts);
            transformerHandler.characters(str.toCharArray(), 0, str.length());
        }

        transformerHandler.endElement("", "", elementName);
    }

    private int getClassAsInteger(Class tmpClazz) {
        Integer result = selectMethodMap.get(tmpClazz);
        if (result == null) {
            if (Map.class.isAssignableFrom(tmpClazz)) {
                result = 108;
            } else if (Set.class.isAssignableFrom(tmpClazz)) {
                result = 109;
            } else if (List.class.isAssignableFrom(tmpClazz)) {
                result = 110;
            } else if (tmpClazz.isArray()) {
                result = 114;
            } else
                result = -1;
        }
        return result;
    }
}

