package de.pannous.xvantage.core.parsing;

import org.w3c.dom.Node;

/**
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public interface Parsing {

    /**
     * Converts a node of a known class into an object.
     */
    Object parse(Node node);
}
