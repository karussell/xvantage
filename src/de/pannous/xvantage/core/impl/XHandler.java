package de.pannous.xvantage.core.impl;

import de.pannous.xvantage.core.Binding;
import de.pannous.xvantage.core.BindingLeaf;
import de.pannous.xvantage.core.BindingTree;
import de.pannous.xvantage.core.DataPool;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Reads an xml with the help of the provides bindings. As a result the DataPool
 * is initialized.
 * 
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class XHandler extends DefaultHandler {

    private DataPool dataPool;
    private Binding activeBinding;
    private BindingLeaf currentLeaf;
    private StringBuilder sbForOneElement = new StringBuilder();
    /**
     * BindingTree does not represent the full xml dom structure.
     * so we should only go one hierarchie deeper if we are in the same level
     * in both trees. see test case testReadObjectsWithPathIncluded
     */
    private int mismatchInTree = 0;
    private List<Exception> exceptions;

    public XHandler(DataPool pool, BindingTree bindingTree, List<Exception> exceptions) {
        dataPool = pool;
        this.exceptions = exceptions;
        currentLeaf = bindingTree.getRoot();
        if (currentLeaf == null)
            throw new NullPointerException("root == null is not supported");
    }

    @Override
    public void startElement(String uri, String name, String qName, Attributes attr) {
        if (currentLeaf == null)
            throw new UnsupportedOperationException("This is a bug! qName was " + qName);

        if (currentLeaf.getChildsMap().get(qName) != null && mismatchInTree == 0) {
            currentLeaf = currentLeaf.getChildsMap().get(qName);
        } else {
            if (!(currentLeaf.isRoot() && currentLeaf.getName().equals(qName)))
                mismatchInTree++;
        }

        if (currentLeaf.getMountedBindingsMap().get(qName) != null && mismatchInTree == 0) {
            Binding bind = currentLeaf.getMountedBindingsMap().get(qName);
            if (bind != null && activeBinding == null)
                activeBinding = bind;
        }

        if (activeBinding != null) {
            sbForOneElement.append('<');
            sbForOneElement.append(qName);
            sbForOneElement.append('>');
        }
    }

    @Override
    public void endElement(String uri, String name, String qName) {
        if (activeBinding != null) {
            sbForOneElement.append("</");
            sbForOneElement.append(qName);
            sbForOneElement.append('>');

            if (qName.equals(activeBinding.getElementName())) {
                String value = sbForOneElement.toString();
                Map<Long, Object> map = dataPool.getData(activeBinding.getClassObject());

                // TODO PERFORMANCE: parse the already parsed string again :-(
                // by now we can use DOM. this is easier for now
                try {
                    Entry<Long, Object> entry = activeBinding.parseObject(value);
                    if (entry != null)
                        map.put(entry.getKey(), entry.getValue());
                } catch (Exception ex) {
                    exceptions.add(ex);
                }

                activeBinding = null;
                sbForOneElement = new StringBuilder();
            }
        }

        if (currentLeaf == null)
            throw new IllegalStateException("This is a bug! Current leaf name should be " + qName + " but leaf == null");

        // go only higher if we went into this element in startElement
        if (qName.equals(currentLeaf.getName())) {
            if (mismatchInTree == 0) {
                currentLeaf = currentLeaf.getParent();
            }
        } else
            mismatchInTree--;
    }

    @Override
    public void characters(char ch[], int start, int length) {
        if (activeBinding != null)
            sbForOneElement.append(ch, start, length);
    }

    public DataPool getResult() {
        return dataPool;
    }
}
