package de.pannous.xvantage.core.util.test;

import de.pannous.xvantage.core.ObjectStringTransformer;

/**
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class TesterMain {

    protected ObjectStringTransformer objectParser;

    public void setUp() throws Exception {
        objectParser = new ObjectStringTransformer();
    }
}
