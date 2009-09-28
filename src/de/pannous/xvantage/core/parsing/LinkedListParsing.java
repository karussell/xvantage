package de.pannous.xvantage.core.parsing;

import java.util.LinkedList;
import java.util.List;
import org.w3c.dom.Node;

/**
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class LinkedListParsing implements Parsing {

    private ObjectParsing transformer;

    public LinkedListParsing(ObjectParsing transformer) {
        this.transformer = transformer;
    }

    public Object parse(Node node) {
        List linkedList = new LinkedList();
        transformer.fillCollection(linkedList, node);
        return linkedList;
    }
}
