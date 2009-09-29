package de.pannous.xvantage.core.parsing;

import de.pannous.xvantage.core.Binding;
import de.pannous.xvantage.core.DataPool;
import de.pannous.xvantage.core.ObjectStringTransformer;
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
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class ObjectParsing extends ObjectStringTransformer {

    private Class<? extends Map> defaultMapImpl = HashMap.class;
    private Class<? extends Set> defaultSetImpl = HashSet.class;
    private Class<? extends List> defaultListImpl = ArrayList.class;
    private DocumentBuilder builder;
    private long idCounter = 0;
    private Logger logger = Logger.getLogger(getClass().getName());

    public ObjectParsing() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(false);
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void init(DataPool dataPool) {
        super.init(dataPool);
        idCounter = 0;
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

    public Object parseObjectAsProperty(Class tmpClazz, Node node) {
        Parsing parsing = getClassParsing(tmpClazz);
        if (parsing == null) {
            // if no collection or no primitive type was found we use a reference
            // e.g. <mainTask>1</mainTask>             
            try {
                Long id = (Long) longParse.parse(node);
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
            }
            if (node.getChildNodes().getLength() == 0)
                return null;

            try {
                Object obj = Helper.getPrivateConstructor(tmpClazz).newInstance();
                fillWithProperties(new Binding("/unknown/", tmpClazz), obj, node.getChildNodes());
                return obj;
            } catch (Exception ex) {
                throw new UnsupportedOperationException(ex);
            }            
        }
        return parsing.parse(node);
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
            if (m == null) {
                logger.info("No setter found for:" + node.getNodeName());
                continue;
            }
            if (binding.shouldIgnore(m.getName()))
                continue;

            Class tmpClazz = m.getParameterTypes()[0];
            m.invoke(obj, parseObjectAsProperty(tmpClazz, node));
        }
    }

    private Parsing getClassParsing(Class tmpClazz) {
        Parsing parsing = selectParseMethodMap.get(tmpClazz);
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

    private Class getClassFromAlias(String classAlias) throws ClassNotFoundException {
        Class clazz = classToString.getSecond(classAlias);
        if (clazz == null)
            return Class.forName(classAlias);

        return clazz;
    }
    // it is important that this declaration comes after all Parsing objects
    // are initialized
    private HashMap<Class, Parsing> selectParseMethodMap = new HashMap<Class, Parsing>() {

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

            put(String.class, STRING_PARSING);

            put(BitSet.class, BITSET_PARSING);
        }
    };

    public void putParsing(Class clazz, Parsing p) {
        selectParseMethodMap.put(clazz, p);
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
    public static Parsing STRING_PARSING = new StringParsing();
    public static Parsing BITSET_PARSING = new BitSetParsing();
    private Parsing linkedListParse = new LinkedListParsing(this);
    private ArrayParsing arrayParse = new ArrayParsing(this);
}
