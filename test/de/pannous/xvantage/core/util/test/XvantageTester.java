package de.pannous.xvantage.core.util.test;

import de.pannous.xvantage.core.Binding;
import de.pannous.xvantage.core.BindingTree;
import de.pannous.xvantage.core.DataPool;
import de.pannous.xvantage.core.ObjectStringTransformer;
import de.pannous.xvantage.core.impl.DefaultDataPool;

/**
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class XvantageTester {

    protected ObjectStringTransformer objectParser;
    protected DataPool dataPool;
    protected BindingTree bindingTree;

    public void setUp() throws Exception {
        objectParser = new ObjectStringTransformer();
        bindingTree = new BindingTree(objectParser);

        // reconfigure objectParser for every new Xvantage.readObjects call
        dataPool = new DefaultDataPool();
        objectParser.setDataPool(dataPool);
    }

    protected <T> Binding<T> newBinding(String string, Class<T> aClass) {
        return new Binding<T>(string, aClass);
    }
}
