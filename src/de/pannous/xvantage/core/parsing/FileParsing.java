package de.pannous.xvantage.core.parsing;

import java.io.File;
import org.w3c.dom.Node;

/**
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class FileParsing implements Parsing {

    public Object parse(Node node) {
        return new File(node.getTextContent());
    }
}
