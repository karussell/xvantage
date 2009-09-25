package de.pannous.xvantage.core;

import de.pannous.xvantage.core.util.MapEntry;
import de.pannous.xvantage.core.util.Helper;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.AttributesImpl;

/**
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class Binding {

    private String elementName;
    private String pathName;
    private Class clazz;
    private Map<String, Method> getterMethods;
    private Map<String, Method> setterMethods;
    private long idCounter = 0L;
    private ObjectStringTransformer op;
    private DocumentBuilder builder;

    public Binding(ObjectStringTransformer op, String pathAndElement, Class clazz) {
        this.op = op;
        this.clazz = clazz;
        getterMethods = new HashMap();
        setterMethods = new HashMap();
        for (Method method : clazz.getMethods()) {

            String xmlElement = Helper.getPropertyFromJavaMethod(method.getName(), method.getReturnType() == boolean.class);
            if (xmlElement != null) {
                method.setAccessible(true);

                if (Helper.isSetter(method)) {
                    setterMethods.put(xmlElement, method);
                } else if (Helper.isGetter(method)) {
                    if (!method.getReturnType().equals(Class.class))
                        getterMethods.put(xmlElement, method);
                }
            }
        }

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(false);
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            throw new RuntimeException(ex);
        }

        if (!pathAndElement.startsWith("/"))
            pathAndElement = "/" + pathAndElement;

        int lastIndex = pathAndElement.lastIndexOf('/');
        if (lastIndex < 0) {
            throw new IllegalStateException("Cannot happen");
        }

        elementName = pathAndElement.substring(lastIndex + 1).trim();
        if (elementName.length() == 0) {
            elementName = Helper.getJavaModifier(clazz.getSimpleName());
        }

        pathName = pathAndElement.substring(0, lastIndex + 1);
    }

    public String getElementName() {
        return elementName;
    }

    public void setElementName(String elementName) {
        this.elementName = elementName;
    }

    public String getPath() {
        return pathName;
    }

    public void setPath(String pathName) {
        this.pathName = pathName;
    }

    public Class getClassObject() {
        return clazz;
    }

    /**
     * @return the id and the parsed object
     */
    public Entry<Long, Object> parseObject(String value) throws Exception {
        Document doc = builder.parse(new InputSource(new StringReader(value)));

        return parseNodeList(Helper.getFirstElement(doc.getChildNodes()).getChildNodes());
    }

    /**
     * @param list
     * @throws Exception, NumberFormatException could occur, failure to call newInstance, ..
     */
    private Entry<Long, Object> parseNodeList(NodeList list) throws Exception {
        Constructor c = Helper.getPrivateConstructor(clazz);
        if (c == null)
            throw new IllegalAccessException("Cannot access constructor of " + clazz);

        Object obj = c.newInstance();
        for (int ii = 0; ii < list.getLength(); ii++) {
            Node node = list.item(ii);
            if (node.getNodeType() != Node.ELEMENT_NODE)
                continue;
            Method m = setterMethods.get(node.getNodeName());
            if (m == null)
                continue;

            Class tmpClazz = m.getParameterTypes()[0];
            m.invoke(obj, op.parsePrimitiveOrCollection(tmpClazz, node));
        }

        // 1. TODO parsePrimitiveOrCollection id!
        return new MapEntry<Long, Object>(idCounter++, obj);
    }

    public Map<Long, Object> parseObjectList(InputSource iSource) throws Exception {
        Map<Long, Object> map = new HashMap<Long, Object>();
        XPath xPath = XPathFactory.newInstance().newXPath();
        NodeList nList = (NodeList) xPath.evaluate(pathName + elementName, iSource, XPathConstants.NODESET);
        for (int ii = 0; ii <
                nList.getLength(); ii++) {
            Node node = nList.item(ii);
            NodeList elementListWithProperties = node.getChildNodes();
            if (elementListWithProperties.getLength() == 0)
                continue;

            Entry<Long, Object> entry = parseNodeList(elementListWithProperties);
            map.put(entry.getKey(), entry.getValue());
        }

        return map;
    }
    private AttributesImpl atts = new AttributesImpl();

    public void writeObject(Object oneObject, TransformerHandler transformerHandler) throws Exception {
        transformerHandler.startElement("", "", getElementName(), atts);

        for (Entry<String, Method> entry : getterMethods.entrySet()) {
            Object result = entry.getValue().invoke(oneObject);
            op.writePrimitiveOrCollection(result, entry.getValue().getReturnType(), entry.getKey(), transformerHandler);
        }

        transformerHandler.endElement("", "", getElementName());
    }

    @Override
    public String toString() {
        return "" + clazz.getName() + " -> " + elementName;
    }
}
