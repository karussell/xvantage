package de.pannous.xvantage.core.parsing;

import java.util.BitSet;
import org.w3c.dom.Node;

/**
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class BitSetParsing implements Parsing {

    public Object parse(Node node) {
        String bitSetAsStr = node.getTextContent();
        // remove the {}
        bitSetAsStr = bitSetAsStr.substring(1, bitSetAsStr.length() - 1);
        BitSet bitSet = new BitSet();
        for (String str : bitSetAsStr.split(",")) {
            str = str.trim();
            if (str.length() > 0)
                bitSet.set(Integer.parseInt(str.trim()));
        }
        return bitSet;
    }
}
