package de.pannous.xvantage.core.impl;

import de.pannous.xvantage.core.Binding;
import de.pannous.xvantage.core.BindingLeaf;
import de.pannous.xvantage.core.BindingTree;
import de.pannous.xvantage.core.DataPool;
import de.pannous.xvantage.core.parsing.ObjectParsing;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Reads an xml with the help of the provides bindings. As a result the DataPool
 * is initialized.
 * 
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class XDOMHandler extends DefaultHandler {

    private Document factory;
    private Element current;
    private Binding activeBinding;
    private BindingLeaf currentLeaf;
    private ObjectParsing parsing;
    private int mismatchInTree = 0;

    public XDOMHandler(ObjectParsing parsing, BindingTree bindingTree) {
        this.parsing = parsing;
        currentLeaf = bindingTree.getRoot();
        if (currentLeaf == null)
            throw new NullPointerException("root == null is not supported. Did you already mount some classed?");
    }

    @Override
    public void startDocument() throws SAXException {
        factory = createDoc();
    }

    public static Document createDoc() {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (Exception e) {
            throw new RuntimeException("can't get DOM factory", e);
        }
    }

    @Override
    public void startElement(String uri, String local, String qName, Attributes attrs)
            throws SAXException {

        if (currentLeaf == null)
            throw new UnsupportedOperationException("This is a bug! qName was " + qName);

        if (currentLeaf.getChildsMap().get(qName) != null && mismatchInTree == 0) {
            // go deeper
            currentLeaf = currentLeaf.getChildsMap().get(qName);
        } else {
            // block going deeper + block going into mounted bindings
            if (!(currentLeaf.isRoot() && currentLeaf.getName().equals(qName)))
                mismatchInTree++;
        }

        if (currentLeaf.getMountedBindingsMap().get(qName) != null && mismatchInTree == 0) {
            Binding bind = currentLeaf.getMountedBindingsMap().get(qName);
            if (bind != null && activeBinding == null) {
                activeBinding = bind;
            }
        }

        if (activeBinding != null) {
            if (current == null) {
                // start a new subtree
                current = factory.createElementNS(uri, qName);
            } else {
                Element childElement = factory.createElement(qName);
                current.appendChild(childElement);
                current = childElement;
            }

            // Add each attribute.
            for (int i = 0; i < attrs.getLength(); ++i) {
                String nsUri = attrs.getURI(i);
                String qname = attrs.getQName(i);
                String value = attrs.getValue(i);
                Attr attr = factory.createAttributeNS(nsUri, qname);
                attr.setValue(value);
                current.setAttributeNodeNS(attr);
            }
        }
    }

    @Override
    public void endElement(String uri, String local, String qName) throws SAXException {
        if (currentLeaf == null)
            throw new IllegalStateException("This is a bug! Current leaf name should be " + qName + " but leaf == null");

        // go only higher if we went into this element in startElement
        if (mismatchInTree == 0 && qName.equals(currentLeaf.getName())) {
            currentLeaf = currentLeaf.getParent();
        } else
            mismatchInTree--;

        if (activeBinding != null) {
            Node parent = current.getParentNode();

            // end of subtree
            if (parent == null)
                current.normalize();

            if (mismatchInTree == 0 && qName.equals(activeBinding.getElementName())) {
                try {
                    parsing.parseObject(activeBinding, activeBinding.getClassObject(), current);
                } catch (Exception ex) {
                    throw new UnsupportedOperationException("Couldn't parse:" +
                            current.getTextContent() + " (" + activeBinding + ")", ex);
                }

                activeBinding = null;
            }
            // climb up one level
            current = (Element) parent;
        }
    }

    @Override
    public void characters(char buf[], int offset, int length) throws SAXException {
        if (current != null)
            current.appendChild(factory.createTextNode(new String(buf, offset, length)));
    }

    public DataPool getResult() {
        return parsing.getDataPool();
    }
}
