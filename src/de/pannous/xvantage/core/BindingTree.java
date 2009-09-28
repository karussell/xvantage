package de.pannous.xvantage.core;

import de.pannous.xvantage.core.writing.ObjectWriting;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * This tree represents the structure to mount some bindings into the DOM tree.
 * 
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class BindingTree {

    private Map<Class, Binding> bindings = new HashMap<Class, Binding>();
    private BindingLeaf root;
    private int maxLevel;
    private ObjectWriting writing;

    public BindingTree(ObjectWriting tr) {
        writing = tr;
    }

    public void mount(Binding binding) {
        Binding old = bindings.put(binding.getClassObject(), binding);
        if (old != null)
            throw new IllegalArgumentException("Cannot bind one class (" +
                    binding.getClassObject() + ") to several paths!");

        // create sub tree if necessary
        BindingLeaf tmpRoot = null;

        List<String> subElements = new ArrayList<String>();
        for (String str : binding.getPath().split("/")) {
            str = str.trim();
            if (str.length() > 0)
                subElements.add(str);
        }
        subElements.add(binding.getElementName());

        if (root == null) {
            root = new BindingLeaf(subElements.get(0));
        } else {
            // there is only one root
            if (!subElements.get(0).equals(root.getName()))
                throw new IllegalArgumentException("You cannot use multiple roots: " + subElements.get(0) + " != " + root.getName());
        }

        tmpRoot = root;
        int level = 1;
        BindingLeaf currentLeaf = null;
        while (level < subElements.size()) {
            for (BindingLeaf leaf : tmpRoot.getChilds()) {
                if (leaf.getName().equals(subElements.get(level))) {
                    currentLeaf = leaf;
                    break;
                }
            }

            if (currentLeaf == null) {
                currentLeaf = new BindingLeaf(subElements.get(level));
                currentLeaf.setParent(tmpRoot);
            }

            level++;
            tmpRoot = currentLeaf;
            currentLeaf = null;
        }
        maxLevel = level;
        if (tmpRoot.isRoot())
            throw new UnsupportedOperationException("Mounting an object directly as root is currently not supported. " +
                    "Simple do mount(/path/obj, YourObject.class) instead mount(/obj, YourObject.class);");

        tmpRoot.mount(binding);
    }

    public void saveObjects(DataPool pool,
            Writer writer,
            String encoding)
            throws SAXException, TransformerConfigurationException {

        StreamResult streamResult = new StreamResult(writer);
        SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();

        TransformerHandler transformerHandler = tf.newTransformerHandler();
        Transformer serializer = transformerHandler.getTransformer();
        serializer.setOutputProperty(OutputKeys.ENCODING, encoding);
        serializer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformerHandler.setResult(streamResult);
        transformerHandler.startDocument();

        // nothing to do
        if (root == null)
            return;

        processDoc(pool, transformerHandler);
        transformerHandler.endDocument();
    }

    private void processDoc(DataPool dataPool, TransformerHandler transformerHandler)
            throws SAXException {

        if (root.getMountedBindings().size() >= 1)
            throw new UnsupportedOperationException("You cannot mount an object as root. Currently not supported. (More than one will never be supported)");

        processNode(root, dataPool, transformerHandler);
    }
    private AttributesImpl atts = new AttributesImpl();

    private void processNode(BindingLeaf rootLeaf, DataPool dataPool, TransformerHandler transformerHandler) throws SAXException {

        for (Binding bind : rootLeaf.getMountedBindings()) {
            Map<Long, Object> tmpMap = dataPool.getData(bind.getClassObject());
            if (tmpMap != null) {
                for (Object oneObject : tmpMap.values()) {
                    try {
                        writing.writeObject(bind, oneObject, transformerHandler);
                    } catch (Exception ex) {
                        throw new UnsupportedOperationException("Couldn't write object " +
                                oneObject + " (" + bind + ")", ex);
                    }
                }
            }
        }
        if (rootLeaf.getChilds().size() == 0)
            return;

        transformerHandler.startElement("", "", rootLeaf.getName(), atts);
        for (BindingLeaf leaf : rootLeaf.getChilds()) {
            processNode(leaf, dataPool, transformerHandler);
        }
        transformerHandler.endElement("", "", rootLeaf.getName());
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public BindingLeaf getRoot() {
        return root;
    }

    public Collection<Binding> getBindings() {
        return bindings.values();
    }

    public Binding getBinding(Class clazz) {
        return bindings.get(clazz);
    }
}
