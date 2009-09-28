package de.pannous.xvantage.core.parsing;

import org.w3c.dom.Node;

/**
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class StringParsing implements Parsing {

    public Object parse(Node node) {
        return node.getTextContent();
    }
}
