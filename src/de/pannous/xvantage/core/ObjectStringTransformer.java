package de.pannous.xvantage.core;

import de.pannous.xvantage.core.parsing.Parsing;
import de.pannous.xvantage.core.parsing.ArrayParsing;
import de.pannous.xvantage.core.util.BiMap;
import de.pannous.xvantage.core.util.Helper;
import de.pannous.xvantage.core.util.MapEntry;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.BitSet;
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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.sax.TransformerHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
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
public class ObjectStringTransformer {

    private final String valueClassStr = "valueClass";
    private final String keyClassStr = "keyClass";
    private final String valueStr = "value";
    private final String keyStr = "key";
    private final String entryStr = "entry";
    private Class<? extends Map> defaultMapImpl = HashMap.class;
    private Class<? extends Set> defaultSetImpl = HashSet.class;
    private Class<? extends List> defaultListImpl = ArrayList.class;
    private DataPool dataPool;
    private DocumentBuilder builder;
    private long idCounter = 0;
    private AttributesImpl atts = new AttributesImpl();

    public ObjectStringTransformer() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(false);
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void init(DataPool dataPool) {
        this.dataPool = dataPool;
        idCounter = 0;
    }

    public DataPool getDataPool() {
        return dataPool;
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

    /**
     * @return the id and one parsed object
     */
    public <T> Entry<Long, T> parseObject(Binding<T> binding, String value) throws Exception {
        Document doc = builder.parse(new InputSource(new StringReader(value)));

        Class clazz = binding.getClassObject();
        Map<Long, T> objects = dataPool.getData(clazz);
        Element firstElement = Helper.getFirstElement(doc.getChildNodes());
        Long id;
        try {
            id = Long.parseLong(firstElement.getAttribute("id"));
        } catch (Exception ex) {
            id = idCounter++;
        }
        T obj = objects.get(id);

        if (obj == null) {
            // object was already earlier referenced (TODO how to detect duplicate ids?)
            Constructor c = Helper.getPrivateConstructor(clazz);
            if (c == null)
                throw new IllegalAccessException("Cannot access constructor of " + clazz);

            obj = (T) c.newInstance();
            objects.put(id, obj);
        }

        fillWithProperties(binding, obj, firstElement.getChildNodes());
        return new MapEntry<Long, T>(id, obj);
    }

    /**
     * @param obj the object to be initialized with the properties
     * @param list of nodes which should be the properties of an object the mounted class
     *
     * @throws Exception, NumberFormatException could occur, failure to call newInstance, ..
     */
    private <T> void fillWithProperties(Binding<T> binding, T obj, NodeList list) throws Exception {
        for (int ii = 0; ii < list.getLength(); ii++) {
            Node node = list.item(ii);
            if (node.getNodeType() != Node.ELEMENT_NODE)
                continue;
            Method m = binding.getSetterMethods().get(node.getNodeName());
            if (m == null)
                continue;
            if (binding.shouldIgnore(m.getName()))
                continue;

            Class tmpClazz = m.getParameterTypes()[0];
            m.invoke(obj, parseObjectAsProperty(tmpClazz, node));
        }
    }

    Object parseObjectAsProperty(Class tmpClazz, Node node) {
        Parsing parsing = getClassParsing(tmpClazz);
        if (parsing == null) {
            // in the case no collection or no primitive type was found we use a reference
            // example <mainTask>1</mainTask>
            Long id;
            try {
                id = (Long) longParse.parse(node);
                Map<Long, Object> map = dataPool.getData(tmpClazz);
                Object obj = map.get(id);
                if (obj == null) {
                    try {
                        Constructor c = Helper.getPrivateConstructor(tmpClazz);
                        obj = c.newInstance();
                    } catch (Exception ex) {
                        try {
                            obj = tmpClazz.newInstance();
                        } catch (Exception ex2) {
                            throw new UnsupportedOperationException("Couldn't call default constructor of " + tmpClazz + " node:" + node.getTextContent(), ex2);
                        }
                    }
                    map.put(id, obj);
                }
                return obj;
            } catch (NumberFormatException ex) {
                return null;
            }


        }
        return parsing.parse(node);
    }

    public void fillCollection(Collection coll, Node node) {
        try {
            Element root = (Element) node;
            String valC = root.getAttribute(valueClassStr).trim();
            if (valC.length() == 0)
                return;

            Class valueType = getClassFromAlias(valC);
            NodeList list = root.getChildNodes();
            for (int ii = 0; ii < list.getLength(); ii++) {
                Node tmpNode = list.item(ii);
                if (tmpNode.getNodeType() == Node.ELEMENT_NODE)
                    coll.add(parseObjectAsProperty(valueType, tmpNode));
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void fillMap(Map map, Node node) {
        try {
            Element root = (Element) node;
            String valC = root.getAttribute(valueClassStr).trim();
            String keyC = root.getAttribute(keyClassStr).trim();
            if (valC.length() == 0 || keyC.length() == 0)
                return;

            Class valueType = getClassFromAlias(valC);
            Class keyType = getClassFromAlias(keyC);

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
                    map.put(parseObjectAsProperty(keyType, keyNode), parseObjectAsProperty(valueType, valueNode));
            }

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public <T> void writeObject(Binding<T> binding, T oneObject, TransformerHandler transformerHandler) throws Exception {
        BiMap<Long, T> map = dataPool.getData(binding.getClassObject());
        Long id = map.getSecond(oneObject);
        if (id != null)
            atts.addAttribute("", "", "id", "", Long.toString(id));

        transformerHandler.startElement("", "", binding.getElementName(), atts);
        atts.clear();
        for (Entry<String, Method> tmpEntry : binding.getGetterMethods().entrySet()) {
            if (binding.shouldIgnore(tmpEntry.getValue().getName()))
                continue;

            Object result = tmpEntry.getValue().invoke(oneObject);
            writeObjectAsProperty(result, tmpEntry.getValue().getReturnType(), tmpEntry.getKey(), transformerHandler);
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
    public void writeObjectAsProperty(Object object, Class clazz,
            String elementName, TransformerHandler transformerHandler) throws SAXException {

        atts.clear();
        if (clazz.isArray()) {
            Object[] array = (Object[]) object;
            int size = array.length;
            if (size == 0) {
                transformerHandler.startElement("", "", elementName, atts);
            } else {
                boolean firstEntry = true;
                for (Object innerObj : array) {
                    if (firstEntry) {
                        firstEntry = false;
                        atts.addAttribute("", "", valueClassStr, "", getAliasFromClass(innerObj.getClass()));
                        transformerHandler.startElement("", "", elementName, atts);
                    }
                    writeObjectAsProperty(innerObj, innerObj.getClass(), valueStr, transformerHandler);
                }
            }
        } else if (Collection.class.isAssignableFrom(clazz)) {
            int size = ((Collection) object).size();
            if (size == 0) {
                transformerHandler.startElement("", "", elementName, atts);
            } else {
                boolean firstEntry = true;
                for (Object innerObj : (Iterable) object) {
                    if (firstEntry) {
                        firstEntry = false;
                        atts.addAttribute("", "", valueClassStr, "", getAliasFromClass(innerObj.getClass()));
                        transformerHandler.startElement("", "", elementName, atts);
                    }
                    writeObjectAsProperty(innerObj, innerObj.getClass(), valueStr, transformerHandler);
                }
            }
        } else if (Map.class.isAssignableFrom(clazz)) {
            Map map = (Map) object;
            if (map.size() == 0) {
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
                    writeObjectAsProperty(entry.getKey(), entry.getKey().getClass(), keyStr, transformerHandler);
                    writeObjectAsProperty(entry.getValue(), entry.getValue().getClass(), valueStr, transformerHandler);
                    transformerHandler.endElement("", "", entryStr);
                }
            }
        } else {
            BiMap<Long, Object> ids = dataPool.getData(clazz);

            if (ids != null) {
                Long id = ids.getSecond(object);
                if (id != null) {
                    object = Long.toString(id);
                }
            }

            String str = object == null ? "" : object.toString();
            transformerHandler.startElement("", "", elementName, atts);
            transformerHandler.characters(str.toCharArray(), 0, str.length());
        }

        transformerHandler.endElement("", "", elementName);
    }

    private Class getClassFromAlias(String classAlias) throws ClassNotFoundException {
        Class clazz = stringToPrimitiveClasses.get(classAlias.toLowerCase());
        if (clazz == null)
            return Class.forName(classAlias);

        return clazz;
    }

    private String getAliasFromClass(Class clazz) {
        String res = classToString.get(clazz);
        if (res == null)
            return clazz.getName();

        return res;
    }

    private Parsing getClassParsing(Class tmpClazz) {
        Parsing parsing = selectMethodMap.get(tmpClazz);
        if (parsing == null) {
            if (Map.class.isAssignableFrom(tmpClazz)) {
                parsing = mapParse;
            } else if (Set.class.isAssignableFrom(tmpClazz)) {
                parsing = setParse;
            } else if (List.class.isAssignableFrom(tmpClazz)) {
                parsing = listParse;
            } else if (tmpClazz.isArray()) {
                arrayParse.setComponentType(tmpClazz.getComponentType());
                parsing = arrayParse;
            } else if (Collection.class.isAssignableFrom(tmpClazz)) {
                parsing = listParse;
            }
            // else unknown class => no collection/primitive
        }
        return parsing;
    }
    private static Parsing byteParse = new Parsing() {

        public Object parse(Node node) {
            return Byte.parseByte(node.getTextContent().trim());
        }
    };
    private static Parsing floatParse = new Parsing() {

        public Object parse(Node node) {
            return Float.parseFloat(node.getTextContent().trim());
        }
    };
    private static Parsing doubleParse = new Parsing() {

        public Object parse(Node node) {
            return Double.parseDouble(node.getTextContent().trim());
        }
    };
    private static Parsing longParse = new Parsing() {

        public Object parse(Node node) {
            return Long.parseLong(node.getTextContent().trim());
        }
    };
    private static Parsing intParse = new Parsing() {

        public Object parse(Node node) {
            return Integer.parseInt(node.getTextContent().trim());
        }
    };
    private static Parsing shortParse = new Parsing() {

        public Object parse(Node node) {
            return Short.parseShort(node.getTextContent().trim());
        }
    };
    private static Parsing boolParse = new Parsing() {

        public Object parse(Node node) {
            return Boolean.parseBoolean(node.getTextContent().trim());
        }
    };
    private static Parsing charParse = new Parsing() {

        public Object parse(Node node) {
            String str = node.getTextContent();
            if (str.length() > 0)
                return str.charAt(0);
            return "";
        }
    };
    private Parsing mapParse = new Parsing() {

        public Object parse(Node node) {
            Map map;
            try {
                map = defaultMapImpl.newInstance();
            } catch (Exception ex) {
                map = new HashMap();
            }
            fillMap(map, node);
            return map;
        }
    };
    private Parsing setParse = new Parsing() {

        public Object parse(Node node) {
            Set set;
            try {
                set = defaultSetImpl.newInstance();
            } catch (Exception ex) {
                set = new HashSet();
            }
            fillCollection(set, node);
            return set;
        }
    };
    private Parsing listParse = new Parsing() {

        public Object parse(Node node) {
            List list;
            try {
                list = defaultListImpl.newInstance();
            } catch (Exception ex) {
                list = new ArrayList();
            }
            fillCollection(list, node);
            return list;
        }
    };
    private Parsing linkedSetParse = new Parsing() {

        public Object parse(Node node) {
            Set linkedSet = new LinkedHashSet();
            fillCollection(linkedSet, node);
            return linkedSet;
        }
    };
    private Parsing linkedMapParse = new Parsing() {

        public Object parse(Node node) {
            Map linkedMap = new LinkedHashMap();
            fillMap(linkedMap, node);
            return linkedMap;
        }
    };
    private Parsing linkedListParse = new Parsing() {

        public Object parse(Node node) {
            List linkedList = new LinkedList();
            fillCollection(linkedList, node);
            return linkedList;
        }
    };
    private Parsing stringParse = new Parsing() {

        public Object parse(Node node) {
            return node.getTextContent();
        }
    };
    private Parsing bitSetParse = new Parsing() {

        public Object parse(Node node) {
            String bitSetAsStr = node.getTextContent();
            // remove the {}
            bitSetAsStr = bitSetAsStr.substring(1, bitSetAsStr.length() - 1);
            BitSet bitSet = new BitSet();
            for (String str : bitSetAsStr.split(",")) {
                str = str.trim();
                if (str.length() > 0)
                    bitSet.set(Integer.parseInt(str.trim()));
            }
            return bitSet;
        }
    };
    private ArrayParsing arrayParse = new ArrayParsing(this);
    // it is important that this declaration comes after all Parsing objects
    // are initialized
    private HashMap<Class, Parsing> selectMethodMap = new HashMap<Class, Parsing>() {

        {
            put(Byte.class, byteParse);
            put(byte.class, byteParse);

            put(Double.class, doubleParse);
            put(double.class, doubleParse);

            put(Float.class, floatParse);
            put(float.class, floatParse);

            put(Long.class, longParse);
            put(long.class, longParse);

            put(Integer.class, intParse);
            put(int.class, intParse);

            put(Short.class, shortParse);
            put(short.class, shortParse);

            put(Boolean.class, boolParse);
            put(boolean.class, boolParse);

            put(Character.class, charParse);
            put(char.class, charParse);

            put(Map.class, mapParse);
            put(HashMap.class, mapParse);

            put(Set.class, setParse);
            put(HashSet.class, setParse);

            put(ArrayList.class, listParse);

            put(LinkedHashSet.class, linkedSetParse);

            put(LinkedHashMap.class, linkedMapParse);

            put(LinkedList.class, linkedListParse);

            put(String.class, stringParse);

            put(BitSet.class, bitSetParse);
        }
    };
    // avoid long class names for primitive types
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

            put("bitSet", BitSet.class);
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

            put(BitSet.class, "bitSet");
        }
    };
}

