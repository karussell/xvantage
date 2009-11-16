package de.pannous.xvantage.core.util.test;

/**
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 */
public class SimpleObj {

    private String name;    

    private SimpleObj() {
    }
    
    public SimpleObj(String n) {
        setName(n);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "name:" + name;
    }
}
