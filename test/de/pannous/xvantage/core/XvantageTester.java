package de.pannous.xvantage.core;

import de.pannous.xvantage.core.impl.DefaultDataPool;
import de.pannous.xvantage.core.parsing.ObjectParsing;
import de.pannous.xvantage.core.writing.ObjectWriting;

/**
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class XvantageTester {

    protected ObjectWriting writing;
    protected ObjectParsing parsing;
    protected DataPool dataPool;
    protected BindingTree bindingTree;

    public void setUp() throws Exception {
        writing = new ObjectWriting();
        parsing = new ObjectParsing();
        bindingTree = new BindingTree(writing);

        // reconfigure writing for every new Xvantage.readObjects call
        dataPool = new DefaultDataPool();
        writing.init(dataPool);
        parsing.init(dataPool);
    }

    protected <T> Binding<T> newBinding(String string, Class<T> aClass) {
        return new Binding<T>(string, aClass);
    }
}
