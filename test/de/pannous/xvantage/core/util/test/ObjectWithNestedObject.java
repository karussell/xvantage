package de.pannous.xvantage.core.util.test;

/**
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class ObjectWithNestedObject {

    private SimpleObj simpleObject;
    private EmptyObj emptyObject;

    public ObjectWithNestedObject(String name) {
        simpleObject = new SimpleObj(name);
        emptyObject = new EmptyObj();
    }

    public SimpleObj getSimpleObject() {
        return simpleObject;
    }

    public void setSimpleObject(SimpleObj simpleObject) {
        this.simpleObject = simpleObject;
    }

    public EmptyObj getEmptyObject() {
        return emptyObject;
    }

    public void setEmptyObject(EmptyObj emptyObject) {
        this.emptyObject = emptyObject;
    }
}
