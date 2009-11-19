package de.pannous.xvantage.core.parsing;

import org.w3c.dom.Node;

/**
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class ClassParsing implements Parsing {

    public Object parse(Node node) {
        try {
            return Class.forName(node.getTextContent());
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }
}
