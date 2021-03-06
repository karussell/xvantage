package de.pannous.xvantage.core.impl;

import de.pannous.xvantage.core.Binding;
import de.pannous.xvantage.core.BindingLeaf;
import de.pannous.xvantage.core.BindingTree;
import de.pannous.xvantage.core.DataPool;
import de.pannous.xvantage.core.parsing.ObjectParsing;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Reads an xml with the help of the provides bindings. As a result the DataPool
 * is initialized.
 * 
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class XHandler extends DefaultHandler {

    private Binding activeBinding;
    private BindingLeaf currentLeaf;
    private StringBuilder sbForOneElement = new StringBuilder();
    /**
     * BindingTree does not represent the full xml dom structure.
     * so we should only go one hierarchie deeper if we are in the same level
     * in both trees. see test case testReadObjectsWithPathIncluded
     */
    private int mismatchInTree = 0;
    private ObjectParsing parsing;

    public XHandler(ObjectParsing parsing, BindingTree bindingTree) {
        this.parsing = parsing;
        currentLeaf = bindingTree.getRoot();
        if (currentLeaf == null)
            throw new NullPointerException("root == null is not supported. Did you already mount some classed?");
    }

    @Override
    public void startElement(String uri, String name, String qName, Attributes attr) {
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
            sbForOneElement.append('<');
            sbForOneElement.append(qName);
            for (int ii = 0; ii < attr.getLength(); ii++) {
                sbForOneElement.append(" ");
                sbForOneElement.append(attr.getQName(ii));
                sbForOneElement.append("=\"");
                sbForOneElement.append(attr.getValue(ii));
                sbForOneElement.append("\"");
            }
            sbForOneElement.append('>');
        }
    }

    @Override
    public void endElement(String uri, String name, String qName) {
        if (currentLeaf == null)
            throw new IllegalStateException("This is a bug! Current leaf name should be " + qName + " but leaf == null");

        // go only higher if we went into this element in startElement
        if (mismatchInTree == 0 && qName.equals(currentLeaf.getName())) {
            currentLeaf = currentLeaf.getParent();
        } else
            mismatchInTree--;

        if (activeBinding != null) {
            sbForOneElement.append("</");
            sbForOneElement.append(qName);
            sbForOneElement.append('>');

            if (mismatchInTree == 0 && qName.equals(activeBinding.getElementName())) {
                String value = sbForOneElement.toString();

                // TODO PERFORMANCE: parse the already parsed string again :-(
                // by now we can use DOM. this is easier for now
                try {
                    parsing.parseObject(activeBinding, value);
                } catch (Exception ex) {
                    throw new UnsupportedOperationException("Couldn't parse:" +
                            value + " (" + activeBinding + ")", ex);
                }

                activeBinding = null;
                sbForOneElement = new StringBuilder();
            }
        }
    }

    @Override
    public void characters(char ch[], int start, int length) {
        if (activeBinding != null)
            sbForOneElement.append(ch, start, length);
    }

    public DataPool getResult() {
        return parsing.getDataPool();
    }
}
